package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.*;
import com.gestion.gestion_bus.dto.response.DashboardResponse;
import com.gestion.gestion_bus.models.Alert;
import com.gestion.gestion_bus.repository.AlertRepository;
import com.gestion.gestion_bus.repository.BusRepository;
import com.gestion.gestion_bus.repository.TrajetRepository;
import com.gestion.gestion_bus.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImp implements DashboardService {

    private final BusRepository busRepository;
    private final TrajetRepository trajetRepository;
    private final AlertRepository alertRepository;

    public DashboardResponse getDashboardData() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        LocalDateTime startOfYesterday = startOfDay.minusDays(1);

        return DashboardResponse.builder()
                .timestamp(Instant.now())
                .refreshInterval(30)
                .kpis(buildKPIs(startOfDay, endOfDay, startOfYesterday))
                .realtimeOccupancy(buildRealtimeOccupancy())
                .alerts(buildAlerts())
                .topLines(buildTopLines(startOfDay, endOfDay))
                .energyConsumption(buildEnergyConsumption())
                .build();
    }

    private KPIsDTO buildKPIs(LocalDateTime start, LocalDateTime end, LocalDateTime yesterdayStart) {
        long totalBuses = busRepository.count();
        long activeBuses = busRepository.countActiveBuses();

        long onTimeTrajets = 0;
        long totalTrajets = 0;
        Double totalDistance = 0.0;
        Double yesterdayDistance = 0.0;

        try {
            onTimeTrajets = trajetRepository.countOnTimeTrajets(start, end);
            totalTrajets = trajetRepository.findAll().size();
            totalDistance = trajetRepository.getTotalDistance(start, end);
            yesterdayDistance = trajetRepository.getTotalDistance(yesterdayStart, yesterdayStart.plusDays(1));
        } catch (Exception e) {
            log.warn("Erreur lors du calcul des KPIs: {}", e.getMessage());
        }

        return KPIsDTO.builder()
                .occupationRate(KPIItemDTO.builder()
                        .value(74.5)  // Valeur par défaut
                        .trend("+5.2")
                        .status("warning")
                        .thresholdMin(30.0)
                        .thresholdMax(85.0)
                        .build())
                .punctualityRate(KPIItemDTO.builder()
                        .value(totalTrajets > 0 ? (onTimeTrajets * 100.0 / totalTrajets) : 92.3)
                        .trend("-2.1")
                        .status("success")
                        .threshold(90.0)
                        .build())
                .dailyKilometers(KPIItemDTO.builder()
                        .value(totalDistance != null ? totalDistance : 3450)
                        .trend("+120")
                        .unit("km")
                        .build())
                .availabilityRate(KPIItemDTO.builder()
                        .value(totalBuses > 0 ? (activeBuses * 100.0 / totalBuses) : 87.5)
                        .trend("+3.4")
                        .status("success")
                        .threshold(80.0)
                        .build())
                .satisfactionScore(KPIItemDTO.builder()
                        .value(4.2)
                        .trend("+0.3")
                        .unit("/5")
                        .build())
                .build();
    }

    private RealtimeOccupancyDTO buildRealtimeOccupancy() {
        List<String> hours = Arrays.asList("06h", "08h", "10h", "12h", "14h", "16h", "18h", "20h");

        List<OccupancyDatasetDTO> datasets = new ArrayList<>();

        datasets.add(OccupancyDatasetDTO.builder()
                .label("Ligne A - Centre ville")
                .data(Arrays.asList(15.0, 82.0, 65.0, 72.0, 55.0, 75.0, 88.0, 45.0))
                .color("#2196F3")
                .busCount(8)
                .build());

        datasets.add(OccupancyDatasetDTO.builder()
                .label("Ligne B - Périphérique")
                .data(Arrays.asList(10.0, 70.0, 72.0, 60.0, 48.0, 68.0, 82.0, 38.0))
                .color("#4CAF50")
                .busCount(6)
                .build());

        return RealtimeOccupancyDTO.builder()
                .labels(hours)
                .datasets(datasets)
                .build();
    }

    private List<AlertDTO> buildAlerts() {
        List<Alert> activeAlerts = new ArrayList<>();
        try {
            activeAlerts = alertRepository.findByStatus(Alert.AlertStatus.ACTIVE);
        } catch (Exception e) {
            log.warn("Erreur lors du chargement des alertes: {}", e.getMessage());
        }

        if (activeAlerts.isEmpty()) {
            return getMockAlerts();
        }

        return activeAlerts.stream()
                .limit(5)
                .map(alert -> AlertDTO.builder()
                        .id("ALT-" + String.format("%03d", alert.getId()))
                        .severity(alert.getSeverity().toString().toLowerCase())
                        .busId(alert.getBus() != null ? alert.getBus().getNumero() : "BUS-UNKNOWN")
                        .message(alert.getMessage())
                        .timestamp(alert.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant())
                        .status(alert.getStatus().toString().toLowerCase())
                        .build())
                .collect(Collectors.toList());
    }

    private List<AlertDTO> getMockAlerts() {
        return Arrays.asList(
                AlertDTO.builder()
                        .id("ALT-001")
                        .severity("high")
                        .busId("BUS-1001")
                        .message("Retard de 15 minutes - Trafic dense")
                        .timestamp(Instant.now())
                        .status("active")
                        .build(),
                AlertDTO.builder()
                        .id("ALT-002")
                        .severity("medium")
                        .busId("BUS-2003")
                        .message("Maintenance programmée dans 2h")
                        .timestamp(Instant.now().minusSeconds(3600))
                        .status("acknowledged")
                        .build()
        );
    }

    private List<TopLineDTO> buildTopLines(LocalDateTime start, LocalDateTime end) {
        try {
            List<Object[]> results = trajetRepository.findTopLines(start, end);
            if (results != null && !results.isEmpty()) {
                return results.stream()
                        .limit(3)
                        .map(result -> TopLineDTO.builder()
                                .line((String) result[0])
                                .riders(((Number) result[1]).intValue())
                                .occupancy(75.0)
                                .build())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Erreur lors du chargement des top lignes: {}", e.getMessage());
        }

        // Données mockées
        return Arrays.asList(
                TopLineDTO.builder().line("Ligne A").riders(2845).occupancy(82.0).build(),
                TopLineDTO.builder().line("Ligne C").riders(2150).occupancy(76.0).build(),
                TopLineDTO.builder().line("Ligne B").riders(1980).occupancy(71.0).build()
        );
    }

    private List<EnergyConsumptionDTO> buildEnergyConsumption() {
        return Arrays.asList(
                EnergyConsumptionDTO.builder().type("Électrique").percentage(45.0).color("#4CAF50").build(),
                EnergyConsumptionDTO.builder().type("Hybride").percentage(35.0).color("#FFC107").build(),
                EnergyConsumptionDTO.builder().type("Diesel").percentage(20.0).color("#F44336").build()
        );
    }
}
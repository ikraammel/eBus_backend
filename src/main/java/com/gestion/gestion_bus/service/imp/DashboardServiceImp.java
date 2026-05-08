package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.*;
import com.gestion.gestion_bus.dto.response.DashboardResponse;
import com.gestion.gestion_bus.enums.BusEtat;
import com.gestion.gestion_bus.enums.ReclamationStatus;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.models.Alert;
import com.gestion.gestion_bus.repository.*;
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
    private final UserRepository userRepository;
    private final ReclamationRepository reclamationRepository;
    private final ObjetPerduRepository objetPerduRepository;
    private final AbonnementRepository abonnementRepository;
    private final LigneRepository ligneRepository;

    @Override
    public DashboardResponse getDashboardData() {
        LocalDateTime startOfDay    = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay      = LocalDateTime.now().with(LocalTime.MAX);
        LocalDateTime startYesterday = startOfDay.minusDays(1);

        return DashboardResponse.builder()
                .timestamp(Instant.now())
                .refreshInterval(30)
                // ── Compteurs globaux réels ───────────────────────────────
                .users(userRepository.count())
                .bus(busRepository.countActiveBuses())
                .reclamations(reclamationRepository.count())
                .objets(objetPerduRepository.count())
                // ── Sections calculées ────────────────────────────────────
                .kpis(buildKPIs(startOfDay, endOfDay, startYesterday))
                .realtimeOccupancy(buildRealtimeOccupancy(startOfDay, endOfDay))
                .alerts(buildAlerts())
                .topLines(buildTopLines(startOfDay, endOfDay))
                .energyConsumption(buildEnergyConsumption())
                .build();
    }


    private KPIsDTO buildKPIs(LocalDateTime start, LocalDateTime end, LocalDateTime yesterdayStart) {

        /* ── Disponibilité des bus ─────────────────────────────────────────── */
        long totalBus  = busRepository.count();
        long activeBus = busRepository.countActiveBuses();
        double availabilityRate  = totalBus > 0 ? (activeBus * 100.0 / totalBus) : 0.0;

        long totalBusYesterday  = totalBus; // la flotte ne change pas en 24h
        long activeBusYesterday = busRepository.countByEtat(BusEtat.ACTIVE); // même valeur → tendance neutre
        double availabilityTrend = availabilityRate - (totalBusYesterday > 0
                ? (activeBusYesterday * 100.0 / totalBusYesterday) : availabilityRate);

        /* ── Ponctualité ─────────────────────────────────────────────────── */
        double punctualityRate  = 0.0;
        double punctualityTrend = 0.0;
        try {
            long onTimeToday  = trajetRepository.countOnTimeTrajets(start, end);
            long totalToday   = trajetRepository.countTrajetsBetween(start, end);
            long onTimeYest   = trajetRepository.countOnTimeTrajets(yesterdayStart, start);
            long totalYest    = trajetRepository.countTrajetsBetween(yesterdayStart, start);

            punctualityRate  = totalToday  > 0 ? (onTimeToday  * 100.0 / totalToday)  : 0.0;
            double punctYest = totalYest   > 0 ? (onTimeYest   * 100.0 / totalYest)   : punctualityRate;
            punctualityTrend = punctualityRate - punctYest;
        } catch (Exception e) {
            log.warn("Erreur calcul ponctualité: {}", e.getMessage());
        }

        /* ── Distance quotidienne ─────────────────────────────────────────── */
        double distanceToday     = 0.0;
        double distanceYesterday = 0.0;
        try {
            Double d1 = trajetRepository.getTotalDistance(start, end);
            Double d2 = trajetRepository.getTotalDistance(yesterdayStart, start);
            distanceToday     = d1 != null ? d1 : 0.0;
            distanceYesterday = d2 != null ? d2 : 0.0;
        } catch (Exception e) {
            log.warn("Erreur calcul distance: {}", e.getMessage());
        }
        double distanceTrend = distanceToday - distanceYesterday;

        /* ── Taux d'occupation moyen ──────────────────────────────────────── */
        double occupationRate  = 0.0;
        double occupationTrend = 0.0;
        try {
            Double occToday = trajetRepository.getAverageOccupancyRate(start, end);
            Double occYest  = trajetRepository.getAverageOccupancyRate(yesterdayStart, start);
            occupationRate  = occToday != null ? occToday  : 0.0;
            double occY     = occYest  != null ? occYest   : occupationRate;
            occupationTrend = occupationRate - occY;
        } catch (Exception e) {
            log.warn("Erreur calcul occupation: {}", e.getMessage());
        }

        /* ── Score de satisfaction (basé sur réclamations résolues vs total) ─ */
        double satisfactionScore = 5.0;
        double satisfactionTrend = 0.0;
        try {
            long totalRecl   = reclamationRepository.count();
            long resolues    = reclamationRepository.countByStatus(ReclamationStatus.TRAITEE);
            // Score sur 5 : si 100% résolues → 5, si 0% → 1
            if (totalRecl > 0) {
                double resolRate = (double) resolues / totalRecl;
                satisfactionScore = Math.round((1.0 + resolRate * 4.0) * 10.0) / 10.0;
            }
            // Tendance basée sur les abonnements actifs (proxy de fidélité)
            long abonsActifs = abonnementRepository.countByStatus(StatusAbonnement.ACTIF);
            long totalAbons  = abonnementRepository.count();
            satisfactionTrend = totalAbons > 0 ? Math.round((abonsActifs * 100.0 / totalAbons - 70) / 10.0 * 10) / 10.0 : 0.0;
        } catch (Exception e) {
            log.warn("Erreur calcul satisfaction: {}", e.getMessage());
        }

        /* ── Construction du DTO ─────────────────────────────────────────── */
        return KPIsDTO.builder()
                .occupationRate(KPIItemDTO.builder()
                        .value(round2(occupationRate))
                        .trend(formatTrend(occupationTrend))
                        .status(occupationRate > 85 ? "warning" : occupationRate > 50 ? "success" : "error")
                        .thresholdMin(30.0)
                        .thresholdMax(85.0)
                        .build())
                .punctualityRate(KPIItemDTO.builder()
                        .value(round2(punctualityRate))
                        .trend(formatTrend(punctualityTrend))
                        .status(punctualityRate >= 90 ? "success" : punctualityRate >= 70 ? "warning" : "error")
                        .threshold(90.0)
                        .build())
                .dailyKilometers(KPIItemDTO.builder()
                        .value(round2(distanceToday))
                        .trend(formatTrend(distanceTrend))
                        .unit("km")
                        .status(distanceToday > 0 ? "success" : "normal")
                        .build())
                .availabilityRate(KPIItemDTO.builder()
                        .value(round2(availabilityRate))
                        .trend(formatTrend(availabilityTrend))
                        .status(availabilityRate >= 80 ? "success" : availabilityRate >= 60 ? "warning" : "error")
                        .threshold(80.0)
                        .build())
                .satisfactionScore(KPIItemDTO.builder()
                        .value(satisfactionScore)
                        .trend(formatTrend(satisfactionTrend))
                        .unit("/5")
                        .status(satisfactionScore >= 4.0 ? "success" : satisfactionScore >= 2.5 ? "warning" : "error")
                        .build())
                .build();
    }

    // =========================================================================
    // Occupation en temps réel — par ligne, avec données réelles du jour
    // =========================================================================
    private RealtimeOccupancyDTO buildRealtimeOccupancy(LocalDateTime start, LocalDateTime end) {
        List<String> hours = Arrays.asList("06h", "08h", "10h", "12h", "14h", "16h", "18h", "20h");

        List<OccupancyDatasetDTO> datasets = new ArrayList<>();

        try {
            // Récupère les lignes réelles avec leurs bus
            List<Object[]> ligneStats = trajetRepository.findTopLines(start, end);

            // Pour chaque ligne trouvée (max 3), construire un dataset horaire
            String[] colors = {"#2196F3", "#4CAF50", "#FF9800", "#9C27B0"};
            int colorIdx = 0;

            for (Object[] row : ligneStats) {
                if (colorIdx >= colors.length) break;
                String ligneNumero = (String) row[0];

                // Occupation heure par heure pour cette ligne
                List<Double> hourlyData = new ArrayList<>();
                for (int h = 6; h <= 20; h += 2) {
                    LocalDateTime slotStart = start.withHour(h);
                    LocalDateTime slotEnd   = slotStart.plusHours(2);
                    try {
                        Double occ = trajetRepository.getOccupancyRateByLigneAndPeriod(ligneNumero, slotStart, slotEnd);
                        hourlyData.add(occ != null ? round2(occ) : 0.0);
                    } catch (Exception ex) {
                        hourlyData.add(0.0);
                    }
                }

                long busCount = busRepository.countByLigneNumero(ligneNumero);

                datasets.add(OccupancyDatasetDTO.builder()
                        .label(ligneNumero)
                        .data(hourlyData)
                        .color(colors[colorIdx++])
                        .busCount((int) busCount)
                        .build());
            }
        } catch (Exception e) {
            log.warn("Erreur occupation temps réel: {}", e.getMessage());
        }

        // Si aucune donnée disponible, on retourne une structure vide (le frontend gère)
        return RealtimeOccupancyDTO.builder()
                .labels(hours)
                .datasets(datasets)
                .build();
    }

    private List<AlertDTO> buildAlerts() {
        try {
            List<Alert> activeAlerts = alertRepository.findByStatus(Alert.AlertStatus.ACTIVE);
            if (!activeAlerts.isEmpty()) {
                return activeAlerts.stream()
                        .limit(5)
                        .map(alert -> AlertDTO.builder()
                                .id("ALT-" + String.format("%03d", alert.getId()))
                                .severity(alert.getSeverity().toString().toLowerCase())
                                .busId(alert.getBus() != null ? alert.getBus().getNumero() : "N/A")
                                .message(alert.getMessage())
                                .timestamp(alert.getTimestamp()
                                        .atZone(java.time.ZoneId.systemDefault()).toInstant())
                                .status(alert.getStatus().toString().toLowerCase())
                                .build())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Erreur chargement alertes: {}", e.getMessage());
        }
        return Collections.emptyList();
    }


    private List<TopLineDTO> buildTopLines(LocalDateTime start, LocalDateTime end) {
        try {
            List<Object[]> results = trajetRepository.findTopLines(start, end);
            if (results != null && !results.isEmpty()) {
                return results.stream()
                        .limit(4)
                        .map(result -> {
                            String ligneNumero = (String) result[0];
                            int riders       = ((Number) result[1]).intValue();

                            // Calcul du taux d'occupation réel pour cette ligne
                            double occupancy = 0.0;
                            try {
                                Double occ = trajetRepository.getOccupancyRateByLigneAndPeriod(ligneNumero, start, end);
                                occupancy = occ != null ? round2(occ) : 0.0;
                            } catch (Exception ex) {
                                log.debug("Occupation non disponible pour {}: {}", ligneNumero, ex.getMessage());
                            }

                            return TopLineDTO.builder()
                                    .line(ligneNumero)
                                    .riders(riders)
                                    .occupancy(occupancy)
                                    .build();
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Erreur top lignes: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<EnergyConsumptionDTO> buildEnergyConsumption() {
        try {
            List<Object[]> stats = busRepository.countByEtatGrouped();
            if (stats != null && !stats.isEmpty()) {
                // On mappe BusEtat → catégorie énergétique
                // Hypothèse : ACTIVE → Électrique, EN_MAINTENANCE → Hybride, INACTIVE → Diesel
                long electrique = 0, hybride = 0, diesel = 0, total = 0;
                for (Object[] row : stats) {
                    String etat  = row[0] != null ? row[0].toString() : "";
                    long   count = ((Number) row[1]).longValue();
                    total += count;
                    switch (etat) {
                        case "ACTIVE"          -> electrique += count;
                        case "EN_MAINTENANCE"  -> hybride    += count;
                        default                -> diesel     += count;
                    }
                }
                if (total > 0) {
                    final long t = total;
                    return Arrays.asList(
                            EnergyConsumptionDTO.builder()
                                    .type("Électrique").percentage(round2(electrique * 100.0 / t)).color("#4CAF50").build(),
                            EnergyConsumptionDTO.builder()
                                    .type("Hybride").percentage(round2(hybride * 100.0 / t)).color("#FFC107").build(),
                            EnergyConsumptionDTO.builder()
                                    .type("Diesel").percentage(round2(diesel * 100.0 / t)).color("#F44336").build()
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Erreur consommation énergie: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private String formatTrend(double diff) {
        double r = Math.round(diff * 10.0) / 10.0;
        if (r > 0) return "+" + r;
        if (r < 0) return String.valueOf(r);
        return "0";
    }
}
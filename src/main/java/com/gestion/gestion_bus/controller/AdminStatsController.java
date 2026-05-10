package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.AdminStatsDto;
import com.gestion.gestion_bus.enums.ReclamationStatus;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.enums.StatusDossier;
import com.gestion.gestion_bus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminStatsController {

    private final UserRepository userRepository;
    private final AbonnementRepository abonnementRepository;
    private final TicketRepository ticketRepository;
    private final ReclamationRepository reclamationRepository;
    private final ObjetPerduRepository objetPerduRepository;
    private final BusRepository busRepository;
    private final LigneRepository ligneRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public AdminStatsDto getStats() {

        LocalDateTime now        = LocalDateTime.now();
        LocalDateTime debutMois  = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime ilY12Mois  = now.minusMonths(12);

        // ── Utilisateurs ──────────────────────────────────────────────────────
        long totalUsers       = userRepository.count();
        long nouveauxCeMois   = userRepository.countUsersCreatedSince(debutMois);
        long dossiersEnAttente = userRepository.countByStatusDossier(StatusDossier.EN_ATTENTE);
        long dossiersValides   = userRepository.countByStatusDossier(StatusDossier.VALIDE);
        long dossiersRejetes   = userRepository.countByStatusDossier(StatusDossier.REJETE);

        // ── Abonnements ───────────────────────────────────────────────────────
        long abonActifs    = abonnementRepository.countByStatus(StatusAbonnement.ACTIF);
        long abonEnAttente = abonnementRepository.countByStatus(StatusAbonnement.EN_ATTENTE);
        long abonExpires   = abonnementRepository.countByStatus(StatusAbonnement.EXPIRE);
        double revenuAbon  = abonnementRepository.sumRevenuAbonnementsActifs();

        // ── Tickets ───────────────────────────────────────────────────────────
        long totalTickets    = ticketRepository.count();
        double revenuTickets = ticketRepository.sumRevenuTickets();

        // ── Réclamations ──────────────────────────────────────────────────────
        long reclTotal    = reclamationRepository.count();
        long reclOuvertes = reclamationRepository.countByStatus(ReclamationStatus.EN_ATTENTE);
        long reclResolues = reclamationRepository.countByStatus(ReclamationStatus.TRAITEE);
        Map<String, Long> reclParStatut = new LinkedHashMap<>();
        reclParStatut.put("EN_COURS", reclOuvertes);
        reclParStatut.put("RESOLUE",  reclResolues);
        reclParStatut.put("AUTRE",    Math.max(0, reclTotal - reclOuvertes - reclResolues));

        // ── Objets perdus ─────────────────────────────────────────────────────
        long totalObjets = objetPerduRepository.count();

        // ── Bus & Lignes ──────────────────────────────────────────────────────
        long totalBus    = busRepository.count();
        long busActifs   = busRepository.countActiveBuses();
        long totalLignes = ligneRepository.count();

        // ── Inscriptions par mois (12 derniers mois) ──────────────────────────
        // La requête retourne [year, month, count] pour être compatible JPQL (pas de DATE_FORMAT)
        Map<String, Long> dbMap = new LinkedHashMap<>();
        for (Object[] row : userRepository.countInscriptionsParMois(ilY12Mois)) {
            int  year  = ((Number) row[0]).intValue();
            int  month = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            String key = String.format("%04d-%02d", year, month);
            dbMap.put(key, count);
        }

        List<AdminStatsDto.MonthlyCountDTO> inscriptions = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime mois  = now.minusMonths(i);
            String key          = String.format("%04d-%02d", mois.getYear(), mois.getMonthValue());
            String label        = mois.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            inscriptions.add(AdminStatsDto.MonthlyCountDTO.builder()
                    .mois(label)
                    .count(dbMap.getOrDefault(key, 0L))
                    .build());
        }

        // ── Abonnements par type ──────────────────────────────────────────────
        Map<String, Long> abonParType = new LinkedHashMap<>();
        for (Object[] row : abonnementRepository.countParType()) {
            abonParType.put(
                    row[0] != null ? row[0].toString() : "Inconnu",
                    ((Number) row[1]).longValue()
            );
        }

        return AdminStatsDto.builder()
                .totalUtilisateurs(totalUsers)
                .nouveauxCeMois(nouveauxCeMois)
                .dossiersEnAttente(dossiersEnAttente)
                .dossiersValides(dossiersValides)
                .dossiersRejetes(dossiersRejetes)
                .abonnementsActifs(abonActifs)
                .abonnementsEnAttente(abonEnAttente)
                .abonnementsExpires(abonExpires)
                .revenuTotalAbonnements(Math.round(revenuAbon * 100.0) / 100.0)
                .totalTickets(totalTickets)
                .revenuTickets(Math.round(revenuTickets * 100.0) / 100.0)
                .reclamationsTotal(reclTotal)
                .reclamationsOuvertes(reclOuvertes)
                .reclamationsResolues(reclResolues)
                .objetsPerdusTotal(totalObjets)
                .totalBus(totalBus)
                .busActifs(busActifs)
                .totalLignes(totalLignes)
                .inscriptionsParMois(inscriptions)
                .abonnementsParType(abonParType)
                .reclamationsParStatut(reclParStatut)
                .build();
    }
}
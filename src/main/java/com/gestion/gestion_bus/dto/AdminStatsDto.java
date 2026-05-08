package com.gestion.gestion_bus.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
@Builder
public class AdminStatsDto {

    private long totalUtilisateurs;
    private long nouveauxCeMois;
    private long dossiersEnAttente;
    private long dossiersValides;
    private long dossiersRejetes;

    private long abonnementsActifs;
    private long abonnementsEnAttente;
    private long abonnementsExpires;
    private double revenuTotalAbonnements;

    private long totalTickets;
    private double revenuTickets;

    private long reclamationsOuvertes;
    private long reclamationsResolues;
    private long reclamationsTotal;

    private long objetsPerdusTotal;
    private long objetsRendus;
    private long objetsEnCours;

    private long totalBus;
    private long busActifs;
    private long totalLignes;

    private List<MonthlyCountDTO> inscriptionsParMois;

    private Map<String, Long> abonnementsParType;

    private Map<String, Long> reclamationsParStatut;


    @Data
    @Builder
    public static class MonthlyCountDTO {
        private String mois;
        private long count;
    }
}
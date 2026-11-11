package com.volapp.reservation.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Offre relue chez Duffel, au format interne (jamais le JSON Duffel brut).
 */
public record OffreReponse(
        String id,
        BigDecimal montantTotal,
        String devise,
        Instant expireLe,
        List<Passager> passagers,
        List<Segment> segments) {

    /** Passager de l'offre : son identifiant Duffel est necessaire a la commande. */
    public record Passager(String id, String type) {
    }

    /** Segment de vol, heures locales de l'aeroport. */
    public record Segment(
            int tranche,
            String origine,
            String destination,
            LocalDateTime depart,
            LocalDateTime arrivee,
            String numeroVol,
            String compagnie) {
    }
}

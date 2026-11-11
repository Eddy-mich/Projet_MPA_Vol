package com.volapp.recherche.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Offre au format interne, renvoyee au front-end (jamais le JSON Duffel brut).
 * Les records sont serialisables : ils peuvent etre caches en Redis.
 */
public record Offre(
        String id,
        BigDecimal montantTotal,
        String devise,
        String compagnie,
        String codeCompagnie,
        String marqueFare,
        Instant expireLe,
        List<Passager> passagers,
        List<Tranche> tranches) implements Serializable {

    public record Passager(String id, String type) implements Serializable {
    }

    public record Tranche(
            String origine,
            String destination,
            Duration duree,
            List<Segment> segments) implements Serializable {
    }

    /**
     * depart/arrivee en heure locale de l'aeroport (LocalDateTime, sans fuseau).
     */
    public record Segment(
            String compagnie,
            String codeCompagnie,
            String numeroVol,
            LocalDateTime depart,
            LocalDateTime arrivee,
            Duration duree,
            String origine,
            String destination,
            String terminalDepart,
            String terminalArrivee,
            int nombreEscales) implements Serializable {
    }
}

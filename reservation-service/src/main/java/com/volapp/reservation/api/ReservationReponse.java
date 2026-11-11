package com.volapp.reservation.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Reservation au format interne, renvoyee au front-end. */
public record ReservationReponse(
        UUID id,
        String referenceReservation,
        String statut,
        BigDecimal montantTotal,
        String devise,
        String email,
        String telephone,
        Instant creeLe,
        List<Passager> passagers,
        List<Segment> segments,
        Annulation annulation) {

    public record Passager(
            String titre,
            String prenom,
            String nom,
            LocalDate dateNaissance,
            String genre) {
    }

    public record Segment(
            int tranche,
            String origine,
            String destination,
            LocalDateTime depart,
            LocalDateTime arrivee,
            String numeroVol,
            String compagnie) {
    }

    /** Informations d'annulation ; null si la reservation n'a pas ete annulee. */
    public record Annulation(
            String id,
            BigDecimal montantRembourse,
            String devise,
            Instant annuleeLe) {
    }
}

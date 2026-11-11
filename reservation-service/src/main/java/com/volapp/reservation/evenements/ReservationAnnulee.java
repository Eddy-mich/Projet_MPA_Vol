package com.volapp.reservation.evenements;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Evenement publie apres une annulation confirme (cle "reservation.annulee").
 */
public record ReservationAnnulee(
        UUID reservationId,
        String referenceReservation,
        String email,
        BigDecimal montantRembourse,
        String devise,
        List<Vol> vols) {

    /** Vol annule (heure de depart locale de l'aeroport, ISO-8601). */
    public record Vol(String numeroVol, String depart) {
    }
}

package com.volapp.reservation.evenements;

import java.util.List;
import java.util.UUID;

/**
 * Evenement publie apres une reservation reussie (cle "reservation.confirmee").
 */
public record ReservationConfirmee(
        UUID reservationId,
        String referenceReservation,
        String email,
        List<Vol> vols) {

    /** Vol a surveiller (heure de depart locale de l'aeroport, ISO-8601). */
    public record Vol(String numeroVol, String origine, String destination, String depart) {
    }
}

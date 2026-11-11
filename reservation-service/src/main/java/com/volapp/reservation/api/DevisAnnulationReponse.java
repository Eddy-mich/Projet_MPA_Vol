package com.volapp.reservation.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Devis d'annulation : rien n'est encore annule, l'utilisateur doit confirmer. */
public record DevisAnnulationReponse(
        UUID reservationId,
        String id,
        BigDecimal montantRembourse,
        String devise,
        Instant expireLe) {
}

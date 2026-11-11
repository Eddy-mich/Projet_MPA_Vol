package com.volapp.reservation.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Requete de reservation, telle que l'envoie le front-end.
 * montantAttendu : le prix de l'offre a l'instant de la recherche (controle anti-changement).
 */
public record RequeteReservation(
        @NotBlank String offreId,
        @NotNull @Positive BigDecimal montantAttendu,
        @NotBlank @Email String email,
        @NotBlank String telephone,
        @NotEmpty @Valid List<Passager> passagers) {

    /** Coordonnees d'un passager ; telephone absent = telephone du voyageur utilise. */
    public record Passager(
            @NotBlank @Pattern(regexp = "^(mr|mme|ms)$", message = "doit etre mr, mme ou ms") String titre,
            @NotBlank String prenom,
            @NotBlank String nom,
            @NotNull LocalDate dateNaissance,
            @NotBlank @Pattern(regexp = "^[mf]$", message = "doit etre m ou f") String genre,
            String telephone) {
    }
}

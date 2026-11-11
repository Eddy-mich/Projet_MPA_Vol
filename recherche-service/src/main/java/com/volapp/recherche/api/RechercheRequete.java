package com.volapp.recherche.api;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Requete de recherche de vols, telle que l'envoie le front-end.
 * dateRetour absent = vol aller simple ; present = aller-retour.
 */
public record RechercheRequete(
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$", message = "code IATA a 3 lettres majuscules (ex : CDG)") String origine,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$", message = "code IATA a 3 lettres majuscules (ex : LHR)") String destination,
        @NotNull @FutureOrPresent LocalDate dateDepart,
        @FutureOrPresent LocalDate dateRetour,
        @NotNull @Min(1) @Max(9) Integer adultes,
        @Size(min = 1, max = 20) String cabine) {

    /** Cabine demandee, par defaut "economy". */
    public String cabineOuDefaut() {
        return (cabine == null || cabine.isBlank()) ? "economy" : cabine.trim().toLowerCase();
    }
}

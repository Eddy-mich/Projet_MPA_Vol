package com.volapp.recherche.duffel;

import java.math.BigDecimal;
import java.util.List;

/**
 * Records du JSON Duffel (noms en anglais, identiques aux champs du JSON).
 * Ces records ne sont jamais renvoyes au front-end : passer par l'OffreMapper.
 */
public final class DuffelDtos {

    private DuffelDtos() {
    }

    /** Enveloppe de reponse : { "data": ... }. */
    public record Enveloppe<T>(T data) {
    }

    /** Corps d'erreur Duffel : { "errors": [...] }. */
    public record CorpsErreur(List<Erreur> errors) {
    }

    /** Erreur Duffel : status, code, title, message, detail. */
    public record Erreur(String status, String code, String title, String message, String detail) {
    }

    // --- Recherche d'offres : POST /air/offer_requests?return_offers=true ---

    public record RequeteRecherche(DataRecherche data) {
    }

    public record DataRecherche(
            List<SliceRequete> slices,
            List<PassagerRequete> passengers,
            String cabin_class) {
    }

    public record SliceRequete(
            String origin,
            String destination,
            String departure_date) {
    }

    public record PassagerRequete(String type) {
    }

    /** Donnees d'une recherche : identifiant de requete et liste des offres. */
    public record DonneesRecherche(String id, List<Offre> offers) {
    }

    public record Offre(
            String id,
            Compagnie owner,
            String expires_at,
            BigDecimal base_amount,
            String base_currency,
            BigDecimal total_amount,
            String total_currency,
            BigDecimal tax_amount,
            String tax_currency,
            List<Passager> passengers,
            List<Slice> slices) {
    }

    public record Passager(String id, String type) {
    }

    public record Slice(
            String id,
            Aeroport origin,
            Aeroport destination,
            String duration,
            String fare_brand_name,
            List<Segment> segments) {
    }

    /**
     * Dates des segments au format ISO-8601 sans fuseau (heure locale de l'aeroport) :
     * conserves en texte ici, parsees en LocalDateTime par le mapper.
     */
    public record Segment(
            String id,
            Aeroport origin,
            Aeroport destination,
            String departing_at,
            String arriving_at,
            String duration,
            Compagnie operating_carrier,
            String operating_carrier_flight_number,
            String origin_terminal,
            String destination_terminal,
            List<Escale> stops) {
    }

    public record Escale(String id) {
    }

    public record Aeroport(String iata_code, String name) {
    }

    public record Compagnie(String id, String name, String iata_code) {
    }
}

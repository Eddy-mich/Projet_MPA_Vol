package com.volapp.reservation.duffel;

import java.math.BigDecimal;
import java.util.List;

/**
 * Records correspondant au JSON Duffel (offre, commande, annulation).
 * Les noms de champs restent ceux de l'API Duffel v2.
 */
public final class DuffelDtos {

    private DuffelDtos() {
    }

    /* ---------- Reponse d'erreur Duffel ---------- */

    public record CorpsErreur(List<Erreur> errors) {
    }

    public record Erreur(String status, String code, String title, String message, String detail) {
    }

    /* ---------- GET /air/offers/{id} ---------- */

    public record DonneesOffre(
            String id,
            BigDecimal total_amount,
            String total_currency,
            String expires_at,
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

    /* ---------- POST /air/orders ---------- */

    public record RequeteCommande(DataCommande data) {
    }

    public record DataCommande(
            String type,
            List<String> selected_offers,
            List<PassagerCommande> passengers,
            List<Paiement> payments) {
    }

    public record PassagerCommande(
            String id,
            String title,
            String given_name,
            String family_name,
            String born_on,
            String phone_number,
            String gender,
            String email) {
    }

    public record Paiement(String type, String currency, String amount) {
    }

    public record DonneesCommande(
            String id,
            String booking_reference,
            String offer_id,
            BigDecimal total_amount,
            String total_currency,
            List<Slice> slices) {
    }

    /* ---------- Annulation en deux temps ---------- */

    public record RequeteAnnulation(DataAnnulation data) {
    }

    public record DataAnnulation(String order_id) {
    }

    public record DonneesAnnulation(
            String id,
            String order_id,
            String status,
            BigDecimal refund_amount,
            String refund_currency,
            String expires_at,
            String confirmed_at) {
    }
}

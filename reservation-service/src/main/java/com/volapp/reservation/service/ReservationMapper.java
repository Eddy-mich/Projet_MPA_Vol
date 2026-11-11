package com.volapp.reservation.service;

import com.volapp.reservation.api.DevisAnnulationReponse;
import com.volapp.reservation.api.OffreReponse;
import com.volapp.reservation.api.RequeteReservation;
import com.volapp.reservation.api.ReservationReponse;
import com.volapp.reservation.duffel.DuffelDtos;
import com.volapp.reservation.domaine.Passager;
import com.volapp.reservation.domaine.Reservation;
import com.volapp.reservation.domaine.Segment;
import com.volapp.reservation.domaine.StatutReservation;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Convertit le format Duffel vers le format interne (package api) et l'inverse
 * pour les entites JPA. Les heures des segments sont locales (LocalDateTime).
 */
@Component
public class ReservationMapper {

    /** Reponse Duffel GET /air/offers/{id} -> offre pour le front-end. */
    public OffreReponse mapperOffre(DuffelDtos.DonneesOffre offre) {
        List<OffreReponse.Passager> passagers = offre.passengers() == null ? List.of()
                : offre.passengers().stream()
                        .map(passager -> new OffreReponse.Passager(passager.id(), passager.type()))
                        .toList();
        return new OffreReponse(
                offre.id(),
                offre.total_amount(),
                offre.total_currency(),
                offre.expires_at() == null ? null : Instant.parse(offre.expires_at()),
                passagers,
                segments(offre.slices()));
    }

    /** Commande Duffel -> entite JPA au statut CONFIRME. */
    public Reservation creerEntite(DuffelDtos.DonneesCommande commande, RequeteReservation requete) {
        List<Passager> passagers = requete.passagers().stream()
                .map(p -> new Passager(p.titre(), p.prenom(), p.nom(), p.dateNaissance(), p.genre()))
                .toList();
        return new Reservation(
                commande.booking_reference(),
                commande.id(),
                commande.offer_id(),
                StatutReservation.CONFIRME,
                commande.total_amount(),
                commande.total_currency(),
                requete.email(),
                requete.telephone(),
                Instant.now(),
                passagers,
                segmentsJpa(commande.slices()));
    }

    /** Entite JPA -> reservation pour le front-end. */
    public ReservationReponse mapper(Reservation reservation) {
        List<ReservationReponse.Passager> passagers = reservation.getPassagers().stream()
                .map(p -> new ReservationReponse.Passager(
                        p.getTitre(), p.getPrenom(), p.getNom(), p.getDateNaissance(), p.getGenre()))
                .toList();
        List<ReservationReponse.Segment> segments = reservation.getSegments().stream()
                .map(s -> new ReservationReponse.Segment(
                        s.getTranche(), s.getOrigine(), s.getDestination(),
                        s.getDepart(), s.getArrivee(), s.getNumeroVol(), s.getCompagnie()))
                .toList();
        ReservationReponse.Annulation annulation = reservation.getAnnulationId() == null ? null
                : new ReservationReponse.Annulation(
                        reservation.getAnnulationId(),
                        reservation.getMontantRembourse(),
                        reservation.getDeviseRembourse(),
                        reservation.getAnnuleeLe());
        return new ReservationReponse(
                reservation.getId(),
                reservation.getReferenceReservation(),
                reservation.getStatut().name(),
                reservation.getMontantTotal(),
                reservation.getDevise(),
                reservation.getEmail(),
                reservation.getTelephone(),
                reservation.getCreeLe(),
                passagers,
                segments,
                annulation);
    }

    /** Entite JPA -> devis d'annulation pour le front-end. */
    public DevisAnnulationReponse mapperDevis(Reservation reservation) {
        return new DevisAnnulationReponse(
                reservation.getId(),
                reservation.getAnnulationId(),
                reservation.getMontantRembourse(),
                reservation.getDeviseRembourse(),
                reservation.getDevisExpireLe());
    }

    /** Tranches Duffel -> segments aplatis avec le numero de tranche. */
    private List<OffreReponse.Segment> segments(List<DuffelDtos.Slice> slices) {
        List<OffreReponse.Segment> segments = new ArrayList<>();
        if (slices == null) {
            return segments;
        }
        for (int tranche = 0; tranche < slices.size(); tranche++) {
            DuffelDtos.Slice slice = slices.get(tranche);
            if (slice == null || slice.segments() == null) {
                continue;
            }
            for (DuffelDtos.Segment segment : slice.segments()) {
                if (segment == null) {
                    continue;
                }
                segments.add(new OffreReponse.Segment(
                        tranche + 1,
                        codeAeroport(segment.origin()),
                        codeAeroport(segment.destination()),
                        instantLocal(segment.departing_at()),
                        instantLocal(segment.arriving_at()),
                        segment.operating_carrier_flight_number(),
                        nomCompagnie(segment.operating_carrier())));
            }
        }
        return segments;
    }

    /** Tranches Duffel -> segments JPA. */
    private List<Segment> segmentsJpa(List<DuffelDtos.Slice> slices) {
        List<Segment> segments = new ArrayList<>();
        if (slices == null) {
            return segments;
        }
        for (int tranche = 0; tranche < slices.size(); tranche++) {
            DuffelDtos.Slice slice = slices.get(tranche);
            if (slice == null || slice.segments() == null) {
                continue;
            }
            for (DuffelDtos.Segment segment : slice.segments()) {
                if (segment == null) {
                    continue;
                }
                segments.add(new Segment(
                        tranche + 1,
                        codeAeroport(segment.origin()),
                        codeAeroport(segment.destination()),
                        instantLocal(segment.departing_at()),
                        instantLocal(segment.arriving_at()),
                        segment.operating_carrier_flight_number(),
                        nomCompagnie(segment.operating_carrier())));
            }
        }
        return segments;
    }

    private String nomCompagnie(DuffelDtos.Compagnie compagnie) {
        return compagnie == null ? null : compagnie.name();
    }

    private String codeAeroport(DuffelDtos.Aeroport aeroport) {
        return aeroport == null ? null : aeroport.iata_code();
    }

    private LocalDateTime instantLocal(String heureLocale) {
        return heureLocale == null || heureLocale.isBlank() ? null : LocalDateTime.parse(heureLocale);
    }
}

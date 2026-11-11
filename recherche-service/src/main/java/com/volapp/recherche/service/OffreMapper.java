package com.volapp.recherche.service;

import com.volapp.recherche.api.Offre;
import com.volapp.recherche.duffel.DuffelDtos;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Convertit le format Duffel (package duffel) vers le format interne (package api).
 * Les dates des segments sont l'heure locale de l'aeroport (sans fuseau) : LocalDateTime.
 * expires_at est en UTC avec fuseau : Instant.
 */
@Component
public class OffreMapper {

    public List<Offre> mapper(DuffelDtos.DonneesRecherche donnees) {
        if (donnees == null || donnees.offers() == null) {
            return List.of();
        }
        return donnees.offers().stream()
                .filter(offre -> offre != null && offre.id() != null)
                .map(this::mapper)
                .toList();
    }

    public Offre mapper(DuffelDtos.Offre offre) {
        List<Offre.Passager> passagers = offre.passengers() == null ? List.of()
                : offre.passengers().stream()
                        .map(passager -> new Offre.Passager(passager.id(), passager.type()))
                        .toList();
        List<Offre.Tranche> tranches = offre.slices() == null ? List.of()
                : offre.slices().stream()
                        .map(this::mapperTranche)
                        .toList();
        return new Offre(
                offre.id(),
                offre.total_amount(),
                offre.total_currency(),
                nom(offre.owner()),
                code(offre.owner()),
                marqueFare(offre),
                offre.expires_at() == null ? null : Instant.parse(offre.expires_at()),
                passagers,
                tranches);
    }

    private Offre.Tranche mapperTranche(DuffelDtos.Slice slice) {
        List<Offre.Segment> segments = slice.segments() == null ? List.of()
                : slice.segments().stream()
                        .map(this::mapperSegment)
                        .toList();
        return new Offre.Tranche(
                codeAeroport(slice.origin()),
                codeAeroport(slice.destination()),
                duree(slice.duration()),
                segments);
    }

    private Offre.Segment mapperSegment(DuffelDtos.Segment segment) {
        return new Offre.Segment(
                nom(segment.operating_carrier()),
                code(segment.operating_carrier()),
                segment.operating_carrier_flight_number(),
                instantLocal(segment.departing_at()),
                instantLocal(segment.arriving_at()),
                duree(segment.duration()),
                codeAeroport(segment.origin()),
                codeAeroport(segment.destination()),
                segment.origin_terminal(),
                segment.destination_terminal(),
                segment.stops() == null ? 0 : segment.stops().size());
    }

    private String marqueFare(DuffelDtos.Offre offre) {
        return offre.slices() == null || offre.slices().isEmpty()
                ? null : offre.slices().get(0).fare_brand_name();
    }

    private String nom(DuffelDtos.Compagnie compagnie) {
        return compagnie == null ? null : compagnie.name();
    }

    private String code(DuffelDtos.Compagnie compagnie) {
        return compagnie == null ? null : compagnie.iata_code();
    }

    private String codeAeroport(DuffelDtos.Aeroport aeroport) {
        return aeroport == null ? null : aeroport.iata_code();
    }

    private LocalDateTime instantLocal(String heureLocale) {
        return heureLocale == null || heureLocale.isBlank() ? null : LocalDateTime.parse(heureLocale);
    }

    private Duration duree(String dureeIso) {
        return dureeIso == null || dureeIso.isBlank() ? null : Duration.parse(dureeIso);
    }
}

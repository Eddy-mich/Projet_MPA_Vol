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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de conversion du format Duffel vers le format interne et l'inverse.
 */
class ReservationMapperTest {

    private final ReservationMapper mapper = new ReservationMapper();

    @Test
    void transformeUneOffreDuffelEnOffreInterne() {
        OffreReponse offre = mapper.mapperOffre(offreDuffel());

        assertThat(offre.id()).isEqualTo("off_test");
        assertThat(offre.montantTotal()).isEqualByComparingTo("41.64");
        assertThat(offre.devise()).isEqualTo("EUR");
        assertThat(offre.expireLe()).isEqualTo(Instant.parse("2026-12-01T10:00:00Z"));
        assertThat(offre.passagers()).hasSize(1);
        assertThat(offre.passagers().get(0).id()).isEqualTo("pas_test");
        assertThat(offre.passagers().get(0).type()).isEqualTo("adult");
        assertThat(offre.segments()).hasSize(1);

        OffreReponse.Segment segment = offre.segments().get(0);
        assertThat(segment.tranche()).isEqualTo(1);
        assertThat(segment.origine()).isEqualTo("CDG");
        assertThat(segment.destination()).isEqualTo("LHR");
        assertThat(segment.depart()).isEqualTo(LocalDateTime.parse("2026-12-10T06:58:00"));
        assertThat(segment.arrivee()).isEqualTo(LocalDateTime.parse("2026-12-10T09:04:00"));
        assertThat(segment.numeroVol()).isEqualTo("6677");
        assertThat(segment.compagnie()).isEqualTo("Duffel Airways");
    }

    @Test
    void offreAvecSlicesAbsentesRenvoieDesSegmentsVides() {
        DuffelDtos.DonneesOffre duffel = new DuffelDtos.DonneesOffre(
                "off_test", new BigDecimal("10.00"), "EUR", null, null, null);

        OffreReponse offre = mapper.mapperOffre(duffel);

        assertThat(offre.expireLe()).isNull();
        assertThat(offre.passagers()).isEmpty();
        assertThat(offre.segments()).isEmpty();
    }

    @Test
    void creeEntiteConfirmeDepuisUneCommandeDuffel() {
        Reservation entite = mapper.creerEntite(commandeDuffel(), requete());

        assertThat(entite.getId()).isNotNull();
        assertThat(entite.getReferenceReservation()).isEqualTo("ABC123");
        assertThat(entite.getOrdreDuffel()).isEqualTo("ord_test");
        assertThat(entite.getOffreId()).isEqualTo("off_test");
        assertThat(entite.getStatut()).isEqualTo(StatutReservation.CONFIRME);
        assertThat(entite.getMontantTotal()).isEqualByComparingTo("41.64");
        assertThat(entite.getDevise()).isEqualTo("EUR");
        assertThat(entite.getEmail()).isEqualTo("jean.dupont@example.com");
        assertThat(entite.getTelephone()).isEqualTo("+33601020304");
        assertThat(entite.getAnnulationId()).isNull();
        assertThat(entite.getPassagers()).hasSize(1);
        assertThat(entite.getPassagers().get(0).getPrenom()).isEqualTo("Jean");
        assertThat(entite.getPassagers().get(0).getNom()).isEqualTo("Dupont");
        assertThat(entite.getPassagers().get(0).getDateNaissance()).isEqualTo(LocalDate.of(1990, 5, 1));
        assertThat(entite.getSegments()).hasSize(1);
        assertThat(entite.getSegments().get(0).getNumeroVol()).isEqualTo("6677");
        assertThat(entite.getSegments().get(0).getTranche()).isEqualTo(1);
    }

    @Test
    void transformeEntiteAnnuleeEnReponse() {
        Reservation entite = entiteConfirnee();
        entite.setStatut(StatutReservation.ANNULE);
        entite.setAnnulationId("ore_test");
        entite.setMontantRembourse(new BigDecimal("30.00"));
        entite.setDeviseRembourse("EUR");
        entite.setAnnuleeLe(Instant.parse("2026-12-10T12:00:00Z"));

        ReservationReponse reponse = mapper.mapper(entite);

        assertThat(reponse.referenceReservation()).isEqualTo("ABC123");
        assertThat(reponse.statut()).isEqualTo("ANNULE");
        assertThat(reponse.montantTotal()).isEqualByComparingTo("41.64");
        assertThat(reponse.email()).isEqualTo("jean.dupont@example.com");
        assertThat(reponse.passagers()).hasSize(1);
        assertThat(reponse.segments()).hasSize(1);
        assertThat(reponse.annulation()).isNotNull();
        assertThat(reponse.annulation().id()).isEqualTo("ore_test");
        assertThat(reponse.annulation().montantRembourse()).isEqualByComparingTo("30.00");
        assertThat(reponse.annulation().devise()).isEqualTo("EUR");
        assertThat(reponse.annulation().annuleeLe()).isEqualTo(Instant.parse("2026-12-10T12:00:00Z"));
    }

    @Test
    void entiteConfirmeSansAnnulationRenvoieAnnulationNulle() {
        ReservationReponse reponse = mapper.mapper(entiteConfirnee());

        assertThat(reponse.statut()).isEqualTo("CONFIRME");
        assertThat(reponse.annulation()).isNull();
    }

    @Test
    void transformeEntiteEnDevisAnnulation() {
        Reservation entite = entiteConfirnee();
        entite.setAnnulationId("ore_test");
        entite.setMontantRembourse(new BigDecimal("30.00"));
        entite.setDeviseRembourse("EUR");
        entite.setDevisExpireLe(Instant.parse("2026-12-10T11:00:00Z"));

        DevisAnnulationReponse devis = mapper.mapperDevis(entite);

        assertThat(devis.reservationId()).isEqualTo(entite.getId());
        assertThat(devis.id()).isEqualTo("ore_test");
        assertThat(devis.montantRembourse()).isEqualByComparingTo("30.00");
        assertThat(devis.devise()).isEqualTo("EUR");
        assertThat(devis.expireLe()).isEqualTo(Instant.parse("2026-12-10T11:00:00Z"));
    }

    /* ---------- Fixtures ---------- */

    private Reservation entiteConfirnee() {
        Passager passager = new Passager("mr", "Jean", "Dupont", LocalDate.of(1990, 5, 1), "m");
        Segment segment = new Segment(
                1, "CDG", "LHR",
                LocalDateTime.parse("2026-12-10T06:58:00"),
                LocalDateTime.parse("2026-12-10T09:04:00"),
                "6677", "Duffel Airways");
        return new Reservation(
                "ABC123", "ord_test", "off_test", StatutReservation.CONFIRME,
                new BigDecimal("41.64"), "EUR",
                "jean.dupont@example.com", "+33601020304",
                Instant.parse("2026-12-01T08:00:00Z"),
                List.of(passager), List.of(segment));
    }

    private RequeteReservation requete() {
        RequeteReservation.Passager passager = new RequeteReservation.Passager(
                "mr", "Jean", "Dupont", LocalDate.of(1990, 5, 1), "m", null);
        return new RequeteReservation(
                "off_test", new BigDecimal("41.64"),
                "jean.dupont@example.com", "+33601020304", List.of(passager));
    }

    private DuffelDtos.DonneesOffre offreDuffel() {
        return new DuffelDtos.DonneesOffre(
                "off_test",
                new BigDecimal("41.64"),
                "EUR",
                "2026-12-01T10:00:00Z",
                List.of(new DuffelDtos.Passager("pas_test", "adult")),
                List.of(trancheDuffel()));
    }

    private DuffelDtos.DonneesCommande commandeDuffel() {
        return new DuffelDtos.DonneesCommande(
                "ord_test", "ABC123", "off_test",
                new BigDecimal("41.64"), "EUR",
                List.of(trancheDuffel()));
    }

    private DuffelDtos.Slice trancheDuffel() {
        DuffelDtos.Segment segment = new DuffelDtos.Segment(
                "seg_1",
                new DuffelDtos.Aeroport("CDG", "Paris - Charles de Gaulle"),
                new DuffelDtos.Aeroport("LHR", "London Heathrow"),
                "2026-12-10T06:58:00",
                "2026-12-10T09:04:00",
                "PT2H6M",
                new DuffelDtos.Compagnie("arl_1", "Duffel Airways", "ZZ"),
                "6677",
                "2E", "3",
                List.of());
        return new DuffelDtos.Slice(
                "sli_1",
                new DuffelDtos.Aeroport("CDG", "Paris - Charles de Gaulle"),
                new DuffelDtos.Aeroport("LHR", "London Heathrow"),
                "PT2H6M",
                "Standard",
                List.of(segment));
    }
}

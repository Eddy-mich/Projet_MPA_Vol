package com.volapp.recherche.service;

import com.volapp.recherche.api.Offre;
import com.volapp.recherche.duffel.DuffelDtos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de conversion du format Duffel vers le format interne.
 */
class OffreMapperTest {

    private final OffreMapper mapper = new OffreMapper();

    @Test
    void transformeUneOffreDuffelEnOffreInterne() {
        Offre offre = mapper.mapper(offreDuffel());

        assertThat(offre.id()).isEqualTo("off_test");
        assertThat(offre.montantTotal()).isEqualByComparingTo("41.64");
        assertThat(offre.devise()).isEqualTo("EUR");
        assertThat(offre.compagnie()).isEqualTo("Duffel Airways");
        assertThat(offre.codeCompagnie()).isEqualTo("ZZ");
        assertThat(offre.marqueFare()).isEqualTo("Standard");
        assertThat(offre.expireLe()).isEqualTo(Instant.parse("2026-12-01T10:00:00Z"));
        assertThat(offre.passagers()).hasSize(1);
        assertThat(offre.passagers().get(0).id()).isEqualTo("pas_test");
        assertThat(offre.tranches()).hasSize(1);

        Offre.Tranche tranche = offre.tranches().get(0);
        assertThat(tranche.origine()).isEqualTo("CDG");
        assertThat(tranche.destination()).isEqualTo("LHR");
        assertThat(tranche.duree()).isEqualTo(Duration.parse("PT2H6M"));

        Offre.Segment segment = tranche.segments().get(0);
        assertThat(segment.compagnie()).isEqualTo("Duffel Airways");
        assertThat(segment.codeCompagnie()).isEqualTo("ZZ");
        assertThat(segment.numeroVol()).isEqualTo("6677");
        assertThat(segment.depart()).isEqualTo(LocalDateTime.parse("2026-12-10T06:58:00"));
        assertThat(segment.arrivee()).isEqualTo(LocalDateTime.parse("2026-12-10T09:04:00"));
        assertThat(segment.duree()).isEqualTo(Duration.parse("PT2H6M"));
        assertThat(segment.origine()).isEqualTo("CDG");
        assertThat(segment.destination()).isEqualTo("LHR");
        assertThat(segment.nombreEscales()).isZero();
    }

    @Test
    void gereDesChampsAbsentsSansLever() {
        DuffelDtos.Offre duffel = new DuffelDtos.Offre(
                "off_test", null, "2026-12-01T10:00:00Z",
                null, null, new BigDecimal("10.00"), "EUR", null, null, null, null);

        Offre offre = mapper.mapper(duffel);

        assertThat(offre.compagnie()).isNull();
        assertThat(offre.codeCompagnie()).isNull();
        assertThat(offre.marqueFare()).isNull();
        assertThat(offre.passagers()).isEmpty();
        assertThat(offre.tranches()).isEmpty();
    }

    @Test
    void listeVideOuNulleRenvoieUneListeVide() {
        assertThat(mapper.mapper((DuffelDtos.DonneesRecherche) null)).isEmpty();
        assertThat(mapper.mapper(new DuffelDtos.DonneesRecherche("req_1", null))).isEmpty();
        assertThat(mapper.mapper(new DuffelDtos.DonneesRecherche("req_1", List.of()))).isEmpty();
    }

    @Test
    void dureeEtDatesNullRestentNull() {
        DuffelDtos.Segment segment = new DuffelDtos.Segment(
                "seg_1",
                new DuffelDtos.Aeroport("CDG", "Paris"),
                new DuffelDtos.Aeroport("LHR", "Londres"),
                null, null, null,
                null, null, null, null, List.of());
        DuffelDtos.Slice tranche = new DuffelDtos.Slice(
                "sli_1",
                new DuffelDtos.Aeroport("CDG", "Paris"),
                new DuffelDtos.Aeroport("LHR", "Londres"),
                null, null, List.of(segment));
        DuffelDtos.Offre duffel = new DuffelDtos.Offre(
                "off_test", null, null,
                null, null, new BigDecimal("10.00"), "EUR", null, null,
                List.of(), List.of(tranche));

        Offre offre = mapper.mapper(duffel);

        assertThat(offre.expireLe()).isNull();
        Offre.Segment segmentInterne = offre.tranches().get(0).segments().get(0);
        assertThat(segmentInterne.depart()).isNull();
        assertThat(segmentInterne.arrivee()).isNull();
        assertThat(segmentInterne.duree()).isNull();
    }

    private DuffelDtos.Offre offreDuffel() {
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
        DuffelDtos.Slice tranche = new DuffelDtos.Slice(
                "sli_1",
                new DuffelDtos.Aeroport("CDG", "Paris - Charles de Gaulle"),
                new DuffelDtos.Aeroport("LHR", "London Heathrow"),
                "PT2H6M",
                "Standard",
                List.of(segment));
        return new DuffelDtos.Offre(
                "off_test",
                new DuffelDtos.Compagnie("arl_1", "Duffel Airways", "ZZ"),
                "2026-12-01T10:00:00Z",
                new BigDecimal("35.96"),
                "EUR",
                new BigDecimal("41.64"),
                "EUR",
                new BigDecimal("5.68"),
                "EUR",
                List.of(new DuffelDtos.Passager("pas_test", "adult")),
                List.of(tranche));
    }
}

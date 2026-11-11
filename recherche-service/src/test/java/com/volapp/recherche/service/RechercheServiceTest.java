package com.volapp.recherche.service;

import com.volapp.recherche.api.Offre;
import com.volapp.recherche.api.RechercheRequete;
import com.volapp.recherche.duffel.DuffelClient;
import com.volapp.recherche.duffel.DuffelDtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de construction de la requete Duffel par le service recherche.
 */
@ExtendWith(MockitoExtension.class)
class RechercheServiceTest {

    @Mock
    private DuffelClient duffelClient;

    private RechercheService rechercheService;

    @BeforeEach
    void preparer() {
        rechercheService = new RechercheService(duffelClient, new OffreMapper());
    }

    @Test
    void allerSimpleUneTrancheEtDeuxPassagers() {
        when(duffelClient.chercherOffres(any())).thenAnswer(invocation -> {
            DuffelDtos.RequeteRecherche requete = invocation.getArgument(0);
            DuffelDtos.DataRecherche data = requete.data();
            assertThat(data.slices()).hasSize(1);
            DuffelDtos.SliceRequete tranche = data.slices().get(0);
            assertThat(tranche.origin()).isEqualTo("CDG");
            assertThat(tranche.destination()).isEqualTo("LHR");
            assertThat(tranche.departure_date()).isEqualTo("2026-12-10");
            assertThat(data.passengers()).hasSize(2);
            assertThat(data.passengers().get(0).type()).isEqualTo("adult");
            assertThat(data.cabin_class()).isEqualTo("economy");
            return new DuffelDtos.DonneesRecherche("req_1", List.of());
        });

        RechercheRequete requete = new RechercheRequete(
                "cdg", "lhr", LocalDate.of(2026, 12, 10), null, 2, null);
        List<Offre> offres = rechercheService.rechercherOffres(requete);

        assertThat(offres).isEmpty();
        verify(duffelClient, times(1)).chercherOffres(any());
    }

    @Test
    void allerRetourDeuxTranchesEtCabineDemandee() {
        ArgumentCaptor<DuffelDtos.RequeteRecherche> captor =
                ArgumentCaptor.forClass(DuffelDtos.RequeteRecherche.class);
        when(duffelClient.chercherOffres(any()))
                .thenReturn(new DuffelDtos.DonneesRecherche("req_1", List.of()));

        RechercheRequete requete = new RechercheRequete(
                "CDG", "LHR", LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 15), 1, "Business");
        rechercheService.rechercherOffres(requete);

        verify(duffelClient).chercherOffres(captor.capture());
        DuffelDtos.DataRecherche data = captor.getValue().data();
        assertThat(data.slices()).hasSize(2);
        assertThat(data.slices().get(0).origin()).isEqualTo("CDG");
        assertThat(data.slices().get(0).destination()).isEqualTo("LHR");
        assertThat(data.slices().get(1).origin()).isEqualTo("LHR");
        assertThat(data.slices().get(1).destination()).isEqualTo("CDG");
        assertThat(data.slices().get(1).departure_date()).isEqualTo("2026-12-15");
        assertThat(data.cabin_class()).isEqualTo("business");
    }

    @Test
    void montantTotalEstUnBigDecimal() {
        DuffelDtos.Offre duffel = new DuffelDtos.Offre(
                "off_1",
                new DuffelDtos.Compagnie("arl_1", "Duffel Airways", "ZZ"),
                "2026-12-01T10:00:00Z",
                new BigDecimal("35.96"), "EUR",
                new BigDecimal("41.64"), "EUR",
                new BigDecimal("5.68"), "EUR",
                List.of(), List.of());
        when(duffelClient.chercherOffres(any()))
                .thenReturn(new DuffelDtos.DonneesRecherche("req_1", List.of(duffel)));

        List<Offre> offres = rechercheService.rechercherOffres(new RechercheRequete(
                "CDG", "LHR", LocalDate.of(2026, 12, 10), null, 1, null));

        assertThat(offres).hasSize(1);
        assertThat(offres.get(0).montantTotal()).isEqualByComparingTo("41.64");
    }
}

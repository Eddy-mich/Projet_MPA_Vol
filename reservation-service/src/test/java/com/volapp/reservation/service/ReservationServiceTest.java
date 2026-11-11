package com.volapp.reservation.service;

import com.volapp.reservation.api.OffreReponse;
import com.volapp.reservation.api.RequeteReservation;
import com.volapp.reservation.api.ReservationReponse;
import com.volapp.reservation.duffel.DuffelClient;
import com.volapp.reservation.duffel.DuffelDtos;
import com.volapp.reservation.domaine.Passager;
import com.volapp.reservation.domaine.Reservation;
import com.volapp.reservation.domaine.ReservationRepository;
import com.volapp.reservation.domaine.Segment;
import com.volapp.reservation.domaine.StatutReservation;
import com.volapp.reservation.evenements.EvenementPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de la logique metier de reservation : controle du prix, commande Duffel,
 * annulation en deux temps, evenements.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private DuffelClient duffelClient;

    @Mock
    private ReservationRepository repository;

    @Mock
    private EvenementPublisher publisher;

    private ReservationService reservationService;

    @BeforeEach
    void preparer() {
        reservationService = new ReservationService(
                duffelClient, repository, new ReservationMapper(), publisher);
        lenient().when(repository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void reserverConstruitLaCommandeDuffelEtPublieLevenement() {
        when(duffelClient.lireOffre("off_test")).thenReturn(offreDuffel());
        ArgumentCaptor<DuffelDtos.RequeteCommande> captor =
                ArgumentCaptor.forClass(DuffelDtos.RequeteCommande.class);
        when(duffelClient.creerCommande(any())).thenAnswer(invocation -> {
            DuffelDtos.RequeteCommande requete = invocation.getArgument(0);
            DuffelDtos.DataCommande data = requete.data();
            assertThat(data.type()).isEqualTo("instant");
            assertThat(data.selected_offers()).containsExactly("off_test");
            assertThat(data.passengers()).hasSize(1);
            DuffelDtos.PassagerCommande passager = data.passengers().get(0);
            assertThat(passager.id()).isEqualTo("pas_test");
            assertThat(passager.title()).isEqualTo("mr");
            assertThat(passager.given_name()).isEqualTo("Jean");
            assertThat(passager.family_name()).isEqualTo("Dupont");
            assertThat(passager.born_on()).isEqualTo("1990-05-01");
            assertThat(passager.phone_number()).isEqualTo("+33601020304");
            assertThat(passager.gender()).isEqualTo("m");
            assertThat(passager.email()).isEqualTo("jean.dupont@example.com");
            DuffelDtos.Paiement paiement = data.payments().get(0);
            assertThat(paiement.type()).isEqualTo("balance");
            assertThat(paiement.currency()).isEqualTo("EUR");
            assertThat(paiement.amount()).isEqualTo("41.64");
            return commandeDuffel();
        });

        ReservationReponse reponse = reservationService.reserver(requete());

        assertThat(reponse.referenceReservation()).isEqualTo("ABC123");
        assertThat(reponse.statut()).isEqualTo("CONFIRME");
        assertThat(reponse.montantTotal()).isEqualByComparingTo("41.64");
        verify(duffelClient).creerCommande(captor.capture());
        verify(repository).save(any(Reservation.class));
        verify(publisher).publierConfirmation(any());
    }

    @Test
    void reserverLeveSiLePrixATChange() {
        when(duffelClient.lireOffre("off_test"))
                .thenReturn(offreDuffelAvecMontant("42.00"));

        assertThatThrownBy(() -> reservationService.reserver(requete()))
                .isInstanceOf(Exceptions.PrixModifieException.class)
                .hasMessageContaining("42.00");
        verify(duffelClient, never()).creerCommande(any());
    }

    @Test
    void reserverLeveSiLOffreEstIntrouvablee() {
        when(duffelClient.lireOffre("off_test"))
                .thenThrow(new Exceptions.ErreurDuffel(404, "not_found", "Offre introuvable"));

        assertThatThrownBy(() -> reservationService.reserver(requete()))
                .isInstanceOf(Exceptions.OffreIntrouvableeException.class);
        verify(duffelClient, never()).creerCommande(any());
    }

    @Test
    void reserverLeveSiLOffreEstExpiree() {
        DuffelDtos.DonneesOffre expiree = new DuffelDtos.DonneesOffre(
                "off_test", new BigDecimal("41.64"), "EUR",
                Instant.now().minusSeconds(60).toString(),
                List.of(new DuffelDtos.Passager("pas_test", "adult")),
                List.of());
        when(duffelClient.lireOffre("off_test")).thenReturn(expiree);

        assertThatThrownBy(() -> reservationService.reserver(requete()))
                .isInstanceOf(Exceptions.OffreExpireeException.class);
        verify(duffelClient, never()).creerCommande(any());
    }

    @Test
    void reserverSignaleLaCommandeDuffelSilEnregistrementEchoue() {
        when(duffelClient.lireOffre("off_test")).thenReturn(offreDuffel());
        when(duffelClient.creerCommande(any())).thenReturn(commandeDuffel());
        when(repository.save(any(Reservation.class)))
                .thenThrow(new RuntimeException("base inaccessible"));

        assertThatThrownBy(() -> reservationService.reserver(requete()))
                .isInstanceOf(Exceptions.EchecEnregistrementException.class)
                .hasMessageContaining("ord_test");
        verify(publisher, never()).publierConfirmation(any());
    }

    @Test
    void lireOffreRetourneLePrixAjour() {
        when(duffelClient.lireOffre("off_test")).thenReturn(offreDuffel());

        OffreReponse offre = reservationService.lireOffre("off_test");

        assertThat(offre.id()).isEqualTo("off_test");
        assertThat(offre.montantTotal()).isEqualByComparingTo("41.64");
        assertThat(offre.passagers().get(0).id()).isEqualTo("pas_test");
    }

    @Test
    void demanderAnnulationStockeLeDevis() {
        Reservation entite = entiteConfirnee();
        when(repository.findById(entite.getId())).thenReturn(Optional.of(entite));
        when(duffelClient.demanderDevisAnnulation("ord_test"))
                .thenReturn(new DuffelDtos.DonneesAnnulation(
                        "ore_test", "ord_test", "pending",
                        new BigDecimal("30.00"), "EUR",
                        Instant.now().plusSeconds(24 * 3600).toString(), null));

        var devis = reservationService.demanderAnnulation(entite.getId());

        assertThat(devis.id()).isEqualTo("ore_test");
        assertThat(devis.montantRembourse()).isEqualByComparingTo("30.00");
        assertThat(entite.getAnnulationId()).isEqualTo("ore_test");
        assertThat(entite.getStatut()).isEqualTo(StatutReservation.CONFIRME);
        verify(repository).save(entite);
    }

    @Test
    void demanderAnnulationRefuseSiDejaAnnulee() {
        Reservation entite = entiteConfirnee();
        entite.setStatut(StatutReservation.ANNULE);
        when(repository.findById(entite.getId())).thenReturn(Optional.of(entite));

        assertThatThrownBy(() -> reservationService.demanderAnnulation(entite.getId()))
                .isInstanceOf(Exceptions.ReservationDejaAnnuleeException.class);
        verify(duffelClient, never()).demanderDevisAnnulation(any());
    }

    @Test
    void confirmerAnnulationRefuseSansDevis() {
        Reservation entite = entiteConfirnee();
        when(repository.findById(entite.getId())).thenReturn(Optional.of(entite));

        assertThatThrownBy(() -> reservationService.confirmerAnnulation(entite.getId(), "ore_test"))
                .isInstanceOf(Exceptions.DevisAnnulationAbsentException.class);
        verify(duffelClient, never()).confirmerAnnulation(any());
    }

    @Test
    void confirmerAnnulationRefuseUnDevisEtranger() {
        Reservation entite = entiteConfirnee();
        entite.setAnnulationId("ore_autre");
        entite.setDevisExpireLe(Instant.now().plusSeconds(3600));
        when(repository.findById(entite.getId())).thenReturn(Optional.of(entite));

        assertThatThrownBy(() -> reservationService.confirmerAnnulation(entite.getId(), "ore_test"))
                .isInstanceOf(Exceptions.DevisAnnulationInvalideeException.class);
        verify(duffelClient, never()).confirmerAnnulation(any());
    }

    @Test
    void confirmerAnnulationRefuseUnDevisExpire() {
        Reservation entite = entiteConfirnee();
        entite.setAnnulationId("ore_test");
        entite.setDevisExpireLe(Instant.now().minusSeconds(60));
        when(repository.findById(entite.getId())).thenReturn(Optional.of(entite));

        assertThatThrownBy(() -> reservationService.confirmerAnnulation(entite.getId(), "ore_test"))
                .isInstanceOf(Exceptions.DevisAnnulationExpireeException.class);
        verify(duffelClient, never()).confirmerAnnulation(any());
    }

    @Test
    void confirmerAnnulationAnnuLeChezDuffelEtPublieLevenement() {
        Reservation entite = entiteConfirnee();
        entite.setAnnulationId("ore_test");
        entite.setMontantRembourse(new BigDecimal("30.00"));
        entite.setDeviseRembourse("EUR");
        entite.setDevisExpireLe(Instant.now().plusSeconds(3600));
        when(repository.findById(entite.getId())).thenReturn(Optional.of(entite));

        ReservationReponse reponse = reservationService.confirmerAnnulation(entite.getId(), "ore_test");

        assertThat(reponse.statut()).isEqualTo("ANNULE");
        assertThat(reponse.annulation()).isNotNull();
        verify(duffelClient).confirmerAnnulation("ore_test");
        verify(repository).save(entite);
        verify(publisher).publierAnnulation(any());
    }

    @Test
    void lireUneReservationIntrouvableLeve() {
        UUID idIntrouvable = UUID.randomUUID();
        when(repository.findById(idIntrouvable)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.lire(idIntrouvable))
                .isInstanceOf(Exceptions.ReservationIntrouvableeException.class);
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
        return offreDuffelAvecMontant("41.64");
    }

    private DuffelDtos.DonneesOffre offreDuffelAvecMontant(String montant) {
        return new DuffelDtos.DonneesOffre(
                "off_test",
                new BigDecimal(montant),
                "EUR",
                Instant.now().plusSeconds(24 * 3600).toString(),
                List.of(new DuffelDtos.Passager("pas_test", "adult")),
                List.of());
    }

    private DuffelDtos.DonneesCommande commandeDuffel() {
        return new DuffelDtos.DonneesCommande(
                "ord_test", "ABC123", "off_test",
                new BigDecimal("41.64"), "EUR",
                List.of());
    }
}

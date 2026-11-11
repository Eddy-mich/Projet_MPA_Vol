package com.volapp.reservation.service;

import com.volapp.reservation.api.DevisAnnulationReponse;
import com.volapp.reservation.api.OffreReponse;
import com.volapp.reservation.api.RequeteReservation;
import com.volapp.reservation.api.ReservationReponse;
import com.volapp.reservation.duffel.DuffelClient;
import com.volapp.reservation.duffel.DuffelDtos;
import com.volapp.reservation.domaine.Reservation;
import com.volapp.reservation.domaine.ReservationRepository;
import com.volapp.reservation.domaine.StatutReservation;
import com.volapp.reservation.evenements.EvenementPublisher;
import com.volapp.reservation.evenements.ReservationAnnulee;
import com.volapp.reservation.evenements.ReservationConfirmee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Logique metier de reservation : relecture d'offre, controle du prix,
 * commande Duffel (hors transaction), enregistrement court, annulation en deux temps.
 */
@Service
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    private final DuffelClient duffelClient;
    private final ReservationRepository repository;
    private final ReservationMapper reservationMapper;
    private final EvenementPublisher publisher;

    public ReservationService(DuffelClient duffelClient, ReservationRepository repository,
                              ReservationMapper reservationMapper, EvenementPublisher publisher) {
        this.duffelClient = duffelClient;
        this.repository = repository;
        this.reservationMapper = reservationMapper;
        this.publisher = publisher;
    }

    /** Relecture de l'offre chez Duffel : prix a jour, expiration, identifiants passagers. */
    public OffreReponse lireOffre(String offreId) {
        return reservationMapper.mapperOffre(consulterOffre(offreId));
    }

    public List<ReservationReponse> lister() {
        return repository.findAll().stream()
                .map(reservationMapper::mapper)
                .toList();
    }

    public ReservationReponse lire(UUID id) {
        return reservationMapper.mapper(chargerReservation(id));
    }

    /**
     * Reserve : relecture de l'offre, controle du prix, commande Duffel
     * (hors transaction), enregistrement en base, evenement "reservation.confirmee".
     */
    public ReservationReponse reserver(RequeteReservation requete) {
        DuffelDtos.DonneesOffre offre = consulterOffre(requete.offreId());
        if (offre.total_amount() == null
                || offre.total_amount().compareTo(requete.montantAttendu()) != 0) {
            throw new Exceptions.PrixModifieException(offre.total_amount(), offre.total_currency());
        }
        DuffelDtos.DonneesCommande commande = duffelClient.creerCommande(
                construireRequeteCommande(offre, requete));
        Reservation reservation;
        try {
            reservation = repository.save(reservationMapper.creerEntite(commande, requete));
        } catch (RuntimeException e) {
            logger.error("Commande Duffel {} creee mais non enregistree en base", commande.id(), e);
            throw new Exceptions.EchecEnregistrementException(commande.id());
        }
        publisher.publierConfirmation(construireEvenementConfirmation(reservation));
        return reservationMapper.mapper(reservation);
    }

    /** Demande un devis d'annulation chez Duffel : rien n'est encore annule. */
    public DevisAnnulationReponse demanderAnnulation(UUID id) {
        Reservation reservation = chargerReservation(id);
        if (reservation.getStatut() == StatutReservation.ANNULE) {
            throw new Exceptions.ReservationDejaAnnuleeException("Cette reservation est deja annulee.");
        }
        DuffelDtos.DonneesAnnulation devis = duffelClient.demanderDevisAnnulation(reservation.getOrdreDuffel());
        reservation.setAnnulationId(devis.id());
        reservation.setMontantRembourse(devis.refund_amount());
        reservation.setDeviseRembourse(devis.refund_currency());
        reservation.setDevisExpireLe(devis.expires_at() == null ? null : Instant.parse(devis.expires_at()));
        repository.save(reservation);
        return reservationMapper.mapperDevis(reservation);
    }

    /** Confirme l'annulation chez Duffel : la reservation passe au statut ANNULE. */
    public ReservationReponse confirmerAnnulation(UUID id, String annulationId) {
        Reservation reservation = chargerReservation(id);
        if (reservation.getStatut() == StatutReservation.ANNULE) {
            throw new Exceptions.ReservationDejaAnnuleeException("Cette reservation est deja annulee.");
        }
        if (reservation.getAnnulationId() == null) {
            throw new Exceptions.DevisAnnulationAbsentException(
                    "Demandez d'abord un devis d'annulation avant de confirmer.");
        }
        if (!reservation.getAnnulationId().equals(annulationId)) {
            throw new Exceptions.DevisAnnulationInvalideeException(
                    "Ce devis ne correspond pas a cette reservation.");
        }
        if (reservation.getDevisExpireLe() != null && reservation.getDevisExpireLe().isBefore(Instant.now())) {
            throw new Exceptions.DevisAnnulationExpireeException(
                    "Le devis d'annulation a expire : demandez un nouveau devis.");
        }
        duffelClient.confirmerAnnulation(annulationId);
        reservation.setStatut(StatutReservation.ANNULE);
        reservation.setAnnuleeLe(Instant.now());
        repository.save(reservation);
        publisher.publierAnnulation(construireEvenementAnnulation(reservation));
        return reservationMapper.mapper(reservation);
    }

    /** Relecture de l'offre : 404 si introuvable, 410 si expiree. */
    private DuffelDtos.DonneesOffre consulterOffre(String offreId) {
        DuffelDtos.DonneesOffre offre;
        try {
            offre = duffelClient.lireOffre(offreId);
        } catch (Exceptions.ErreurDuffel e) {
            if (e.statutHttp() == 404) {
                throw new Exceptions.OffreIntrouvableeException(
                        "Cette offre est introuvable ou n'est plus disponible.");
            }
            throw e;
        }
        if (offre.expires_at() != null && Instant.parse(offre.expires_at()).isBefore(Instant.now())) {
            throw new Exceptions.OffreExpireeException("Cette offre a expire : relancez votre recherche.");
        }
        return offre;
    }

    private Reservation chargerReservation(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new Exceptions.ReservationIntrouvableeException(
                        "Cette reservation est introuvable."));
    }

    /** Requete de commande Duffel : type "instant", identifiants pas_... de l'offre, paiement "balance". */
    private DuffelDtos.RequeteCommande construireRequeteCommande(DuffelDtos.DonneesOffre offre,
                                                                 RequeteReservation requete) {
        List<DuffelDtos.PassagerCommande> passagers = new ArrayList<>();
        List<DuffelDtos.Passager> passagersOffre = offre.passengers() == null ? List.of() : offre.passengers();
        for (int i = 0; i < requete.passagers().size(); i++) {
            RequeteReservation.Passager passager = requete.passagers().get(i);
            String pasId = i < passagersOffre.size() ? passagersOffre.get(i).id() : null;
            String telephone = passager.telephone() == null || passager.telephone().isBlank()
                    ? requete.telephone() : passager.telephone();
            passagers.add(new DuffelDtos.PassagerCommande(
                    pasId,
                    passager.titre(),
                    passager.prenom(),
                    passager.nom(),
                    passager.dateNaissance().toString(),
                    telephone,
                    passager.genre(),
                    requete.email()));
        }
        DuffelDtos.Paiement paiement = new DuffelDtos.Paiement(
                "balance", offre.total_currency(), offre.total_amount().toPlainString());
        return new DuffelDtos.RequeteCommande(new DuffelDtos.DataCommande(
                "instant",
                List.of(offre.id()),
                passagers,
                List.of(paiement)));
    }

    private ReservationConfirmee construireEvenementConfirmation(Reservation reservation) {
        List<ReservationConfirmee.Vol> vols = reservation.getSegments().stream()
                .map(segment -> new ReservationConfirmee.Vol(
                        segment.getNumeroVol(),
                        segment.getOrigine(),
                        segment.getDestination(),
                        segment.getDepart().toString()))
                .toList();
        return new ReservationConfirmee(
                reservation.getId(),
                reservation.getReferenceReservation(),
                reservation.getEmail(),
                vols);
    }

    private ReservationAnnulee construireEvenementAnnulation(Reservation reservation) {
        List<ReservationAnnulee.Vol> vols = reservation.getSegments().stream()
                .map(segment -> new ReservationAnnulee.Vol(
                        segment.getNumeroVol(),
                        segment.getDepart().toString()))
                .toList();
        return new ReservationAnnulee(
                reservation.getId(),
                reservation.getReferenceReservation(),
                reservation.getEmail(),
                reservation.getMontantRembourse(),
                reservation.getDeviseRembourse(),
                vols);
    }
}

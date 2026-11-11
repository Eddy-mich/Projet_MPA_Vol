package com.volapp.reservation.api;

import com.volapp.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Point d'entree REST du service reservation.
 * Le gateway expose ces routes sous /api/reservations/**.
 */
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /** Relire une offre chez Duffel : prix a jour, expiration, identifiants passagers. */
    @GetMapping("/offres/{offreId}")
    public OffreReponse lireOffre(@PathVariable String offreId) {
        return reservationService.lireOffre(offreId);
    }

    /** Reserver une offre : controle du prix, commande Duffel, enregistrement, evenement. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationReponse reserver(@Valid @RequestBody RequeteReservation requete) {
        return reservationService.reserver(requete);
    }

    /** Lister les reservations (aucun compte : toutes sont visibles). */
    @GetMapping
    public List<ReservationReponse> lister() {
        return reservationService.lister();
    }

    /** Lire une reservation. */
    @GetMapping("/{id}")
    public ReservationReponse lire(@PathVariable UUID id) {
        return reservationService.lire(id);
    }

    /** Demander un devis d'annulation : rien n'est encore annule. */
    @PostMapping("/{id}/annulation")
    public DevisAnnulationReponse demanderAnnulation(@PathVariable UUID id) {
        return reservationService.demanderAnnulation(id);
    }

    /** Confirmer l'annulation : la reservation passe au statut ANNULE. */
    @PostMapping("/{id}/annulation/{annulationId}/confirmer")
    public ReservationReponse confirmerAnnulation(@PathVariable UUID id,
                                                  @PathVariable String annulationId) {
        return reservationService.confirmerAnnulation(id, annulationId);
    }
}

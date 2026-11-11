package com.volapp.reservation.api;

import com.volapp.reservation.service.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les exceptions en ProblemDetail (RFC 7807), detail toujours en francais.
 */
@RestControllerAdvice
public class GestionErreurs {

    private static final Logger logger = LoggerFactory.getLogger(GestionErreurs.class);

    @ExceptionHandler(Exceptions.ErreurDuffel.class)
    public ProblemDetail erreurDuffel(Exceptions.ErreurDuffel e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.valueOf(e.statutHttp()), e.getMessage());
        if (e.code() != null) {
            detail.setProperty("code", e.code());
        }
        return detail;
    }

    @ExceptionHandler(Exceptions.OffreIntrouvableeException.class)
    public ProblemDetail offreIntrouvablee(Exceptions.OffreIntrouvableeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(Exceptions.OffreExpireeException.class)
    public ProblemDetail offreExpiree(Exceptions.OffreExpireeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, e.getMessage());
    }

    @ExceptionHandler(Exceptions.PrixModifieException.class)
    public ProblemDetail prixModifie(Exceptions.PrixModifieException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        detail.setProperty("montantActuel", e.montantActuel());
        detail.setProperty("devise", e.devise());
        return detail;
    }

    @ExceptionHandler(Exceptions.ReservationIntrouvableeException.class)
    public ProblemDetail reservationIntrouvablee(Exceptions.ReservationIntrouvableeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(Exceptions.ReservationDejaAnnuleeException.class)
    public ProblemDetail reservationDejaAnnulee(Exceptions.ReservationDejaAnnuleeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(Exceptions.DevisAnnulationAbsentException.class)
    public ProblemDetail devisAnnulationAbsent(Exceptions.DevisAnnulationAbsentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(Exceptions.DevisAnnulationInvalideeException.class)
    public ProblemDetail devisAnnulationInvalidee(Exceptions.DevisAnnulationInvalideeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(Exceptions.DevisAnnulationExpireeException.class)
    public ProblemDetail devisAnnulationExpiree(Exceptions.DevisAnnulationExpireeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, e.getMessage());
    }

    @ExceptionHandler(Exceptions.EchecEnregistrementException.class)
    public ProblemDetail echecEnregistrement(Exceptions.EchecEnregistrementException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        detail.setProperty("ordreDuffel", e.ordreDuffel());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail requeteInvalide(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(erreur -> "champ \"" + erreur.getField() + "\" : " + erreur.getDefaultMessage())
                .findFirst()
                .orElse("Requete invalide.");
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail erreurInattendue(Exception e) {
        logger.error("Erreur inattendue du service reservation", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur inattendue du service reservation.");
    }
}

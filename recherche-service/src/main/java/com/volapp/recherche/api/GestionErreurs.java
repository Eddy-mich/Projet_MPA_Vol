package com.volapp.recherche.api;

import com.volapp.recherche.service.Exceptions;
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
        logger.error("Erreur inattendue du service recherche", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur inattendue du service recherche.");
    }
}

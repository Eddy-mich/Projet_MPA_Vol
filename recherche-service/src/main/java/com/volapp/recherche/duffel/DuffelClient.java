package com.volapp.recherche.duffel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volapp.recherche.service.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

/**
 * Client HTTP de l'API Duffel (recherche d'offres).
 * Seule cette classe du service parle a Duffel.
 */
@Component
public class DuffelClient {

    private static final Logger logger = LoggerFactory.getLogger(DuffelClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public DuffelClient(RestClient duffelRestClient, ObjectMapper objectMapper) {
        this.restClient = duffelRestClient;
        this.objectMapper = objectMapper;
    }

    /** POST /air/offer_requests?return_offers=true */
    public DuffelDtos.DonneesRecherche chercherOffres(DuffelDtos.RequeteRecherche requete) {
        try {
            Map<?, ?> reponse = restClient.post()
                    .uri("/air/offer_requests?return_offers=true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requete)
                    .retrieve()
                    .body(Map.class);
            if (reponse == null || !(reponse.get("data") instanceof Map<?, ?> donnees)) {
                throw new Exceptions.ErreurDuffel(502, null,
                        "Reponse inattendue de Duffel lors de la recherche d'offres.");
            }
            DuffelDtos.DonneesRecherche resultats = objectMapper.convertValue(
                    donnees, DuffelDtos.DonneesRecherche.class);
            return resultats;
        } catch (HttpClientErrorException e) {
            throw traduireErreur(e);
        } catch (HttpServerErrorException e) {
            throw traduireErreurInterne(e);
        } catch (ResourceAccessException e) {
            throw new Exceptions.ErreurDuffel(504, null,
                    "Le service Duffel ne repond pas (delai depasse).");
        } catch (RestClientException e) {
            throw traduireEchecTransport(e);
        }
    }

    /**
     * Traduit une erreur HTTP de Duffel en exception metier :
     * 401/403 -> 502 (jeton refuse), 422 -> 422, 429 -> 429,
     * erreur compagnie -> 502 avec code "erreur_compagnie".
     */
    private Exceptions.ErreurDuffel traduireErreur(HttpClientErrorException e) {
        int statut = e.getStatusCode().value();
        DuffelDtos.CorpsErreur corps = lireCorpsErreur(e);
        DuffelDtos.Erreur erreur = corps == null || corps.errors() == null || corps.errors().isEmpty()
                ? null : corps.errors().get(0);
        String code = erreur == null ? null : erreur.code();
        String texte = texteErreur(erreur);

        if ("airline_error".equals(code)) {
            return new Exceptions.ErreurDuffel(502, "erreur_compagnie",
                    "La compagnie a renvoye une erreur : " + (texte == null ? "erreur inconnue" : texte));
        }
        return switch (statut) {
            case 401, 403 -> new Exceptions.ErreurDuffel(502, null,
                    "Le jeton Duffel a ete refuse : verifiez DUFFEL_ACCESS_TOKEN.");
            case 422 -> new Exceptions.ErreurDuffel(422, "refus_compagnie",
                    texte == null ? "La demande a ete refusee par Duffel." : texte);
            case 429 -> new Exceptions.ErreurDuffel(429, null,
                    "Trop de requetes envoyees a Duffel : patientez un instant puis reessayez.");
            default -> new Exceptions.ErreurDuffel(502, "erreur_compagnie",
                    "Erreur inattendue de Duffel (statut " + statut + ").");
        };
    }

    /** Texte d'erreur Duffel : message, sinon detail, sinon title. */
    private String texteErreur(DuffelDtos.Erreur erreur) {
        if (erreur == null) {
            return null;
        }
        if (erreur.message() != null && !erreur.message().isBlank()) {
            return erreur.message();
        }
        if (erreur.detail() != null && !erreur.detail().isBlank()) {
            return erreur.detail();
        }
        return erreur.title();
    }

    /**
     * Erreur interne de Duffel (5xx) : journalisee, renvoyee en 502 avec un message lisible.
     */
    private Exceptions.ErreurDuffel traduireErreurInterne(HttpServerErrorException e) {
        logger.error("Duffel a renvoye le statut {} : {}", e.getStatusCode().value(), e.getMessage(), e);
        return new Exceptions.ErreurDuffel(502, "erreur_duffel",
                "Duffel rencontre une erreur interne : reessayez dans un instant.");
    }

    /**
     * Echec de transport non couvre (autre qu'un delai) : journalise, renvoye en 502.
     */
    private Exceptions.ErreurDuffel traduireEchecTransport(RestClientException e) {
        logger.error("Echec inattendu de l'appel a Duffel : {}", e.getMessage(), e);
        return new Exceptions.ErreurDuffel(502, null,
                "Impossible de contacter Duffel : reessayez dans un instant.");
    }

    private DuffelDtos.CorpsErreur lireCorpsErreur(HttpClientErrorException e) {
        try {
            return objectMapper.readValue(e.getResponseBodyAsString(), DuffelDtos.CorpsErreur.class);
        } catch (Exception ex) {
            return null;
        }
    }
}

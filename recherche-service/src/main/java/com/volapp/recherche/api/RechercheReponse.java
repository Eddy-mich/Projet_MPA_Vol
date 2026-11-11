package com.volapp.recherche.api;

import java.io.Serializable;
import java.util.List;

/**
 * Reponse de recherche : liste des offres au format interne.
 */
public record RechercheReponse(List<Offre> offres) implements Serializable {
}

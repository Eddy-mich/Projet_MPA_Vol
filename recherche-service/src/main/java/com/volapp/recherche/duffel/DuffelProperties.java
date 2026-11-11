package com.volapp.recherche.duffel;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration du client Duffel.
 * Le jeton vient de la variable d'environnement DUFFEL_ACCESS_TOKEN.
 */
@ConfigurationProperties(prefix = "duffel")
public record DuffelProperties(String accessToken, String baseUrl) {

    public DuffelProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.duffel.com";
        }
    }
}

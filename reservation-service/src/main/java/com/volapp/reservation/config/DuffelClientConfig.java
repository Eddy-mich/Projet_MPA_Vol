package com.volapp.reservation.config;

import com.volapp.reservation.duffel.DuffelProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * RestClient Duffel : jeton Bearer et version d'API fixes sur tous les appels.
 */
@Configuration
public class DuffelClientConfig {

    @Bean
    public RestClient duffelRestClient(DuffelProperties properties, RestClient.Builder builder) {
        return builder
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.accessToken())
                .defaultHeader("Duffel-Version", "v2")
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

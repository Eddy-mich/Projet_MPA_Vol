package com.volapp.recherche;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
public class RechercheApplication {

    public static void main(String[] args) {
        SpringApplication.run(RechercheApplication.class, args);
    }
}

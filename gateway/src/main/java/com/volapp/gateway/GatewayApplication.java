package com.volapp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Gateway : unique point d'entree du front-end.
 * /api/recherche/**  -> recherche-service
 * /api/reservations/** -> reservation-service
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

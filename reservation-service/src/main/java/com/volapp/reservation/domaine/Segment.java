package com.volapp.reservation.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

/** Segment de vol (colonnes de la table reservation_segments), heures locales de l'aeroport. */
@Embeddable
public class Segment {

    @Column(nullable = false)
    private int tranche;

    @Column(nullable = false)
    private String origine;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private LocalDateTime depart;

    @Column(nullable = false)
    private LocalDateTime arrivee;

    private String numeroVol;

    private String compagnie;

    protected Segment() {
    }

    public Segment(int tranche, String origine, String destination,
                   LocalDateTime depart, LocalDateTime arrivee,
                   String numeroVol, String compagnie) {
        this.tranche = tranche;
        this.origine = origine;
        this.destination = destination;
        this.depart = depart;
        this.arrivee = arrivee;
        this.numeroVol = numeroVol;
        this.compagnie = compagnie;
    }

    public int getTranche() {
        return tranche;
    }

    public String getOrigine() {
        return origine;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDepart() {
        return depart;
    }

    public LocalDateTime getArrivee() {
        return arrivee;
    }

    public String getNumeroVol() {
        return numeroVol;
    }

    public String getCompagnie() {
        return compagnie;
    }
}

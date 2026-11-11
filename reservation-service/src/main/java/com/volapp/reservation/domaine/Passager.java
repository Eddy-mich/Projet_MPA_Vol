package com.volapp.reservation.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

/** Passager d'une reservation (colones de la table reservation_passagers). */
@Embeddable
public class Passager {

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private LocalDate dateNaissance;

    @Column(nullable = false)
    private String genre;

    protected Passager() {
    }

    public Passager(String titre, String prenom, String nom, LocalDate dateNaissance, String genre) {
        this.titre = titre;
        this.prenom = prenom;
        this.nom = nom;
        this.dateNaissance = dateNaissance;
        this.genre = genre;
    }

    public String getTitre() {
        return titre;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getNom() {
        return nom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public String getGenre() {
        return genre;
    }
}

package com.volapp.reservation.domaine;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Reservation (table "reservations").
 * Les colonnes nullable servent a l'annulation en deux temps.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String referenceReservation;

    /** Identifiant de la commande Duffel (ord_...). */
    @Column(nullable = false)
    private String ordreDuffel;

    @Column(nullable = false)
    private String offreId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReservation statut;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montantTotal;

    @Column(nullable = false)
    private String devise;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false)
    private Instant creeLe;

    /** Identifiant du devis d'annulation Duffel (ore_...), null tant que le devis n'est pas demande. */
    private String annulationId;

    private BigDecimal montantRembourse;

    private String deviseRembourse;

    /** Expiration du devis d'annulation. */
    private Instant devisExpireLe;

    /** Instant de la confirmation d'annulation. */
    private Instant annuleeLe;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "reservation_passagers")
    private List<Passager> passagers = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "reservation_segments")
    @OrderBy("tranche ASC, depart ASC")
    private List<Segment> segments = new ArrayList<>();

    protected Reservation() {
    }

    public Reservation(String referenceReservation, String ordreDuffel, String offreId,
                       StatutReservation statut, BigDecimal montantTotal, String devise,
                       String email, String telephone, Instant creeLe,
                       List<Passager> passagers, List<Segment> segments) {
        this.id = UUID.randomUUID();
        this.referenceReservation = referenceReservation;
        this.ordreDuffel = ordreDuffel;
        this.offreId = offreId;
        this.statut = statut;
        this.montantTotal = montantTotal;
        this.devise = devise;
        this.email = email;
        this.telephone = telephone;
        this.creeLe = creeLe;
        this.passagers = new ArrayList<>(passagers);
        this.segments = new ArrayList<>(segments);
    }

    public UUID getId() {
        return id;
    }

    public String getReferenceReservation() {
        return referenceReservation;
    }

    public String getOrdreDuffel() {
        return ordreDuffel;
    }

    public String getOffreId() {
        return offreId;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public String getDevise() {
        return devise;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public String getAnnulationId() {
        return annulationId;
    }

    public void setAnnulationId(String annulationId) {
        this.annulationId = annulationId;
    }

    public BigDecimal getMontantRembourse() {
        return montantRembourse;
    }

    public void setMontantRembourse(BigDecimal montantRembourse) {
        this.montantRembourse = montantRembourse;
    }

    public String getDeviseRembourse() {
        return deviseRembourse;
    }

    public void setDeviseRembourse(String deviseRembourse) {
        this.deviseRembourse = deviseRembourse;
    }

    public Instant getDevisExpireLe() {
        return devisExpireLe;
    }

    public void setDevisExpireLe(Instant devisExpireLe) {
        this.devisExpireLe = devisExpireLe;
    }

    public Instant getAnnuleeLe() {
        return annuleeLe;
    }

    public void setAnnuleeLe(Instant annuleeLe) {
        this.annuleeLe = annuleeLe;
    }

    public List<Passager> getPassagers() {
        return passagers;
    }

    public List<Segment> getSegments() {
        return segments;
    }
}

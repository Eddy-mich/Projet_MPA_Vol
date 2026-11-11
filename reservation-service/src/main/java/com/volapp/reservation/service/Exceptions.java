package com.volapp.reservation.service;

import java.math.BigDecimal;

/**
 * Exceptions metier de reservation, traduites en ProblemDetail (RFC 7807) par GestionErreurs.
 */
public final class Exceptions {

    private Exceptions() {
    }

    /** Erreur renvoyee par l'API Duffel, avec le statut HTTP a renvoyer au client. */
    public static class ErreurDuffel extends RuntimeException {

        private final int statutHttp;
        private final String code;

        public ErreurDuffel(int statutHttp, String code, String message) {
            super(message);
            this.statutHttp = statutHttp;
            this.code = code;
        }

        public int statutHttp() {
            return statutHttp;
        }

        public String code() {
            return code;
        }
    }

    /** L'offre n'existe plus chez Duffel (404). */
    public static class OffreIntrouvableeException extends RuntimeException {
        public OffreIntrouvableeException(String message) {
            super(message);
        }
    }

    /** L'offre est expiree (410). */
    public static class OffreExpireeException extends RuntimeException {
        public OffreExpireeException(String message) {
            super(message);
        }
    }

    /** Le prix de l'offre a change depuis la recherche (409). */
    public static class PrixModifieException extends RuntimeException {

        private final BigDecimal montantActuel;
        private final String devise;

        public PrixModifieException(BigDecimal montantActuel, String devise) {
            super("Le prix de l'offre a change : il est maintenant de "
                    + montantActuel + " " + devise + ". Relancez votre recherche.");
            this.montantActuel = montantActuel;
            this.devise = devise;
        }

        public BigDecimal montantActuel() {
            return montantActuel;
        }

        public String devise() {
            return devise;
        }
    }

    /** La reservation est introuvable (404). */
    public static class ReservationIntrouvableeException extends RuntimeException {
        public ReservationIntrouvableeException(String message) {
            super(message);
        }
    }

    /** La reservation est deja annulee (409). */
    public static class ReservationDejaAnnuleeException extends RuntimeException {
        public ReservationDejaAnnuleeException(String message) {
            super(message);
        }
    }

    /** Aucun devis d'annulation n'a ete demande (409). */
    public static class DevisAnnulationAbsentException extends RuntimeException {
        public DevisAnnulationAbsentException(String message) {
            super(message);
        }
    }

    /** Le devis ne correspond pas a cette reservation (409). */
    public static class DevisAnnulationInvalideeException extends RuntimeException {
        public DevisAnnulationInvalideeException(String message) {
            super(message);
        }
    }

    /** Le devis d'annulation a expire (410). */
    public static class DevisAnnulationExpireeException extends RuntimeException {
        public DevisAnnulationExpireeException(String message) {
            super(message);
        }
    }

    /** La commande Duffel existe mais n'a pas pu etre enregistree en base (500). */
    public static class EchecEnregistrementException extends RuntimeException {

        private final String ordreDuffel;

        public EchecEnregistrementException(String ordreDuffel) {
            super("La commande a ete creee chez Duffel mais n'a pas pu etre enregistree "
                    + "(commande " + ordreDuffel + "). Contactez le support.");
            this.ordreDuffel = ordreDuffel;
        }

        public String ordreDuffel() {
            return ordreDuffel;
        }
    }
}

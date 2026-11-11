package com.volapp.recherche.service;

/**
 * Exceptions metier de recherche, traduites en ProblemDetail (RFC 7807) par GestionErreurs.
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
}

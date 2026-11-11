<script setup>
import { ref } from "vue";
import RechercheForm from "./components/RechercheForm.vue";
import ListeOffres from "./components/ListeOffres.vue";
import ReservationForm from "./components/ReservationForm.vue";
import Confirmation from "./components/Confirmation.vue";
import MesReservations from "./components/MesReservations.vue";
import { rechercherVols } from "./api.js";

// Navigation par état (convention : pas de routeur).
const etat = ref("recherche");
const offres = ref([]);
const offreChoisie = ref(null);
const reservationCreee = ref(null);
const chargement = ref(false);
const erreur = ref("");

async function lancerRecherche(requete) {
  etat.value = "resultats";
  chargement.value = true;
  erreur.value = "";
  try {
    const reponse = await rechercherVols(requete);
    offres.value = reponse.offres || [];
  } catch (e) {
    erreur.value = e.message;
    offres.value = [];
  } finally {
    chargement.value = false;
  }
}

function choisirOffre(offre) {
  offreChoisie.value = offre;
  erreur.value = "";
  etat.value = "reservation";
}

function reservationTerminee(reservation) {
  reservationCreee.value = reservation;
  etat.value = "confirmation";
}

function allerRecherche() {
  erreur.value = "";
  etat.value = "recherche";
}

function allerReservations() {
  erreur.value = "";
  etat.value = "reservations";
}
</script>

<template>
  <header class="entete">
    <h1>
      <span class="logo-marque" aria-hidden="true">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true">
          <path
            d="M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"
          />
        </svg>
      </span>
      VolApp
    </h1>
    <nav>
      <button
        :class="{ actif: etat !== 'reservations' }"
        @click="allerRecherche"
      >
        Recherche
      </button>
      <button
        :class="{ actif: etat === 'reservations' }"
        @click="allerReservations"
      >
        Mes réservations
      </button>
    </nav>
  </header>

  <template v-if="etat === 'recherche'">
    <section class="hero">
      <div class="hero-contenu">
        <p class="hero-kicker">Vols au départ de tous les aéroports</p>
        <h2>Où souhaitez-vous partir&nbsp;?</h2>
        <p class="hero-sous-titre">
          Trouvez votre vol, comparez les horaires et réservez en quelques clics.
        </p>
      </div>
      <svg class="hero-decor" viewBox="0 0 520 220" fill="none" aria-hidden="true">
        <path
          d="M10 200 C 150 40, 370 40, 510 20"
          stroke="rgba(255,255,255,0.4)"
          stroke-width="2"
          stroke-dasharray="6 8"
          stroke-linecap="round"
        />
        <g transform="translate(486, 8) rotate(28)">
          <path
            d="M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"
            fill="#7cc4f5"
          />
        </g>
      </svg>
    </section>

    <main class="contenu">
      <div class="carte-recherche">
        <RechercheForm @lancer="lancerRecherche" />
      </div>

      <section class="atouts" aria-label="Nos engagements">
        <div class="atout">
          <span class="atout-icone" aria-hidden="true">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 1v22M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
            </svg>
          </span>
          <div>
            <h3>Tarifs transparents</h3>
            <p>Le prix affiché est le prix payé, taxes comprises.</p>
          </div>
        </div>
        <div class="atout">
          <span class="atout-icone" aria-hidden="true">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 12a9 9 0 1 0 3-6.7L3 8" />
              <path d="M3 3v5h5" />
            </svg>
          </span>
          <div>
            <h3>Annulation en ligne</h3>
            <p>Demandez un remboursement et suivez le statut en un clic.</p>
          </div>
        </div>
        <div class="atout">
          <span class="atout-icone" aria-hidden="true">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
          </span>
          <div>
            <h3>Mode test sécurisé</h3>
            <p>Paiement simulé, aucun débit réel n'est effectué.</p>
          </div>
        </div>
      </section>
    </main>
  </template>

  <main v-else-if="etat === 'resultats'" class="contenu">
    <ListeOffres
      :offres="offres"
      :chargement="chargement"
      :erreur="erreur"
      @choisir="choisirOffre"
      @nouvelle="allerRecherche"
    />
  </main>

  <main v-else-if="etat === 'reservation'" class="contenu">
    <ReservationForm
      :offre="offreChoisie"
      @confirmer="reservationTerminee"
      @retour="allerRecherche"
    />
  </main>

  <main v-else-if="etat === 'confirmation'" class="contenu">
    <Confirmation
      :reservation="reservationCreee"
      @reservations="allerReservations"
      @recherche="allerRecherche"
    />
  </main>

  <main v-else-if="etat === 'reservations'" class="contenu">
    <MesReservations />
  </main>

  <footer class="pied">
    <span><strong>VolApp</strong> — projet MPa · vols en mode test Duffel (compagnie fictive ZZ)</span>
    <span>Aucun paiement réel n'est effectué.</span>
  </footer>
</template>

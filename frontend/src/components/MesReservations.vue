<script setup>
import { onMounted, ref } from "vue";
import { confirmerAnnulation, demanderAnnulation, listerReservations } from "../api.js";
import { formaterHeure, formaterJourHeure, formaterInstant, formaterPrix } from "../format.js";

const reservations = ref([]);
const chargement = ref(true);
const erreur = ref("");
const operationEnCours = ref("");
const devisEnCours = ref(null);
const erreurAnnulation = ref("");

function titrePassager(passager) {
  const prenom = passager.prenom.charAt(0).toUpperCase() + passager.prenom.slice(1);
  return `${prenom} ${passager.nom.toUpperCase()}`;
}

onMounted(async () => {
  try {
    reservations.value = await listerReservations();
  } catch (e) {
    erreur.value = e.message;
  } finally {
    chargement.value = false;
  }
});

async function demander(reservation) {
  operationEnCours.value = reservation.id;
  erreurAnnulation.value = "";
  try {
    devisEnCours.value = await demanderAnnulation(reservation.id);
  } catch (e) {
    erreurAnnulation.value = e.message;
    devisEnCours.value = null;
  } finally {
    operationEnCours.value = "";
  }
}

async function annulerLeDevis() {
  if (!devisEnCours.value) {
    return;
  }
  operationEnCours.value = devisEnCours.value.reservationId;
  try {
    devisEnCours.value = null;
  } finally {
    operationEnCours.value = "";
  }
}

async function confirmer() {
  if (!devisEnCours.value) {
    return;
  }
  const reservationId = devisEnCours.value.reservationId;
  operationEnCours.value = reservationId;
  try {
    await confirmerAnnulation(devisEnCours.value.devisId);
    devisEnCours.value = null;
    reservations.value = await listerReservations();
  } catch (e) {
    erreurAnnulation.value = e.message;
  } finally {
    operationEnCours.value = "";
  }
}
</script>

<template>
  <div>
    <div class="page-tete">
      <h2>Mes réservations</h2>
      <p v-if="!chargement && !erreur" class="compteur">
        {{ reservations.length }} réservation{{ reservations.length > 1 ? "s" : "" }}
      </p>
    </div>

    <div v-if="chargement" class="chargement">
      <span class="spinner" aria-hidden="true"></span>
      Chargement de vos réservations…
    </div>

    <p v-else-if="erreur" class="bandeau erreur" role="alert">{{ erreur }}</p>

    <p v-else-if="!reservations.length" class="vide">
      Vous n'avez encore aucune réservation.
    </p>

    <template v-else>
      <article v-for="reservation in reservations" :key="reservation.id" class="carte">
        <div class="reservation">
          <div class="reservation-tete">
            <span class="reference">{{ reservation.referenceReservation }}</span>
            <span class="badge" :class="reservation.statut === 'CONFIRME' ? 'confirme' : 'annule'">
              {{ reservation.statut === 'CONFIRME' ? "Confirmée" : "Annulée" }}
            </span>
          </div>
          <p class="infos">
            Réservée le {{ formaterInstant(reservation.creeLe) }} ·
            {{ formaterPrix(reservation.montantTotal, reservation.devise) }} ·
            {{ reservation.passagers.map(titrePassager).join(", ") }}
          </p>
        </div>

        <div>
          <div v-for="(segment, index) in reservation.segments" :key="index" class="segment">
            <div class="segment-borne depart">
              <span class="heure">{{ formaterHeure(segment.depart) }}</span>
              <span class="code">{{ segment.origine }}</span>
            </div>
            <div class="segment-corps">
              <div class="segment-haut">
                <span>Vol {{ segment.numeroVol }} ({{ segment.compagnie }})</span>
                <span v-if="segment.tranche === 1" class="tranche-libelle">Retour</span>
              </div>
              <div class="segment-trace">
                <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                  <path
                    d="M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"
                  />
                </svg>
              </div>
              <div class="segment-bas">
                <span>{{ formaterJourHeure(segment.depart) }}</span>
              </div>
            </div>
            <div class="segment-borne arrivee">
              <span class="heure">{{ formaterHeure(segment.arrivee) }}</span>
              <span class="code">{{ segment.destination }}</span>
            </div>
          </div>
        </div>

        <div v-if="reservation.statut === 'CONFIRME'" class="reservation-actions">
          <button class="danger" :disabled="operationEnCours === reservation.id" @click="demander(reservation)">
            {{ operationEnCours === reservation.id ? "Demande en cours…" : "Demander une annulation" }}
          </button>
        </div>
        <div v-else class="infos annulation-infos">
          Annulée le {{ formaterInstant(reservation.annulation.annuleeLe) }} ·
          Remboursé : {{ formaterPrix(reservation.annulation.montantRembourse, reservation.annulation.devise) }}
        </div>

        <div v-if="devisEnCours && devisEnCours.reservationId === reservation.id" class="bandeau avertissement devis">
          <p>
            Devis d'annulation : un montant de
            <strong>{{ formaterPrix(devisEnCours.montantRembourse, devisEnCours.devise) }}</strong>
            sera remboursé.
            <br />
            Ce devis est valable jusqu'au {{ formaterInstant(devisEnCours.expireLe) }}.
          </p>
          <div class="devis-actions">
            <button class="primaire" :disabled="operationEnCours === reservation.id" @click="confirmer">
              Confirmer l'annulation
            </button>
            <button class="secondaire" :disabled="operationEnCours === reservation.id" @click="annulerLeDevis">
              Ne pas annuler
            </button>
          </div>
        </div>

        <p v-if="erreurAnnulation" class="bandeau erreur" role="alert">{{ erreurAnnulation }}</p>
      </article>
    </template>
  </div>
</template>

<style scoped>
.compteur {
  margin: 0;
  color: var(--encre-attenuée);
  font-size: 0.95rem;
}

.reservation-tete {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 0.3rem;
}

.reservation .infos {
  margin: 0 0 0.9rem;
}

.tranche-libelle {
  padding: 0.1rem 0.55rem;
  border-radius: 999px;
  background: rgba(14, 134, 212, 0.1);
  color: var(--balise-fonce);
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.reservation-actions {
  margin-top: 0.9rem;
  padding-top: 0.9rem;
  border-top: 1px solid var(--liseré);
}

.annulation-infos {
  margin: 0.9rem 0 0;
  padding-top: 0.9rem;
  border-top: 1px solid var(--liseré);
}

.devis {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.devis p {
  margin: 0;
}

.devis-actions {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}
</style>

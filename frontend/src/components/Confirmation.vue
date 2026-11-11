<script setup>
import { formaterPrix } from "../format.js";

const props = defineProps({
  reservation: { type: Object, required: true },
});

const emettre = defineEmits(["reservations", "recherche"]);
</script>

<template>
  <section class="confirmation">
    <div class="confirmation-banniere">
      <span class="confirmation-badge" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20 6 9 17l-5-5" />
        </svg>
      </span>
      <h2>Réservation confirmée !</h2>
      <p class="reference-ligne">
        Référence de réservation
        <span class="reference">{{ reservation.referenceReservation }}</span>
      </p>
    </div>

    <div class="carte">
      <p class="texte">
        Un e-mail de confirmation sera envoyé à <strong>{{ reservation.email }}</strong>.
      </p>
      <p class="texte">
        Montant total :
        <strong class="montant">{{ formaterPrix(reservation.montantTotal, reservation.devise) }}</strong>
        <small>(paiement simulé, aucun débit réel)</small>
      </p>
      <div class="actions">
        <button class="primaire" @click="emettre('reservations')">Voir mes réservations</button>
        <button class="secondaire" @click="emettre('recherche')">Nouvelle recherche</button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.confirmation {
  max-width: 640px;
  margin: 0 auto;
  width: 100%;
}

.confirmation-banniere {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 2.5rem 1.5rem 2rem;
  margin-bottom: 1rem;
  text-align: center;
  color: #fff;
  border-radius: 24px 24px 0 0;
  background:
    radial-gradient(420px 180px at 80% -30%, rgba(124, 196, 245, 0.35), transparent 60%),
    linear-gradient(150deg, var(--marine) 0%, var(--marine-claire) 60%, #1d5488 100%);
  box-shadow: var(--ombre-carte);
}

.confirmation-banniere h2 {
  margin: 0;
  color: #fff;
  font-size: 1.7rem;
}

.confirmation-badge {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  margin-bottom: 0.5rem;
  border-radius: 50%;
  background: rgba(31, 157, 97, 0.25);
  border: 2px solid rgba(255, 255, 255, 0.35);
  color: #7ce3ae;
}

.confirmation-badge svg {
  width: 30px;
  height: 30px;
}

.reference-ligne {
  margin: 0;
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.85);
}

.confirmation .reference {
  display: inline-block;
  margin-top: 0.15rem;
  padding: 0.35rem 0.9rem;
  color: var(--marine);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  box-shadow: inset 0 0 0 2px var(--liseré);
}

.texte {
  margin: 0.5rem 0;
  color: var(--encre);
}

.texte small {
  display: block;
  margin-top: 0.2rem;
  color: var(--encre-attenuée);
  font-size: 0.8rem;
}

.montant {
  font-family: var(--police-titre);
  font-size: 1.15rem;
  color: var(--marine);
}

.actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1.25rem;
  flex-wrap: wrap;
}
</style>

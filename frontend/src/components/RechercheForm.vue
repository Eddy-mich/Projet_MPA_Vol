<script setup>
import { reactive } from "vue";

// Émet l'événement « lancer » avec le formulaire validé (convention du projet).
const emit = defineEmits(["lancer"]);

const formulaire = reactive({
  origine: "CDG",
  destination: "LYS",
  depart: "",
  retour: "",
  voyageurs: 1,
  cabine: "economy",
});

function soumettre() {
  const requete = {
    origine: formulaire.origine.toUpperCase(),
    destination: formulaire.destination.toUpperCase(),
    dateDepart: formulaire.depart,
    adultes: Number(formulaire.voyageurs),
    cabine: formulaire.cabine,
  };
  if (formulaire.retour) {
    requete.dateRetour = formulaire.retour;
  }
  emit("lancer", requete);
}
</script>

<template>
  <section class="recherche-form">
    <h2>Rechercher un vol</h2>
    <form class="grille" @submit.prevent="soumettre">
      <label class="champ">
        <span>Origine (code IATA)</span>
        <input
          v-model.trim="formulaire.origine"
          maxlength="3"
          placeholder="CDG"
          required
          class="code-iata"
        />
      </label>
      <label class="champ">
        <span>Destination (code IATA)</span>
        <input
          v-model.trim="formulaire.destination"
          maxlength="3"
          placeholder="LYS"
          required
          class="code-iata"
        />
      </label>
      <label class="champ">
        <span>Départ</span>
        <input v-model="formulaire.depart" type="date" required />
      </label>
      <label class="champ">
        <span>Retour (facultatif)</span>
        <input v-model="formulaire.retour" type="date" />
      </label>
      <label class="champ">
        <span>Voyageurs</span>
        <select v-model.number="formulaire.voyageurs">
          <option v-for="n in 9" :key="n" :value="n">{{ n }}</option>
        </select>
      </label>
      <label class="champ">
        <span>Cabine</span>
        <select v-model="formulaire.cabine">
          <option value="economy">Économie</option>
          <option value="business">Affaires</option>
        </select>
      </label>
      <div class="grille-actions">
        <button class="primaire" type="submit">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <circle cx="11" cy="11" r="7" />
            <path d="m21 21-4.3-4.3" />
          </svg>
          Rechercher
        </button>
      </div>
    </form>
  </section>
</template>

<style scoped>
.recherche-form h2 {
  margin-bottom: 1.1rem;
}

.recherche-form .grille {
  grid-template-columns: repeat(4, 1fr);
}

.grille-actions {
  grid-column: span 2;
  display: flex;
  align-items: flex-end;
}

.grille-actions button {
  width: 100%;
}

@media (max-width: 900px) {
  .recherche-form .grille {
    grid-template-columns: repeat(2, 1fr);
  }

  .grille-actions {
    grid-column: 1 / -1;
  }
}

@media (max-width: 540px) {
  .recherche-form .grille {
    grid-template-columns: 1fr;
  }

  .grille-actions {
    grid-column: 1 / -1;
  }
}
</style>

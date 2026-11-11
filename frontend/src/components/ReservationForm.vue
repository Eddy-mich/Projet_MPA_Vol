<script setup>
import { onMounted, reactive, ref } from "vue";
import { lireOffre, reserver } from "../api.js";
import { formaterHeure, formaterJourHeure, formaterInstant, formaterPrix } from "../format.js";

const props = defineProps({
  offre: { type: Object, required: true },
});

const emettre = defineEmits(["confirmer", "retour"]);

const chargement = ref(true);
const erreur = ref("");
const avertissement = ref("");
const envoiEnCours = ref(false);

// Offre relue chez Duffel : prix a jour et identifiants des passagers (pas_...).
const offreFraiche = ref(null);

const voyageur = reactive({
  email: "",
  telephone: "",
});

const passagers = ref([]);

function creerPassager(id) {
  return {
    id,
    titre: "mr",
    prenom: "",
    nom: "",
    dateNaissance: "",
    genre: "m",
    telephone: "",
  };
}

onMounted(async () => {
  try {
    const relue = await lireOffre(props.offre.id);
    offreFraiche.value = relue;
    passagers.value = relue.passagers.map((p) => creerPassager(p.id));
    if (Number(relue.montantTotal) !== Number(props.offre.montantTotal)) {
      avertissement.value =
        `Le prix de l'offre a changé depuis la recherche : il est maintenant de ` +
        `${formaterPrix(relue.montantTotal, relue.devise)} au lieu de ` +
        `${formaterPrix(props.offre.montantTotal, props.offre.devise)}.`;
    }
  } catch (e) {
    erreur.value = e.message;
  } finally {
    chargement.value = false;
  }
});

function valide() {
  if (!passagers.value.every((p) => p.prenom.trim() && p.nom.trim() && p.dateNaissance)) {
    erreur.value = "Veuillez compléter les informations de tous les passagers.";
    return false;
  }
  if (!voyageur.email.trim() || !voyageur.telephone.trim()) {
    erreur.value = "Veuillez indiquer votre e-mail et votre téléphone.";
    return false;
  }
  erreur.value = "";
  return true;
}

async function confirmer() {
  if (!valide()) {
    return;
  }
  envoiEnCours.value = true;
  erreur.value = "";
  try {
    const corps = {
      offreId: props.offre.id,
      montantAttendu: offreFraiche.value.montantTotal,
      email: voyageur.email.trim(),
      telephone: voyageur.telephone.trim(),
      passagers: passagers.value.map((p) => ({
        titre: p.titre,
        prenom: p.prenom.trim(),
        nom: p.nom.trim(),
        dateNaissance: p.dateNaissance,
        genre: p.genre,
        telephone: p.telephone.trim() || null,
      })),
    };
    const reservation = await reserver(corps);
    emettre("confirmer", reservation);
  } catch (e) {
    erreur.value = e.message;
  } finally {
    envoiEnCours.value = false;
  }
}
</script>

<template>
  <div>
    <div v-if="chargement" class="chargement">
      <span class="spinner" aria-hidden="true"></span>
      Vérification du prix de l'offre…
    </div>

    <template v-else-if="erreur">
      <p class="bandeau erreur" role="alert">{{ erreur }}</p>
      <button class="secondaire" @click="emettre('retour')">← Reprendre la recherche</button>
    </template>

    <template v-else-if="offreFraiche">
      <p v-if="avertissement" class="bandeau avertissement" role="alert">
        {{ avertissement }}
      </p>

      <div class="devis">
        <form class="devis-formulaire" @submit.prevent="confirmer">
          <section class="carte">
            <h2>Vos coordonnées</h2>
            <div class="grille deux">
              <label>
                <span>E-mail</span>
                <input v-model.trim="voyageur.email" type="email" placeholder="jean.dupont@exemple.fr" required />
              </label>
              <label>
                <span>Téléphone</span>
                <input v-model.trim="voyageur.telephone" type="tel" placeholder="+33 6 01 02 03 04" required />
              </label>
            </div>
          </section>

          <section class="carte">
            <h2>Vos passagers</h2>
            <div class="passagers">
              <div v-for="passager in passagers" :key="passager.id" class="passager-form">
                <label>
                  <span>Titre</span>
                  <select v-model="passager.titre">
                    <option value="mr">Monsieur</option>
                    <option value="mme">Madame</option>
                    <option value="ms">Mademoiselle</option>
                  </select>
                </label>
                <label>
                  <span>Genre</span>
                  <select v-model="passager.genre">
                    <option value="m">Masculin</option>
                    <option value="f">Féminin</option>
                  </select>
                </label>
                <label>
                  <span>Prénom</span>
                  <input v-model.trim="passager.prenom" required />
                </label>
                <label>
                  <span>Nom</span>
                  <input v-model.trim="passager.nom" required />
                </label>
                <label>
                  <span>Date de naissance</span>
                  <input v-model="passager.dateNaissance" type="date" required />
                </label>
                <label>
                  <span>Téléphone (optionnel)</span>
                  <input v-model.trim="passager.telephone" type="tel" />
                </label>
              </div>
            </div>
          </section>

          <p v-if="erreur" class="bandeau erreur" role="alert">{{ erreur }}</p>

          <div class="devis-actions">
            <button class="primaire" type="submit" :disabled="envoiEnCours">
              {{ envoiEnCours ? "Réservation en cours…" : "Confirmer la réservation" }}
            </button>
            <button type="button" class="secondaire" @click="emettre('retour')">← Annuler</button>
          </div>
        </form>

        <aside class="carte resume">
          <h2>Votre offre</h2>
          <div v-for="(segment, index) in offreFraiche.segments" :key="index" class="segment">
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
          <div class="resume-total">
            <span class="prix">
              {{ formaterPrix(offreFraiche.montantTotal, offreFraiche.devise) }}
              <small>
                {{ passagers.length }} passager{{ passagers.length > 1 ? "s" : "" }} · offre valable jusqu'au {{ formaterInstant(offreFraiche.expireLe) }}
              </small>
            </span>
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>

<style scoped>
.devis {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(300px, 1fr);
  gap: 1rem;
  align-items: start;
}

.resume {
  position: sticky;
  top: 84px;
}

.passager-form {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.9rem 1rem;
  padding: 1.25rem;
  background: #fff;
  border: 1px solid var(--liseré);
  border-radius: var(--rayon);
  box-shadow: var(--ombre-carte);
}

.devis-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
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

.resume-total {
  padding-top: 0.9rem;
  margin-top: 0.5rem;
  border-top: 1px solid var(--liseré);
}

@media (max-width: 900px) {
  .devis {
    grid-template-columns: 1fr;
  }

  .resume {
    position: static;
    order: -1;
  }
}

@media (max-width: 540px) {
  .passager-form {
    grid-template-columns: 1fr;
  }
}
</style>

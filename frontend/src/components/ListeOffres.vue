<script setup>
import { formaterHeure, formaterJourHeure, formaterInstant, formaterDuree, formaterPrix } from "../format.js";

const props = defineProps({
  offres: { type: Array, required: true },
  chargement: { type: Boolean, default: false },
  erreur: { type: String, default: "" },
});

const emettre = defineEmits(["choisir", "nouvelle"]);

function libelleTranchePour(offre, index) {
  if (offre.tranches.length === 1) {
    return null;
  }
  return index === 0 ? "Aller" : "Retour";
}

function escales(segment) {
  if (segment.nombreEscales === 0) {
    return "direct";
  }
  return `${segment.nombreEscales} escale${segment.nombreEscales > 1 ? "s" : ""}`;
}
</script>

<template>
  <div>
    <div v-if="chargement" class="chargement">
      <span class="spinner" aria-hidden="true"></span>
      Recherche des vols en cours…
    </div>

    <template v-else>
      <p v-if="erreur" class="bandeau erreur" role="alert">
        {{ erreur }}
      </p>

      <p v-else-if="!offres.length" class="vide">
        Aucun vol trouvé pour cette recherche. Essayez d'autres dates ou des codes IATA différents.
      </p>

      <template v-else>
        <div class="page-tete">
          <h2>
            {{ offres.length }} vol{{ offres.length > 1 ? "s" : "" }} disponible{{ offres.length > 1 ? "s" : "" }}
          </h2>
          <button class="secondaire" @click="emettre('nouvelle')">← Nouvelle recherche</button>
        </div>

        <article v-for="offre in offres" :key="offre.id" class="carte">
          <div class="offre">
            <div class="offre-tete">
              <span class="offre-avatar" aria-hidden="true">{{ offre.codeCompagnie.charAt(0) }}</span>
              <div class="offre-identite">
                <strong>{{ offre.compagnie }}</strong>
                <span class="marque">{{ offre.codeCompagnie }} · {{ offre.marqueFare }}</span>
                <small class="expiration">
                  offre valable jusqu'au {{ formaterInstant(offre.expireLe) }}
                </small>
              </div>
              <span class="prix">
                {{ formaterPrix(offre.montantTotal, offre.devise) }}
                <small>{{ offre.passagers.length }} passager{{ offre.passagers.length > 1 ? "s" : "" }}</small>
              </span>
            </div>

            <div>
              <div
                v-for="(tranche, index) in offre.tranches"
                :key="index"
                class="tranche"
              >
                <span v-if="libelleTranchePour(offre, index)" class="tranche-libelle">
                  {{ libelleTranchePour(offre, index) }}
                </span>
                <div
                  v-for="(segment, numero) in tranche.segments"
                  :key="numero"
                  class="segment"
                >
                  <div class="segment-borne depart">
                    <span class="heure">{{ formaterHeure(segment.depart) }}</span>
                    <span class="code">{{ segment.origine }}</span>
                  </div>
                  <div class="segment-corps">
                    <div class="segment-haut">
                      <span>Vol {{ segment.numeroVol }} ({{ segment.compagnie }})</span>
                      <span
                        class="segment-escales"
                        :class="{ direct: segment.nombreEscales === 0 }"
                      >
                        {{ escales(segment) }}
                      </span>
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
                      <span>·</span>
                      <span>{{ formaterDuree(segment.duree) }}</span>
                      <template v-if="segment.terminalDepart">
                        <span>·</span>
                        <span>terminal {{ segment.terminalDepart }}</span>
                      </template>
                      <template v-if="segment.terminalArrivee">
                        <span>·</span>
                        <span>terminal d'arrivée {{ segment.terminalArrivee }}</span>
                      </template>
                    </div>
                  </div>
                  <div class="segment-borne arrivee">
                    <span class="heure">{{ formaterHeure(segment.arrivee) }}</span>
                    <span class="code">{{ segment.destination }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="offre-bas">
              <span class="offre-note">
                Prix total pour {{ offre.passagers.length }} passager{{ offre.passagers.length > 1 ? "s" : "" }}
              </span>
              <button class="primaire" @click="emettre('choisir', offre)">
                Choisir cette offre
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <path d="M5 12h14M13 6l6 6-6 6" />
                </svg>
              </button>
            </div>
          </div>
        </article>
      </template>
    </template>
  </div>
</template>

<style scoped>
.offre {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.offre-tete {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 1rem;
}

.offre-identite {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.offre-identite strong {
  font-family: var(--police-titre);
  font-size: 1.05rem;
}

.marque {
  color: var(--encre-attenuée);
  font-size: 0.88rem;
}

.expiration {
  color: var(--encre-attenuée);
  font-size: 0.8rem;
}

.tranche {
  margin-bottom: 0.5rem;
}

.tranche-libelle {
  display: inline-block;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--balise-fonce);
  margin-bottom: 0.25rem;
}

.offre-bas {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  padding-top: 0.75rem;
  border-top: 1px solid var(--liseré);
}

.offre-note {
  font-size: 0.85rem;
  color: var(--encre-attenuée);
}

@media (max-width: 600px) {
  .offre-tete {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .offre-tete .prix {
    grid-column: 1 / -1;
  }
}
</style>

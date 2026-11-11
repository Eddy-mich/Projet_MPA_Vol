/*
 * Toutes les URL de l'API, definies une seule fois (convention du projet).
 * Le front n'appelle que la gateway : /api/...
 */
const BASE = "/api";

export class ErreurApi extends Error {
  constructor(statut, detail) {
    super(detail);
    this.statut = statut;
  }
}

export async function rechercherVols({ origine, destination, dateDepart, dateRetour, adultes, cabine }) {
  const corps = {
    origine: origine.trim().toUpperCase(),
    destination: destination.trim().toUpperCase(),
    dateDepart,
    adultes,
  };
  if (dateRetour) {
    corps.dateRetour = dateRetour;
  }
  if (cabine) {
    corps.cabine = cabine;
  }
  return requete("POST", `${BASE}/recherche/vols`, corps);
}

export function lireOffre(offreId) {
  return requete("GET", `${BASE}/reservations/offres/${offreId}`);
}

export function reserver(corps) {
  return requete("POST", `${BASE}/reservations`, corps);
}

export function listerReservations() {
  return requete("GET", `${BASE}/reservations`);
}

export function lireReservation(id) {
  return requete("GET", `${BASE}/reservations/${id}`);
}

export function demanderAnnulation(id) {
  return requete("POST", `${BASE}/reservations/${id}/annulation`);
}

export function confirmerAnnulation(id, annulationId) {
  return requete("POST", `${BASE}/reservations/${id}/annulation/${annulationId}/confirmer`);
}

async function requete(methode, url, corps) {
  let reponse;
  try {
    reponse = await fetch(url, {
      method: methode,
      headers: corps !== undefined ? { "Content-Type": "application/json" } : undefined,
      body: corps !== undefined ? JSON.stringify(corps) : undefined,
    });
  } catch {
    throw new ErreurApi(0, "Impossible de joindre le serveur : le gateway est-il lance ?");
  }
  if (!reponse.ok) {
    const detail = await lireDetailErreur(reponse);
    throw new ErreurApi(reponse.status, detail);
  }
  if (reponse.status === 204) {
    return null;
  }
  return reponse.json();
}

async function lireDetailErreur(reponse) {
  try {
    const probleme = await reponse.json();
    return probleme.detail || probleme.title || "Erreur inattendue du serveur.";
  } catch {
    return "Erreur inattendue du serveur.";
  }
}

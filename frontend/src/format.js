/*
 * Formatage des dates, heures, durees et prix (convention du projet).
 */

export function formaterPrix(montant, devise) {
  if (montant === null || montant === undefined) {
    return "—";
  }
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: devise || "EUR",
  }).format(montant);
}

export function formaterDate(iso) {
  if (!iso) {
    return "—";
  }
  const [annee, mois, jour] = iso.slice(0, 10).split("-");
  return new Date(Number(annee), Number(mois) - 1, Number(jour)).toLocaleDateString("fr-FR", {
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

export function formaterHeure(iso) {
  if (!iso) {
    return "—";
  }
  return iso.slice(11, 16);
}

export function formaterJourHeure(iso) {
  if (!iso) {
    return "—";
  }
  return `${formaterDate(iso)} · ${formaterHeure(iso)}`;
}

export function formaterInstant(iso) {
  if (!iso) {
    return "—";
  }
  return new Date(iso).toLocaleString("fr-FR", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

/** Duree ISO-8601 (PT2H6M) -> "2 h 06" ou "35 min". */
export function formaterDuree(dureeIso) {
  if (!dureeIso) {
    return "—";
  }
  const correspondance = dureeIso.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
  if (!correspondance) {
    return dureeIso;
  }
  const heures = Number(correspondance[1] || 0);
  const minutes = Number(correspondance[2] || 0);
  if (heures === 0) {
    return `${minutes} min`;
  }
  return `${heures} h ${String(minutes).padStart(2, "0")}`;
}

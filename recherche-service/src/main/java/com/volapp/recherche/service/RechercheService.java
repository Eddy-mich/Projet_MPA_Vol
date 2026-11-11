package com.volapp.recherche.service;

import com.volapp.recherche.api.Offre;
import com.volapp.recherche.api.RechercheRequete;
import com.volapp.recherche.duffel.DuffelClient;
import com.volapp.recherche.duffel.DuffelDtos;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * Recherche des offres de vols chez Duffel, avec cache Redis de 5 minutes.
 * L'appel Duffel n'a jamais lieu a l'interieur d'une transaction de base.
 */
@Service
public class RechercheService {

    private final DuffelClient duffelClient;
    private final OffreMapper offreMapper;

    public RechercheService(DuffelClient duffelClient, OffreMapper offreMapper) {
        this.duffelClient = duffelClient;
        this.offreMapper = offreMapper;
    }

    @Cacheable(value = "recherches", key = "#requete")
    public List<Offre> rechercherOffres(RechercheRequete requete) {
        DuffelDtos.DonneesRecherche donnees =
                duffelClient.chercherOffres(construireRequete(requete));
        return offreMapper.mapper(donnees);
    }

    private DuffelDtos.RequeteRecherche construireRequete(RechercheRequete requete) {
        String origine = requete.origine().toUpperCase(Locale.ROOT);
        String destination = requete.destination().toUpperCase(Locale.ROOT);

        List<DuffelDtos.SliceRequete> slices = new ArrayList<>();
        slices.add(new DuffelDtos.SliceRequete(origine, destination, requete.dateDepart().toString()));
        if (requete.dateRetour() != null) {
            slices.add(new DuffelDtos.SliceRequete(destination, origine, requete.dateRetour().toString()));
        }

        List<DuffelDtos.PassagerRequete> passagers = IntStream
                .rangeClosed(1, requete.adultes())
                .mapToObj(index -> new DuffelDtos.PassagerRequete("adult"))
                .toList();

        return new DuffelDtos.RequeteRecherche(
                new DuffelDtos.DataRecherche(slices, passagers, requete.cabineOuDefaut()));
    }
}

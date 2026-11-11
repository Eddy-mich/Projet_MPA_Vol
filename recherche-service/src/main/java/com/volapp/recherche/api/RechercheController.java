package com.volapp.recherche.api;

import com.volapp.recherche.service.RechercheService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Point d'entree REST du service recherche : POST /recherche/vols.
 * Le gateway expose cette route sous /api/recherche/vols.
 */
@RestController
@RequestMapping("/recherche")
public class RechercheController {

    private final RechercheService rechercheService;

    public RechercheController(RechercheService rechercheService) {
        this.rechercheService = rechercheService;
    }

    @PostMapping("/vols")
    public RechercheReponse rechercher(@Valid @RequestBody RechercheRequete requete) {
        return new RechercheReponse(rechercheService.rechercherOffres(requete));
    }
}

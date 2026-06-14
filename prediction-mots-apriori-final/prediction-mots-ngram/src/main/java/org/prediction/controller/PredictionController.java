package org.prediction.controller;

import org.prediction.service.PredictionService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller — expose les endpoints de prédiction.
 *
 * GET /api/predict?texte=...   → top-5 mots suggérés
 * GET /api/regles?limit=20     → afficher les règles générées (debug)
 * GET /api/stats               → statistiques du modèle
 */
@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    /**
     * Pré-charger les règles au démarrage de l'application
     * pour que la première requête soit instantanée.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initialiserAuDemarrage() {
        System.out.println("[Controller] Initialisation du modèle Apriori...");
        predictionService.initialiser();
        System.out.println("[Controller] Modèle prêt. " 
            + predictionService.nombreRegles() + " règles disponibles.");
    }

    /**
     * Endpoint principal : prédire les mots suivants.
     *
     * Exemple : GET /api/predict?texte=je+ne
     * Réponse : ["sais","peux","veux","comprends","supporte"]
     */
    @GetMapping("/predict")
    public List<String> predire(@RequestParam String texte) {
        return predictionService.predire(texte);
    }

    /**
     * Endpoint debug : voir les règles générées.
     *
     * Exemple : GET /api/regles?limit=30
     */
    @GetMapping("/regles")
    public List<String> voirRegles(@RequestParam(defaultValue = "30") int limit) {
        return predictionService.afficherRegles(limit);
    }

    /**
     * Statistiques du modèle.
     *
     * GET /api/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of(
            "nombreRegles", predictionService.nombreRegles(),
            "statut", "actif",
            "algorithme", "Apriori séquentiel (bigrammes + trigrammes)",
            "minSupport", 3,
            "minConfiance", "10%"
        );
    }
}

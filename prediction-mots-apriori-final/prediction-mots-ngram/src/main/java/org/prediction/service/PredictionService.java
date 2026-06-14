package org.prediction.service;

import org.prediction.model.RegleAssociation;
import org.prediction.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Prédit le(s) mot(s) suivant(s) à partir du texte saisi par l'utilisateur.
 *
 * Stratégie :
 *  1. Tokeniser l'input utilisateur
 *  2. Chercher les règles dont l'antécédent correspond aux DERNIERS mots tapés
 *     - D'abord les règles trigramme : [w_{n-1}, w_n] => w_{n+1}  (plus précis)
 *     - Ensuite les règles bigramme  : [w_n] => w_{n+1}           (fallback)
 *  3. Score = confiance × min(lift, 5)  pour éviter que le lift explose
 *  4. Retourner les top-5 mots suggérés (sans doublons, sans mots déjà tapés)
 */
@Service
public class PredictionService {

    private final AprioriService  aprioriService;
    private final DatasetService  datasetService;

    // Résultat chargé une seule fois
    private List<RegleAssociation> regles = null;

    public PredictionService(AprioriService aprioriService,
                             DatasetService datasetService) {
        this.aprioriService = aprioriService;
        this.datasetService = datasetService;
    }

    /** Initialise les règles (appelé au démarrage via le controller) */
    public void initialiser() {
        if (regles == null) {
            List<Transaction> transactions = datasetService.chargerTransactions();
            regles = aprioriService.genererRegles(transactions);
        }
    }

    /**
     * Retourne les top-5 mots suggérés pour le texte donné.
     *
     * @param texte  ce que l'utilisateur a tapé
     * @return liste de mots suggérés triés par score décroissant
     */
    public List<String> predire(String texte) {
        if (regles == null) initialiser();

        // ── Nettoyer et tokeniser l'input ──────────────────────────────────
        String nettoye = texte.trim().toLowerCase()
                              .replace('\u2019', '\'')
                              .replace('\u2018', '\'')
                              .replaceAll("[^a-zA-ZÀ-ÿ' ]", " ")
                              .replaceAll("\\s+", " ")
                              .trim();

        if (nettoye.isEmpty()) return Collections.emptyList();

        String[] tokens = nettoye.split("\\s+");
        List<String> mots = new ArrayList<>();
        for (String t : tokens) {
            if (t.length() >= 2) mots.add(t);
        }
        if (mots.isEmpty()) return Collections.emptyList();

        // Derniers 1 et 2 mots pour matcher les règles
        String dernierMot   = mots.get(mots.size() - 1);
        String avantDernier = mots.size() >= 2 ? mots.get(mots.size() - 2) : null;

        // Mots déjà tapés → ne pas les re-suggérer
        Set<String> dejaTypes = new HashSet<>(mots);

        // ── Calculer les scores ────────────────────────────────────────────
        // score[mot_suggere] = meilleur score parmi toutes les règles qui matchent
        Map<String, Double> scores = new HashMap<>();

        for (RegleAssociation regle : regles) {
            List<String> ant = regle.getAntecedent();
            boolean match = false;

            if (ant.size() == 2 && avantDernier != null) {
                // Règle trigramme : [avant-dernier, dernier] => suivant
                match = ant.get(0).equals(avantDernier) && ant.get(1).equals(dernierMot);
            } else if (ant.size() == 1) {
                // Règle bigramme : [dernier] => suivant
                match = ant.get(0).equals(dernierMot);
            }

            if (!match) continue;

            String suggestion = regle.getConsequent();

            // Ne pas suggérer un mot déjà tapé
            if (dejaTypes.contains(suggestion)) continue;

            // Score = confiance × lift plafonné à 5
            double score = regle.getConfiance() * Math.min(regle.getLift(), 5.0);

            // Bonus x1.5 pour les règles trigramme (plus précises)
            if (ant.size() == 2) score *= 1.5;

            scores.merge(suggestion, score, Math::max);
        }

        // ── Fallback : si aucune règle trigramme/bigramme ne matche ────────
        // On cherche les règles dont l'antécédent contient au moins un mot tapé
        if (scores.isEmpty() && !mots.isEmpty()) {
            for (RegleAssociation regle : regles) {
                if (regle.getAntecedent().contains(dernierMot)) {
                    String suggestion = regle.getConsequent();
                    if (dejaTypes.contains(suggestion)) continue;
                    double score = regle.getConfiance() * 0.5; // poids réduit
                    scores.merge(suggestion, score, Math::max);
                }
            }
        }

        // ── Retourner top-5 ────────────────────────────────────────────────
        return scores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /** Pour le debug : retourne les N premières règles */
    public List<String> afficherRegles(int limit) {
        if (regles == null) initialiser();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, regles.size()); i++) {
            result.add(regles.get(i).toString());
        }
        return result;
    }

    public int nombreRegles() {
        if (regles == null) initialiser();
        return regles.size();
    }
}

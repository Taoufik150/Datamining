package org.prediction.service;

import org.prediction.model.RegleAssociation;
import org.prediction.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ============================================================
 *  ALGORITHME APRIORI SÉQUENTIEL — codé 100% en Java pur
 * ============================================================
 *
 * Principe adapté à la prédiction de mots :
 *
 *  On travaille avec des N-GRAMMES ORDONNÉS extraits des phrases.
 *  Un N-gramme = séquence de N mots consécutifs.
 *
 *  Exemple de phrase : "je ne sais pas quoi dire"
 *    Bigrammes  : [je,ne]  [ne,sais]  [sais,pas]  [pas,quoi]  [quoi,dire]
 *    Trigrammes : [je,ne,sais]  [ne,sais,pas]  ...
 *
 *  Étapes Apriori :
 *   1. Compter tous les unigrammes → garder ceux >= minSupport  (L1)
 *   2. Générer les bigrammes candidats à partir de L1
 *      → les compter → filtrer >= minSupport                    (L2)
 *   3. Générer les trigrammes à partir de L2
 *      → les compter → filtrer >= minSupport                    (L3)
 *   4. A partir de L2 et L3, générer des règles :
 *        antecedent (1 ou 2 mots) => consequent (1 mot suivant)
 *      → garder si confiance >= minConfiance
 *
 *  On cherche UNIQUEMENT des règles de la forme :
 *    [w1]        => w2   (bigramme : w1 est suivi de w2)
 *    [w1, w2]    => w3   (trigramme : w1 w2 est suivi de w3)
 *
 *  Ce qui correspond exactement à la prédiction du mot suivant.
 */
@Service
public class AprioriService {

    // ── Seuils ─────────────────────────────────────────────────────────────
    // minSupport en nombre absolu de transactions (pas en %)
    // Avec 10 000 phrases, support=3 = 0.03% → on garde les séquences
    // qui apparaissent au moins 3 fois.
    private static final int    MIN_SUPPORT_COUNT = 3;
    private static final double MIN_CONFIANCE     = 0.10; // 10%

    // Cache des règles (calculé une seule fois au démarrage)
    private List<RegleAssociation> reglesCache = null;

    // ── Point d'entrée ──────────────────────────────────────────────────────
    public List<RegleAssociation> genererRegles(List<Transaction> transactions) {
        if (reglesCache != null) return reglesCache;

        long debut = System.currentTimeMillis();
        System.out.println("[Apriori] Démarrage avec " + transactions.size() + " transactions...");

        int N = transactions.size();

        // ── ÉTAPE 1 : compter tous les unigrammes ───────────────────────────
        Map<String, Integer> comptesUni = new HashMap<>();
        for (Transaction t : transactions) {
            // On compte chaque mot une seule fois par transaction (règle Apriori)
            Set<String> vus = new HashSet<>(t.getMots());
            for (String mot : vus) {
                comptesUni.merge(mot, 1, Integer::sum);
            }
        }

        // L1 : unigrammes fréquents
        Set<String> L1 = new HashSet<>();
        for (Map.Entry<String, Integer> e : comptesUni.entrySet()) {
            if (e.getValue() >= MIN_SUPPORT_COUNT) {
                L1.add(e.getKey());
            }
        }
        System.out.println("[Apriori] L1 (unigrammes fréquents) : " + L1.size());

        // ── ÉTAPE 2 : compter les bigrammes ORDONNÉS ────────────────────────
        // Un bigramme [w1, w2] est compté si w1 apparaît AVANT w2 dans
        // la même transaction ET tous les deux sont dans L1.
        Map<String, Integer> comptesBi = new HashMap<>();
        for (Transaction t : transactions) {
            List<String> mots = t.getMots();
            Set<String> pairesVues = new HashSet<>();
            for (int i = 0; i < mots.size() - 1; i++) {
                String w1 = mots.get(i);
                String w2 = mots.get(i + 1);
                if (!L1.contains(w1) || !L1.contains(w2)) continue;
                String cle = w1 + "|||" + w2;
                if (pairesVues.add(cle)) {
                    comptesBi.merge(cle, 1, Integer::sum);
                }
            }
        }

        // L2 : bigrammes fréquents
        Map<String, Integer> L2 = new HashMap<>();
        for (Map.Entry<String, Integer> e : comptesBi.entrySet()) {
            if (e.getValue() >= MIN_SUPPORT_COUNT) {
                L2.put(e.getKey(), e.getValue());
            }
        }
        System.out.println("[Apriori] L2 (bigrammes fréquents)  : " + L2.size());

        // ── ÉTAPE 3 : compter les trigrammes ORDONNÉS ───────────────────────
        // Un trigramme [w1,w2,w3] : w1 w2 consécutifs ET w2 w3 consécutifs.
        // On exige que [w1,w2] et [w2,w3] soient tous deux dans L2.
        Map<String, Integer> comptesTri = new HashMap<>();
        for (Transaction t : transactions) {
            List<String> mots = t.getMots();
            Set<String> triosVus = new HashSet<>();
            for (int i = 0; i < mots.size() - 2; i++) {
                String w1 = mots.get(i);
                String w2 = mots.get(i + 1);
                String w3 = mots.get(i + 2);
                // Pruning Apriori : les deux sous-bigrammes doivent être fréquents
                if (!L2.containsKey(w1 + "|||" + w2)) continue;
                if (!L2.containsKey(w2 + "|||" + w3)) continue;
                String cle = w1 + "|||" + w2 + "|||" + w3;
                if (triosVus.add(cle)) {
                    comptesTri.merge(cle, 1, Integer::sum);
                }
            }
        }

        // L3 : trigrammes fréquents
        Map<String, Integer> L3 = new HashMap<>();
        for (Map.Entry<String, Integer> e : comptesTri.entrySet()) {
            if (e.getValue() >= MIN_SUPPORT_COUNT) {
                L3.put(e.getKey(), e.getValue());
            }
        }
        System.out.println("[Apriori] L3 (trigrammes fréquents) : " + L3.size());

        // ── ÉTAPE 4 : générer les règles d'association ──────────────────────
        List<RegleAssociation> regles = new ArrayList<>();

        // Règles depuis L2 : [w1] => w2
        for (Map.Entry<String, Integer> e : L2.entrySet()) {
            String[] parts = e.getKey().split("\\|\\|\\|");
            String w1 = parts[0];
            String w2 = parts[1];

            int countBi  = e.getValue();
            int countAnt = comptesUni.getOrDefault(w1, 0);
            int countCons = comptesUni.getOrDefault(w2, 0);

            if (countAnt == 0 || countCons == 0) continue;

            double support   = (double) countBi / N;
            double confiance = (double) countBi / countAnt;
            double lift      = confiance / ((double) countCons / N);

            if (confiance >= MIN_CONFIANCE) {
                regles.add(new RegleAssociation(
                    List.of(w1), w2, support, confiance, lift
                ));
            }
        }

        // Règles depuis L3 : [w1, w2] => w3  (priorité plus haute)
        for (Map.Entry<String, Integer> e : L3.entrySet()) {
            String[] parts = e.getKey().split("\\|\\|\\|");
            String w1 = parts[0];
            String w2 = parts[1];
            String w3 = parts[2];

            int countTri  = e.getValue();
            int countAnt  = L2.getOrDefault(w1 + "|||" + w2, 0);
            int countCons = comptesUni.getOrDefault(w3, 0);

            if (countAnt == 0 || countCons == 0) continue;

            double support   = (double) countTri / N;
            double confiance = (double) countTri / countAnt;
            double lift      = confiance / ((double) countCons / N);

            if (confiance >= MIN_CONFIANCE) {
                regles.add(new RegleAssociation(
                    List.of(w1, w2), w3, support, confiance, lift
                ));
            }
        }

        // Trier par confiance × lift décroissant
        regles.sort((a, b) ->
            Double.compare(b.getConfiance() * b.getLift(),
                           a.getConfiance() * a.getLift()));

        long fin = System.currentTimeMillis();
        System.out.println("[Apriori] " + regles.size() + " règles générées en "
                           + (fin - debut) + " ms.");

        reglesCache = regles;
        return regles;
    }
}

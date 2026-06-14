package org.prediction.service;

import org.prediction.model.Transaction;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Charge le dataset.txt et le transforme en liste de Transaction.
 *
 * Chaque ligne = une phrase.
 * On tokenise en mots, on filtre les mots trop courts.
 * On NE supprime PAS les stopwords — en Apriori séquentiel sur du
 * français naturel, "je ne sais" est un itemset utile.
 */
@Service
public class DatasetService {

    private List<Transaction> cache = null;

    public List<Transaction> chargerTransactions() {
        if (cache != null) return cache;

        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    Objects.requireNonNull(
                        getClass().getResourceAsStream("/dataset.txt")),
                    StandardCharsets.UTF_8))) {

            String ligne;
            while ((ligne = reader.readLine()) != null) {

                ligne = ligne.trim().toLowerCase();
                if (ligne.isEmpty()) continue;

                // Découper sur les espaces (dataset déjà nettoyé)
                String[] tokens = ligne.split("\\s+");

                List<String> mots = new ArrayList<>();
                for (String token : tokens) {
                    // Garder uniquement les mots d'au moins 2 caractères
                    if (token.length() >= 2) {
                        mots.add(token);
                    }
                }

                // Une transaction doit avoir au moins 2 mots
                if (mots.size() >= 2) {
                    transactions.add(new Transaction(mots));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger dataset.txt : " + e.getMessage(), e);
        }

        System.out.println("[DatasetService] " + transactions.size() + " transactions chargées.");
        cache = transactions;
        return transactions;
    }
}

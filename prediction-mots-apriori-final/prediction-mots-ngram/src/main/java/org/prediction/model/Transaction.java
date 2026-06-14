package org.prediction.model;

import java.util.List;

/**
 * Représente une transaction = une phrase découpée en mots ordonnés.
 * On garde l'ordre des mots pour l'Apriori séquentiel.
 */
public class Transaction {

    private final List<String> mots;

    public Transaction(List<String> mots) {
        this.mots = mots;
    }

    public List<String> getMots() {
        return mots;
    }

    public int taille() {
        return mots.size();
    }

    @Override
    public String toString() {
        return mots.toString();
    }
}

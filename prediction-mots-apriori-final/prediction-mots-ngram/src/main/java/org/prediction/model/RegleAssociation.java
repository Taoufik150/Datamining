package org.prediction.model;

import java.util.List;

/**
 * Une règle d'association séquentielle :
 *   antecedent => consequent
 *
 * Exemple : ["je", "ne"] => ["sais"]
 *
 * Métriques :
 *   - support    : fréquence de l'itemset complet dans toutes les transactions
 *   - confiance  : P(consequent | antecedent) = freq(antecedent+consequent) / freq(antecedent)
 *   - lift       : confiance / freq(consequent)  — mesure la vraie pertinence
 */
public class RegleAssociation {

    private final List<String> antecedent;
    private final String       consequent;
    private final double       support;
    private final double       confiance;
    private final double       lift;

    public RegleAssociation(List<String> antecedent,
                            String       consequent,
                            double       support,
                            double       confiance,
                            double       lift) {
        this.antecedent = antecedent;
        this.consequent = consequent;
        this.support    = support;
        this.confiance  = confiance;
        this.lift       = lift;
    }

    public List<String> getAntecedent() { return antecedent; }
    public String       getConsequent() { return consequent; }
    public double       getSupport()    { return support;    }
    public double       getConfiance()  { return confiance;  }
    public double       getLift()       { return lift;       }

    @Override
    public String toString() {
        return antecedent + " => " + consequent
             + "  [sup=" + String.format("%.4f", support)
             + "  conf=" + String.format("%.4f", confiance)
             + "  lift=" + String.format("%.2f",  lift) + "]";
    }
}

package org.prediction;

import org.junit.jupiter.api.Test;
import org.prediction.model.Transaction;
import org.prediction.service.AprioriService;
import org.prediction.model.RegleAssociation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AprioriServiceTest {

    @Test
    void testReglesGenerees() {
        AprioriService service = new AprioriService();

        List<Transaction> transactions = List.of(
            new Transaction(List.of("je", "ne", "sais", "pas")),
            new Transaction(List.of("je", "ne", "peux", "pas")),
            new Transaction(List.of("je", "ne", "veux", "pas")),
            new Transaction(List.of("je", "ne", "sais", "rien")),
            new Transaction(List.of("je", "ne", "comprends", "pas")),
            new Transaction(List.of("je", "ne", "sais", "pas", "quoi", "dire")),
            new Transaction(List.of("tu", "ne", "sais", "pas")),
            new Transaction(List.of("tu", "ne", "peux", "pas")),
            new Transaction(List.of("il", "ne", "sait", "pas"))
        );

        List<RegleAssociation> regles = service.genererRegles(transactions);

        System.out.println("=== Règles générées ===");
        regles.forEach(System.out::println);

        assertFalse(regles.isEmpty(), "Des règles doivent être générées");

        // Vérifier qu'une règle [je] => ne existe
        boolean regleJeNe = regles.stream().anyMatch(r ->
            r.getAntecedent().equals(List.of("je")) &&
            r.getConsequent().equals("ne")
        );
        assertTrue(regleJeNe, "La règle [je] => ne doit exister");
    }

    @Test
    void testConfianceValide() {
        AprioriService service = new AprioriService();

        List<Transaction> transactions = List.of(
            new Transaction(List.of("je", "ne", "sais")),
            new Transaction(List.of("je", "ne", "peux")),
            new Transaction(List.of("je", "ne", "veux")),
            new Transaction(List.of("je", "fais")),
            new Transaction(List.of("je", "suis"))
        );

        List<RegleAssociation> regles = service.genererRegles(transactions);

        for (RegleAssociation r : regles) {
            assertTrue(r.getConfiance() >= 0.0 && r.getConfiance() <= 1.0,
                "Confiance doit être entre 0 et 1 : " + r);
            assertTrue(r.getSupport() >= 0.0 && r.getSupport() <= 1.0,
                "Support doit être entre 0 et 1 : " + r);
            assertTrue(r.getLift() >= 0.0,
                "Lift doit être positif : " + r);
        }
    }
}

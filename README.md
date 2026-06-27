# Prédiction de Mots — Algorithme Apriori

Ce projet est une application web de prédiction de mots en français, basée sur l'algorithme d'exploration de données **Apriori**. Il utilise des règles d'association générées à partir de n-grammes (bigrammes et trigrammes) pour suggérer les mots suivants au fur et à mesure que l'utilisateur tape une phrase.

Ce travail s'inscrit dans le cadre d'un projet de Data Mining (Fouille de données). Un document explicatif détaillé (`memoire.pdf`) est également inclus à la racine du projet.

## 🛠 Technologies Utilisées

* **Backend :** Java 17, Spring Boot 3.2.0
* **Frontend :** HTML5, CSS3, JavaScript (Vanilla)
* **Algorithme :** Apriori (Génération de règles d'association basées sur la confiance)
* **Outil de build :** Maven

## 📂 Structure du Projet

```text
.
├── memoire.pdf                     # Mémoire / Rapport détaillé du projet
└── prediction-mots-apriori-final/  
    └── prediction-mots-ngram/      # Code source de l'application web
        ├── pom.xml                 # Fichier de configuration Maven
        └── src/
            ├── main/java/          # Code source backend (API REST et Algorithmique)
            │   └── org/prediction/ # Logique de l'algorithme Apriori, traitement des datasets et prédictions
            └── main/resources/
                └── static/         # Code source frontend (index.html avec UI design vintage)
```

## 🚀 Installation et Démarrage

### Prérequis
* Java Development Kit (JDK) 17 ou supérieur
* Maven (facultatif si vous utilisez un IDE avec Maven intégré)

### Étapes pour exécuter l'application
1. Ouvrez un terminal et naviguez vers le dossier de l'application :
   ```bash
   cd "prediction-mots-apriori-final/prediction-mots-ngram"
   ```
2. Compilez et démarrez l'application avec Maven :
   ```bash
   mvn spring-boot:run
   ```
   *(Alternativement, lancez directement la classe `PredictionApplication` depuis votre IDE)*

3. Ouvrez votre navigateur web et accédez à l'adresse suivante :
   [http://localhost:8080](http://localhost:8080)

## 💡 Fonctionnalités

* **Prédiction en temps réel :** L'application suggère des mots au fur et à mesure de la saisie au clavier.
* **Architecture Client-Serveur :** Backend performant en Spring Boot exposant une API REST, consommé par une interface web réactive via des appels `fetch`.
* **Interface Utilisateur (UI) soignée :** Design thématique vintage élégant, conçu pour offrir une excellente expérience utilisateur avec des raccourcis rapides.
* **Métriques :** Affiche en temps réel le nombre de règles d'association générées par l'algorithme Apriori et utilisées par le modèle.

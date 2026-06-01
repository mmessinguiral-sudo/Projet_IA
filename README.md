# 🐾 Projet IA / Java / THS - Classification Chien vs Chat

Ce dépôt contient le code source de la chaîne de traitement intelligente développée dans le cadre du projet ISEN[cite: 1]. L'objectif est d'implémenter, d'entraîner et d'évaluer un neurone artificiel capable de classifier des images de chiens et de chats[cite: 1].

---

## 🛠️ Architecture du Projet

Le projet s'appuie obligatoirement sur les briques logicielles fournies par l'équipe enseignante[cite: 1] :

*   `iNeurone.java` : Interface imposée définissant le contrat du neurone (méthodes `metAJour`, `sortie`, `apprentissage`)[cite: 2].
*   `Neurone.java` : Classe abstraite implémentant la structure globale du neurone, la gestion des synapses, du biais, ainsi que l'algorithme d'apprentissage par descente de gradient (MSE)[cite: 3].
*   `NeuroneHeavyside.java` : Spécialisation du neurone utilisant une fonction d'activation binaire par seuil (Heaviside)[cite: 4].
*   `testNeurone.java` : Programme de test initial permettant de valider l'apprentissage sur des fonctions logiques (ET / OU)[cite: 5].
*   `Image.java` : Classe utilitaire gérant la lecture des fichiers Jpeg/Png, leur labellisation automatique, et leur mise à plat sous forme de tableau de pixels[cite: 1, 6].

---

## 👥 Répartition de l'Équipe & Tâches en Parallèle

Pour optimiser le développement au sein de notre groupe de 5, les tâches sont parallélisées afin de maximiser l'efficacité collective sans créer de goulots d'étranglement[cite: 1].

### 📅 Étape 1 : Fondations & Tests Unitaires (Niveau 1)
*Objectif : Valider les briques logicielles et étendre les capacités du neurone[cite: 1].*

| Tâche | Assignation | Description | Dépendances |
| :--- | :--- | :--- | :--- |
| **1.1 Analyse de l'apprentissage** | Chef de Projet | Étudier et commenter l'algorithme d'apprentissage fourni dans `Neurone.java`[cite: 1, 3]. | Aucune |
| **1.2 Implémentation Sigmoïde** | Développeur IA 1 | Créer `NeuroneSigmoide.java` en redéfinissant la méthode `activation()` avec l'équation sigmoïde[cite: 1, 3]. | Aucune |
| **1.3 Implémentation ReLU** | Développeur IA 2 | Créer `NeuroneReLU.java` en redéfinissant la méthode `activation()` avec l'équation ReLU[cite: 1, 3]. | Aucune |
| **1.4 Parsing global des Images** | Développeurs Data 1 & 2 | Utiliser la méthode `listeFichiers()` de `Image.java` pour scanner récursivement les répertoires[cite: 6]. | Aucune |

### 📅 Étape 2 : Pipeline de Données (Niveau 2)
*Objectif : Transformer les images brutes en tenseurs assimilables par le neurone[cite: 1].*

| Tâche | Assignation | Description | Dépendances |
| :--- | :--- | :--- | :--- |
| **2.1 Labellisation Dynamique** | Développeur Data 1 | Détecter automatiquement si le fichier est un chat (`0`) ou un chien (`1`) en analysant son chemin[cite: 1, 6]. | 1.4 |
| **2.2 Algorithme de Mélange** | Développeur Data 2 | Coder le brassage aléatoire (`shuffle`) des données pour éviter le biais d'apprentissage[cite: 1]. | 2.1 |
| **2.3 Module de Normalisation** | Développeurs Data 1 & 2 | Convertir les valeurs de pixels `int` [0;255] en `float` [0.0;1.0] pour l'alimentation du réseau[cite: 1, 6]. | 2.2 |

### 📅 Étape 3 : Intégration & Évaluation (Niveau 2)
*Objectif : Assembler la chaîne complète, entraîner le modèle et valider ses performances[cite: 1].*

| Tâche | Assignation | Description | Dépendances |
| :--- | :--- | :--- | :--- |
| **3.1 Script d'Entraînement** | Chef de Projet | Injecter le bloc `float[][]` normalisé dans la méthode `apprentissage()` du neurone sélectionné[cite: 1, 3]. | Étapes 1 & 2 |
| **3.2 Validation (Inférence)** | Développeurs IA 1 & 2 | Charger le dataset `test`, exécuter `metAJour()` et comptabiliser les prédictions correctes[cite: 1, 3]. | 3.1 |
| **3.3 Calcul des Métriques** | Développeur Data 1 | Générer le score de précision final (Taux de réussite global)[cite: 1]. | 3.2 |

### 📅 Étape 4 : Démarche Scientifique & Livrables (Niveau 3)
*Objectif : Éprouver le système et préparer la soutenance[cite: 1].*

| Tâche | Assignation | Description | Dépendances |
| :--- | :--- | :--- | :--- |
| **4.1 Tests de Robustesse** | Tout le groupe | Évaluer l'impact de la suppression du mélange, de la normalisation, ou de l'injection de bruit blanc[cite: 1]. | Étape 3 |
| **4.2 Rapport Technique** | Chef de Projet | Rédiger le document PDF final synthétisant la démarche, les protocoles et les résultats chiffrés[cite: 1]. | 4.1 |
| **4.3 Support de Soutenance** | Tout le groupe | Concevoir la présentation PDF (7 minutes max) mettant en avant 2 difficultés majeures résolues[cite: 1]. | 4.2 |

---

## 🚫 Contraintes Strictes de Rendu

Pénalité de 100% appliquée sur la note finale en cas de non-respect de ces règles[cite: 1] :
1.  **Aucune bibliothèque Java externe** ne doit être ajoutée au livrable[cite: 1].
2.  **Ne pas inclure les datasets d'images** dans l'archive finale (uniquement les fichiers sources `.java`)[cite: 1].
3.  Respect absolu de la date limite de rendu fixée au **05/06 avant 12h00**[cite: 1].

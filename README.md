# 🐾 Projet IA / Java / THS - Classification Multi-Tâches (Chiens, Chats, Sauvages)

Ce dépôt contient le code source de la chaîne de traitement intelligente développée dans le cadre du projet ISEN. L'architecture permet de traiter deux classifications binaires de front (Chien vs Chat et Sauvage vs Non-Sauvage) à l'aide de trois types de neurones artificiels (Heaviside, Sigmoïde, ReLU).

---

## 🛠️ Architecture du Projet

Le projet s'appuie obligatoirement sur les briques logicielles fournies :
* `iNeurone.java` : Interface imposée définissant le contrat du neurone (`metAJour`, `sortie`, `apprentissage`, `sauvegarde`, `chargement`).
* `Neurone.java` : Classe abstraite gérant la structure globale, le biais, les synapses, et l'apprentissage par descente de gradient (MSE).
* `NeuroneHeavyside.java` : Neurone utilisant une fonction d'activation binaire par seuil.
* `Image.java` : Classe utilitaire gérant le chargement des fichiers, l'extraction des pixels et l'encapsulation du label.

---

## 📐 Méthodologie d'Expérimentation (Les 6 Étapes du Professeur)

Le projet progresse de manière rigoureuse à travers six jalons d'apprentissage et d'interconnexion :

1.  **Heaviside - Tâche Domestique :** Entraînement sur la distinction sélective Chien (1) vs Chat (0).
2.  **Heaviside - Tâche Sauvage :** Entraînement sur la distinction sélective Sauvage (1) vs Non-Sauvage (0).
3.  **Transition Sigmoïde :** Répétition des étapes 1 et 2 avec la classe `NeuroneSigmoide.java`.
4.  **Transition ReLU :** Répétition des étapes 1 et 2 avec la classe `NeuroneReLU.java`.
5.  **Mise en Parallèle (1 couche) :** Exécution simultanée des deux meilleurs neurones spécialisés et sauvegardés (un pour Chien/Chat, un pour Sauvage) pour obtenir un double diagnostic sur une image de test.
6.  **Extension (Architecture Hybride Connectée) :** Interconnexion des neurones en cascade (ex: couche d'entrée en ReLU pour filtrer le bruit, connectée à une couche finale en Sigmoïde pour la décision).

---

## ⚡ Verrous Techniques & Solutions Implémentées

### 1. Sécurisation du Mélange (Shuffle)
* **Problématique :** Risque de dissocier le tableau des entrées (images) du tableau des sorties (labels) lors du brassage aléatoire.
* **Solution :** Le mélange (`Collections.shuffle`) s'effectue directement sur une `List<Image>` d'objets complets. Le champ `label` étant encapsulé au sein de la classe `Image`, l'association pixel-label reste totalement indissociable. L'extraction vers les tableaux de primitives `float[][]` requis par le neurone se fait exclusivement après ce brassage.

### 2. Gestion séquentielle de `eta` (Vitesse d'apprentissage)
* **Problématique :** L'attribut `eta` étant partagé globalement (`private static float eta`), modifier sa valeur impacte instantanément tous les neurones en mémoire.
* **Solution :** Les neurones des étapes 1 à 4 sont entraînés de manière strictement **séquentielle** (les uns après les autres). Cela permet d'ajuster dynamiquement le coefficient via `fixeCoefApprentissage(nouvelEta)` pour chaque type de neurone afin de lutter contre le bruitage. Une fois l'apprentissage d'un neurone stabilisé, ses poids synaptiques et son biais sont figés sur le disque dur à l'aide de la méthode `sauvegarde()`.

### 3. Inférence Optimisée sans Réapprentissage
* Pour les étapes 5 et 6 (Mise en parallèle et cascade), le programme principal ne relance aucun apprentissage. Il instancie les structures de neurones vides et restaure instantanément leurs états entraînés grâce à la méthode `chargement()`.

---

## 👥 Répartition de l'Équipe & Tâches en Parallèle

### 📅 Phase A : Fondations & Pipelines (En Parallèle)

| Tâche | Assignation | Description | Dépendances |
| :--- | :--- | :--- | :--- |
| **IA.1 - Coder la Sigmoïde** | Développeur IA 1 | Créer `NeuroneSigmoide.java` en surchargeant la fonction `activation()`. | Aucune |
| **IA.2 - Coder le ReLU** | Développeur IA 2 | Créer `NeuroneReLU.java` en surchargeant la fonction `activation()`. | Aucune |
| **DATA.1 - Chargement sûr** | Développeur Data 1 | Scanner le répertoire, instancier les objets `Image` et sécuriser le `Shuffle` sur la liste globale. | Aucune |
| **DATA.2 - Normalisation** | Développeur Data 2 | Écrire le convertisseur de pixels `int` [0;255] en `float` [0.0;1.0] post-mélange. | Aucune |

### 📅 Phase B : Entraînements séquentiels & Sauvegardes (Étapes 1 à 4)

| Tâche | Assignation | Description | Dépendances |
| :--- | :--- | :--- | :--- |
| **B.1 - Classifs Chien/Chat** | Développeur IA 1 | Entraîner successivement Heaviside, Sigmoïde, ReLU sur les labels domestiques. Exécuter les `sauvegarde()`. | Phase A |
| **B.2 - Classifs Sauvages** | Développeur IA 2 | Entraîner successivement Heaviside, Sigmoïde, ReLU sur les labels sauvages. Exécuter les `sauvegarde()`. | Phase A |
| **B.3 - Suivi Métriques** | Analyste / CP | Enregistrer dans un tableur les courbes d'apprentissage, taux de succès, d'échecs et vitesse de convergence. | Phase A |

### 📅 Phase C : Intégrations Complexes & Livrables (Étapes 5 & 6)

| Tâche | Assignation | Description | Dépendances |
| :--- | :--- | :--- | :--- |
| **C.1 - Parallélisation** | Développeur Data 1 & 2 | Créer le `main` de l'Étape 5 chargeant les deux meilleurs neurones spécialisés en simultané. | Phase B |
| **C.2 - Réseau en cascade** | Développeurs IA 1 & 2 | Connecter les sorties des neurones intermédiaires (ReLU) vers un neurone décisionnel (Sigmoïde). | Phase B |
| **C.3 - Rédaction Scientifique** | Tout le groupe / Chef de Projet | Finaliser le rapport d'expérimentation (PDF) et concevoir le support de soutenance (7 min). | C.1, C.2 |

---

## 📊 Critères d'Évaluation du Rapport Technique

Une attention critique est portée sur l'analyse scientifique des données :
* **Analyse comparative poussée :** Comparaison fine des taux de succès/échecs entre Heaviside, Sigmoïde et ReLU.
* **Journal des essais :** Description transparente de toutes les configurations architecturales testées en Étape 6, incluant les échecs rencontrés (ex: saturation ou divergence) et les solutions trouvées.

---

## 🚫 Contraintes Strictes de Rendu

* **Aucune bibliothèque Java externe** tolérée.
* **Exclusion stricte des datasets d'images** dans le livrable (uniquement les codes sources `.java`).
* Date limite absolue : **05/06 avant 11h59**.

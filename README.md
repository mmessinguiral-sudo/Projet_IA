# 🐾 Projet IA / Java / THS - Classification Multi-Tâches (ISEN)

Ce dépôt contient le code source de la chaîne de traitement intelligente développée dans le cadre du projet ISEN3. L'objectif est de concevoir un réseau de neurones from scratch en Java, capable de classifier des images (Chiens, Chats, Animaux Sauvages) via une architecture parallèle sur une seule couche.

---

## 🛠️ Architecture Logicielle & Briques Fournies

Le projet s'appuie obligatoirement et exclusivement sur les briques logicielles fournies par l'équipe enseignante :
* `iNeurone.java` : Interface définissant le contrat du neurone (`metAJour`, `sortie`, `apprentissage`, `sauvegarde`, `chargement`).
* `Neurone.java` : Classe abstraite gérant la structure mémoire (synapses, biais) et l'algorithme d'apprentissage (Descente de gradient - MSE).
* `NeuroneHeavyside.java` : Spécialisation avec fonction d'activation binaire par seuil.
* `testNeurone.java` : Fichier de validation unitaire (portes logiques ET/OU, tests de robustesse au bruit).
* `Image.java` : Utilitaire de lecture de fichiers, d'extraction des pixels (niveaux de gris) et d'encapsulation du label.

---

## 📐 Méthodologie & Protocole d'Expérimentation (Les 6 Étapes)

Le développement suit une démarche scientifique stricte allant de la validation unitaire à l'architecture finale :

1.  **Niveau 1 - Heaviside (Domestique) :** Entraînement sur la distinction Chien (1) vs Chat (0). Test de robustesse face au bruit.
2.  **Niveau 1 - Heaviside (Sauvage) :** Entraînement sur la distinction Sauvage (1) vs Non-Sauvage (0).
3.  **Niveau 2 - Sigmoïde :** Implémentation de `NeuroneSigmoide.java` et répétition des étapes 1 et 2.
4.  **Niveau 2 - ReLU :** Implémentation de `NeuroneReLU.java` et répétition des étapes 1 et 2.
5.  **Niveau 3 - Mise en Parallèle (Couche Unique) :** Exécution simultanée des meilleurs neurones sauvegardés. Une image est soumise aux différents neurones en parallèle pour obtenir un vecteur de prédictions (ex: `[Chat: 98%, Sauvage: 2%]`).
6.  **Niveau 3 - Extensions & Justifications :** Expérimentation d'architectures alternatives (ex: arbre binaire de décision). **Impératif :** Tout choix de seuil de déclenchement (ex: seuil empirique fixé à 65%) doit être mathématiquement et statistiquement justifié dans le rapport.

---

## 🏷️ Pipeline de Données : Protocole de Labellisation

Pour garantir l'intégrité des données, le mélange (`shuffle`) s'effectue sur une `List<Image>` globale. Les tableaux de primitives `float[][]` ne sont générés qu'après cette étape pour éviter toute dissociation mémoire entre une image et son label.

| Nom du dossier détecté | Label `Image.java` | Cible Neurone 1 (Chien/Chat) | Cible Neurone 2 (Sauvage) |
| :--- | :---: | :---: | :---: |
| `"cat"` | `0` | **`0.0f`** (Inactif) | **`0.0f`** (Inactif) |
| `"dog"` | `1` | **`1.0f`** (Actif) | **`0.0f`** (Inactif) |
| `"wild"` | `2` | **`0.0f`** (Inactif) | **`1.0f`** (Actif) |

---

## 👥 Répartition de l'Équipe & Parallélisation des Tâches

Pour optimiser le temps de développement, le groupe de 5 est divisé en pôles d'expertise.

### 🧠 Pôle "Cœur IA" (Développeurs IA 1 & 2)
*Focus : Mathématiques, pointeurs mémoire, fonctions d'activation et entraînement.*

* **Dev IA 1 :** Implémentation de `NeuroneSigmoide.java`. Chargé des séquences d'entraînement pour la classification **Domestique** (Chien/Chat) avec sauvegarde des poids.
* **Dev IA 2 :** Implémentation de `NeuroneReLU.java`. Chargé des séquences d'entraînement pour la classification **Sauvage** avec sauvegarde des poids. Expérimentations sur l'ajustement du coefficient `eta`.

### 💾 Pôle "Data & Pipeline" (Développeurs Data 1 & 2)
*Focus : Parcours de fichiers, structures de données, I/O et normalisation.*

* **Dev Data 1 :** Automatisation du scan des répertoires (`listeFichiers`), mapping des labels (0, 1, 2) selon le nom des dossiers, et implémentation sécurisée du `Collections.shuffle()`.
* **Dev Data 2 :** Conversion logicielle des données brutes (pixels `int` 0-255) vers des tenseurs normalisés (`float` 0.0-1.0). Gestion de la sérialisation (lecture/écriture des fichiers `.txt` de sauvegarde des neurones).

### 📊 Pôle "Intégration & Scientifique" (Chef de Projet / Analyste)
*Focus : Architecture globale, démarche ingénieur et analyse critique.*

* **Intégration :** Création du `main()` final orchestrant l'architecture en parallèle (Étape 5). Chargement des neurones pré-entraînés sans réapprentissage pour la phase d'inférence (Test).
* **Analyse :** Documentation exhaustive des tentatives architecturales (succès ET échecs). Justification stricte des seuils d'activation choisis (ex: 65%). Centralisation des métriques (Taux d'erreur, MSE, nombre d'itérations).

---

## 🔬 Démarche Scientifique & Livrables (Évaluation)

Le projet est évalué sur la rigueur de la démarche ingénieur :
* **Analyse de la Robustesse (Niveau 1) :** Évaluation de la dégradation des performances face à l'injection d'un bruit de fond contrôlé sur les entrées (Signal sur Bruit).
* **Journal des Échecs :** Le rapport doit inclure ce qui n'a *pas* fonctionné (ex: saturation du gradient avec ReLU, ou seuil de décision inadapté) et les correctifs appliqués.
* **Soutenance (7 min) :** Présentation obligatoire de **deux difficultés techniques majeures** rencontrées par le groupe et de leurs résolutions.

---

## 🚫 Contraintes de Livraison Absolues (Pénalité de 100%)

1.  **Strictement aucune bibliothèque externe** (Imports Java standards uniquement).
2.  **Aucune image / dataset** dans l'archive finale (uniquement les `.java`).
3.  Présentation (PDF) à envoyer par courriel aux 3 moniteurs le **05/06 avant 11h59**.
4.  Code source complet à envoyer par courriel le **05/06 avant 12h00**.

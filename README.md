

# 🐾 Projet IA / Java / THS - Classification Multi-Tâches (Chiens, Chats, Sauvages)

Ce dépôt contient le code source de la chaîne de traitement intelligente développée dans le cadre du projet ISEN3. L'objectif est d'implémenter, d'entraîner et d'évaluer une architecture de neurones artificiels capable de classifier des images selon deux tâches distinctes en parallèle : la classification domestique (Chien vs Chat) et la classification environnementale (Sauvage vs Non-sauvage).

---

## 🛠️ Architecture du Projet

Le projet s'appuie obligatoirement sur les briques logicielles fournies par l'équipe enseignante :

* `iNeurone.java` : Interface imposée définissant le contrat du neurone (méthodes `metAJour`, `sortie`, `apprentissage`, ainsi que les fonctions optionnelles de persistance `sauvegarde` et `chargement`).
* `Neurone.java` : Classe abstraite implémentant la structure globale du neurone, la gestion des synapses, du biais, la variable d'apprentissage globale `static float eta`, ainsi que l'algorithme d'apprentissage par descente de gradient (MSE).
* `NeuroneHeavyside.java` : Spécialisation du neurone utilisant une fonction d'activation binaire par seuil (Heaviside).
* `testNeurone.java` : Programme de test initial permettant de valider l'apprentissage sur des fonctions logiques (ET / OU) et de tester la tolérance au bruit.
* `Image.java` : Classe utilitaire gérant la lecture des fichiers Jpeg/Png, leur labellisation automatique (Labels `0` pour Chat, `1` pour Chien, `2` pour Sauvage), et leur mise à plat sous forme de tableau de pixels.

---

## 🏷️ Protocole de Labellisation & Vecteurs Cibles

Pour entraîner nos différents modèles sans dissocier les images de leurs étiquettes, la labellisation est lue depuis l'arborescence des fichiers, puis traduite en profils de sorties (`float[]`) distincts selon la tâche ciblée :

| Dossier source détecté | Label stocké (`Image.java`) | Cible Tâche 1 (Chien vs Chat) | Cible Tâche 2 (Sauvage Oui/Non) |
| --- | --- | --- | --- |
| **`"cat"`** | `0` | **`0.0f`** (Désactivé) | **`0.0f`** (Désactivé) |
| **`"dog"`** | `1` | **`1.0f`** (Activé) | **`0.0f`** (Désactivé) |
| **`"wild"`** | `2` | **`0.0f`** (Désactivé) | **`1.0f`** (Activé) |

---

## 👥 Répartition de l'Équipe & Tâches en Parallèle

Pour optimiser le développement au sein de notre groupe de 5, les tâches sont parallélisées afin de maximiser l'efficacité collective sans créer de goulots d'étranglement.

### 📅 Étape 1 : Fondations & Tests Unitaires (Niveau 1)

*Objectif : Valider les briques logicielles de base et étendre les types de fonctions d'activation.*

| Tâche | Assignation | Description | Dépendances |
| --- | --- | --- | --- |
| **1.1 Analyse de l'apprentissage** | Chef de Projet | Étudier et commenter l'algorithme d'apprentissage fourni dans `Neurone.java` (Descente de gradient et calcul de l'erreur quadratique moyenne - MSE). | Aucune |
| **1.2 Implémentation Sigmoïde** | Développeur IA 1 | Créer `NeuroneSigmoide.java` en redéfinissant la méthode `activation()` avec l'équation de la fonction logistique sigmoïde. | Aucune |
| **1.3 Implémentation ReLU** | Développeur IA 2 | Créer `NeuroneReLU.java` en redéfinissant la méthode `activation()` avec l'équation de l'unité de rectification linéaire (ReLU). | Aucune |
| **1.4 Parsing global des Images** | Développeurs Data 1 & 2 | Utiliser la méthode `listeFichiers()` de `Image.java` pour scanner récursivement les répertoires du dataset. | Aucune |

### 📅 Étape 2 : Pipeline de Données Sûr & Labellisation (Niveau 2)

*Objectif : Transformer les images brutes en tenseurs assimilables par le réseau sans perte de liaison.*

| Tâche | Assignation | Description | Dépendances |
| --- | --- | --- | --- |
| **2.1 Labellisation Dynamique** | Développeur Data 1 | Détecter automatiquement si le fichier est un chat (`0`), un chien (`1`) ou un animal sauvage (`2`) en analysant de manière robuste son chemin d'accès. | 1.4 |
| **2.2 Algorithme de Mélange** | Développeur Data 2 | Coder le brassage aléatoire (`shuffle`) directement sur la `List<Image>` globale. **Sécurité :** l'image et son label restant encapsulés dans l'objet, cela empêche de dissocier les labels lors du mélange. | 2.1 |
| **2.3 Module de Normalisation** | Développeurs Data 1 & 2 | Convertir les valeurs de pixels `int` [0;255] en `float` [0.0;1.0] après l'étape de mélange pour générer les matrices finales `float[][] entrees`. | 2.2 |

### 📅 Étape 3 : Entraînements Séquentiels & Architecture Parallèle (Niveau 2 & 3)

*Objectif : Gérer la contrainte du `eta` statique, entraîner les modèles, sauvegarder leurs états et concevoir le réseau multi-neurones.*

| Tâche | Assignation | Description | Dépendances |
| --- | --- | --- | --- |
| **3.1 Script d'Entraînement Séparé** | Chef de Projet | Orchestrer les entraînements de manière **strictement séquentielle**. Comme `eta` est `static`, appeler `fixeCoefApprentissage()` pour ajuster la vitesse d'apprentissage de chaque neurone l'un après l'autre afin de lutter efficacement contre le bruit. | Étapes 1 & 2 |
| **3.2 Module de Sauvegarde & Persistance** | Développeur Data 2 | Appliquer la fonction `sauvegarde()` à la fin de chaque entraînement pour figer les poids synaptiques et les biais sur le disque (évite de refaire réapprendre les neurones à chaque exécution). | 3.1 |
| **3.3 Intégration Parallèle (Étape 5 & 6)** | Développeurs IA 1 & 2 | Créer le programme d'évaluation qui instancie les neurones, appelle `chargement()` pour restaurer leurs poids, puis les positionne en **parallèle (1 seule couche)** pour traiter une image simultanément. En extension, concevoir l'architecture hybride interconnectée (cascade). | 3.2 |
| **3.4 Calcul des Métriques & Succès** | Développeur Data 1 | Soumettre le dataset de test, exécuter `metAJour()` et comptabiliser précisément les taux de succès (Accuracy) et d'échecs globaux pour chaque configuration. | 3.3 |

### 📅 Étape 4 : Démarche Scientifique & Livrables (Niveau 3)

*Objectif : Éprouver mathématiquement le système, documenter les trajectoires et préparer la soutenance.*

| Tâche | Assignation | Description | Dépendances |
| --- | --- | --- | --- |
| **4.1 Tests de Robustesse** | Tout le groupe | Analyser le comportement du réseau face à la suppression du mélange, à l'absence de normalisation ou lors de l'application de la fonction `fixeCoefApprentissage` pour contrer le bruitage des images. | Étape 3 |
| **4.2 Rapport Technique (Analyse)** | Chef de Projet | Rédiger le document PDF scientifique : comparer finement Heaviside vs Sigmoïde vs ReLU. **Obligation :** Décrire précisément tous les tests effectués, dresser le journal de ce qui a échoué, et apporter une justification mathématique/statistique stricte à tout choix de seuil de décision (ex: si choix d'un seuil empirique de 65%). | 4.1 |
| **4.3 Support de Soutenance** | Tout le groupe | Concevoir le support de présentation PDF (7 minutes maximum, répartition équitable de la parole) mettant explicitement en valeur **au moins deux difficultés majeures rencontrées** et la manière dont elles ont été surmontées. | 4.2 |

---

## 🚫 Contraintes Strictes de Rendu

Le non-respect de ces règles de l'école entraînera l'application immédiate d'une pénalité de 100% sur la note finale :

1. **Zéro bibliothèque externe :** Seules les bibliothèques Java standards (`java.util`, `java.io`, `java.nio`) sont autorisées.
2. **Exclusion des jeux de données :** Ne jamais inclure les dossiers d'images d'entraînement ou de test dans l'archive de rendu (uniquement les fichiers sources `.java`).
3. **Date limite de la présentation (Soutenance) :** Envoi du support au format PDF (nommé avec le numéro de groupe) le **05/06 avant 11h59** au plus tard par courriel aux 3 moniteurs.
4. **Date limite du code source :** Envoi de l'archive des codes sources sources le **05/06 avant 12h00** pile.

```

```

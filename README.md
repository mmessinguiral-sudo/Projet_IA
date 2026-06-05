# Projet_IA - Instructions d'utilisation

Ce dépôt contient un ensemble de classes Java pour charger, normaliser et tester un petit pipeline d'analyse d'images d'animaux.

**Emplacement du dossier `dataset_animaux`**
- Placez le dossier `dataset_animaux` à la racine du projet (au même niveau que `Image.java` et le dossier `neurone`).
- Structure recommandée :

```
dataset_animaux/
  train/
    cat/    (ou chat/)    -> images de chats
    dog/    (ou chien/)   -> images de chiens
    wild/   (ou sauvage/) -> images d'animaux sauvages
  test/
    cat/  dog/  wild/ (même organisation que train)
```

Remarque : le code identifie la classe d'une image en recherchant des sous-chaînes dans le chemin/fichier :
- `cat` ou `chat` → CHAT
- `dog` ou `chien` → CHIEN
- `wild` ou `sauvage` → SAUVAGE
Assurez-vous que les noms de dossiers ou de fichiers contiennent ces tokens (en minuscules ou majuscules). Exemple accepté : `dataset_animaux/train/dog/010552.jpg`.

**Format attendu des images**
- Les images sont lues avec `ImageIO` (formats usuels : JPG, PNG, ...). `Image.java` convertit automatiquement vers niveaux de gris ou conserve 3 canaux selon l'option.
- Le pipeline est écrit pour fonctionner avec des images carrées (le code utilise souvent 64×64 dans des exemples). Ce n'est pas strictement imposé, mais des tailles uniformes (ex. 64×64) facilitent l'usage.

**Compilation (Windows - CMD)**
Ouvrez un terminal `cmd` à la racine du projet (le dossier contenant `Image.java` et le dossier `neurone`).

Option A — Compiler depuis la racine :

```bat
javac Image.java neurone\*.java
```

Exécuter le programme principal de normalisation/évaluation :

```bat
java -cp neurone Normalisation_Labellisation
```

Option B — Compiler et exécuter depuis le dossier `neurone` :

```bat
cd neurone
javac *.java ..\Image.java
java Normalisation_Labellisation
```

Notes sur les commandes :
- `javac` compile les fichiers `.java` et produit des `.class`.
- `-cp neurone` ajoute le dossier `neurone` au classpath pour exécuter les classes qui se trouvent dans ce dossier (les classes utilisent le package par défaut).
- Si une classe `main` différente doit être lancée, remplacez `Normalisation_Labellisation` par le nom de la classe contenant `public static void main` (ex. `TestFFT`, `NewtestNeurone`, `testNeurone`, etc.). Exemple :

```bat
java -cp neurone TestFFT
```

**Dépannage rapide**
- Si `Files.walk` lève une exception ou si aucune image n'est trouvée, vérifiez le chemin `dataset_animaux` et les permissions.
- Pour utiliser un chemin absolu, éditez `Normalisation_Labellisation.java` et remplacez les chemins `dataset_animaux/train/` et `dataset_animaux/test/` par le chemin complet.
- Assurez-vous d'avoir un JDK installé (commande `javac` disponible).

Si vous voulez, je peux :
- vérifier et adapter les chemins dans le code pour accepter un argument en ligne de commande, ou
- lancer une compilation test (si vous m'autorisez à exécuter des commandes dans le terminal).



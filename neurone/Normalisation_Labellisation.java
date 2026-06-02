import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Normalisation_Labellisation {

    /**
     * STRUCTURE DE RETOUR (Conteneur sécurisé)
     * Permet d'associer définitivement la matrice d'entrées X et ses cibles y
     * pour qu'elles ne soient JAMAIS dissociées lors des transferts dans le projet.
     */
    public static class Dataset {
        public final float[][] X; // Matrice des pixels normalisés [nb_images][nb_pixels]
        public final float[] y;   // Vecteur des étiquettes cibles [nb_images]

        public Dataset(float[][] X, float[] y) {
            this.X = X;
            this.y = y;
        }
    }

    /**
     * TÂCHE 1 : NORMALISATION DES AMPLITUDES
     * Convertit le signal brut (0 à 255) en valeurs décimales (0.0 à 1.0)
     */
    public static float[] normaliserImage(int[] pixelsBruts) {
        if (pixelsBruts == null) return null;
        
        float[] pixelsNormalises = new float[pixelsBruts.length];
        for (int i = 0; i < pixelsBruts.length; i++) {
            pixelsNormalises[i] = (float) pixelsBruts[i] / 255.0f;
        }
        return pixelsNormalises;
    }

    /**
     * CHARGEMENT POUR LA TÂCHE 1 : Chiens vs Chats uniquement (Étapes 1, 3, 4 du prof)
     * Filtre le dossier pour exclure les animaux sauvages.
     */
    public static Dataset chargerTacheDomestique(String cheminDossier) {
        List<String> cheminsFichiers = Image.listeFichiers(cheminDossier);
        List<Image> listeImagesPourMelange = new ArrayList<>();

        if (cheminsFichiers == null) return new Dataset(new float[0][0], new float[0]);

        for (String chemin : cheminsFichiers) {
            String nomMin = chemin.toLowerCase();
            int labelBase = -1;

            if (nomMin.contains("cat") || nomMin.contains("chat")) {
                labelBase = 0;
            } else if (nomMin.contains("dog") || nomMin.contains("chien")) {
                labelBase = 1;
            }

            // On ignore volontairement les "wild" pour cette tâche
            if (labelBase == 0 || labelBase == 1) {
                Image img = new Image(chemin, labelBase, true);
                if (img.donnees() != null) {
                    listeImagesPourMelange.add(img);
                }
            }
        }

        // Mélange des objets Image : garantit la non-dissociation couple (pixels/label)
        Collections.shuffle(listeImagesPourMelange);

        float[][] X = new float[listeImagesPourMelange.size()][];
        float[] y = new float[listeImagesPourMelange.size()];

        for (int i = 0; i < listeImagesPourMelange.size(); i++) {
            Image img = listeImagesPourMelange.get(i);
            X[i] = normaliserImage(img.donnees());
            y[i] = (img.label() == 1) ? 1.0f : 0.0f; // Cible : 1.0f = Chien, 0.0f = Chat
        }

        return new Dataset(X, y);
    }

    /**
     * CHARGEMENT POUR LA TÂCHE 2 : Sauvage Oui / Non (Étapes 2, 3, 4 du prof)
     * Prend toutes les images et génère un label binaire (1.0 = Sauvage, 0.0 = Domestique)
     */
    public static Dataset chargerTacheSauvage(String cheminDossier) {
        List<String> cheminsFichiers = Image.listeFichiers(cheminDossier);
        List<Image> listeImagesPourMelange = new ArrayList<>();

        if (cheminsFichiers == null) return new Dataset(new float[0][0], new float[0]);

        for (String chemin : cheminsFichiers) {
            String nomMin = chemin.toLowerCase();
            int labelBase = -1;

            if (nomMin.contains("cat") || nomMin.contains("chat")) {
                labelBase = 0;
            } else if (nomMin.contains("dog") || nomMin.contains("chien")) {
                labelBase = 1;
            } else if (nomMin.contains("wild") || nomMin.contains("sauvage")) {
                labelBase = 2;
            }

            if (labelBase != -1) {
                Image img = new Image(chemin, labelBase, true);
                if (img.donnees() != null) {
                    listeImagesPourMelange.add(img);
                }
            }
        }

        // Mélange des objets Image
        Collections.shuffle(listeImagesPourMelange);

        float[][] X = new float[listeImagesPourMelange.size()][];
        float[] y = new float[listeImagesPourMelange.size()];

        for (int i = 0; i < listeImagesPourMelange.size(); i++) {
            Image img = listeImagesPourMelange.get(i);
            X[i] = normaliserImage(img.donnees());
            y[i] = (img.label() == 2) ? 1.0f : 0.0f; // Cible : 1.0f = Sauvage, 0.0f = Chat ou Chien
        }

        return new Dataset(X, y);
    }

    /**
     * Le main sert maintenant d'outil de vérification pour valider votre usine à données
     */
    public static void main(String[] args) {
        System.out.println("=== TEST UNITAIRE DU PIPELINE DATA ===");
        
        // Test de chargement Train
        Dataset trainDomestique = chargerTacheDomestique("dataset_animaux/train/");
        Dataset trainSauvage = chargerTacheSauvage("dataset_animaux/train/");
        
        // Test de chargement Test (essentiel pour les métriques demandées !)
        Dataset testDomestique = chargerTacheDomestique("dataset_animaux/test/");
        Dataset testSauvage = chargerTacheSauvage("dataset_animaux/test/");

        System.out.println("Données d'entraînement Domestiques chargées : " + trainDomestique.X.length + " images.");
        System.out.println("Données d'entraînement Sauvages chargées    : " + trainSauvage.X.length + " images.");
        System.out.println("Données de test Domestiques chargées        : " + testDomestique.X.length + " images.");
        System.out.println("Données de test Sauvages chargées           : " + testSauvage.X.length + " images.");
        
        if(trainDomestique.X.length > 0) {
            System.out.println("Dimension d'une image (nb de synapses requises) : " + trainDomestique.X[0].length + " pixels.");
        }
        System.out.println("=======================================");
    }
}

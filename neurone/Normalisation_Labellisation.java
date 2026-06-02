import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Normalisation_Labellisation {

    /**
     * STRUCTURE DE RETOUR EN PARALLÈLE (Conteneur sécurisé)
     * Aligne une matrice d'images X avec ses DEUX vecteurs cibles y_tache1 et y_tache2.
     * Garantit que l'image à l'index [i] correspond aux mêmes étiquettes pour les deux neurones.
     */
    public static class Dataset {
        public final float[][] X;         // Matrice des pixels normalisés [nb_images][4096]
        public final float[] y_tache1;    // Cible Domestique : 1.0f = Chien | 0.0f = Chat ou Sauvage
        public final float[] y_tache2;    // Cible Environnement : 1.0f = Sauvage | 0.0f = Domestique

        public Dataset(float[][] X, float[] y_tache1, float[] y_tache2) {
            this.X = X;
            this.y_tache1 = y_tache1;
            this.y_tache2 = y_tache2;
        }
    }

    /**
     * TÂCHE 2.3 : NORMALISATION DES AMPLITUDES
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
     * TÂCHES 1.4, 2.1, 2.2 & 2.3 UNIFIÉES
     * Charge l'ensemble du dossier, extrait les labels de base, mélange de manière sûre,
     * puis génère la matrice d'entrées et les deux vecteurs cibles parallèles.
     */
    public static Dataset chargerPipelineUnique(String cheminDossier) {
        // Tâche 1.4 : Parsing global des images via la méthode imposée
        List<String> cheminsFichiers = Image.listeFichiers(cheminDossier);
        List<Image> listeImagesPourMelange = new ArrayList<>();

        if (cheminsFichiers == null) {
            return new Dataset(new float[0][0], new float[0], new float[0]);
        }

        for (String chemin : cheminsFichiers) {
            String nomMin = chemin.toLowerCase();
            int labelBase = -1;

            // Tâche 2.1 : Labellisation Dynamique Robuste (Mapping Image.java)
            if (nomMin.contains("cat") || nomMin.contains("chat")) {
                labelBase = 0; // Label stocké pour Chat
            } else if (nomMin.contains("dog") || nomMin.contains("chien")) {
                labelBase = 1; // Label stocké pour Chien
            } else if (nomMin.contains("wild") || nomMin.contains("sauvage")) {
                labelBase = 2; // Label stocké pour Sauvage
            }

            // Si le fichier correspond à nos critères, on l'instancie en niveaux de gris (true)
            if (labelBase != -1) {
                Image img = new Image(chemin, labelBase, true);
                if (img.donnees() != null) {
                    listeImagesPourMelange.add(img);
                }
            }
        }

        // Tâche 2.2 : Algorithme de Mélange sécurisé sur la liste d'objets (Garantit le lien Image <-> Label)
        Collections.shuffle(listeImagesPourMelange);

        // Préparation des structures de données finales alignées
        int nbImages = listeImagesPourMelange.size();
        float[][] X = new float[nbImages][];
        float[] y_tache1 = new float[nbImages];
        float[] y_tache2 = new float[nbImages];

        // Tâche 2.3 : Module de Normalisation exécuté après le mélange
        for (int i = 0; i < nbImages; i++) {
            Image img = listeImagesPourMelange.get(i);
            
            // 1. Extraction et normalisation des caractéristiques (pixels)
            X[i] = normaliserImage(img.donnees());
            
            // 2. Traduction en profils de sorties distincts selon le tableau officiel du sujet :
            // Tâche 1 (Chien vs Chat) : dog = 1.0f | cat = 0.0f | wild = 0.0f
            y_tache1[i] = (img.label() == 1) ? 1.0f : 0.0f;
            
            // Tâche 2 (Sauvage Oui/Non) : wild = 1.0f | cat = 0.0f | dog = 0.0f
            y_tache2[i] = (img.label() == 2) ? 1.0f : 0.0f;
        }

        return new Dataset(X, y_tache1, y_tache2);
    }
/**
     * TÂCHE 3.4 : CALCUL DES MÉTRIQUES ET SUCCÈS (Accuracy)
     * Teste les neurones sur le jeu de données de test et calcule le % de réussite.
     */
    public static void evaluerPerformances(iNeurone nDomestique, iNeurone nSauvage, Dataset testData) {
        if (testData.X.length == 0) {
            System.out.println("Erreur : Aucun dataset de test fourni pour l'évaluation.");
            return;
        }

        int bonnesReponsesT1 = 0;
        int bonnesReponsesT2 = 0;
        int totalImages = testData.X.length;

        for (int i = 0; i < totalImages; i++) {
            // 1. Évaluation de la Tâche 1 (Chien vs Chat)
            nDomestique.metAJour(testData.X[i]);
            float sortieT1 = nDomestique.sortie();
            // Seuil de décision à 0.5f (en dessous = Chat, au dessus = Chien)
            float predictionT1 = (sortieT1 >= 0.5f) ? 1.0f : 0.0f;
            if (predictionT1 == testData.y_tache1[i]) {
                bonnesReponsesT1++;
            }

            // 2. Évaluation de la Tâche 2 (Sauvage vs Domestique)
            nSauvage.metAJour(testData.X[i]);
            float sortieT2 = nSauvage.sortie();
            // Seuil de décision à 0.5f (en dessous = Domestique, au dessus = Sauvage)
            float predictionT2 = (sortieT2 >= 0.5f) ? 1.0f : 0.0f;
            if (predictionT2 == testData.y_tache2[i]) {
                bonnesReponsesT2++;
            }
        }

        // Calcul des pourcentages de précision (Accuracy)
        float accuracyT1 = ((float) bonnesReponsesT1 / totalImages) * 100f;
        float accuracyT2 = ((float) bonnesReponsesT2 / totalImages) * 100f;

        System.out.println("\n========= RÉSULTATS DE L'ÉVALUATION (JEU DE TEST) =========");
        System.out.printf("Tâche 1 (Chien vs Chat)         : %.2f%% de succès (%d / %d)%n", accuracyT1, bonnesReponsesT1, totalImages);
        System.out.printf("Tâche 2 (Sauvage vs Domestique) : %.2f%% de succès (%d / %d)%n", accuracyT2, bonnesReponsesT2, totalImages);
        System.out.println("===========================================================");
    }
    
    public static void main(String[] args) {
        System.out.println("=== TEST UNITAIRE DU PIPELINE DATA MULTI-TÂCHES ===");
        
        // Un seul appel pour tout le bloc d'entraînement
        System.out.println("Chargement du bloc Train...");
        Dataset trainData = chargerPipelineUnique("dataset_animaux/train/");
        
        // Un seul appel pour tout le bloc d'évaluation (Essentiel pour la Tâche 3.4 du Dev Data 1)
        System.out.println("Chargement du bloc Test...");
        Dataset testData = chargerPipelineUnique("dataset_animaux/test/");

        System.out.println("\n================ STATISTIQUES ================");
        System.out.println("Nombre total d'images d'entraînement : " + trainData.X.length);
        System.out.println("Nombre total d'images de test         : " + testData.X.length);
        
        if (trainData.X.length > 0) {
            System.out.println("Dimension du vecteur d'entrée        : " + trainData.X[0].length + " synapses (pixels).");
            System.out.println("Taille des vecteurs cibles Train T1  : " + trainData.y_tache1.length);
            System.out.println("Taille des vecteurs cibles Train T2  : " + trainData.y_tache2.length);
            System.out.println("Statut de l'alignement parallèle     : PARFAIT POUR L'ÉTAPE 3.3");
        }
        System.out.println("==============================================");
    }
}

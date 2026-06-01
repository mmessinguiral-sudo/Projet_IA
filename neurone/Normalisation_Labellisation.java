import java.util.ArrayList;
import java.util.List;

public class Normalisation_Labellisation {

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

    public static void main(String[] args) {
        // Entrée : Dossier contenant le jeu de données d'entraînement
        String cheminDossier = "dataset_animaux/train/"; 
        
        System.out.println("Début de la lecture et de la préparation des données...");
        
        // Utilisation du code imposé (Image.java) pour lister les fichiers du disque dur
        List<String> cheminsFichiers = Image.listeFichiers(cheminDossier);
        
        if (cheminsFichiers == null || cheminsFichiers.isEmpty()) {
            System.err.println("Erreur : Aucun fichier trouvé dans le dossier " + cheminDossier);
            return;
        }

        List<float[]> listEntrees = new ArrayList<>();
        List<Float> listLabels = new ArrayList<>();

        for (String chemin : cheminsFichiers) {
            
            // TÂCHE 2 : LABELLISATION AUTOMATIQUE (Imposée par le sujet)
            // Actif (1.0) = Chat | Inactif (0.0) = Pas chat (Chien)
            float label = 0.0f; 
            
            String nomFichierMinuscule = chemin.toLowerCase();
            if (nomFichierMinuscule.contains("cat") || nomFichierMinuscule.contains("chat")) {
                label = 1.0f; 
            }

            // Utilisation du constructeur imposé (Image.java) en mode niveaux de gris (true)
            Image img = new Image(chemin, (int) label, true);
            
            if (img.donnees() == null) {
                continue;
            }

            // Application de la normalisation
            float[] entreesNormalisees = normaliserImage(img.donnees());

            listEntrees.add(entreesNormalisees);
            listLabels.add(label);
        }

        // SORTIES COMPILÉES : Structures prêtes pour le traitement intelligent
        float[][] X_train = listEntrees.toArray(new float[listEntrees.size()][]);
        float[] y_train = new float[listLabels.size()];
        for (int i = 0; i < listLabels.size(); i++) {
            y_train[i] = listLabels.get(i);
        }

        System.out.println("=== BILAN DE LA PRÉPARATION ===");
        System.out.println("Nombre d'images lues avec succès : " + X_train.length);
        System.out.println("Données converties en float[][] et normalisées (0.0 à 1.0).");
        System.out.println("Labels générés dans y_train[] (1.0 = Chat, 0.0 = Chien).");
        System.out.println("================================\n");
        
        // SUITE DU PROJET (Camarades) :
        // TODO : Mélanger (shuffler) X_train et y_train
        // TODO : Entraîner le neurone (n.apprentissage(X_train, y_train, MSElimite))
    }
}
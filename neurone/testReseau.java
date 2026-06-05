public class testReseau {

    // Point d'accès vers le sous-ensemble de validation
    final static String dossierTest = "Projet_IA-main/dataset_animaux/test/";

    public static void main(String[] args) {
        
        // Configuration du pipeline d'entrée (prétraitement des matrices d'images)
        boolean activerNormalisation = true;  
        boolean modeGris             = false; 
        boolean modeTSL              = true;  

        System.out.println(" PHASE DE PRODUCTION : RÉSEAU MULTI-CLASSES");

        System.out.println("Lecture du dossier de Test...");
        Normalisation_Labellisation.Dataset testData = Normalisation_Labellisation.chargerPipelineUnique(
                dossierTest, false, activerNormalisation, modeGris, modeTSL, false);
        
        if (testData.X.length == 0) {
            System.err.println("Aucune image trouvée. Vérifie le chemin du dossier !");
            return;
        }
        
        // Dimensionnement du vecteur d'entrée selon le format des données chargées
        final int tailleImage = testData.X[0].length;

        System.out.println("Création et chargement des 3 neurones");
        final iNeurone neuroneChat = new NeuroneSigmoide(tailleImage);
        final iNeurone neuroneChien = new NeuroneSigmoide(tailleImage);
        final iNeurone neuroneSauvage = new NeuroneSigmoide(tailleImage);
        
        // Restauration des vecteurs de poids pré-entraînés (persistance d'état)
        neuroneChat.chargement("poids_chat_sigmoide.txt");
        neuroneChien.chargement("poids_chien_sigmoide.txt");
        neuroneSauvage.chargement("poids_sauvage_sigmoide.txt");

        System.out.println("Mise en réseau parallèle...");
        // Encapsulation des classifieurs binaires au sein d'une topologie multi-expert
        iNeurone[] equipe = {neuroneChat, neuroneChien, neuroneSauvage};
        Reseau monReseau = new Reseau(equipe);

        System.out.println("RÉSULTATS DU RÉSEAU");
        
        int succesTotal = 0;
        int totalImages = testData.X.length;

        // Phase d'inférence globale sur l'ensemble de test
        for (int i = 0; i < totalImages; i++) {
            
            // Évaluation simultanée du pattern par les trois experts
            float[] reponses = monReseau.evaluerImage(testData.X[i]);
            
            // Calcul de la classe dominante (Logique ArgMax)
            int indexGagnant = 0;
            float confianceMax = reponses[0];
            
            for (int j = 1; j < reponses.length; j++) {
                if (reponses[j] > confianceMax) {
                    confianceMax = reponses[j];
                    indexGagnant = j;
                }
            }

            int indexVraiLabel = -1;

            // Décodage de la vérité terrain (One-Hot-Encoding inverse)
            if (testData.y_chat[i] == 1.0f) indexVraiLabel = 0;
            else if (testData.y_chien[i] == 1.0f) indexVraiLabel = 1;
            else if (testData.y_sauvage[i] == 1.0f) indexVraiLabel = 2;

            // Confrontation du modèle face à la cible
            if (indexGagnant == indexVraiLabel) {
                succesTotal++;
            }
        }

        // Calcul des métriques de performance globales
        float precisionGlobale = ((float) succesTotal / totalImages) * 100f;
        
        System.out.println("BILAN DU RESEAU");
        System.out.printf(" Précision globale de l'IA : %.2f%% (%d bonnes réponses sur %d)\n", 
                          precisionGlobale, succesTotal, totalImages);
    }
}

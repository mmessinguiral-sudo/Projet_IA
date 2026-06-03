public class testReseau {

    final static String dossierTest = "Projet_IA-main/dataset_animaux/test/";

    public static void main(String[] args) {
        
        // =========================================================================
        // CONFIGURATION (DOIT ÊTRE IDENTIQUE À CELLE DE L'ENTRAÎNEMENT !)
        // =========================================================================
        boolean activerNormalisation = true;  
        boolean modeGris             = false; 
        boolean modeTSL              = true; 
        // Note : Le mélange et la data augmentation sont ignorés pour l'évaluation

        System.out.println("==================================================");
        System.out.println(" PHASE DE PRODUCTION : RÉSEAU MULTI-CLASSES");
        System.out.println("==================================================");

        System.out.println("1. Lecture du dossier de Test...");
        // Appel avec les nouveaux paramètres (mélange=false, dataAugmentation=false pour le test)
        Normalisation_Labellisation.Dataset testData = Normalisation_Labellisation.chargerPipelineUnique(
                dossierTest, false, activerNormalisation, modeGris, modeTSL, false);
        
        if (testData.X.length == 0) {
            System.err.println("Aucune image trouvée. Vérifie le chemin du dossier !");
            return;
        }
        final int tailleImage = testData.X[0].length;

        System.out.println("2. Création et chargement des 3 spécialistes (Sigmoïde)...");
        final iNeurone neuroneChat = new NeuroneSigmoide(tailleImage);
        final iNeurone neuroneChien = new NeuroneSigmoide(tailleImage);
        final iNeurone neuroneSauvage = new NeuroneSigmoide(tailleImage);

        // /!\ Assure-toi d'utiliser les bons fichiers selon ton entraînement
        neuroneChat.chargement("poids_chat_sigmoide.txt");
        neuroneChien.chargement("poids_chien_sigmoide.txt");
        neuroneSauvage.chargement("poids_sauvage_sigmoide.txt");

        System.out.println("3. Mise en réseau parallèle...");
        iNeurone[] equipe = {neuroneChat, neuroneChien, neuroneSauvage};
        Reseau monReseau = new Reseau(equipe);

        System.out.println("\n================ RÉSULTATS DU RÉSEAU ================");
        
        int succesTotal = 0;
        int totalImages = testData.X.length;

        for (int i = 0; i < totalImages; i++) {
            
            float[] reponses = monReseau.evaluerImage(testData.X[i]);
            
            // --- LOGIQUE ARGMAX ---
            int indexGagnant = 0;
            float confianceMax = reponses[0];
            
            for (int j = 1; j < reponses.length; j++) {
                if (reponses[j] > confianceMax) {
                    confianceMax = reponses[j];
                    indexGagnant = j;
                }
            }

            // --- NOUVELLE VÉRIFICATION DE LA VRAIE RÉPONSE ---
            int indexVraiLabel = -1;
            if (testData.y_chat[i] == 1.0f) indexVraiLabel = 0;
            else if (testData.y_chien[i] == 1.0f) indexVraiLabel = 1;
            else if (testData.y_sauvage[i] == 1.0f) indexVraiLabel = 2;

            if (indexGagnant == indexVraiLabel) {
                succesTotal++;
            }
        }

        float precisionGlobale = ((float) succesTotal / totalImages) * 100f;
        
        System.out.println("\n##################################################");
        System.out.println("# BILAN DU CLASSIFIEUR MULTI-CLASSES (RÉSEAU)    #");
        System.out.println("##################################################");
        System.out.printf(" Précision globale de l'IA : %.2f%% (%d bonnes réponses sur %d)\n", 
                          precisionGlobale, succesTotal, totalImages);
        System.out.println("##################################################");
    }
}
public class testReseau {

    final static String dossierTest = "Projet_IA-main/dataset_animaux/test/";

    public static void main(String[] args) {
        
        //Paramètres des images
        boolean activerNormalisation = true;  
        boolean modeGris             = false; 
        boolean modeTSL              = true; 

        System.out.println(" PHASE DE PRODUCTION : RÉSEAU MULTI-CLASSES");

        //Chargement des test
        System.out.println("Lecture du dossier de Test...");
        Normalisation_Labellisation.Dataset testData = Normalisation_Labellisation.chargerPipelineUnique(
                dossierTest, false, activerNormalisation, modeGris, modeTSL, false);
        
        if (testData.X.length == 0) {
            System.err.println("Aucune image trouvée. Vérifie le chemin du dossier !");
            return;
        }
        final int tailleImage = testData.X[0].length;

        System.out.println("Création et chargement des 3 neurones");
        final iNeurone neuroneChat = new NeuroneSigmoide(tailleImage);
        final iNeurone neuroneChien = new NeuroneSigmoide(tailleImage);
        final iNeurone neuroneSauvage = new NeuroneSigmoide(tailleImage);
        
        //Chargement des données déjà entrainé
        neuroneChat.chargement("poids_chat_sigmoide.txt");
        neuroneChien.chargement("poids_chien_sigmoide.txt");
        neuroneSauvage.chargement("poids_sauvage_sigmoide.txt");

        System.out.println("Mise en réseau parallèle...");
        // Assemblage des experts dans iNeurone au global
        iNeurone[] equipe = {neuroneChat, neuroneChien, neuroneSauvage};
        Reseau monReseau = new Reseau(equipe);

        System.out.println("RÉSULTATS DU RÉSEAU");
        
        int succesTotal = 0;
        int totalImages = testData.X.length;

        for (int i = 0; i < totalImages; i++) {
            
            //On envoie au 3 neurone en meme temps
            float[] reponses = monReseau.evaluerImage(testData.X[i]);
            
            int indexGagnant = 0;
            float confianceMax = reponses[0];
            // Parcours des probabilités pour trouver le score le plus élevé
            for (int j = 1; j < reponses.length; j++) {
                if (reponses[j] > confianceMax) {
                    confianceMax = reponses[j];
                    indexGagnant = j;
                }
            }

            int indexVraiLabel = -1;

            //Label originaux des images
            if (testData.y_chat[i] == 1.0f) indexVraiLabel = 0;
            else if (testData.y_chien[i] == 1.0f) indexVraiLabel = 1;
            else if (testData.y_sauvage[i] == 1.0f) indexVraiLabel = 2;

            if (indexGagnant == indexVraiLabel) {
                succesTotal++;
            }
        }

        float precisionGlobale = ((float) succesTotal / totalImages) * 100f;
        
        System.out.println("BILAN DU RESEAU");
        System.out.printf(" Précision globale de l'IA : %.2f%% (%d bonnes réponses sur %d)\n", 
                          precisionGlobale, succesTotal, totalImages);
    }
}

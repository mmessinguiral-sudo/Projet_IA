public class testHierarchique {

    final static String dossierTest = "Projet_IA-main/dataset_animaux/test/";
    final static float SEUIL_INCONNU = 0.20f; 

    public static void main(String[] args) {
        
        // =========================================================================
        // CONFIGURATION (DOIT ÊTRE IDENTIQUE À CELLE DE L'ENTRAÎNEMENT !)
        // =========================================================================
        boolean activerNormalisation = true;  
        boolean modeGris             = false; 
        boolean modeTSL              = true; 

        System.out.println("==================================================");
        System.out.println(" PHASE DE PRODUCTION : RÉSEAU HIÉRARCHIQUE (2 COUCHES)");
        System.out.println("==================================================");

        System.out.println("1. Lecture du dossier de Test...");
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

        neuroneChat.chargement("poids_chat_sigmoide.txt");
        neuroneChien.chargement("poids_chien_sigmoide.txt");
        neuroneSauvage.chargement("poids_sauvage_sigmoide.txt");

        System.out.println("\n================ ANALYSE EN COURS ================");
        
        int totalVraiSauvage = 0;
        int totalVraiDomestique = 0;

        int succesFiltreSauvage = 0;
        int fauxPositifSauvage = 0; 
        int fauxNegatifSauvage = 0; 

        int evaluesCouche2 = 0;
        int succesChat = 0;
        int succesChien = 0;
        int erreursCouche2 = 0;
        
        int succesTotal = 0;
        int imagesInconnues = 0;
        int totalImages = testData.X.length;

        for (int i = 0; i < totalImages; i++) {
            
            float[] imageActuelle = testData.X[i];
            
            // --- NOUVELLE IDENTIFICATION DE LA VRAIE RÉPONSE ---
            int indexVraiLabel = -1;
            if (testData.y_chat[i] == 1.0f) indexVraiLabel = 0;
            else if (testData.y_chien[i] == 1.0f) indexVraiLabel = 1;
            else if (testData.y_sauvage[i] == 1.0f) indexVraiLabel = 2;

            if (indexVraiLabel == 2) totalVraiSauvage++;
            else totalVraiDomestique++;

            // COUCHE 1 : LE FILTRE SAUVAGE
            neuroneSauvage.metAJour(imageActuelle);
            
            // On garde ton seuil optimisé à 0.35f
            if (neuroneSauvage.sortie() >= 0.35f) {
                if (indexVraiLabel == 2) {
                    succesFiltreSauvage++;
                    succesTotal++;
                } else {
                    fauxPositifSauvage++; 
                }
            } 
            else {
                evaluesCouche2++;
                
                if (indexVraiLabel == 2) {
                    fauxNegatifSauvage++; 
                    erreursCouche2++; 
                }

                // COUCHE 2 : LE DUEL DOMESTIQUE
                neuroneChat.metAJour(imageActuelle);
                neuroneChien.metAJour(imageActuelle);

                float probaChat = neuroneChat.sortie();
                float probaChien = neuroneChien.sortie();
                float ecart = Math.abs(probaChat - probaChien);

                int indexGagnant = -1;

                if (ecart < SEUIL_INCONNU) {
                    imagesInconnues++;
                } else if (probaChat > probaChien) {
                    indexGagnant = 0; 
                } else {
                    indexGagnant = 1; 
                }

                if (indexGagnant != -1) {
                    if (indexGagnant == indexVraiLabel) {
                        succesTotal++;
                        if (indexGagnant == 0) succesChat++;
                        if (indexGagnant == 1) succesChien++;
                    } else if (indexVraiLabel != 2) {
                        erreursCouche2++; 
                    }
                }
            }
        }

        System.out.println("\n##################################################");
        System.out.println("# RAPPORT DÉTAILLÉ DU RÉSEAU HIÉRARCHIQUE        #");
        System.out.println("##################################################");
        
        System.out.println("\n[ÉTAPE 1 : FILTRE SAUVAGE]");
        System.out.printf("- Vrais Animaux Sauvages détectés : %d / %d\n", succesFiltreSauvage, totalVraiSauvage);
        System.out.printf("- Faux Positifs (Domestiques pris pour des Sauvages) : %d\n", fauxPositifSauvage);
        System.out.printf("- Faux Négatifs (Sauvages qui ont passé le filtre)   : %d\n", fauxNegatifSauvage);

        System.out.println("\n[ÉTAPE 2 : DUEL CHIEN VS CHAT]");
        System.out.printf("- Images arrivées à cette étape : %d\n", evaluesCouche2);
        System.out.printf("- Chiens correctement identifiés : %d\n", succesChien);
        System.out.printf("- Chats correctement identifiés  : %d\n", succesChat);
        System.out.printf("- Confusions ou erreurs à cette étape : %d\n", erreursCouche2);

        System.out.println("\n==================================================");
        System.out.println("[BILAN GLOBAL]");
        float precisionGlobale = ((float) succesTotal / totalImages) * 100f;
        float pourcentageInconnu = ((float) imagesInconnues / totalImages) * 100f;
        
        System.out.printf(" Précision finale  : %.2f%% (%d / %d)\n", precisionGlobale, succesTotal, totalImages);
        System.out.printf(" Taux de rejet     : %.2f%% (%d doutes)\n", pourcentageInconnu, imagesInconnues);
        System.out.println("##################################################");
    }
}
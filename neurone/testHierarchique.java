public class testHierarchique {

    final static String dossierTest = "Projet_IA-main/dataset_animaux/test/";
    final static float SEUIL_INCONNU = 0.50f; 

    public static void main(String[] args) {
       
        boolean activerNormalisation = true;  
        boolean modeGris             = false; 
        boolean modeTSL              = true; 

        System.out.println(" PHASE DE PRODUCTION : RÉSEAU 2 COUCHES");

        System.out.println("Lecture du dossier de Test...");
        Normalisation_Labellisation.Dataset testData = Normalisation_Labellisation.chargerPipelineUnique(
                dossierTest, false, activerNormalisation, modeGris, modeTSL, false);
        
        if (testData.X.length == 0) {
            System.err.println("Aucune image trouvée. Vérifie le chemin du dossier !");
            return;
        }
        final int tailleImage = testData.X[0].length;

        System.out.println("Création et chargement des 3 spécialistes (Sigmoïde)...");
        final iNeurone neuroneChat = new NeuroneSigmoide(tailleImage);
        final iNeurone neuroneChien = new NeuroneSigmoide(tailleImage);
        final iNeurone neuroneSauvage = new NeuroneSigmoide(tailleImage);

        neuroneChat.chargement("poids_chat_sigmoide.txt");
        neuroneChien.chargement("poids_chien_sigmoide.txt");
        neuroneSauvage.chargement("poids_sauvage_sigmoide.txt");

        System.out.println("\nANALYSE EN COURS");
        
        //Variable de retour
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

            //On donne le label au classe
            int indexVraiLabel = -1;
            if (testData.y_chat[i] == 1.0f) indexVraiLabel = 0;
            else if (testData.y_chien[i] == 1.0f) indexVraiLabel = 1;
            else if (testData.y_sauvage[i] == 1.0f) indexVraiLabel = 2;

            if (indexVraiLabel == 2) totalVraiSauvage++;
            else totalVraiDomestique++;

            neuroneSauvage.metAJour(imageActuelle);
            //Si c'est un animal sauvage on sort
            //seuil a 0.35 pour minimiser les faux négatifs
            if (neuroneSauvage.sortie() >= 0.35f) {
                if (indexVraiLabel == 2) {
                    succesFiltreSauvage++;
                    succesTotal++;
                } else {
                    fauxPositifSauvage++; // Domestique classé à tort comme sauvage
                }
            } 
            else {
                evaluesCouche2++;
                
                if (indexVraiLabel == 2) {
                    fauxNegatifSauvage++; 
                    erreursCouche2++; 
                }

                // Inférence sur les deux modèles domestiques
                neuroneChat.metAJour(imageActuelle);
                neuroneChien.metAJour(imageActuelle);

                float probaChat = neuroneChat.sortie();
                float probaChien = neuroneChien.sortie();
                // Calcul du delta pour le rejet par seuil de confiance
                float ecart = Math.abs(probaChat - probaChien);

                int indexGagnant = -1;

                if (ecart < SEUIL_INCONNU) {
                    imagesInconnues++; // Prédiction trop incertaine -> Rejet
                } else if (probaChat > probaChien) {
                    indexGagnant = 0; 
                } else {
                    indexGagnant = 1; 
                }

                // Mise à jour des métriques de la couche 2
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

        //Retour des résultat finaux
        System.out.println("RAPPORT DÉTAILLÉ DU RÉSEAU HIÉRARCHIQUE");
        
        System.out.println("\nFILTRE SAUVAGE");
        System.out.printf("- Vrais Animaux Sauvages détectés : %d / %d\n", succesFiltreSauvage, totalVraiSauvage);
        System.out.printf("- Faux Positifs : %d\n", fauxPositifSauvage);
        System.out.printf("- Faux Négatifs  : %d\n", fauxNegatifSauvage);

        System.out.println("\nDUEL CHIEN VS CHAT");
        System.out.printf("- Images arrivées à cette étape : %d\n", evaluesCouche2);
        System.out.printf("- Chiens : %d\n", succesChien);
        System.out.printf("- Chats : %d\n", succesChat);
        System.out.printf("- Confusions ou erreurs à cette étape : %d\n", erreursCouche2);

        System.out.println("BILAN GLOBAL");
        float precisionGlobale = ((float) succesTotal / totalImages) * 100f;
        float pourcentageInconnu = ((float) imagesInconnues / totalImages) * 100f;
        
        System.out.printf(" Précision finale  : %.2f%% (%d / %d)\n", precisionGlobale, succesTotal, totalImages);
        System.out.printf(" Taux de rejet     : %.2f%% (%d doutes)\n", pourcentageInconnu, imagesInconnues);
    }
}

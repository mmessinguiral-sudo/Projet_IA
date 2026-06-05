public class NewtestNeurone {
 
    final static float MSElimite = 0.1f;
    final static String dossierTrain = "Projet_IA-main/dataset_animaux/train/";
    final static String dossierTest = "Projet_IA-main/dataset_animaux/test/";
 
    public static void main(String[] args) {
       
        boolean activerMelange       = true;  // false = Test Extension 5 (Sans mélange)
        boolean activerNormalisation = true;  // false = Test Extension 6 (Sans normalisation)
        boolean modeGris             = false;  // false = Test Extension 3 (Mode Couleur RGB)
        boolean modeTSL              = true; // true  = Test Extension 4 (Couleur TSL - nécessite modeGris = false)
        boolean dataAugmentation     = true; // true  = Test Extension 7 (Augmentation Miroir)
 
        System.out.println(" CONFIGURATION DU PIPELINE DATA ");
        System.out.printf("Mélange: %b | Normalisation: %b | Mode Gris: %b | Mode TSL: %b | Augmentation: %b\n\n",
                activerMelange, activerNormalisation, modeGris, modeTSL, dataAugmentation);
        
        //Chargement des données de test et d'entrainement
        System.out.println("Chargement global des données d'entraînement...");
        Normalisation_Labellisation.Dataset trainData = Normalisation_Labellisation.chargerPipelineUnique(
                dossierTrain, activerMelange, activerNormalisation, modeGris, modeTSL, dataAugmentation);
 
        System.out.println("Chargement global des données de test...");
        Normalisation_Labellisation.Dataset testData = Normalisation_Labellisation.chargerPipelineUnique(
                dossierTest, activerMelange, activerNormalisation, modeGris, modeTSL, false);
            
                
        if (trainData.X.length == 0 || testData.X.length == 0) {
            System.out.println("Erreur : Aucun dataset chargé. Vérifie tes dossiers.");
            return;
        }
 
        // Adapte automatiquement la taille (4096 en Gris, 12288 en RGB/TSL)
        final int tailleImage = trainData.X[0].length;
 
        
        //Eta pour les 3 neurones

        // Neurone.fixeCoefApprentissage(0.1f);   //Heavyside
        // Neurone.fixeCoefApprentissage(0.001f); //ReLU
        Neurone.fixeCoefApprentissage(0.001f);    //Sigmoïde
 
        //Commenter/Décommenter pour entrainer différent types de neurones

        // iNeurone nChat = new NeuroneHeavyside(tailleImage);
        // iNeurone nChien = new NeuroneHeavyside(tailleImage);
        // iNeurone nSauvage = new NeuroneHeavyside(tailleImage);
 
        // iNeurone nChat = new NeuroneReLU(tailleImage);
        // iNeurone nChien = new NeuroneReLU(tailleImage);
        // iNeurone nSauvage = new NeuroneReLU(tailleImage);
 
        iNeurone nChat = new NeuroneSigmoide(tailleImage);
        iNeurone nChien = new NeuroneSigmoide(tailleImage);
        iNeurone nSauvage = new NeuroneSigmoide(tailleImage);
 
 
        //Apprentissage des 3 experts
        System.out.println(" TEST -> CHAT");
        final float[] resChat = apprentissageEtTest(nChat, trainData.X, trainData.y_chat, testData.X, testData.y_chat, "chat");
 
        System.out.println(" TEST -> CHIEN");
        final float[] resChien = apprentissageEtTest(nChien, trainData.X, trainData.y_chien, testData.X, testData.y_chien, "chien");
 
        System.out.println(" TEST -> SAUVAGE");
        final float[] resSauvage = apprentissageEtTest(nSauvage, trainData.X, trainData.y_sauvage, testData.X, testData.y_sauvage, "sauvage");
 
 
        //Performance individuelle
        System.out.println("# RÉSUMÉ DES PERFORMANCES INDIVIDUELLES #");
        System.out.printf("Chat    -> Succès : %.2f%% | Échec : %.2f%%\n", resChat[0], resChat[1]);
        System.out.printf("Chien   -> Succès : %.2f%% | Échec : %.2f%%\n", resChien[0], resChien[1]);
        System.out.printf("Sauvage -> Succès : %.2f%% | Échec : %.2f%%\n", resSauvage[0], resSauvage[1]);
 
        //Evaluation globale
        System.out.println("\n==================================================");
        System.out.println("# ÉVALUATION DU SYSTÈME GLOBAL (CLASSIFICATION)  #");
        System.out.println("==================================================");
        Normalisation_Labellisation.evaluerPerformances(nChat, nChien, nSauvage, testData);
    }
 

    public static float[] apprentissageEtTest(final iNeurone n, final float[][] entrees, final float[] resultats, final float[][] testEntrees, final float[] testResultats, final String nomSauvegarde) {
       
        System.out.println("Apprentissage en cours sur " + entrees.length + " images...");
        n.apprentissage(entrees, resultats, MSElimite);
        
        //Sauvegarde de l'entrainement
//        final String cheminSauvegarde = "poids_" + nomSauvegarde + "_heavyside.txt";
//        final String cheminSauvegarde = "poids_" + nomSauvegarde + "_relu.txt";
        final String cheminSauvegarde = "poids_" + nomSauvegarde + "_sigmoide.txt";
        n.sauvegarde(cheminSauvegarde);
 
        final Neurone vueNeurone = (Neurone) n;
        System.out.println("Biais final trouvé : " + vueNeurone.biais());
 
        System.out.println("Évaluation sur les données de TEST...");
 
        int succesTest = 0;
        int totalTest = testEntrees.length;
 
        for (int i = 0; i < totalTest; i++) {
            n.metAJour(testEntrees[i]);
            final float prediction = n.sortie() >= 0.5f ? 1.0f : 0.0f;
 
            if (prediction == testResultats[i]) {
                succesTest++;
            }
        }
 
        final float pourcentageTest = ((float) succesTest / totalTest) * 100f;
        final float pourcentageEchec = 100f - pourcentageTest;
 
        System.out.printf("-> Résultat immédiat de l'expert : Succès = %.2f%% | Échec = %.2f%%\n", pourcentageTest, pourcentageEchec);
 
        return new float[]{pourcentageTest, pourcentageEchec};
    }
}

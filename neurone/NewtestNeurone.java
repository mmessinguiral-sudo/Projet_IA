//NECESSITE TOUT LES FICHIERS DANS LE MEME DOSSIER (DONC IL FAUT DEPLACER Image.java)

public class NewtestNeurone {
 
    final static float MSElimite = 0.1f;
    final static String dossierTrain = "Projet_IA-main/dataset_animaux/train/";
    final static String dossierTest = "Projet_IA-main/dataset_animaux/test/";
 
    public static void main(String[] args) {
       
        // =========================================================================
        // CONFIGURATION DES EXTENSIONS (Modifie les true/false pour faire tes tests)
        // =========================================================================
        boolean activerMelange       = true;  // false = Test Extension 5 (Sans mélange)
        boolean activerNormalisation = true;  // false = Test Extension 6 (Sans normalisation)
        boolean modeGris             = false;  // false = Test Extension 3 (Mode Couleur RGB)
        boolean modeTSL              = true; // true  = Test Extension 4 (Couleur TSL - nécessite modeGris = false)
        boolean dataAugmentation     = true; // true  = Test Extension 7 (Augmentation Miroir)
 
        System.out.println("=== CONFIGURATION DU PIPELINE DATA ===");
        System.out.printf("Mélange: %b | Normalisation: %b | Mode Gris: %b | Mode TSL: %b | Augmentation: %b\n\n",
                activerMelange, activerNormalisation, modeGris, modeTSL, dataAugmentation);
 
        // 1. Chargement des données avec les paramètres d'extensions choisis
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
 
        // La taille s'adapte automatiquement (4096 en Gris, 12288 en RGB/TSL)
        final int tailleImage = trainData.X[0].length;
 
        // =========================================================================
        // Extension 1 : Choix du type de neurone commun pour les 3 experts
        // =========================================================================
        // Neurone.fixeCoefApprentissage(0.1f);   // Coef conseillé pour Heavyside
        // Neurone.fixeCoefApprentissage(0.001f); // Coef conseillé pour ReLU
        Neurone.fixeCoefApprentissage(0.001f);    // Coef conseillé pour Sigmoïde
 
        // Décommente la ligne du type de neurone que tu veux tester :
        // iNeurone nChat = new NeuroneHeavyside(tailleImage);
        // iNeurone nChien = new NeuroneHeavyside(tailleImage);
        // iNeurone nSauvage = new NeuroneHeavyside(tailleImage);
 
        // iNeurone nChat = new NeuroneReLU(tailleImage);
        // iNeurone nChien = new NeuroneReLU(tailleImage);
        // iNeurone nSauvage = new NeuroneReLU(tailleImage);
 
        iNeurone nChat = new NeuroneSigmoide(tailleImage);
        iNeurone nChien = new NeuroneSigmoide(tailleImage);
        iNeurone nSauvage = new NeuroneSigmoide(tailleImage);
 
 
        // 2. Lancement de l'apprentissage et du test pour les 3 Experts (Extension 2)
        System.out.println("\n==================================================");
        System.out.println(" TEST -> EXPERT CHAT");
        System.out.println("==================================================");
        final float[] resChat = apprentissageEtTest(nChat, trainData.X, trainData.y_chat, testData.X, testData.y_chat, "chat");
 
        System.out.println("\n==================================================");
        System.out.println(" TEST -> EXPERT CHIEN");
        System.out.println("==================================================");
        final float[] resChien = apprentissageEtTest(nChien, trainData.X, trainData.y_chien, testData.X, testData.y_chien, "chien");
 
        System.out.println("\n==================================================");
        System.out.println(" TEST -> EXPERT SAUVAGE");
        System.out.println("==================================================");
        final float[] resSauvage = apprentissageEtTest(nSauvage, trainData.X, trainData.y_sauvage, testData.X, testData.y_sauvage, "sauvage");
 
 
        // 3. AFFICHAGE DU RÉSUMÉ DES PERFORMANCES INDIVIDUELLES
        System.out.println("\n\n##################################################");
        System.out.println("# RÉSUMÉ DES PERFORMANCES INDIVIDUELLES #");
        System.out.println("##################################################");
        System.out.printf(" 1. Expert Chat    -> Succès : %.2f%% | Échec : %.2f%%\n", resChat[0], resChat[1]);
        System.out.printf(" 2. Expert Chien   -> Succès : %.2f%% | Échec : %.2f%%\n", resChien[0], resChien[1]);
        System.out.printf(" 3. Expert Sauvage -> Succès : %.2f%% | Échec : %.2f%%\n", resSauvage[0], resSauvage[1]);
        System.out.println("##################################################");
 
        // 4. EXTENSION 2 & TÂCHE 3.4 : ÉVALUATION MULTI-CLASSE DU SYSTÈME GLOBAL
        System.out.println("\n==================================================");
        System.out.println("# ÉVALUATION DU SYSTÈME GLOBAL (CLASSIFICATION)  #");
        System.out.println("==================================================");
        Normalisation_Labellisation.evaluerPerformances(nChat, nChien, nSauvage, testData);
    }
 
    /**
     * Méthode d'apprentissage et d'évaluation d'un neurone isolé
     */
    public static float[] apprentissageEtTest(final iNeurone n, final float[][] entrees, final float[] resultats, final float[][] testEntrees, final float[] testResultats, final String nomSauvegarde) {
       
        System.out.println("Apprentissage en cours sur " + entrees.length + " images...");
        n.apprentissage(entrees, resultats, MSElimite);
 
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
}

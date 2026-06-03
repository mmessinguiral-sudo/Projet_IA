//NECESSITE TOUT LES FICHIERS DANS LE MEME DOSSIER (DONC IL FAUT DEPLACER Image.java)

public class NewtestNeurone {
 
    final static float MSElimite = 0.1f;
    final static String dossierTrain = "Projet_IA-main/dataset_animaux/train/";
    final static String dossierTest = "Projet_IA-main/dataset_animaux/test/";
 
    public static void main(String[] args) {
       
        boolean activerMelange       = true;  // false = Test Extension 5 (Sans mélange)
        boolean activerNormalisation = true;  // false = Test Extension 6 (Sans normalisation)
        boolean modeGris             = false;  // false = Test Extension 3 (Mode Couleur RGB)
        boolean modeTSL              = true; // true  = Test Extension 4 (Couleur TSL - nécessite modeGris = false)
        boolean dataAugmentation     = false; // true  = Test Extension 7 (Augmentation Miroir)
 
        System.out.println("=== CONFIGURATION DU PIPELINE DATA ===");
        System.out.printf("Mélange: %b | Normalisation: %b | Mode Gris: %b | Mode TSL: %b | Augmentation: %b\n\n",
                activerMelange, activerNormalisation, modeGris, modeTSL, dataAugmentation);
 
        System.out.println("Chargement global des données d'entraînement...");
        Normalisation_Labellisation.Dataset trainData = Normalisation_Labellisation.chargerPipelineUnique(
                dossierTrain, activerMelange, activerNormalisation, modeGris, modeTSL, dataAugmentation);
 
        System.out.println("Chargement global des données de test...");
        Normalisation_Labellisation.Dataset testData = Normalisation_Labellisation.chargerPipelineUnique(
                dossierTest, activerMelange, activerNormalisation, modeGris, modeTSL, false);
 
 
        System.out.println("\n==================================================");
        System.out.println(" TEST -> EXPERT CHAT");
        System.out.println("==================================================");
        final float[] resChat = apprentissageEtTest(trainData.X, trainData.y_chat, testData.X, testData.y_chat, "chat");
 
        System.out.println("\n==================================================");
        System.out.println(" TEST -> EXPERT CHIEN");
        System.out.println("==================================================");
        final float[] resChien = apprentissageEtTest(trainData.X, trainData.y_chien, testData.X, testData.y_chien, "chien");
 
        System.out.println("\n==================================================");
        System.out.println(" TEST -> EXPERT SAUVAGE");
        System.out.println("==================================================");
        final float[] resSauvage = apprentissageEtTest(trainData.X, trainData.y_sauvage, testData.X, testData.y_sauvage, "sauvage");
 
 
        System.out.println("\n\n##################################################");
        System.out.println("# RÉSUMÉ DES PERFORMANCES INDIVIDUELLES #");
        System.out.println("##################################################");
        System.out.printf(" 1. Expert Chat    -> Succès : %.2f%% | Échec : %.2f%%\n", resChat[0], resChat[1]);
        System.out.printf(" 2. Expert Chien   -> Succès : %.2f%% | Échec : %.2f%%\n", resChien[0], resChien[1]);
        System.out.printf(" 3. Expert Sauvage -> Succès : %.2f%% | Échec : %.2f%%\n", resSauvage[0], resSauvage[1]);
        System.out.println("##################################################");
    }
 
    public static float[] apprentissageEtTest(final float[][] entrees, final float[] resultats, final float[][] testEntrees, final float[] testResultats, final String nomSauvegarde) {
        if (entrees == null || entrees.length == 0) {
            return new float[]{0f, 100f};
        }
 
        final int tailleImage = entrees[0].length;
 
        // Neurone.fixeCoefApprentissage(0.1f);   // HEAVYSIDE
        // Neurone.fixeCoefApprentissage(0.001f); // RELU
        Neurone.fixeCoefApprentissage(0.001f);    // SIGMOIDE
 
        // final iNeurone n = new NeuroneHeavyside(tailleImage);
        // final iNeurone n = new NeuroneReLU(tailleImage);
        final iNeurone n = new NeuroneSigmoide(tailleImage);
 
        System.out.println("Apprentissage en cours sur " + entrees.length + " images (Taille vecteur : " + tailleImage + ")...");
        n.apprentissage(entrees, resultats, MSElimite);
 
        final String cheminSauvegarde = "poids_" + nomSauvegarde + "_sigmoide.txt";
        n.sauvegarde(cheminSauvegarde);
 
        final Neurone vueNeurone = (Neurone) n;
        System.out.println("Biais final trouvé : " + vueNeurone.biais());
        System.out.println("Poids synaptiques sauvegardés dans : " + cheminSauvegarde);
 
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
 
        System.out.printf("-> Résultat immédiat : Succès = %.2f%% | Échec = %.2f%%\n", pourcentageTest, pourcentageEchec);
 
        return new float[]{pourcentageTest, pourcentageEchec};
    }
}

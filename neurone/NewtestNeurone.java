public class NewtestNeurone
{
    final static float MSElimite = 0.1f;
    final static String dossierTrain = "Projet_IA-main/dataset_animaux/train/";
    final static String dossierTest = "Projet_IA-main/dataset_animaux/test/";

    public static void main(String[] args)
    {
        System.out.println("Chargement global des données d'entraînement...");
        Normalisation_Labellisation.Dataset trainData = Normalisation_Labellisation.chargerPipelineUnique(dossierTrain);
        
        System.out.println("Chargement global des données de test...");
        Normalisation_Labellisation.Dataset testData = Normalisation_Labellisation.chargerPipelineUnique(dossierTest);


        System.out.println("\n==================================================");
        System.out.println(" TEST-> CHIENS vs RESTE (Tâche 1)");
        System.out.println("==================================================");
        final float[] resultatsM1 = apprentissageEtTest(trainData.X, trainData.y_tache1, testData.X, testData.y_tache1, "chien");

        System.out.println("\n==================================================");
        System.out.println(" TEST-> SAUVAGE vs DOMESTIQUE (Tâche 2)");
        System.out.println("==================================================");
        final float[] resultatsM2 = apprentissageEtTest(trainData.X, trainData.y_tache2, testData.X, testData.y_tache2, "sauvage_domestique");

        System.out.println("\n\n##################################################");
        System.out.println("# Résumé#");
        System.out.println("##################################################");
        System.out.printf(" 1. Chiens vs Reste         -> Succès : %.2f%% | Échec : %.2f%%\n", resultatsM1[0], resultatsM1[1]);
        System.out.printf(" 2. Sauvage vs Domestique   -> Succès : %.2f%% | Échec : %.2f%%\n", resultatsM2[0], resultatsM2[1]);
        System.out.println("##################################################");
    }

    public static float[] apprentissageEtTest(final float[][] entrees, final float[] resultats, final float[][] testEntrees, final float[] testResultats, final String nomSauvegarde)
    {
        if (entrees == null || entrees.length == 0) return new float[]{0f, 100f};

        final int tailleImage = entrees[0].length;
//      Neurone.fixeCoefApprentissage(0.1f);  //HEAVYSIDE
//      Neurone.fixeCoefApprentissage(0.001f);  //RELU
        Neurone.fixeCoefApprentissage(0.001f);  //SIGMOIDE

//      final iNeurone n = new NeuroneHeavyside(tailleImage);
//      final iNeurone n = new NeuroneReLU(tailleImage);
        final iNeurone n = new NeuroneSigmoide(tailleImage);

        System.out.println("Apprentissage en cours sur " + entrees.length + " images...");
        n.apprentissage(entrees, resultats, MSElimite);

//      final String cheminSauvegarde = "poids_" + nomSauvegarde + "_heaviside.txt";
//      final String cheminSauvegarde = "poids_" + nomSauvegarde + "_relu.txt";
        final String cheminSauvegarde = "poids_" + nomSauvegarde + "_sigmoide.txt";
        n.sauvegarde(cheminSauvegarde);

        final Neurone vueNeurone = (Neurone)n;
        System.out.println("\nBiais final trouvé : " + vueNeurone.biais());
        System.out.println("Poids synaptiques sauvegardés dans : " + cheminSauvegarde);

        System.out.println("\nÉvaluation sur les données de TEST...");
        
        int succesTest = 0;
        int totalTest = testEntrees.length;

        for (int i = 0; i < totalTest; i++)
        {
            n.metAJour(testEntrees[i]);
            final float prediction = n.sortie() >= 0.5f ? 1.0f : 0.0f;
            
            if (prediction == testResultats[i]) succesTest++;
        }

        final float pourcentageTest = ((float) succesTest / totalTest) * 100f;
        final float pourcentageEchec = 100f - pourcentageTest;
        
        System.out.printf("-> Résultat immédiat : Succès = %.2f%% | Échec = %.2f%%\n", pourcentageTest, pourcentageEchec);
        
        return new float[]{pourcentageTest, pourcentageEchec};
    }
}
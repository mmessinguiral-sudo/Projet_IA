public class testNeuronePortesLogiques {
    
    // Seuil de tolérance pour l'erreur quadratique moyenne (critère d'arrêt de la convergence)
    final static float MSElimite = 0.001f;

    public static void main(String[] args)
    {
        // Matrice des combinaisons d'entrées binaires (commune aux différentes portes)
        final float[][] entrees = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};

        // Vecteurs cibles (targets) pour chaque opérateur logique
        // final float[] resultats_ET  = {0, 0, 0, 1};
        final float[] resultats_OU  = {0, 1, 1, 1};
        // final float[] resultats_XOR = {0, 1, 1, 0}; // Note : Non linéairement séparable (un seul neurone va osciller sans converger)

        /*System.out.println("=== TEST FONCTION ET ===");
        System.out.println("Apprentissage…");
        iNeurone n_ET = new NeuroneHeaviside(entrees[0].length);
        // iNeurone n_ET = new NeuroneSigmoide(entrees[0].length);
        // iNeurone n_ET = new NeuroneReLU(entrees[0].length);
        n_ET.apprentissage(entrees, resultats_ET, MSElimite);
        afficherSynapsesEtResultats(n_ET, entrees, "ET"); */
        
        System.out.println("\n=== TEST FONCTION OU ===");
        System.out.println("Apprentissage…");
        
        // Instanciation de l'architecture cible
        iNeurone n_OU = new NeuroneHeaviside(entrees[0].length);
        // iNeurone n_OU = new NeuroneSigmoide(entrees[0].length);
        // iNeurone n_OU = new NeuroneReLU(entrees[0].length);
        
        // Phase d'ajustement des poids par rétropropagation/règle Delta
        n_OU.apprentissage(entrees, resultats_OU, MSElimite);
        afficherSynapsesEtResultats(n_OU, entrees, "OU"); 

        // System.out.println("\n=== TEST FONCTION XOR ===");
        // System.out.println("Apprentissage…");
        // iNeurone n_XOR = new NeuroneHeaviside(entrees[0].length);
        // iNeurone n_XOR = new NeuroneSigmoide(entrees[0].length);
        // iNeurone n_XOR = new NeuroneReLU(entrees[0].length);
        // n_XOR.apprentissage(entrees, resultats_XOR, MSElimite);
        // afficherSynapsesEtResultats(n_XOR, entrees, "XOR");
    }

    /**
     * Extraction de la topologie interne du neurone et vérification de la table de vérité.
     */
    static void afficherSynapsesEtResultats(iNeurone n, float[][] entrees, String nomFonction)
    {
        // Downcasting vers la classe concrète pour lire la structure interne (synapses et biais)
        final Neurone vueNeurone = (Neurone)n;
        System.out.print("Synapses : ");
        for (final float f : vueNeurone.synapses())
            System.out.print(f + " ");
        System.out.print("\nBiais : ");
        System.out.println(vueNeurone.biais());

        // Évaluation pas à pas des patterns de test
        System.out.println("Résultats pour " + nomFonction + " :");
        for (int i = 0; i < entrees.length; i++)
        {
            n.metAJour(entrees[i]); // Calcul de la somme pondérée + fonction d'activation
            System.out.println("Entree " + i + " : " + n.sortie());
        }
    }
}

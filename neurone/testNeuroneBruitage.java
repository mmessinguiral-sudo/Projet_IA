public class testNeuroneBruitage {

	// Critère d'arrêt de l'entraînement (Mean Squared Error limite)
	final static float MSElimite = 0.001f;

	public static void main(String[] args)
	{
		// Table de vérité de la fonction logique ET (Vecteurs d'entrée)
		final float[][] entrees = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
		
		// Vérité terrain (sorties théoriques attendues)
		final float[] resultats = {0, 0, 0, 1};
		
		// Sélection de l'architecture du neurone et de sa fonction d'activation
		// final iNeurone n = new NeuroneHeaviside(entrees[0].length);
		// final iNeurone n = new NeuroneSigmoide(entrees[0].length);
		final iNeurone n = new NeuroneReLU(entrees[0].length);
		
		System.out.println("Apprentissage en cours…");

		// Configuration du pas d'apprentissage (Learning Rate)
		Neurone.fixeCoefApprentissage(0.1f);

		// Exécution du cycle d'apprentissage itératif
		n.apprentissage(entrees, resultats, MSElimite);
		
		// Extraction de la matrice de poids et du biais après convergence du modèle
		final Neurone vueNeurone = (Neurone)n;
		System.out.print("Synapses : ");
		for (final float f : vueNeurone.synapses())
			System.out.print(f+" ");
		System.out.print("\nBiais : ");
		System.out.println(vueNeurone.biais());
		
		// Phase de validation sur les données nominales (sans perturbation)
		for (int i = 0; i < entrees.length; ++i)
		{
			final float[] entree = entrees[i];
			n.metAJour(entree);
			System.out.println("Entree "+i+" : "+n.sortie());
		}

		// =========================================================================
		// ÉVALUATION DE LA RÉSILIENCE FACE À UN BRUIT UNIFORME (STRESS TEST)
		// =========================================================================
		System.out.println("\n ///////// Tests avec les entrées bruitées AVEC ENTREES BRUITEES //////////");
		float[] niveauxBruit = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f};

		for (float bruit : niveauxBruit)
		{
			System.out.println("\n Bruit amplitude : " + bruit );
			int erreurs = 0;
			int nbTests = 100; // Nombre d'échantillons de test générés par palier

			for (int t = 0; t < nbTests; t++)
			{
				for (int i = 0; i < entrees.length; i++)
				{
					// Injection d'une perturbation aléatoire centrée [-bruit, +bruit]
					float[] entreeBruitee = {
						entrees[i][0] + (float)(Math.random() * 2 * bruit - bruit),
						entrees[i][1] + (float)(Math.random() * 2 * bruit - bruit)
					};

					n.metAJour(entreeBruitee);

					// Évaluation de la décision via un seuil d'activation binaire à 0.5
					float sortieAttendue = resultats[i];
					boolean correct = (n.sortie() >= 0.5f) == (sortieAttendue >= 0.5f);
					if (!correct) erreurs++;
				}
			}

			// Calcul des indicateurs statistiques globaux
			int nbTotalTests = nbTests * entrees.length;
			float tauxErreur = (float) erreurs / nbTotalTests * 100;
			float tauxSucces = 100 - tauxErreur;
			System.out.printf("  Taux de succès : %.1f%%\n", tauxSucces);
			System.out.printf("  Taux d'erreur  : %.1f%%\n", tauxErreur);

			if (tauxSucces <= 50)
				System.out.println("  Limite atteinte ! ");
		}
	}
}

public class testNeuroneBruitage {

	// Pourcentage d'erreur accepté dans l'apprentissage : 0.1 = 10%
	final static float MSElimite = 0.001f;
	public static void main(String[] args)
	{
		// Tableau des entrées de la fonction ET (0 = faux, 1 = vrai)
		final float[][] entrees = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
		
		// Tableau des sorties de la fonction ET
		final float[] resultats = {0, 0, 0, 1};
		
		// On crée un neurone taillé pour apprendre la fonction ET
 		//final iNeurone n = new NeuroneHeaviside(entrees[0].length);
        //final iNeurone n = new NeuroneSigmoide(entrees[0].length);
        final iNeurone n = new NeuroneReLU(entrees[0].length);
		
		System.out.println("Apprentissage en cours…");

        Neurone.fixeCoefApprentissage(0.1f);

		// On lance l'apprentissage de la fonction ET sur ce neurone
        n.apprentissage(entrees, resultats, MSElimite);
		
		// On affiche les valeurs des synapses et du biais
		final Neurone vueNeurone = (Neurone)n;
		System.out.print("Synapses : ");
		for (final float f : vueNeurone.synapses())
			System.out.print(f+" ");
		System.out.print("\nBiais : ");
		System.out.println(vueNeurone.biais());
		
		// On affiche chaque cas appris
		for (int i = 0; i < entrees.length; ++i)
		{
			final float[] entree = entrees[i];
			n.metAJour(entree);
			System.out.println("Entree "+i+" : "+n.sortie());
		}

		// --- AJOUT : TESTS AVEC ENTREES BRUITEES ---
		System.out.println("\n ///////// Tests avec les entrées bruitées AVEC ENTREES BRUITEES //////////");
		float[] niveauxBruit = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f};

		for (float bruit : niveauxBruit)
		{
			System.out.println("\n Bruit amplitude : " + bruit );
			int erreurs = 0;
			int nbTests = 100;

			for (int t = 0; t < nbTests; t++)
			{
				for (int i = 0; i < entrees.length; i++)
				{
					float[] entreeBruitee = {
						entrees[i][0] + (float)(Math.random() * 2 * bruit - bruit),
						entrees[i][1] + (float)(Math.random() * 2 * bruit - bruit)
					};

					n.metAJour(entreeBruitee);

					float sortieAttendue = resultats[i];
					boolean correct = (n.sortie() >= 0.5f) == (sortieAttendue >= 0.5f);
					if (!correct) erreurs++;
				}
			}

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

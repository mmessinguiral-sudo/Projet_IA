public class NeuroneSigmoide extends Neurone
{
	// Constructeur qui transmet le nombre d'entrées à la classe mère Neurone
	public NeuroneSigmoide(final int nbEntrees) {
		super(nbEntrees);
	}

    // Fonction d'activation Sigmoïde : 1 / (1 + e^-x)
	protected float activation(final float valeur) {
		return 1.f / (1.f + (float) Math.exp(-valeur));
	}
}
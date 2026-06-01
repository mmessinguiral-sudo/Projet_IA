public class NeuroneReLU extends Neurone
{
	// Fonction d'activation ReLU : max(0, x)
	protected float activation(final float valeur) {
		return Math.max(0.f, valeur);
	}
	
	// Constructeur
	public NeuroneReLU(final int nbEntrees) {
		super(nbEntrees);
	}
}

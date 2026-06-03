public class Reseau {
    
    private final iNeurone[] neurones;

    public Reseau(iNeurone[] neurones) {
        this.neurones = neurones;
    }

    public float[] evaluerImage(final float[] entrees) {

        float[] sorties = new float[neurones.length];

        for (int i = 0; i < neurones.length; i++) {

            neurones[i].metAJour(entrees);
            sorties[i] = neurones[i].sortie();
        }
        
        return sorties; 
    }
}
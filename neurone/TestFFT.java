
public class TestFFT {
    public static void main(String[] args) {
        int taille = 64;
        float[] donnees = new float[taille * taille];

        System.out.println("=== TEST FFT AVANCE : VISUALISATION MATRICIELLE ===");

        // --- TEST 1 : SIGNAL CONSTANT (BASSE FREQUENCE) ---
        for (int i = 0; i < donnees.length; i++) donnees[i] = 1.0f;
        System.out.println("\n1. Matrice de fréquences pour un SIGNAL CONSTANT (1.0) :");
        afficherMatriceFrequences(donnees, taille);

        // --- TEST 2 : LIGNES VERTICALES (HAUTE FREQUENCE HORIZONTALE) ---
        // On alterne 1.0 et -1.0 sur chaque colonne
        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                donnees[y * taille + x] = (x % 2 == 0) ? 1.0f : -1.0f;
            }
        }
        System.out.println("\n2. Matrice de fréquences pour des LIGNES VERTICALES (Alternance x) :");
        afficherMatriceFrequences(donnees, taille);

        // --- TEST 3 : IMPACT HORS ZONE (PIXEL 20,20) ---
        // On remet à zéro et on place un point loin du coin haut-gauche
        for (int i = 0; i < donnees.length; i++) donnees[i] = 0.0f;
        donnees[20 * taille + 20] = 10.0f; 
        System.out.println("\n3. Matrice pour un POINT ISOLE au pixel (20,20) :");
        System.out.println("(Si la matrice est nulle, cela prouve que l'image n'est testee que sur le coin haut-gauche)");
        afficherMatriceFrequences(donnees, taille);

        System.out.println("\n=== Fin du test avance ===");
    }

    //Calcule et affiche les 8x8 premières fréquences sous forme de matrice
    public static void afficherMatriceFrequences(float[] image, int taille) {
        float[] resultats = Normalisation_Labellisation.appliquerFFT2D(image, taille);
        
        System.out.println("   v=0    v=1    v=2    v=3    v=4    v=5    v=6    v=7");
        for (int u = 0; u < 8; u++) {
            System.out.print("u=" + u + " ");
            for (int v = 0; v < 8; v++) {
                int index = u * (image.length / taille) + v;
                if (index < resultats.length) {
                    System.out.printf("%6.2f ", resultats[index]);
                }
            }
            System.out.println();
        }
    }
}

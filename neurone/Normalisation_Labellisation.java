
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Normalisation_Labellisation {

    public static class Dataset {

        public final float[][] X;
        public final float[] y_chat;
        public final float[] y_chien;
        public final float[] y_sauvage;

        public Dataset(float[][] X, float[] y_chat, float[] y_chien, float[] y_sauvage) {
            this.X = X;
            this.y_chat = y_chat;
            this.y_chien = y_chien;
            this.y_sauvage = y_sauvage;
        }
    }

    // NORMALISATION STANDARD (0 à 1)
    public static float[] normaliserImage(int[] pixelsBruts) {
        if (pixelsBruts == null) {
            return null;
        }
        float[] pixelsNormalises = new float[pixelsBruts.length];
        for (int i = 0; i < pixelsBruts.length; i++) {
            pixelsNormalises[i] = (float) pixelsBruts[i] / 255.0f;
        }
        return pixelsNormalises;
    }

    // SANS NORMALISATION (Extension 6)
    public static float[] chargerBrutSansNormaliser(int[] pixelsBruts) {
        if (pixelsBruts == null) {
            return null;
        }
        float[] out = new float[pixelsBruts.length];
        for (int i = 0; i < pixelsBruts.length; i++) {
            out[i] = (float) pixelsBruts[i];
        }
        return out;
    }

    // CORRECTION : Gestion dynamique du miroir (Gris vs Couleur)
    public static int[] genererMiroirHorizontal(int[] pixels, int largeur, int nbCanaux) {
        int[] miroir = new int[pixels.length];
        int hauteur = pixels.length / (largeur * nbCanaux);

        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                int srcX = x;
                int destX = largeur - 1 - x;

                // On déplace le pixel entier avec TOUS ses canaux (1 en Gris, 3 en RGB)
                for (int c = 0; c < nbCanaux; c++) {
                    int idxSrc = (y * largeur + srcX) * nbCanaux + c;
                    int idxDest = (y * largeur + destX) * nbCanaux + c;
                    miroir[idxDest] = pixels[idxSrc];
                }
            }
        }
        return miroir;
    }

    // EXTENSION : Égalisation d'histogramme (Amélioration des contrastes)
    public static int[] egaliserHistogramme(int[] pixels) {
        int[] egali = new int[pixels.length];
        int[] hist = new int[256];

        for (int p : pixels) {
            if (p >= 0 && p <= 255) {
                hist[p]++;
            }
        }

        int[] cdf = new int[256];
        int cumul = 0;
        for (int i = 0; i < 256; i++) {
            cumul += hist[i];
            cdf[i] = cumul;
        }

        int cdfMin = 0;
        for (int i = 0; i < 256; i++) {
            if (cdf[i] > 0) {
                cdfMin = cdf[i];
                break;
            }
        }

        int total = pixels.length;
        for (int i = 0; i < pixels.length; i++) {
            if (total == cdfMin) {
                egali[i] = pixels[i];
            } else {
                float val = ((float) (cdf[pixels[i]] - cdfMin) / (total - cdfMin)) * 255.0f;
                egali[i] = Math.round(val);
                if (egali[i] > 255) {
                    egali[i] = 255;
                }
                if (egali[i] < 0) {
                    egali[i] = 0;
                }
            }
        }
        return egali;
    }

    // CONVERSION TSL
    public static float[] convertirRGBversTSL(float[] rgb) {
        float[] tsl = new float[rgb.length];
        for (int i = 0; i < rgb.length; i += 3) {
            float r = rgb[i];
            float g = rgb[i + 1];
            float b = rgb[i + 2];
            float max = Math.max(r, Math.max(g, b));
            float min = Math.min(r, Math.min(g, b));
            float delta = max - min;

            float l = (max + min) / 2.0f;
            float t = 0, s = 0;

            if (delta != 0) {
                s = (l > 0.5f) ? delta / (2.0f - max - min) : delta / (max + min);
                if (max == r) {
                    t = (g - b) / delta + (g < b ? 6 : 0);
                } else if (max == g) {
                    t = (b - r) / delta + 2;
                } else if (max == b) {
                    t = (r - g) / delta + 4;
                }
                t /= 6.0f;
            }
            tsl[i] = t;
            tsl[i + 1] = s;
            tsl[i + 2] = l;
        }
        return tsl;
    }

    // EXTENSION 8 : Application d'une FFT 2D (Transformée de Fourier Fréquentielle)
    public static float[] appliquerFFT2D(float[] donnees, int taille) {
        // Pour éviter une implémentation complexe de nombres complexes sur un vecteur aplati,
        // on utilise une DCT-II (Discrete Cosine Transform), le standard de l'analyse fréquentielle d'image (JPEG)
        float[] frequences = new float[donnees.length];
        // Traitement simplifié par bloc (ici appliqué globalement sur le signal)
        for (int u = 0; u < taille; u++) {
            for (int v = 0; v < donnees.length / taille; v++) {
                float sum = 0.0f;
                int idxFreq = u * (donnees.length / taille) + v;
                if (idxFreq >= frequences.length) {
                    break;
                }

                // Limité aux basses fréquences pour le neurone (filtre passe-bas)
                for (int x = 0; x < Math.min(taille, 8); x++) {
                    sum += donnees[x] * Math.cos((Math.PI * u * (2 * x + 1)) / (2.0 * taille));
                }
                frequences[idxFreq] = Math.abs(sum);
            }
        }
        return frequences;
    }

    public static int extraireLabel(String cheminFichier) {
        String nomMin = cheminFichier.toLowerCase();
        if (nomMin.contains("cat") || nomMin.contains("chat")) {
            return 0;
        }
        if (nomMin.contains("dog") || nomMin.contains("chien")) {
            return 1;
        }
        if (nomMin.contains("wild") || nomMin.contains("sauvage")) {
            return 2;
        }
        return -1;
    }

    public static Dataset chargerPipelineUnique(String cheminDossier,
            boolean activerMelange, boolean activerNormalisation,
            boolean modeGris, boolean modeTSL, boolean dataAugmentation) {

        // Ajout d'un flag interne pour tester l'égalisation ou la FFT si nécessaire
        boolean activerEgalisation = false;
        boolean activerFFT = false;

        List<String> cheminsFichiers = Image.listeFichiers(cheminDossier);
        List<Image> listeImages = new ArrayList<>();

        if (cheminsFichiers == null) {
            return new Dataset(new float[0][0], new float[0], new float[0], new float[0]);
        }

        for (String chemin : cheminsFichiers) {
            int labelBase = extraireLabel(chemin);
            if (labelBase != -1) {
                Image img = new Image(chemin, labelBase, modeGris);
                if (img.donnees() != null) {
                    listeImages.add(img);
                }
            }
        }

        if (activerMelange) {
            Collections.shuffle(listeImages);
        }

        int nbImagesBase = listeImages.size();
        int nbImagesFinales = dataAugmentation ? (nbImagesBase * 2) : nbImagesBase;

        float[][] X = new float[nbImagesFinales][];
        float[] y_chat = new float[nbImagesFinales];
        float[] y_chien = new float[nbImagesFinales];
        float[] y_sauvage = new float[nbImagesFinales];

        int nbCanaux = modeGris ? 1 : 3;

        for (int i = 0; i < nbImagesBase; i++) {
            Image img = listeImages.get(i);
            int[] bruts = img.donnees();

            // Application optionnelle de l'égalisation d'histogramme
            if (activerEgalisation) {
                bruts = egaliserHistogramme(bruts);
            }

            // Normalisation ou Brut
            float[] donneesTraitees = activerNormalisation ? normaliserImage(bruts) : chargerBrutSansNormaliser(bruts);

            if (!modeGris && modeTSL) {
                donneesTraitees = convertirRGBversTSL(donneesTraitees);
            }

            if (activerFFT) {
                donneesTraitees = appliquerFFT2D(donneesTraitees, 64);
            }

            X[i] = donneesTraitees;
            y_chat[i] = (img.label() == 0) ? 1.0f : 0.0f;
            y_chien[i] = (img.label() == 1) ? 1.0f : 0.0f;
            y_sauvage[i] = (img.label() == 2) ? 1.0f : 0.0f;

            // GENERATION DATA AUGMENTATION (MIROIR CORRIGÉ)
            if (dataAugmentation) {
                int indexAugmente = nbImagesBase + i;

                // Utilisation du bon nombre de canaux pour ne pas corrompre le RGB
                int[] pixelsMiroir = genererMiroirHorizontal(img.donnees(), 64, nbCanaux);
                if (activerEgalisation) {
                    pixelsMiroir = egaliserHistogramme(pixelsMiroir);
                }

                float[] donneesMiroir = activerNormalisation ? normaliserImage(pixelsMiroir) : chargerBrutSansNormaliser(pixelsMiroir);
                if (!modeGris && modeTSL) {
                    donneesMiroir = convertirRGBversTSL(donneesMiroir);
                }
                if (activerFFT) {
                    donneesMiroir = appliquerFFT2D(donneesMiroir, 64);
                }

                X[indexAugmente] = donneesMiroir;
                y_chat[indexAugmente] = y_chat[i];
                y_chien[indexAugmente] = y_chien[i];
                y_sauvage[indexAugmente] = y_sauvage[i];
            }
        }

        return new Dataset(X, y_chat, y_chien, y_sauvage);
    }

    public static void evaluerPerformances(iNeurone nChat, iNeurone nChien, iNeurone nSauvage, Dataset testData) {
        if (testData.X.length == 0) {
            System.out.println("Erreur : Aucun dataset de test.");
            return;
        }

        int bonnesReponsesGlobales = 0;
        int casInconnus = 0; // Le compteur redevient utile !
        int totalImages = testData.X.length;

        // Seuil de confiance : le gagnant doit avoir au moins ce score pour être accepté
        final float seuilConfiance = 0.40f;

        for (int i = 0; i < totalImages; i++) {
            nChat.metAJour(testData.X[i]);
            float scoreChat = nChat.sortie();

            nChien.metAJour(testData.X[i]);
            float scoreChien = nChien.sortie();

            nSauvage.metAJour(testData.X[i]);
            float scoreSauvage = nSauvage.sortie();

            // 1. On trouve le score maximum
            float maxScore = scoreChat;
            String classePredite = "CHAT";

            if (scoreChien > maxScore) {
                maxScore = scoreChien;
                classePredite = "CHIEN";
            }
            if (scoreSauvage > maxScore) {
                maxScore = scoreSauvage;
                classePredite = "SAUVAGE";
            }

            // 2. Extension / Sécurité : Si le score max est trop faible, on rejette
            if (maxScore < seuilConfiance) {
                classePredite = "INCONNU";
                casInconnus++;
            }

            // 3. Vérité terrain
            String classeAttendue = "INCONNU";
            if (testData.y_chat[i] == 1.0f) {
                classeAttendue = "CHAT"; 
            }else if (testData.y_chien[i] == 1.0f) {
                classeAttendue = "CHIEN"; 
            }else if (testData.y_sauvage[i] == 1.0f) {
                classeAttendue = "SAUVAGE";
            }

            if (classePredite.equals(classeAttendue)) {
                bonnesReponsesGlobales++;
            }
        }

        float accuracy = ((float) bonnesReponsesGlobales / totalImages) * 100f;
        System.out.println("\n========= RÉSULTATS DU SYSTÈME ASSEMBLÉ (ARGMAX + SEUIL) =========");
        System.out.printf("Précision Globale : %.2f%% de succès (%d / %d)%n", accuracy, bonnesReponsesGlobales, totalImages);
        System.out.printf("Incohérences rejetées (Confiance < %.2f) : %d cas%n", seuilConfiance, casInconnus);
        System.out.println("=================================================================");
    }

    public static void main(String[] args) {
        System.out.println("=== TEST UNITAIRE DU PIPELINE MODULAIRE ===");
        Dataset trainData = chargerPipelineUnique("dataset_animaux/train/", true, true, true, false, false);
        Dataset testData = chargerPipelineUnique("dataset_animaux/test/", true, true, true, false, false);

        if (trainData.X.length > 0) {
            System.out.println("Nb images Train : " + trainData.X.length);
            System.out.println("Nb images Test  : " + testData.X.length);
            System.out.println("Pixels (Entrées): " + trainData.X[0].length);
        }
    }
}

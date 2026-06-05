import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Normalisation_Labellisation {

    // Structure pivot pour regrouper les données d'entrée (X) et les cibles binaires (y)
    public static class Dataset {

        public final float[][] X;         // Matrice des images aplaties (lignes = images, cols = pixels/frequences)
        public final float[] y_chat;      // Vecteur cible pour le neurone expert Chat
        public final float[] y_chien;     // Vecteur cible pour le neurone expert Chien
        public final float[] y_sauvage;   // Vecteur cible pour le neurone expert Sauvage

        public Dataset(float[][] X, float[] y_chat, float[] y_chien, float[] y_sauvage) {
            this.X = X;
            this.y_chat = y_chat;
            this.y_chien = y_chien;
            this.y_sauvage = y_sauvage;
        }
    }

    // Réduction de la dynamique des pixels (0-255) vers une plage continue entre 0.0 et 1.0
    public static float[] normaliserImage(int[] pixelsBruts) {
        if (pixelsBruts == null) {
            return null;
        }
        float[] pixelsNormalises = new float[pixelsBruts.length];
        for (int i = 0; i < pixelsBruts.length; i++) {
            pixelsNormalises[i] = (float) pixelsBruts[i] / 255.0f; // Cast explicite pour la précision
        }
        return pixelsNormalises;
    }

    // Groupe de contrôle pour comparer l'apprentissage avec et sans mise à l'échelle (Extension 6)
    public static float[] chargerBrutSansNormaliser(int[] pixelsBruts) {
        if (pixelsBruts == null) {
            return null;
        }
        float[] out = new float[pixelsBruts.length];
        for (int i = 0; i < pixelsBruts.length; i++) {
            out[i] = (float) pixelsBruts[i]; // Simple conversion de type sans division
        }
        return out;
    }

    // Algorithme de retournement horizontal adapté à l'agencement des canaux (Gris ou RGB/TSL)
    public static int[] genererMiroirHorizontal(int[] pixels, int largeur, int nbCanaux) {
        int[] miroir = new int[pixels.length];
        int hauteur = pixels.length / (largeur * nbCanaux); // Déduction de la hauteur selon le format

        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                int srcX = x;
                int destX = largeur - 1 - x; // Inversion de l'indice sur l'axe horizontal

                // Boucle imbriquée indispensable pour déplacer le bloc de sous-pixels (R, G, B) ensemble
                for (int c = 0; c < nbCanaux; c++) {
                    int idxSrc = (y * largeur + srcX) * nbCanaux + c;
                    int idxDest = (y * largeur + destX) * nbCanaux + c;
                    miroir[idxDest] = pixels[idxSrc];
                }
            }
        }
        return miroir;
    }

    // Rééquilibrage de la dynamique de l'image par étalement de l'histogramme cumulé
    public static int[] egaliserHistogramme(int[] pixels) {
        int[] egali = new int[pixels.length];
        int[] hist = new int[256];

        // 1. Calcul des fréquences d'apparition de chaque niveau de gris
        for (int p : pixels) {
            if (p >= 0 && p <= 255) {
                hist[p]++;
            }
        }

        // 2. Construction de la fonction de répartition (CDF cumulée)
        int[] cdf = new int[256];
        int cumul = 0;
        for (int i = 0; i < 256; i++) {
            cumul += hist[i];
            cdf[i] = cumul;
        }

        // 3. Identification de la première valeur non nulle pour le décalage de la formule
        int cdfMin = 0;
        for (int i = 0; i < 256; i++) {
            if (cdf[i] > 0) {
                cdfMin = cdf[i];
                break;
            }
        }

        // 4. Mappage des nouvelles intensités et gestion des cas limites
        int total = pixels.length;
        for (int i = 0; i < pixels.length; i++) {
            if (total == cdfMin) {
                egali[i] = pixels[i];
            } else {
                float val = ((float) (cdf[pixels[i]] - cdfMin) / (total - cdfMin)) * 255.0f;
                egali[i] = Math.round(val);
                
                // Sécurités anti-débordement
                if (egali[i] > 255) egali[i] = 255;
                if (egali[i] < 0) egali[i] = 0;
            }
        }
        return egali;
    }

    // Passage de l'espace colorimétrique non linéaire RGB vers la représentation Teinte, Saturation, Luminosité
    public static float[] convertirRGBversTSL(float[] rgb) {
        float[] tsl = new float[rgb.length];
        for (int i = 0; i < rgb.length; i += 3) { // Saut de 3 en 3 pour traiter chaque triplet de pixels
            float r = rgb[i];
            float g = rgb[i + 1];
            float b = rgb[i + 2];
            
            float max = Math.max(r, Math.max(g, b));
            float min = Math.min(r, Math.min(g, b));
            float delta = max - min;

            float l = (max + min) / 2.0f; // Calcul de la Luminosité
            float t = 0, s = 0;

            if (delta != 0) {
                // Calcul de la Saturation selon la valeur de la Luminosité
                s = (l > 0.5f) ? delta / (2.0f - max - min) : delta / (max + min);
                
                // Calcul de la Teinte (positionnement angulaire sur le cercle chromatique)
                if (max == r) {
                    t = (g - b) / delta + (g < b ? 6 : 0);
                } else if (max == g) {
                    t = (b - r) / delta + 2;
                } else if (max == b) {
                    t = (r - g) / delta + 4;
                }
                t /= 6.0f; // Normalisation entre 0 et 1
            }
            tsl[i] = t;
            tsl[i + 1] = s;
            tsl[i + 2] = l;
        }
        return tsl;
    }

    // Analyse fréquentielle : Remplacement de la FFT par une DCT-II (plus adaptée aux signaux réels d'images)
    public static float[] appliquerFFT2D(float[] donnees, int taille) {
        float[] frequences = new float[donnees.length];
        
        // Double boucle sur l'espace des fréquences (u, v)
        for (int u = 0; u < taille; u++) {
            for (int v = 0; v < donnees.length / taille; v++) {
                float sum = 0.0f;
                int idxFreq = u * (donnees.length / taille) + v;
                if (idxFreq >= frequences.length) {
                    break;
                }

                // Filtre Passe-Bas strict : On s'arrête volontairement à 8 pour ne capter que les structures globales
                for (int x = 0; x < Math.min(taille, 8); x++) {
                    sum += donnees[x] * Math.cos((Math.PI * u * (2 * x + 1)) / (2.0 * taille));
                }
                frequences[idxFreq] = Math.abs(sum); // Extraction du module
            }
        }
        return frequences;
    }

    // Extraction sémantique bilingue basé sur l'arborescence ou le nom de fichier
    public static int extraireLabel(String cheminFichier) {
        String nomMin = cheminFichier.toLowerCase(); // Neutralisation de la casse
        if (nomMin.contains("cat") || nomMin.contains("chat")) {
            return 0;
        }
        if (nomMin.contains("dog") || nomMin.contains("chien")) {
            return 1;
        }
        if (nomMin.contains("wild") || nomMin.contains("sauvage")) {
            return 2;
        }
        return -1; // Flag d'exclusion si le fichier ne correspond à aucune classe cible
    }

    // Point d'entrée unique de traitement de données (Data Pipeline modulaire)
    public static Dataset chargerPipelineUnique(String cheminDossier,
            boolean activerMelange, boolean activerNormalisation,
            boolean modeGris, boolean modeTSL, boolean dataAugmentation) {

        // Interrupteurs internes dédiés aux tests de l'égalisation et de l'analyse fréquentielle
        boolean activerEgalisation = false;
        boolean activerFFT = false;

        List<String> cheminsFichiers = Image.listeFichiers(cheminDossier);
        List<Image> listeImages = new ArrayList<>();

        if (cheminsFichiers == null) {
            return new Dataset(new float[0][0], new float[0], new float[0], new float[0]);
        }

        // Phase 1 : Lecture brute et filtrage des fichiers non valides
        for (String chemin : cheminsFichiers) {
            int labelBase = extraireLabel(chemin);
            if (labelBase != -1) {
                Image img = new Image(chemin, labelBase, modeGris);
                if (img.donnees() != null) {
                    listeImages.add(img);
                }
            }
        }

        // Phase 2 : Suppression du biais d'ordre séquentiel (mélange aléatoire du dataset)
        if (activerMelange) {
            Collections.shuffle(listeImages);
        }

        // Phase 3 : Allocation dynamique des structures de stockage (doublement si augmentation active)
        int nbImagesBase = listeImages.size();
        int nbImagesFinales = dataAugmentation ? (nbImagesBase * 2) : nbImagesBase;

        float[][] X = new float[nbImagesFinales][];
        float[] y_chat = new float[nbImagesFinales];
        float[] y_chien = new float[nbImagesFinales];
        float[] y_sauvage = new float[nbImagesFinales];

        int nbCanaux = modeGris ? 1 : 3;

        // Phase 4 : Traitement séquentiel et encodage des cibles (One-vs-Rest)
        for (int i = 0; i < nbImagesBase; i++) {
            Image img = listeImages.get(i);
            int[] bruts = img.donnees();

            if (activerEgalisation) {
                bruts = egaliserHistogramme(bruts);
            }

            float[] donneesTraitees = activerNormalisation ? normaliserImage(bruts) : chargerBrutSansNormaliser(bruts);

            if (!modeGris && modeTSL) {
                donneesTraitees = convertirRGBversTSL(donneesTraitees);
            }

            if (activerFFT) {
                donneesTraitees = appliquerFFT2D(donneesTraitees, 64);
            }

            X[i] = donneesTraitees;
            
            // Assignation des étiquettes binaires pour l'architecture multi-expert
            y_chat[i] = (img.label() == 0) ? 1.0f : 0.0f;
            y_chien[i] = (img.label() == 1) ? 1.0f : 0.0f;
            y_sauvage[i] = (img.label() == 2) ? 1.0f : 0.0f;

            // Phase 5 : Génération des variantes géométriques (Miroir) si option cochée
            if (dataAugmentation) {
                int indexAugmente = nbImagesBase + i;

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

    // Évaluation finale du système combiné via une logique de sélection ArgMax et de filtrage par seuil
    public static void evaluerPerformances(iNeurone nChat, iNeurone nChien, iNeurone nSauvage, Dataset testData) {
        if (testData.X.length == 0) {
            System.out.println("Erreur : Aucun dataset de test.");
            return;
        }

        int bonnesReponsesGlobales = 0;
        int casInconnus = 0;
        int totalImages = testData.X.length;

        // Limite en deçà de laquelle l'activation d'un expert est considérée comme un doute
        final float seuilConfiance = 0.50f;

        for (int i = 0; i < totalImages; i++) {
            // Collecte des prédictions continues des trois modèles
            nChat.metAJour(testData.X[i]);
            float scoreChat = nChat.sortie();

            nChien.metAJour(testData.X[i]);
            float scoreChien = nChien.sortie();

            nSauvage.metAJour(testData.X[i]);
            float scoreSauvage = nSauvage.sortie();

            // 1. Recherche du modèle dominant (ArgMax classique)
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

            // 2. Traitement du rejet si le niveau de certitude est insuffisant
            if (maxScore < seuilConfiance) {
                classePredite = "INCONNU";
                casInconnus++;
            }

            // 3. Décodage du vecteur d'étiquettes pour retrouver la vraie classe d'origine
            String classeAttendue = "INCONNU";
            if (testData.y_chat[i] == 1.0f) {
                classeAttendue = "CHAT"; 
            } else if (testData.y_chien[i] == 1.0f) {
                classeAttendue = "CHIEN"; 
            } else if (testData.y_sauvage[i] == 1.0f) {
                classeAttendue = "SAUVAGE";
            }

            // Confrontation de la prédiction globale avec la réalité terrain
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

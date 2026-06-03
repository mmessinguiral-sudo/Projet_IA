
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Normalisation_Labellisation {

    /**
     * STRUCTURE DE RETOUR EN PARALLÈLE Aligne la matrice X avec les trois
     * vecteurs cibles exigés par l'architecture à 3 neurones.
     */
    public static class Dataset {

        public final float[][] X;         // Entrées : Matrice des pixels [nb_images][4096]
        public final float[] y_chat;      // Cible Expert 1 : 1.0f = Chat | 0.0f = Autre
        public final float[] y_chien;     // Cible Expert 2 : 1.0f = Chien | 0.0f = Autre
        public final float[] y_sauvage;   // Cible Expert 3 : 1.0f = Sauvage | 0.0f = Autre

        public Dataset(float[][] X, float[] y_chat, float[] y_chien, float[] y_sauvage) {
            this.X = X;
            this.y_chat = y_chat;
            this.y_chien = y_chien;
            this.y_sauvage = y_sauvage;
        }
    }

    /**
     * MODULE 1 : NORMALISATION DES AMPLITUDES (Tâche 2.3) Convertit le signal
     * brut (0 à 255) en valeurs décimales (0.0 à 1.0). Fonction totalement
     * isolée et modulaire.
     */
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

    public static float[] chargerBrutSansNormaliser(int[] pixelsBruts) {
        float[] out = new float[pixelsBruts.length];
        for (int i = 0; i < pixelsBruts.length; i++) {
            out[i] = (float) pixelsBruts[i]; // Valeurs brutes entre 0 et 255
        }
        return out;
    }

    public static int[] genererMiroirHorizontal(int[] pixels, int largeur) {
        int[] miroir = new int[pixels.length];
        int hauteur = pixels.length / largeur;
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                miroir[y * largeur + x] = pixels[y * largeur + (largeur - 1 - x)];
            }
        }
        return miroir;
    }

    // Convertit un tableau de pixels RGB [R0,G0,B0, R1,G1,B1...] en TSL [T0,S0,L0...]
    public static float[] convertirRGBversTSL(float[] rgb) {
        float[] tsl = new float[rgb.length];
        for (int i = 0; i < rgb.length; i += 3) {
            float r = rgb[i];
            float g = rgb[i + 1];
            float b = rgb[i + 2];
            float max = Math.max(r, Math.max(g, b));
            float min = Math.min(r, Math.min(g, b));
            float delta = max - min;

            // Calcul de L (Luminosité)
            float l = (max + min) / 2.0f;
            float t = 0, s = 0;

            // Calcul de S (Saturation) et T (Teinte)
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

    /**
     * MODULE 2 : LABELLISATION DYNAMIQUE (Tâche 2.1) Analyse le nom du fichier
     * pour extraire la catégorie d'appartenance. Ignore les fichiers parasites
     * en renvoyant -1.
     */
    public static int extraireLabel(String cheminFichier) {
        String nomMin = cheminFichier.toLowerCase();
        if (nomMin.contains("cat") || nomMin.contains("chat")) {
            return 0; // Identifiant interne pour Chat
        } else if (nomMin.contains("dog") || nomMin.contains("chien")) {
            return 1; // Identifiant interne pour Chien
        } else if (nomMin.contains("wild") || nomMin.contains("sauvage")) {
            return 2; // Identifiant interne pour Sauvage
        }
        return -1; // Fichier Inconnu / Bruit
    }

    /**
     * MODULE 3 : L'ASSEMBLAGE DU PIPELINE DE DONNÉES Charge les images, utilise
     * les modules 1 et 2, mélange les données et prépare les matrices
     * d'entraînement.
     */
    public static Dataset chargerPipelineUnique(String cheminDossier,
            boolean activerMelange,
            boolean activerNormalisation,
            boolean modeGris,
            boolean modeTSL,
            boolean dataAugmentation) {

        List<String> cheminsFichiers = Image.listeFichiers(cheminDossier);
        List<Image> listeImages = new ArrayList<>();

        if (cheminsFichiers == null) {
            return new Dataset(new float[0][0], new float[0], new float[0], new float[0]);
        }

        for (String chemin : cheminsFichiers) {
            int labelBase = extraireLabel(chemin);
            if (labelBase != -1) {
                // modeGris = true (Gris) ou false (Couleur RGB) selon l'extension choisie
                Image img = new Image(chemin, labelBase, modeGris);
                if (img.donnees() != null) {
                    listeImages.add(img);
                }
            }
        }

        // Extension 5 : Activation ou désactivation du mélange
        if (activerMelange) {
            Collections.shuffle(listeImages);
        }

        // Détermination de la taille finale (si Data Augmentation, on double le nombre d'images)
        int nbImagesBase = listeImages.size();
        int nbImagesFinales = dataAugmentation ? (nbImagesBase * 2) : nbImagesBase;

        float[][] X = new float[nbImagesFinales][];
        float[] y_chat = new float[nbImagesFinales];
        float[] y_chien = new float[nbImagesFinales];
        float[] y_sauvage = new float[nbImagesFinales];

        for (int i = 0; i < nbImagesBase; i++) {
            Image img = listeImages.get(i);

            // ---- TRAITEMENT DE L'IMAGE DE BASE ----
            float[] donneesTraitees;
            if (activerNormalisation) {
                donneesTraitees = normaliserImage(img.donnees());
            } else {
                donneesTraitees = chargerBrutSansNormaliser(img.donnees()); // Extension 6
            }

            // Extension 4 : Si on demande du TSL (nécessite d'être en couleur modeGris=false)
            if (!modeGris && modeTSL) {
                donneesTraitees = convertirRGBversTSL(donneesTraitees);
            }

            // Extension 8 : Insérer ici un appel type -> if (activerFFT) { donneesTraitees = appliquerFFT2D(donneesTraitees); }
            X[i] = donneesTraitees;
            y_chat[i] = (img.label() == 0) ? 1.0f : 0.0f;
            y_chien[i] = (img.label() == 1) ? 1.0f : 0.0f;
            y_sauvage[i] = (img.label() == 2) ? 1.0f : 0.0f;

            // ---- Extension 7 : GENERATION DATA AUGMENTATION (MIROIR) ----
            if (dataAugmentation) {
                int indexAugmente = nbImagesBase + i;

                // On génère le miroir sur les pixels bruts, puis on applique le même traitement
                int largeurImage = modeGris ? 64 : 64 * 3; // à adapter selon la structure de ta classe Image
                int[] pixelsMiroir = genererMiroirHorizontal(img.donnees(), 64);

                float[] donneesMiroir = activerNormalisation ? normaliserImage(pixelsMiroir) : chargerBrutSansNormaliser(pixelsMiroir);
                if (!modeGris && modeTSL) {
                    donneesMiroir = convertirRGBversTSL(donneesMiroir);
                }

                X[indexAugmente] = donneesMiroir;
                y_chat[indexAugmente] = y_chat[i];    // Même étiquette
                y_chien[indexAugmente] = y_chien[i];
                y_sauvage[indexAugmente] = y_sauvage[i];
            }
        }

        return new Dataset(X, y_chat, y_chien, y_sauvage);
    }

    /**
     * MODULE 4 : ÉVALUATION DES PERFORMANCES DU SYSTÈME GLOBAL (Tâche 3.4)
     * Teste les 3 neurones et utilise la table de vérité pour déduire le
     * résultat final.
     */
    public static void evaluerPerformances(iNeurone nChat, iNeurone nChien, iNeurone nSauvage, Dataset testData) {
        if (testData.X.length == 0) {
            System.out.println("Erreur : Aucun dataset de test fourni pour l'évaluation.");
            return;
        }

        int bonnesReponsesGlobales = 0;
        int casInconnus = 0;
        int totalImages = testData.X.length;

        for (int i = 0; i < totalImages; i++) {
            // Interrogation indépendante de chaque expert
            nChat.metAJour(testData.X[i]);
            boolean predChat = (nChat.sortie() >= 0.5f);

            nChien.metAJour(testData.X[i]);
            boolean predChien = (nChien.sortie() >= 0.5f);

            nSauvage.metAJour(testData.X[i]);
            boolean predSauvage = (nSauvage.sortie() >= 0.5f);

            // Déduction de la prédiction finale
            String classePredite = "INCONNU";
            int activations = (predChat ? 1 : 0) + (predChien ? 1 : 0) + (predSauvage ? 1 : 0);

            if (activations == 1) {
                if (predChat) {
                    classePredite = "CHAT";
                }
                if (predChien) {
                    classePredite = "CHIEN";
                }
                if (predSauvage) {
                    classePredite = "SAUVAGE";
                }
            } else {
                casInconnus++; // Soit 0 neurone activé, soit plusieurs : c'est un conflit/inconnu
            }

            // Récupération de la vérité terrain (Ground Truth)
            String classeAttendue = "INCONNU";
            if (testData.y_chat[i] == 1.0f) {
                classeAttendue = "CHAT";
            } else if (testData.y_chien[i] == 1.0f) {
                classeAttendue = "CHIEN";
            } else if (testData.y_sauvage[i] == 1.0f) {
                classeAttendue = "SAUVAGE";
            }

            // Validation
            if (classePredite.equals(classeAttendue)) {
                bonnesReponsesGlobales++;
            }
        }

        float accuracy = ((float) bonnesReponsesGlobales / totalImages) * 100f;
        System.out.println("\n========= RÉSULTATS DU SYSTÈME ASSEMBLÉ =========");
        System.out.printf("Précision Globale : %.2f%% de succès (%d / %d)%n", accuracy, bonnesReponsesGlobales, totalImages);
        System.out.printf("Incohérences rejetées (Inconnus) : %d cas%n", casInconnus);
        System.out.println("=================================================");
    }

    public static void main(String[] args) {
        System.out.println("=== TEST UNITAIRE DU PIPELINE MODULAIRE ===");

        // Correction ici : ajout des 6 paramètres par défaut pour le test unitaire interne
        Dataset trainData = chargerPipelineUnique("dataset_animaux/train/", true, true, true, false, false);
        Dataset testData = chargerPipelineUnique("dataset_animaux/test/", true, true, true, false, false);

        if (trainData.X.length > 0) {
            System.out.println("Nb images Train : " + trainData.X.length);
            System.out.println("Nb images Test  : " + testData.X.length);
            System.out.println("Pixels (Entrées): " + trainData.X[0].length);
            System.out.println("Pipelines cibles: y_chat, y_chien, y_sauvage générés avec succès.");
        }
    }
}

package piJava.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class WikipediaApi {

    /**
     * Récupère un résumé (extrait) de Wikipédia pour un sujet donné.
     * Tente plusieurs variantes pour maximiser les chances de succès.
     * @param subject Le nom de la matière.
     * @return Un résumé textuel ou un message d'erreur.
     */
    public static String getSummary(String subject) {
        if (subject == null || subject.isBlank()) return "Nom de matière invalide.";
        
        // 1. Nettoyage initial : supprimer les codes classes (ex: 3A33), les parenthèses, les tirets de séparation
        String cleanSubject = subject.replaceAll("^[0-9A-Z]{2,4}\\s*[-:]\\s*", "") 
                                     .replaceAll("\\(.*?\\)", "")
                                     .replace("-", " ")
                                     .trim();

        // 2. Préparer les variantes de recherche
        java.util.List<String> variants = new java.util.ArrayList<>();
        variants.add(cleanSubject); // Nom complet nettoyé
        
        // Si le nom contient plusieurs mots (ex: Python Data Science), ajouter les mots séparément
        if (cleanSubject.contains(" ")) {
            String[] parts = cleanSubject.split("\\s+");
            for (String part : parts) {
                if (part.length() > 3) variants.add(part);
            }
            // Ajouter aussi des combinaisons de deux mots si possible
            if (parts.length >= 2) {
                variants.add(parts[0] + " " + parts[1]);
            }
        }

        String[] langs = {"fr", "en"};
        
        for (String term : variants) {
            for (String lang : langs) {
                // Tentative 1 : Terme brut
                String result = fetchFromWiki(term, lang);
                if (isValid(result)) return result;

                // Tentative 2 : Avec " (matière)" ou " (discipline)" pour aider l'API
                String resultContext = fetchFromWiki(term + " (matière)", lang);
                if (isValid(resultContext)) return resultContext;

                resultContext = fetchFromWiki(term + " (discipline)", lang);
                if (isValid(resultContext)) return resultContext;

                // Tentative 3 : Terme avec Underscores (URL format)
                result = fetchFromWiki(term.replace(" ", "_"), lang);
                if (isValid(result)) return result;

                // Tentative 4 : Capitalisation standard (ex: java -> Java)
                if (term.length() > 0) {
                    String cap = term.substring(0, 1).toUpperCase() + term.substring(1).toLowerCase();
                    result = fetchFromWiki(cap, lang);
                    if (isValid(result)) return result;
                }
            }
        }

        return "Informations indisponibles pour '" + subject + "'. Wikipédia ne semble pas avoir d'article correspondant à ces termes.";
    }

    private static boolean isValid(String result) {
        return result != null && 
               !result.equals("Sujet non trouvé") && 
               !result.startsWith("Erreur") &&
               !result.contains("may refer to:") && 
               !result.contains("peut désigner :");
    }

    private static String fetchFromWiki(String encodedSubject, String lang) {
        try {
            // Encodage propre pour l'URL
            String urlEncoded = URLEncoder.encode(encodedSubject, StandardCharsets.UTF_8.toString());
            // Ajout de redirects=true pour suivre les redirections vers les pages correctes
            String urlStr = "https://" + lang + ".wikipedia.org/api/rest_v1/page/summary/" + urlEncoded + "?redirect=true";
            
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setConnectTimeout(3000); // 3 secondes max pour ne pas bloquer l'UI
            conn.setReadTimeout(3000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject json = new JSONObject(response.toString());
                
                // Vérifier si c'est une page de désambiguïsation (type "disambiguation")
                if ("disambiguation".equals(json.optString("type"))) {
                    return "Sujet non trouvé"; // On force la recherche suivante
                }

                String extract = json.optString("extract", "");
                return extract.isBlank() ? "Sujet non trouvé" : extract;
            }
        } catch (Exception e) {
            return "Erreur : " + e.getMessage();
        }
        return "Sujet non trouvé";
    }
}

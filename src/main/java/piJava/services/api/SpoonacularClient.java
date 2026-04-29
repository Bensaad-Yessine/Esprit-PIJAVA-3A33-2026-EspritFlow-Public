package piJava.services.api;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;
import piJava.entities.Recette;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SpoonacularClient {

    private static final String BASE_URL = "https://api.spoonacular.com/recipes/complexSearch";

    private final HttpClient client;
    private final String apiKey;

    /*
        Constructeur :
        - crée le client HTTP
        - lit la clé API depuis le fichier .env
    */
    public SpoonacularClient() {
        this.client = HttpClient.newHttpClient();

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        this.apiKey = dotenv.get("SPOONACULAR_API_KEY");

        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Clé API Spoonacular manquante. Ajoutez SPOONACULAR_API_KEY dans le fichier .env"
            );
        }
    }

    /*
        Méthode principale :
        - reçoit un mot-clé : protein, healthy, salad...
        - appelle Spoonacular
        - récupère les recettes
        - récupère aussi les calories
    */
    public List<Recette> chercherRecettes(String query) throws IOException, InterruptedException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = BASE_URL
                + "?query=" + encodedQuery
                + "&number=3"
                + "&addRecipeInformation=true"
                + "&addRecipeNutrition=true"
                + "&apiKey=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Erreur Spoonacular HTTP " + response.statusCode() + " : " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        JSONArray results = json.optJSONArray("results");

        List<Recette> recettes = new ArrayList<>();

        if (results == null) {
            return recettes;
        }

        for (int i = 0; i < results.length(); i++) {
            JSONObject item = results.getJSONObject(i);

            double calories = extraireCalories(item);

            Recette recette = new Recette(
                    item.optInt("id"),
                    item.optString("title", "Recette sans titre"),
                    item.optString("image", ""),
                    item.optInt("readyInMinutes", 0),
                    item.optInt("servings", 0),
                    item.optString("sourceUrl", ""),
                    calories
            );

            recettes.add(recette);
        }

        return recettes;
    }

    /*
        Extraction des calories depuis le JSON retourné par Spoonacular.

        Structure attendue :
        nutrition
            -> nutrients
                -> name = Calories
                -> amount = valeur
    */
    private double extraireCalories(JSONObject item) {
        try {
            JSONObject nutrition = item.optJSONObject("nutrition");

            if (nutrition == null) {
                return 0;
            }

            JSONArray nutrients = nutrition.optJSONArray("nutrients");

            if (nutrients == null) {
                return 0;
            }

            for (int i = 0; i < nutrients.length(); i++) {
                JSONObject nutrient = nutrients.getJSONObject(i);

                String name = nutrient.optString("name", "");

                if ("Calories".equalsIgnoreCase(name)) {
                    return nutrient.optDouble("amount", 0);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
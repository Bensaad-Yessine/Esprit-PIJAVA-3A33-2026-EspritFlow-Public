package piJava.services.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import io.github.cdimascio.dotenv.Dotenv;

public class GroqClient {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;

    public GroqClient() {
        this.httpClient = HttpClient.newHttpClient();

        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        String envApiKey = dotenv.get("GROQ_API_KEY");
        this.apiKey = (envApiKey != null && !envApiKey.isBlank())
                ? envApiKey
                : System.getenv("GROQ_API_KEY");

        String envModel = dotenv.get("GROQ_MODEL");
        if (envModel == null || envModel.isBlank()) {
            envModel = System.getenv("GROQ_MODEL");
        }

        this.model = (envModel != null && !envModel.isBlank())
                ? envModel
                : "llama-3.1-8b-instant";
    }

    public JSONObject chatJson(JSONArray messages, double temperature, int maxTokens)
            throws IOException, InterruptedException {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GROQ_API_KEY est manquante. Ajoute la clé dans les variables d'environnement.");
        }

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);
        body.put("response_format", new JSONObject().put("type", "json_object"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Erreur Groq HTTP " + response.statusCode() + " : " + response.body());
        }

        JSONObject data = new JSONObject(response.body());

        String content = data
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        try {
            return new JSONObject(content);
        } catch (Exception e) {
            throw new RuntimeException("Groq a retourné une réponse non JSON : " + content);
        }
    }
}
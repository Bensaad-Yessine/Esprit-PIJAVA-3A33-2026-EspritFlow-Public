package piJava.services.api;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;

public class WeatherAiService {

    private final HttpClient client = HttpClient.newHttpClient();
    private String weatherKey;
    private String groqKey;

    public WeatherAiService() {
        Dotenv dotenv = Dotenv.load();
        this.weatherKey = dotenv.get("OPENWEATHER_API_KEY");
        this.groqKey = dotenv.get("GROQ_API_KEY");
    }

    public static class WeatherResult {
        public double temp;
        public int humidity;
        public double wind;
        public String description;
        public String advice;
        public String error;
    }

    public WeatherResult getWeatherAndAdvice() {
        WeatherResult result = new WeatherResult();
        try {
            String url = "https://api.openweathermap.org/data/2.5/weather?q=Tunis&appid="
                    + weatherKey + "&units=metric";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject  weather = new JSONObject(response.body());
            double temp = weather.getJSONObject("main").getDouble("temp");
            String description = weather.getJSONArray("weather")
                    .getJSONObject(0).getString("description");
            int humidity = weather.getJSONObject("main").getInt("humidity");
            double wind = weather.getJSONObject("wind").getDouble("speed");

            // 2️⃣ BUILD PROMPT (same as PHP)
            String prompt = """
                    Tu es un assistant intelligent pour étudiants.

                    Données météo :
                    Température : %s °C
                    Description : %s
                    Humidité : %s %%
                    Vitesse du vent : %s m/s

                    Donne :
                    1) Une recommandation d'étude
                    2) Un conseil vestimentaire
                    3) Une suggestion de productivité

                    Maximum 120 mots. Réponds en français.

                    Format JSON :
                    {
                    "studyAdvice":"...",
                    "clothingAdvice":"...",
                    "productivityAdvice":"..."
                    }
                    """.formatted(temp, description, humidity, wind);

            result.temp = temp;
            result.humidity = humidity;
            result.wind = wind;
            result.description = description;

            // 3️⃣ CALL GROQ
            result.advice = callGroqApi(prompt);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.error = "Erreur météo ou IA";
            return result;
        }
    }

    private String callGroqApi(String prompt) {
        try {JSONObject body = new JSONObject()
                .put("model", "llama-3.1-8b-instant")
                .put("messages", new JSONArray()
                        .put(new JSONObject()
                                .put("role", "system")
                                .put("content", "Tu es un assistant pour étudiants tunisiens. Réponds UNIQUEMENT avec le JSON demandé, sans texte avant ou après."))
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("content", prompt)))
                .put("temperature", 0.3)
                .put("max_tokens", 256);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + groqKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());

            String content = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // 4️⃣ CLEAN + PARSE JSON (same as PHP)
            content = extractJson(content);

            JSONObject parsed = new JSONObject(content);

            return "📚 Étude : " + parsed.getString("studyAdvice") + "\n"
                    + "👕 Tenue : " + parsed.getString("clothingAdvice") + "\n"
                    + "⚡ Productivité : " + parsed.getString("productivityAdvice");

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de l'appel à l'IA";
        }
    }

    private String extractJson(String text) {
        text = text.replace("```json", "").replace("```", "").trim();

        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");

        if (start != -1 && end != -1) {
            return text.substring(start, end + 1);
        }

        return text;
    }

}

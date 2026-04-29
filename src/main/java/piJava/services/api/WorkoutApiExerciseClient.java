package piJava.services.api;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;
import piJava.entities.ExerciceSport;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class WorkoutApiExerciseClient {

    private static final String BASE_URL = "https://api.workoutapi.com/exercises/random";

    private final HttpClient client;
    private final String apiKey;

    public WorkoutApiExerciseClient() {
        this.client = HttpClient.newHttpClient();

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        this.apiKey = dotenv.get("WORKOUT_API_KEY", System.getenv("WORKOUT_API_KEY"));

        System.out.println("WORKOUT_API_KEY chargée ? " + (this.apiKey != null && !this.apiKey.isBlank()));

        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Clé Workout API manquante. Ajoutez WORKOUT_API_KEY dans le fichier .env"
            );
        }
    }

    public List<ExerciceSport> chercherExercices(String bodyPart, String equipment) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Erreur Workout API HTTP " + response.statusCode() + " : " + response.body());
        }

        return convertirReponse(response.body(), bodyPart, equipment);
    }

    private List<ExerciceSport> convertirReponse(String body, String bodyPart, String equipment) {
        List<ExerciceSport> exercices = new ArrayList<>();

        String trimmed = body == null ? "" : body.trim();

        if (trimmed.startsWith("[")) {
            JSONArray array = new JSONArray(trimmed);

            for (int i = 0; i < array.length(); i++) {
                exercices.add(convertirObjet(array.getJSONObject(i), bodyPart, equipment));
            }

            return exercices;
        }

        if (trimmed.startsWith("{")) {
            JSONObject object = new JSONObject(trimmed);

            if (object.has("data") && object.get("data") instanceof JSONArray) {
                JSONArray array = object.getJSONArray("data");

                for (int i = 0; i < array.length(); i++) {
                    exercices.add(convertirObjet(array.getJSONObject(i), bodyPart, equipment));
                }

                return exercices;
            }

            if (object.has("exercises") && object.get("exercises") instanceof JSONArray) {
                JSONArray array = object.getJSONArray("exercises");

                for (int i = 0; i < array.length(); i++) {
                    exercices.add(convertirObjet(array.getJSONObject(i), bodyPart, equipment));
                }

                return exercices;
            }

            exercices.add(convertirObjet(object, bodyPart, equipment));
        }

        return exercices;
    }

    private ExerciceSport convertirObjet(JSONObject item, String bodyPart, String equipment) {
        String name = item.optString("name",
                item.optString("title",
                        item.optString("exercise", "Exercice sportif")));

        String muscle = item.optString("muscle",
                item.optString("target",
                        item.optString("muscleGroup", bodyPart)));

        String instructions = item.optString("instructions",
                item.optString("description",
                        item.optString("execution", "-")));

        String gifUrl = item.optString("gifUrl",
                item.optString("image",
                        item.optString("illustration", "")));

        return new ExerciceSport(
                name,
                bodyPart,
                muscle,
                equipment,
                "-",
                instructions,
                gifUrl
        );
    }
}
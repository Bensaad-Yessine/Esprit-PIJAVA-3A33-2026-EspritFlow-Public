package piJava.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import piJava.entities.PropositionReunion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;

public class GroqService {
    private static final String API_KEY = System.getenv("GROQ_API_KEY");
    private static final String BASE_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";
    private static final int MAX_TITRE_LENGTH = 60;
    private static final int MAX_LIEU_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 240;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static volatile String lastError = "";

    public static String getLastError() {
        return lastError == null ? "" : lastError;
    }

    /**
     * Generate AI meeting proposition for a group
     * @param groupName Group name
     * @param project Project name
     * @param description Group description
     * @param groupId Group ID for DB
     * @return PropositionReunion or null if failed
     */
    public static PropositionReunion generateProposition(String groupName, String project, String description, int groupId) {
        return generateProposition(groupName, project, description, groupId, null, null, null, null);
    }

    public static PropositionReunion generatePropositionFromMessage(
            String groupName,
            String project,
            String description,
            int groupId,
            String userInstruction
    ) {
        String context = (description == null ? "" : description) +
                "\n\nDemande utilisateur a respecter: " + (userInstruction == null ? "" : userInstruction);
        return generateProposition(groupName, project, context, groupId, null, null, null, null);
    }

    public static PropositionReunion generateProposition(
            String groupName,
            String project,
            String description,
            int groupId,
            LocalDate dateReunion,
            LocalTime heureDebut,
            LocalTime heureFin,
            String lieu
    ) {
        lastError = "";
        try {
            String prompt = String.format(
                """
                Génère UNE proposition de réunion pertinente pour le groupe de projet "%s" - Projet "%s".
                
                Contexte: %s
                
                Réponds UNIQUEMENT avec un objet JSON valide (sans ```json ou texte supplémentaire):
                {
                  "titre": "Titre attractif et concis (max 60 chars)",
                  "dateReunion": "YYYY-MM-DD (dans 7-14 jours)",
                  "heureDebut": "HH:MM (9h-17h)",
                  "heureFin": "HH:MM (1h-2h après début)",
                  "lieu": "Salle pertinente (ex: Salle A101, Amphi B, Bureau projet, Visico...)",
                  "description": "Resume clair en une phrase, maximum 220 caracteres"
                }
                
                IMPORTANT: JSON STRICT, valide, dates/times au format exact, sans extras. La description doit rester courte.
                """, groupName, project, description != null ? description : "Pas de description"
            );
            prompt += "\nSi la demande utilisateur contient une date, une heure de debut, une heure de fin ou un lieu, extrais ces valeurs exactement. " +
                    "Pour une date francaise comme 5/2/2025, interprete jour/mois/annee et retourne 2025-02-05. " +
                    "Ne remplace pas le lieu demande par une autre salle.";
            if (dateReunion != null && heureDebut != null && heureFin != null && lieu != null && !lieu.isBlank()) {
                prompt += String.format(
                        "%nUtilise exactement ces informations pour la proposition: date=%s, heureDebut=%s, heureFin=%s, lieu=%s. Ne les modifie pas.",
                        dateReunion,
                        heureDebut,
                        heureFin,
                        lieu
                );
            }

            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);

            JsonArray messages = new JsonArray();
            messages.add(message);

            // Request body
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL);
            requestBody.add("messages", messages);
            JsonObject responseFormat = new JsonObject();
            responseFormat.addProperty("type", "json_object");
            requestBody.add("response_format", responseFormat);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 500);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                lastError = "Groq API HTTP " + response.statusCode() + ": " + extractApiError(response.body());
                System.err.println(lastError);
                return null;
            }

            // Parse response
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            if (jsonResponse == null || !jsonResponse.has("choices") || jsonResponse.getAsJsonArray("choices").isEmpty()) {
                lastError = "Groq response missing choices.";
                return null;
            }
            JsonObject choices = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
            String content = choices.getAsJsonObject("message").get("content").getAsString();

            // Extract JSON from content (handle markdown)
            String jsonStr = extractJsonObject(content);

            // Parse as Prop JSON
            PropJson propJson = gson.fromJson(jsonStr, PropJson.class);
            if (!isValid(propJson)) {
                lastError = "Groq returned incomplete JSON: " + jsonStr;
                return null;
            }

            // Build entity
            PropositionReunion prop = new PropositionReunion();
            prop.setTitre(truncate(propJson.titre, MAX_TITRE_LENGTH));
            prop.setDateReunion(dateReunion != null ? dateReunion : LocalDate.parse(propJson.dateReunion));
            prop.setHeureDebut(heureDebut != null ? heureDebut : LocalTime.parse(propJson.heureDebut));
            prop.setHeureFin(heureFin != null ? heureFin : LocalTime.parse(propJson.heureFin));
            prop.setLieu(truncate(lieu != null && !lieu.isBlank() ? lieu : propJson.lieu, MAX_LIEU_LENGTH));
            prop.setDescription(truncate(propJson.description, MAX_DESCRIPTION_LENGTH));
            prop.setStatut("En attente");
            prop.setDateCreation(LocalDate.now());
            prop.setIdGroupeId(groupId);

            return prop;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            lastError = "Groq request interrupted.";
            System.err.println(lastError);
            return null;
        } catch (IOException | RuntimeException e) {
            lastError = "Groq generation failed: " + e.getMessage();
            System.err.println(lastError);
            return null;
        }
    }

    private static String extractApiError(String body) {
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);
            if (json != null && json.has("error")) {
                JsonObject error = json.getAsJsonObject("error");
                if (error.has("message")) {
                    return error.get("message").getAsString();
                }
            }
        } catch (RuntimeException ignored) {
            // Fall back to raw body below.
        }
        return body == null || body.isBlank() ? "(empty response)" : body;
    }

    private static String extractJsonObject(String content) {
        String cleaned = content == null ? "" : content.trim()
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("\\s*```$", "");
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    private static boolean isValid(PropJson propJson) {
        return propJson != null
                && hasText(propJson.titre)
                && hasText(propJson.dateReunion)
                && hasText(propJson.heureDebut)
                && hasText(propJson.heureFin)
                && hasText(propJson.lieu)
                && hasText(propJson.description);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        if (cleaned.length() <= maxLength) {
            return cleaned;
        }
        return cleaned.substring(0, Math.max(0, maxLength - 3)).trim() + "...";
    }

    private static class PropJson {
        String titre;
        String dateReunion;
        String heureDebut;
        String heureFin;
        String lieu;
        String description;
    }
}


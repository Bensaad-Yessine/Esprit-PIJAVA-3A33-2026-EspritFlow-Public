package piJava.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import piJava.utils.EnvConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * AcademicChatService
 *
 * Sends student questions to the ChatGPT-4 RapidAPI endpoint and returns answers.
 * Uses a focused academic system prompt to keep EspritBot on-topic.
 *
 * API  : https://chatgpt-42.p.rapidapi.com/conversationgpt4-2
 * Key  : RAPIDAPI_KEY in .env  (already present as the project key)
 * Note : We use plain HttpURLConnection + Jackson (already dependencies).
 *        The ChatController calls chat() on a daemon thread so no blocking occurs.
 */
public class AcademicChatService {

    // ─── Endpoint ──────────────────────────────────────────────────────────────
    private static final String API_URL =
            "https://chatgpt-42.p.rapidapi.com/conversationgpt4-2";

    private static final String RAPIDAPI_KEY =
            EnvConfig.get("RAPIDAPI_KEY",
                    "b1636ee67cmsh93edd56b2c3fa42p1e8013jsn6ec864ba80e3");

    private static final String RAPIDAPI_HOST = "chatgpt-42.p.rapidapi.com";

    // ─── Academic system prompt ────────────────────────────────────────────────
    private static final String SYSTEM_PROMPT =
            "You are EspritBot, an intelligent academic assistant for ESPRIT Engineering " +
            "School students in Tunisia. Help with: Mathematics, Algorithms, Java, " +
            "Python, C/C++, JavaScript, Databases (SQL), Networks, Web Dev, AI/ML, " +
            "Software Engineering, study tips, and career advice. " +
            "Rules: (1) Reply in the same language the student uses (French or English). " +
            "(2) Use bullet points and code blocks when helpful. " +
            "(3) If the question is off-topic, say: " +
            "'Je suis ici pour les questions académiques — posez-moi une question de cours 📚'. " +
            "(4) Always end with a short motivational note 🎯.";

    // ─── Conversation history (multi-turn context) ─────────────────────────────
    /** Each entry: {"role": "user"|"assistant", "content": "..."} */
    private final List<String[]> history = new ArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();

    // ─── Public API ────────────────────────────────────────────────────────────

    /**
     * Send a user message and return the AI's response.
     * Maintains multi-turn conversation history automatically.
     *
     * @param userMessage the student's question
     * @return AI response string, or a friendly error message
     */
    public String chat(String userMessage) {
        // Append new user turn
        history.add(new String[]{"user", userMessage});

        try {
            String response = callApi();
            history.add(new String[]{"assistant", response});
            return response;
        } catch (Exception e) {
            System.err.println("[AcademicChatService] Error: " + e.getMessage());
            // Remove the failed user turn so history stays consistent
            if (!history.isEmpty()) history.remove(history.size() - 1);
            return "❌ Erreur de connexion à EspritBot.\nVérifiez votre connexion internet et réessayez.";
        }
    }

    /** Reset conversation history (new session) */
    public void reset() {
        history.clear();
    }

    // ─── API call ──────────────────────────────────────────────────────────────

    private String callApi() throws IOException {
        // ── Build JSON request body ──────────────────────────────
        ObjectNode body = mapper.createObjectNode();

        // Messages array — full conversation history for context
        ArrayNode messages = mapper.createArrayNode();
        for (String[] turn : history) {
            ObjectNode msg = mapper.createObjectNode();
            msg.put("role",    turn[0]);   // "user" or "assistant"
            msg.put("content", turn[1]);
            messages.add(msg);
        }
        body.set("messages", messages);

        // API params
        body.put("system_prompt", SYSTEM_PROMPT);
        body.put("temperature",   0.8);
        body.put("top_k",         5);
        body.put("top_p",         0.9);
        body.put("max_tokens",    512);
        body.put("web_access",    false);

        String requestBody = mapper.writeValueAsString(body);

        // ── Open HTTP connection ─────────────────────────────────
        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type",   "application/json");
        conn.setRequestProperty("x-rapidapi-key",  RAPIDAPI_KEY);
        conn.setRequestProperty("x-rapidapi-host", RAPIDAPI_HOST);
        conn.setDoOutput(true);
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(30_000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        // ── Read response ────────────────────────────────────────
        int status = conn.getResponseCode();
        InputStream is = (status >= 400) ? conn.getErrorStream() : conn.getInputStream();

        if (is == null) throw new IOException("No response body for HTTP " + status);

        String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        if (status != 200) {
            throw new IOException("API error " + status + ": " + raw);
        }

        return parseResponse(raw);
    }

    // ─── Response parsing ──────────────────────────────────────────────────────

    /**
     * The chatgpt-42 RapidAPI typically returns:
     *   { "result": "...", "status": true }
     *
     * Fallback to OpenAI-compatible format:
     *   { "choices": [{ "message": { "content": "..." } }] }
     */
    private String parseResponse(String raw) throws IOException {
        JsonNode root = mapper.readTree(raw);

        // Primary format: { "result": "..." }
        JsonNode resultNode = root.path("result");
        if (!resultNode.isMissingNode() && !resultNode.isNull()) {
            return resultNode.asText().trim();
        }

        // OpenAI-compatible fallback: { "choices": [{"message": {"content": "..."}}] }
        JsonNode choices = root.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            return choices.get(0).path("message").path("content").asText().trim();
        }

        // Last resort: return the raw JSON so the user sees something
        System.err.println("[AcademicChatService] Unknown response format: " + raw);
        return "🤖 (réponse reçue mais format inattendu — contactez l'administrateur)";
    }
}

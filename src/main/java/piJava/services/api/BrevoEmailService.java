package piJava.services.api;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;

public class BrevoEmailService {

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final HttpClient httpClient;
    private final String apiKey;
    private final String mailFrom;

    public BrevoEmailService() {
        this.httpClient = HttpClient.newHttpClient();

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String envApiKey = dotenv.get("BREVO_API_KEY");
        String envMailFrom = dotenv.get("MAIL_FROM");

        this.apiKey = envApiKey != null ? envApiKey : System.getenv("BREVO_API_KEY");
        this.mailFrom = envMailFrom != null ? envMailFrom : System.getenv("MAIL_FROM");
    }

    public boolean sendPdfWithAttachment(
            String toEmail,
            String subject,
            String html,
            File pdfFile,
            String pdfFilename
    ) throws IOException, InterruptedException, org.json.JSONException {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("BREVO_API_KEY est manquante dans .env");
        }

        if (mailFrom == null || mailFrom.isBlank()) {
            throw new IllegalStateException("MAIL_FROM est manquant dans .env");
        }

        if (pdfFile == null || !pdfFile.exists()) {
            throw new IllegalArgumentException("Le fichier PDF est introuvable.");
        }

        byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
        String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

        JSONObject payload = new JSONObject();
        payload.put("sender", new JSONObject()
                .put("email", mailFrom)
                .put("name", "Objectif Santé"));

        payload.put("to", new JSONArray()
                .put(new JSONObject().put("email", toEmail)));

        payload.put("subject", subject);
        payload.put("htmlContent", html);

        JSONArray attachments = new JSONArray();
        attachments.put(new JSONObject()
                .put("content", pdfBase64)
                .put("name", pdfFilename));

        payload.put("attachment", attachments);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BREVO_URL))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return true;
        }

        System.out.println("Brevo indisponible ou non autorisé (HTTP " + response.statusCode() + ") : " + response.body());
        return false;
    }
}
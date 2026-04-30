package piJava.services;

import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import piJava.entities.user;
import piJava.utils.EnvConfig;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GoogleAuthService {

    private static final String CLIENT_ID = EnvConfig.get("GOOGLE_CLIENT_ID", "");
    private static final String CLIENT_SECRET = EnvConfig.get("GOOGLE_CLIENT_SECRET", "");
    private static final int PREFERRED_PORT = EnvConfig.getInt("GOOGLE_OAUTH_PORT", 8001);


    private HttpServer server;
    private int activePort = -1;
    private String redirectUri;
    private final UserServices userServices = new UserServices();

    public interface AuthCallback {
        void onSuccess(user user);
        void onError(String message);
    }

    public void authenticate(AuthCallback callback) {
        if (CLIENT_ID.isBlank() || CLIENT_SECRET.isBlank()) {
            Platform.runLater(() -> callback.onError("Configuration Google manquante: définissez GOOGLE_CLIENT_ID et GOOGLE_CLIENT_SECRET dans .env"));
            return;
        }

        try {
            startServer(callback);
            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "client_id=" + CLIENT_ID +
                    "&redirect_uri=" + redirectUri +
                    "&response_type=code" +
                    "&scope=email%20profile";
            
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                stopServerIfRunning();
                callback.onError("Le navigateur par défaut n'est pas supporté sur ce système.");
            }
        } catch (Exception e) {
            stopServerIfRunning();
            callback.onError("Erreur lors du lancement de l'authentification Google: " + e.getMessage());
        }
    }

    private void startServer(AuthCallback callback) throws IOException {
        if (server != null) return;

        IOException lastError = null;
        for (int selectedPort : candidatePorts()) {
            try {
                server = HttpServer.create(new InetSocketAddress(selectedPort), 0);
                activePort = server.getAddress().getPort();
                redirectUri = "http://localhost:" + activePort + "/oauth2callback";
                break;
            } catch (IOException e) {
                lastError = e;
                server = null;
                activePort = -1;
                redirectUri = null;
            }
        }

        if (server == null) {
            throw new IOException("Aucun port local disponible pour OAuth callback.", lastError);
        }

        redirectUri = "http://localhost:" + activePort + "/oauth2callback";

        server.createContext("/oauth2callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String response = "Authentification reussie ! Vous pouvez fermer cette fenetre.";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

            if (query != null && query.contains("code=")) {
                String code = query.split("code=")[1].split("&")[0];
                exchangeCodeForTokenAndUser(code, callback);
            } else {
                Platform.runLater(() -> callback.onError("Code d'autorisation introuvable."));
            }

            // Stop server after receiving callback
            HttpServer serverToStop = server;
            server = null;
            activePort = -1;
            redirectUri = null;
            if (serverToStop != null) {
                new Thread(() -> serverToStop.stop(0)).start();
            }
        });
        
        // Utiliser des threads Daemon pour que le serveur ne bloque pas la fermeture de l'application
        server.setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }));
        server.start();
    }

    private void exchangeCodeForTokenAndUser(String code, AuthCallback callback) {
        HttpClient client = HttpClient.newHttpClient();
        String callbackRedirectUri = redirectUri;
        if (callbackRedirectUri == null || callbackRedirectUri.isBlank()) {
            Platform.runLater(() -> callback.onError("Redirection OAuth invalide. Veuillez réessayer."));
            return;
        }

        String requestBody = "code=" + code +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&redirect_uri=" + callbackRedirectUri +
                "&grant_type=authorization_code";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    String body = response.body();
                    String accessToken = extractJsonValue(body, "access_token");
                    if (accessToken != null) {
                        fetchUserInfo(accessToken, callback);
                    } else {
                        Platform.runLater(() -> callback.onError("Echec de l'obtention du token d'acces."));
                    }
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> callback.onError("Erreur d'echange de token: " + e.getMessage()));
                    return null;
                });
    }

    private void fetchUserInfo(String accessToken, AuthCallback callback) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    String body = response.body();
                    String email = extractJsonValue(body, "email");
                    String firstName = extractJsonValue(body, "given_name");
                    String lastName = extractJsonValue(body, "family_name");
                    
                    if (email != null) {
                        Platform.runLater(() -> processGoogleUser(email, firstName, lastName, callback));
                    } else {
                        Platform.runLater(() -> callback.onError("Impossible de recuperer l'email depuis Google."));
                    }
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> callback.onError("Erreur d'information utilisateur: " + e.getMessage()));
                    return null;
                });
    }

    private void processGoogleUser(String email, String firstName, String lastName, AuthCallback callback) {
        String normalizedEmail = safeValue(email).toLowerCase();
        if (normalizedEmail.isEmpty()) {
            callback.onError("Email Google invalide.");
            return;
        }

        // 1. Check if user exists
        user existingUser = userServices.getUserByEmail(normalizedEmail);
        
        if (existingUser != null) {
            // User exists, log them in
            callback.onSuccess(existingUser);
        } else {
            // User does not exist, register them silently
            user newUser = new user();
            newUser.setEmail(normalizedEmail);
            newUser.setNom(nonBlankOrDefault(lastName));
            newUser.setPrenom(nonBlankOrDefault(firstName));
            // Use a random secret so BCrypt always receives a non-empty value.
            newUser.setPassword("google-auth-" + UUID.randomUUID());
            newUser.setRoles("[\"ROLE_USER\"]");
            newUser.setIs_verified(1); // Auto-verified
            newUser.setIs_banned(0);
            newUser.setNum_tel("00000000");
            newUser.setSexe("N/A");
            newUser.setDate_de_naissance("2000-01-01");
            
            userServices.add(newUser);
            
            // Re-fetch to get the assigned ID
            user createdUser = userServices.getUserByEmail(normalizedEmail);
            if (createdUser != null) {
                callback.onSuccess(createdUser);
            } else {
                String error = userServices.getLastErrorMessage();
                callback.onError("Erreur d'enregistrement BD: " + (error != null ? error : "Cause inconnue"));
            }
        }
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private String nonBlankOrDefault(String value) {
        String normalized = safeValue(value);
        return normalized.isEmpty() ? "GoogleUser" : normalized;
    }

    private String extractJsonValue(String json, String key) {
        String searchStr = "\"" + key + "\":";
        int index = json.indexOf(searchStr);
        if (index == -1) return null;
        
        int startIndex = json.indexOf("\"", index + searchStr.length());
        if (startIndex == -1) return null;
        startIndex++; // skip quote
        
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex > startIndex) {
            return json.substring(startIndex, endIndex);
        }
        return null;
    }

    private List<Integer> candidatePorts() {
        List<Integer> ports = new ArrayList<>();
        ports.add(PREFERRED_PORT);
        for (int offset = 1; offset <= 10; offset++) {
            ports.add(PREFERRED_PORT + offset);
        }
        ports.add(0); // port éphémère si tout le reste est occupé
        return ports;
    }

    private void stopServerIfRunning() {
        HttpServer serverToStop = server;
        server = null;
        activePort = -1;
        redirectUri = null;
        if (serverToStop != null) {
            serverToStop.stop(0);
        }
    }
}

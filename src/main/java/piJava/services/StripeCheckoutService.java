package piJava.services;

import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import piJava.entities.SubscriptionPack;
import piJava.entities.user;
import piJava.utils.EnvConfig;
import piJava.utils.SubscriptionCurrency;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class StripeCheckoutService {

    private static final String SECRET_KEY = EnvConfig.get("STRIPE_SECRET_KEY", "");
    private static final String PUBLISHABLE_KEY = EnvConfig.get("STRIPE_PUBLISHABLE_KEY", "");

    private HttpServer server;
    private String callbackBaseUrl;

    public interface PaymentCallback {
        void onSuccess(String message);
        void onCancel(String message);
        void onError(String message);
    }

    public void startCheckout(user currentUser, SubscriptionPack pack, PaymentCallback callback) {
        if (pack == null) {
            callback.onError("Pack d'abonnement introuvable.");
            return;
        }
        if (currentUser == null) {
            callback.onError("Veuillez vous connecter avant de payer un abonnement.");
            return;
        }
        if (SECRET_KEY.isBlank()) {
            callback.onError("Configuration Stripe manquante: définissez STRIPE_SECRET_KEY dans .env");
            return;
        }

        try {
            startServer(callback, currentUser, pack);
            String checkoutUrl = createCheckoutSession(currentUser, pack);
            if (checkoutUrl == null || checkoutUrl.isBlank()) {
                stopServerIfRunning();
                callback.onError("Impossible de créer la session Stripe.");
                return;
            }

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(checkoutUrl));
            } else {
                stopServerIfRunning();
                callback.onError("Le navigateur par défaut n'est pas supporté sur ce système.");
            }
        } catch (Exception e) {
            stopServerIfRunning();
            callback.onError("Erreur lors du lancement du paiement Stripe: " + e.getMessage());
        }
    }

    private void startServer(PaymentCallback callback, user currentUser, SubscriptionPack pack) throws IOException {
        if (server != null) {
            return;
        }

        server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        callbackBaseUrl = "http://127.0.0.1:" + port + "/stripe";

        server.createContext("/stripe/success", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String sessionId = extractQueryParam(query, "session_id");

            String html = "<html><body style='font-family:Arial;text-align:center;padding:40px;'>"
                    + "<h2>Paiement lancé avec succès</h2>"
                    + "<p>Vous pouvez fermer cette fenêtre et revenir à l'application.</p>"
                    + "</body></html>";
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

            if (sessionId == null || sessionId.isBlank()) {
                Platform.runLater(() -> callback.onError("Stripe n'a pas renvoyé l'identifiant de session."));
                stopServerIfRunning();
                return;
            }

            verifySessionAndNotify(sessionId, currentUser, pack, callback);
            stopServerIfRunning();
        });

        server.createContext("/stripe/cancel", exchange -> {
            String html = "<html><body style='font-family:Arial;text-align:center;padding:40px;'>"
                    + "<h2>Paiement annulé</h2>"
                    + "<p>Vous pouvez fermer cette fenêtre et revenir à l'application.</p>"
                    + "</body></html>";
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

            Platform.runLater(() -> callback.onCancel("Paiement annulé pour le pack " + pack.getName() + "."));
            stopServerIfRunning();
        });

        server.setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }));
        server.start();
    }

    private String createCheckoutSession(user currentUser, SubscriptionPack pack) throws IOException, InterruptedException {
        String successUrl = callbackBaseUrl + "/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = callbackBaseUrl + "/cancel";
        long amountMinor = toMinorUnits(pack.getPrice());
        String currency = safeCurrency(pack.getCurrency());

        Map<String, String> form = new LinkedHashMap<>();
        form.put("mode", "payment");
        form.put("success_url", successUrl);
        form.put("cancel_url", cancelUrl);
        form.put("customer_email", safeValue(currentUser.getEmail()));
        form.put("client_reference_id", String.valueOf(currentUser.getId()));
        form.put("metadata[user_id]", String.valueOf(currentUser.getId()));
        form.put("metadata[user_email]", safeValue(currentUser.getEmail()));
        form.put("metadata[pack_id]", String.valueOf(pack.getId()));
        form.put("metadata[pack_name]", safeValue(pack.getName()));
        form.put("metadata[pack_currency]", currency.toUpperCase(Locale.ROOT));
        form.put("metadata[pack_amount]", String.valueOf(pack.getPrice()));
        form.put("line_items[0][quantity]", "1");
        form.put("line_items[0][price_data][currency]", currency);
        form.put("line_items[0][price_data][unit_amount]", String.valueOf(amountMinor));
        form.put("line_items[0][price_data][product_data][name]", safeValue(pack.getName()));
        form.put("line_items[0][price_data][product_data][description]", safeValue(pack.getDescription()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.stripe.com/v1/checkout/sessions"))
                .header("Authorization", "Bearer " + SECRET_KEY)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(buildFormBody(form)))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String message = extractJsonValue(response.body(), "message");
            throw new IOException(message != null ? message : ("Stripe HTTP " + response.statusCode()));
        }

        String url = extractJsonValue(response.body(), "url");
        if (url == null || url.isBlank()) {
            throw new IOException("Stripe checkout URL introuvable.");
        }
        return url;
    }

    private void verifySessionAndNotify(String sessionId, user currentUser, SubscriptionPack pack, PaymentCallback callback) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.stripe.com/v1/checkout/sessions/" + URLEncoder.encode(sessionId, StandardCharsets.UTF_8)))
                    .header("Authorization", "Bearer " + SECRET_KEY)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String message = extractJsonValue(response.body(), "message");
                Platform.runLater(() -> callback.onError(message != null ? message : "Impossible de vérifier le paiement Stripe."));
                return;
            }

            String paymentStatus = extractJsonValue(response.body(), "payment_status");
            String status = extractJsonValue(response.body(), "status");
            if ("paid".equalsIgnoreCase(paymentStatus) || "complete".equalsIgnoreCase(status)) {
                Platform.runLater(() -> callback.onSuccess(
                        "Paiement confirmé pour " + pack.getName() + " — utilisateur " + safeValue(currentUser.getEmail())
                ));
            } else {
                Platform.runLater(() -> callback.onError(
                        "Paiement Stripe non confirmé pour " + pack.getName() + " (statut: " + safeValue(paymentStatus) + ")"
                ));
            }
        } catch (Exception e) {
            Platform.runLater(() -> callback.onError("Erreur de vérification Stripe: " + e.getMessage()));
        }
    }

    private String buildFormBody(Map<String, String> form) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : form.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(encode(entry.getKey())).append('=').append(encode(entry.getValue()));
        }
        return sb.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String extractJsonValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }
        String searchStr = "\"" + key + "\":";
        int index = json.indexOf(searchStr);
        if (index == -1) {
            return null;
        }
        int valueStart = json.indexOf('"', index + searchStr.length());
        if (valueStart == -1) {
            return null;
        }
        valueStart++;
        int valueEnd = json.indexOf('"', valueStart);
        if (valueEnd <= valueStart) {
            return null;
        }
        return json.substring(valueStart, valueEnd);
    }

    private String extractQueryParam(String query, String key) {
        if (query == null || query.isBlank() || key == null) {
            return null;
        }
        String prefix = key + "=";
        for (String part : query.split("&")) {
            if (part.startsWith(prefix)) {
                return part.substring(prefix.length());
            }
        }
        return null;
    }

    private long toMinorUnits(BigDecimal price) {
        BigDecimal value = price != null ? price : BigDecimal.ZERO;
        return value.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    private String safeCurrency(String currency) {
        return SubscriptionCurrency.normalize(currency).toLowerCase(Locale.ROOT);
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private void stopServerIfRunning() {
        HttpServer serverToStop = server;
        server = null;
        callbackBaseUrl = null;
        if (serverToStop != null) {
            serverToStop.stop(0);
        }
    }
}


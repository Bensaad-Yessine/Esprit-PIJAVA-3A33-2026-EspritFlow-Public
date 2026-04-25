package piJava.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import piJava.entities.ClasseStats;
import piJava.entities.MatiereGlobalStats;
import piJava.services.ClasseService;
import piJava.services.MatiereService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StatsApiServer {

    private static final int DEFAULT_PORT = 8085;
    private static final Pattern CLASS_STATS_PATTERN = Pattern.compile("^/api/classes/(\\d+)/stats/?$");

    private static final StatsApiServer INSTANCE = new StatsApiServer(DEFAULT_PORT);

    private final int port;
    private HttpServer server;
    private final ClasseService classeService = new ClasseService();
    private final MatiereService matiereService = new MatiereService();

    private StatsApiServer(int port) {
        this.port = port;
    }

    public static StatsApiServer getInstance() {
        return INSTANCE;
    }

    public synchronized void start() throws IOException {
        if (server != null) {
            return;
        }

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/classes", new ClassesHandler());
        server.createContext("/api/matieres", new MatieresHandler());
        server.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "stats-api");
            t.setDaemon(true);
            return t;
        }));
        server.start();
        System.out.println("Stats API started on http://localhost:" + port);
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("Stats API stopped");
        }
    }

    public int getPort() {
        return port;
    }

    private static final class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, JsonUtils.errorJson("Method Not Allowed"));
                return;
            }
            sendJson(exchange, 200, JsonUtils.healthJson());
        }
    }

    private final class ClassesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, JsonUtils.errorJson("Method Not Allowed"));
                    return;
                }

                String path = getPath(exchange.getRequestURI());
                if ("/api/classes/stats".equals(path)) {
                    List<ClasseStats> stats = classeService.getStatistiquesToutesClasses();
                    sendJson(exchange, 200, JsonUtils.classeStatsListToJson(stats));
                    return;
                }

                Matcher matcher = CLASS_STATS_PATTERN.matcher(path);
                if (matcher.matches()) {
                    int classId = Integer.parseInt(matcher.group(1));
                    ClasseStats stats = classeService.getStatistiquesClasse(classId);
                    if (stats == null) {
                        sendJson(exchange, 404, JsonUtils.errorJson("Classe introuvable"));
                        return;
                    }
                    sendJson(exchange, 200, JsonUtils.classeStatsToJson(stats));
                    return;
                }

                sendJson(exchange, 404, JsonUtils.errorJson("Route classes inconnue"));
            } catch (NumberFormatException e) {
                sendJson(exchange, 400, JsonUtils.errorJson("Identifiant de classe invalide"));
            } catch (SQLException e) {
                sendJson(exchange, 500, JsonUtils.errorJson("Erreur base de données: " + e.getMessage()));
            } catch (Exception e) {
                sendJson(exchange, 500, JsonUtils.errorJson("Erreur serveur: " + e.getMessage()));
            }
        }
    }

    private final class MatieresHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, JsonUtils.errorJson("Method Not Allowed"));
                    return;
                }

                String path = getPath(exchange.getRequestURI());
                if ("/api/matieres/stats".equals(path) || "/api/matieres/stats/global".equals(path)) {
                    MatiereGlobalStats stats = matiereService.getStatistiquesGlobales();
                    sendJson(exchange, 200, JsonUtils.matiereGlobalStatsToJson(stats));
                    return;
                }

                sendJson(exchange, 404, JsonUtils.errorJson("Route matieres inconnue"));
            } catch (SQLException e) {
                sendJson(exchange, 500, JsonUtils.errorJson("Erreur base de données: " + e.getMessage()));
            } catch (Exception e) {
                sendJson(exchange, 500, JsonUtils.errorJson("Erreur serveur: " + e.getMessage()));
            }
        }
    }

    private static String getPath(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}



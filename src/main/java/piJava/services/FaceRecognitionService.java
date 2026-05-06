package piJava.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Java HTTP client that communicates with the Python Flask face-recognition API.
 *
 * Python server must be running at PYTHON_API_URL before any call is made.
 * Default: http://localhost:5000
 */
public class FaceRecognitionService {

    // ── Config ────────────────────────────────────────────────
    private static final String PYTHON_API_URL  = "http://localhost:5000";
    private static final String RECOGNIZE_PATH  = "/recognize";
    private static final String HEALTH_PATH     = "/health";
    private static final int    CONNECT_TIMEOUT = 3_000;   // ms
    private static final int    READ_TIMEOUT    = 15_000;  // ms – face processing can take a moment

    private final ObjectMapper mapper = new ObjectMapper();

    // ── Public API ────────────────────────────────────────────

    /**
     * Send a base64-encoded image to the Python API and return a structured result.
     *
     * @param base64Image  full data-URI or raw base64 string from webcam capture
     * @return             FaceRecognitionResult with status, userId, email, fullName, etc.
     * @throws IOException if the HTTP call fails or the server is unreachable
     */
    public FaceRecognitionResult recognizeFace(String base64Image) throws IOException {
        String body = "image=" + java.net.URLEncoder.encode(base64Image, StandardCharsets.UTF_8);

        HttpURLConnection conn = openConnection(RECOGNIZE_PATH, "POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(body.getBytes(StandardCharsets.UTF_8).length));
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int httpStatus = conn.getResponseCode();
        String responseBody = readResponse(conn, httpStatus);

        return parseResult(responseBody, httpStatus);
    }

    /**
     * Quick health-check: returns true if the Python server is reachable and the
     * database is connected.
     */
    public boolean isHealthy() {
        try {
            HttpURLConnection conn = openConnection(HEALTH_PATH, "GET");
            int code = conn.getResponseCode();
            if (code == 200) {
                String body = readResponse(conn, code);
                JsonNode json = mapper.readTree(body);
                return "running".equals(jsonStr(json, "service"))
                    && "connected".equals(jsonStr(json, "database"));
            }
        } catch (Exception ignored) {}
        return false;
    }

    // ── Internal helpers ──────────────────────────────────────

    private HttpURLConnection openConnection(String path, String method) throws IOException {
        URL url = new URL(PYTHON_API_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    private String readResponse(HttpURLConnection conn, int httpStatus) throws IOException {
        try (var stream = httpStatus >= 400 ? conn.getErrorStream() : conn.getInputStream()) {
            if (stream == null) return "{}";
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private FaceRecognitionResult parseResult(String json, int httpStatus) {
        try {
            JsonNode node = mapper.readTree(json);
            String status   = jsonStr(node, "status");
            String message  = jsonStr(node, "message");
            int    userId   = node.has("user_id")  ? node.get("user_id").asInt()   : -1;
            String email    = jsonStr(node, "email");
            String fullName = jsonStr(node, "full_name");
            double distance = node.has("distance")  ? node.get("distance").asDouble() : 1.0;
            double procTime = node.has("processing_time") ? node.get("processing_time").asDouble() : 0.0;

            return new FaceRecognitionResult(status, message, userId, email, fullName,
                                             distance, procTime, httpStatus);
        } catch (Exception e) {
            return FaceRecognitionResult.error("Failed to parse API response: " + e.getMessage());
        }
    }

    private String jsonStr(JsonNode node, String key) {
        return (node != null && node.has(key) && !node.get(key).isNull())
               ? node.get(key).asText() : "";
    }

    // ── Result DTO ────────────────────────────────────────────

    public static class FaceRecognitionResult {

        public enum Status { SUCCESS, UNKNOWN, BANNED, UNVERIFIED, NO_USER, ERROR }

        public final Status status;
        public final String rawStatus;   // original string from Python API
        public final String message;
        public final int    userId;      // -1 if not found
        public final String email;
        public final String fullName;
        public final double distance;    // 0 = perfect match, 1 = no match
        public final double processingTimeSeconds;
        public final int    httpStatusCode;

        public FaceRecognitionResult(String rawStatus, String message, int userId,
                                     String email, String fullName,
                                     double distance, double processingTimeSeconds,
                                     int httpStatusCode) {
            this.rawStatus            = rawStatus;
            this.message              = message;
            this.userId               = userId;
            this.email                = email;
            this.fullName             = fullName;
            this.distance             = distance;
            this.processingTimeSeconds = processingTimeSeconds;
            this.httpStatusCode       = httpStatusCode;
            this.status               = resolveStatus(rawStatus, httpStatusCode);
        }

        private static Status resolveStatus(String raw, int httpCode) {
            if (raw == null) return Status.ERROR;
            return switch (raw) {
                case "success"    -> Status.SUCCESS;
                case "unknown"    -> Status.UNKNOWN;
                case "banned"     -> Status.BANNED;
                case "unverified" -> Status.UNVERIFIED;
                case "no_user"    -> Status.NO_USER;
                default           -> Status.ERROR;
            };
        }

        public boolean isSuccess()    { return status == Status.SUCCESS; }
        public boolean isUnknown()    { return status == Status.UNKNOWN; }
        public boolean isBanned()     { return status == Status.BANNED; }
        public boolean isUnverified() { return status == Status.UNVERIFIED; }

        /** Factory for local error results (no HTTP call made) */
        public static FaceRecognitionResult error(String message) {
            return new FaceRecognitionResult("error", message, -1, "", "", 1.0, 0.0, -1);
        }

        @Override
        public String toString() {
            return "FaceRecognitionResult{status=" + status
                   + ", userId=" + userId + ", email='" + email
                   + "', distance=" + String.format("%.3f", distance)
                   + ", time=" + String.format("%.2f", processingTimeSeconds) + "s}";
        }
    }
}

package piJava.services;

import piJava.entities.Course;
import piJava.utils.EnvConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CourseService - Fetches and parses Udemy courses from the external API.
 * 
 * API Details:
 *   - Endpoint: https://udemy-paid-courses-for-free-api.p.rapidapi.com/rapidapi/courses/search
 *   - Method: GET (form-urlencoded)
 *   - Headers: x-rapidapi-key, x-rapidapi-host
 *   - Params: page, page_size, query
 */
public class CourseService {

    // ── API Configuration ────────────────────────────────────
    private static final String API_BASE_URL = "https://collection-for-coursera-courses.p.rapidapi.com/rapidapi/course/get_course.php";
    private static final String RAPIDAPI_HOST_HEADER = "x-rapidapi-host";
    private static final String RAPIDAPI_HOST_VALUE = "collection-for-coursera-courses.p.rapidapi.com";
    private static final String RAPIDAPI_KEY_HEADER = "x-rapidapi-key";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    // ── Dependencies ─────────────────────────────────────────
    private final HttpClient httpClient;
    private final String apiKey;
    private final Map<String, List<Course>> cache = new ConcurrentHashMap<>();
    private long lastCacheTime = 0;
    private static final long CACHE_DURATION_MS = 3600000; // 1 hour

    // ── Constructors ─────────────────────────────────────────

    public CourseService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .build();
        this.apiKey = EnvConfig.get("UDEMY_API_KEY", "YOUR_API_KEY_HERE");
    }

    // ── API Methods ──────────────────────────────────────────

    /**
     * Fetch courses without authentication (uses cached results if valid).
     */
    public List<Course> searchCourses(String query, int page, int pageSize) throws IOException {
        String cacheKey = query + "|" + page + "|" + pageSize;

        // Check cache
        if (isCacheValid() && cache.containsKey(cacheKey)) {
            System.out.println("[CourseService] Returning cached results for: " + query);
            return cache.get(cacheKey);
        }

        // Fetch from API
        try {
            String url = buildUrl(query, page, pageSize);
            String response = makeRequest(url);
            List<Course> courses = parseResponse(response, query);
            
            // Client-side filtering if there is a query
            if (query != null && !query.trim().isEmpty()) {
                String lowerQuery = query.toLowerCase();
                courses.removeIf(c -> !c.getTitle().toLowerCase().contains(lowerQuery) &&
                                      !c.getCategory().toLowerCase().contains(lowerQuery));
            }
            
            // Cache the result
            cache.put(cacheKey, courses);
            lastCacheTime = System.currentTimeMillis();
            
            return courses;
        } catch (Exception e) {
            System.err.println("[CourseService] Error fetching courses: " + e.getMessage());
            throw new IOException("Failed to fetch courses: " + e.getMessage(), e);
        }
    }

    /**
     * Search with default page size.
     */
    public List<Course> searchCourses(String query, int page) throws IOException {
        return searchCourses(query, page, DEFAULT_PAGE_SIZE);
    }

    /**
     * Search on first page.
     */
    public List<Course> searchCourses(String query) throws IOException {
        return searchCourses(query, 1, DEFAULT_PAGE_SIZE);
    }

    /**
     * Get trending courses (empty query).
     */
    public List<Course> getTrendingCourses(int page) throws IOException {
        return searchCourses("", page, DEFAULT_PAGE_SIZE);
    }

    /**
     * Get courses by category.
     */
    public List<Course> getCoursesByCategory(String category, int page) throws IOException {
        return searchCourses(category, page, DEFAULT_PAGE_SIZE);
    }

    // ── Internal Helpers ────────────────────────────────────

    private String buildUrl(String query, int page, int pageSize) {
        StringBuilder url = new StringBuilder(API_BASE_URL);
        url.append("?page_no=").append(page);
        
        return url.toString();
    }

    private String makeRequest(String urlString) throws IOException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(urlString))
                .GET()
                .header("Accept", "application/json");

        // Add API key if available
        if (apiKey != null && !apiKey.isEmpty() && !"YOUR_API_KEY_HERE".equals(apiKey)) {
            requestBuilder.header(RAPIDAPI_KEY_HEADER, apiKey)
                         .header(RAPIDAPI_HOST_HEADER, RAPIDAPI_HOST_VALUE);
        }

        HttpRequest request = requestBuilder.build();
        
        try {
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return response.body();
            } else if (response.statusCode() == 429) {
                throw new IOException("Rate limited by API. Please try again later.");
            } else if (response.statusCode() == 403) {
                throw new IOException("API access forbidden (403). Are you subscribed to the API on RapidAPI? Response: " + response.body());
            } else {
                throw new IOException("API returned status " + response.statusCode() + ". Response: " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    /**
     * Parse JSON response from API.
     * Expected structure:
     * {
     *   "courses": [
     *     { "id", "title", "description", "category", "coupon_code", 
     *       "coupon_expiry", "course_id", "url", ... }
     *   ]
     * }
     */
    private List<Course> parseResponse(String jsonResponse, String searchQuery) {
        List<Course> courses = new ArrayList<>();
        
        try {
            // Simple JSON parsing without external libraries
            // We'll use String operations and indexOf for basic parsing
            
            int coursesStart = jsonResponse.indexOf("\"reviews\"");
            if (coursesStart == -1) {
                System.err.println("[CourseService] Invalid API response format");
                return courses;
            }

            // Extract array content
            int arrayStart = jsonResponse.indexOf("[", coursesStart);
            int arrayEnd = jsonResponse.lastIndexOf("]");
            
            if (arrayStart == -1 || arrayEnd == -1) {
                return courses;
            }

            String arrayContent = jsonResponse.substring(arrayStart + 1, arrayEnd);
            
            // Split by object boundaries (this is simplified - a real JSON parser is better)
            String[] courseObjects = arrayContent.split("\\},\\s*\\{");
            
            for (int i = 0; i < courseObjects.length && i < 50; i++) {
                String courseJson = courseObjects[i].replaceAll("^\\{", "").replaceAll("\\}$", "");
                Course course = parseCourseObject(courseJson, searchQuery);
                if (course != null) {
                    courses.add(course);
                }
            }
            
        } catch (Exception e) {
            System.err.println("[CourseService] Error parsing JSON: " + e.getMessage());
        }

        return courses;
    }

    /**
     * Parse a single course object from JSON.
     */
    private Course parseCourseObject(String courseJson, String searchQuery) {
        try {
            String title = extractJsonValue(courseJson, "course_name");
            String institution = extractJsonValue(courseJson, "course_institution");
            String courseUrl = extractJsonValue(courseJson, "course_url");
            String apiId = extractJsonValue(courseJson, "course_id");
            
            String description = "Offered by " + (institution != null ? institution : "Coursera");
            String category = (institution != null && !institution.isEmpty()) ? institution : "General";
            String couponCode = "";
            LocalDate expirationDate = null;
            
            if (apiId == null || apiId.isEmpty()) {
                apiId = "coursera-" + System.currentTimeMillis();
            }

            if (title == null || title.isEmpty()) {
                return null;
            }

            return new Course(apiId, title, description, category, couponCode, 
                            expirationDate, courseUrl);
            
        } catch (Exception e) {
            System.err.println("[CourseService] Error parsing course object: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extract a JSON value by key.
     * Handles quoted strings and handles escaping.
     */
    private String extractJsonValue(String json, String key) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*");
        java.util.regex.Matcher matcher = pattern.matcher(json);
        
        if (!matcher.find()) {
            return null;
        }

        int startIdx = matcher.end();
        
        // Handle quoted strings
        if (json.charAt(startIdx) == '"') {
            int endIdx = startIdx + 1;
            while (endIdx < json.length()) {
                if (json.charAt(endIdx) == '"' && json.charAt(endIdx - 1) != '\\') {
                    return json.substring(startIdx + 1, endIdx)
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\");
                }
                endIdx++;
            }
        } else {
            // Handle numbers, booleans, null
            int endIdx = startIdx;
            while (endIdx < json.length() && 
                   json.charAt(endIdx) != ',' && 
                   json.charAt(endIdx) != '}' &&
                   json.charAt(endIdx) != ']') {
                endIdx++;
            }
            return json.substring(startIdx, endIdx).trim();
        }

        return null;
    }

    // ── Cache Management ────────────────────────────────────

    private boolean isCacheValid() {
        return (System.currentTimeMillis() - lastCacheTime) < CACHE_DURATION_MS;
    }

    public void clearCache() {
        cache.clear();
        lastCacheTime = 0;
    }

    public int getCacheSize() {
        return cache.size();
    }

    // ── Utility ──────────────────────────────────────────────

    /**
     * Check if API is reachable.
     */
    public boolean isAPIHealthy() {
        try {
            String testUrl = API_BASE_URL + "?page_no=1";
            String response = makeRequest(testUrl);
            return response != null && response.contains("reviews");
        } catch (Exception e) {
            System.err.println("[CourseService] API health check failed: " + e.getMessage());
            return false;
        }
    }
}


package piJava.utils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GiphyService {
    private static final String API_KEY = "PYOzAhvadRmUsRwBiubOnnLLGYWgnDcZ";

    public static class GifResult {
        public String previewUrl;
        public String originalUrl;
        
        public GifResult(String previewUrl, String originalUrl) {
            this.previewUrl = previewUrl;
            this.originalUrl = originalUrl;
        }
    }

    public static List<GifResult> getTrendingGifs(int limit) {
        String url = "https://api.giphy.com/v1/gifs/trending?api_key=" + API_KEY + "&limit=" + limit;
        return fetchGifs(url);
    }

    public static List<GifResult> searchGifs(String query, int limit) {
        if (query == null || query.isBlank()) {
            return getTrendingGifs(limit);
        }
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://api.giphy.com/v1/gifs/search?api_key=" + API_KEY + "&q=" + encodedQuery + "&limit=" + limit;
            return fetchGifs(url);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static List<GifResult> fetchGifs(String url) {
        List<GifResult> results = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
                com.google.gson.JsonArray data = root.getAsJsonArray("data");
                if (data != null) {
                    for (com.google.gson.JsonElement el : data) {
                        com.google.gson.JsonObject images = el.getAsJsonObject().getAsJsonObject("images");
                        if (images != null) {
                            String preview = images.getAsJsonObject("fixed_height_small").get("url").getAsString();
                            String original = images.getAsJsonObject("original").get("url").getAsString();
                            results.add(new GifResult(preview, original));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
    
    public static String getGifUrl(String query) {
        List<GifResult> results = searchGifs(query, 1);
        if (!results.isEmpty()) {
            return results.get(0).originalUrl;
        }
        return null;
    }

    public static javafx.scene.image.Image fetchImage(String urlString) {
        try {
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();
            HttpResponse<java.io.InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() == 200) {
                return new javafx.scene.image.Image(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

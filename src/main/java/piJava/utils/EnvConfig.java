package piJava.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EnvConfig {

    private static final Map<String, String> DOT_ENV = loadDotEnv();

    private EnvConfig() {
    }

    public static String get(String key, String fallback) {
        String fromSystemProperty = System.getProperty(key);
        if (isNonBlank(fromSystemProperty)) {
            return fromSystemProperty;
        }

        String fromEnv = System.getenv(key);
        if (isNonBlank(fromEnv)) {
            return fromEnv;
        }

        String fromDotEnv = DOT_ENV.get(key);
        if (isNonBlank(fromDotEnv)) {
            return fromDotEnv;
        }

        return fallback;
    }

    public static int getInt(String key, int fallback) {
        String value = get(key, String.valueOf(fallback));
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> values = new HashMap<>();
        Path envPath = Paths.get(".env").toAbsolutePath().normalize();
        if (!Files.exists(envPath)) {
            return values;
        }

        try {
            List<String> lines = Files.readAllLines(envPath);
            for (String line : lines) {
                parseLine(values, line);
            }
        } catch (IOException ignored) {
            // Keep map empty when .env cannot be read.
        }

        return values;
    }

    private static void parseLine(Map<String, String> values, String rawLine) {
        if (rawLine == null) {
            return;
        }

        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }

        int idx = line.indexOf('=');
        if (idx <= 0) {
            return;
        }

        String key = line.substring(0, idx).trim();
        String value = line.substring(idx + 1).trim();

        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }

        values.put(key, value);
    }

    private static boolean isNonBlank(String value) {
        return value != null && !value.isBlank();
    }
}


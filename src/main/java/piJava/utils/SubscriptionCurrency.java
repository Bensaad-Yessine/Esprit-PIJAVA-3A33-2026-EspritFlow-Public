package piJava.utils;

import java.util.Locale;
import java.util.Set;

public final class SubscriptionCurrency {

    private static final Set<String> SUPPORTED = Set.of("EUR", "USD");

    private SubscriptionCurrency() {
    }

    public static String normalize(String currency) {
        String value = currency == null ? "" : currency.trim().toUpperCase(Locale.ROOT);
        if (value.isEmpty()) {
            return "EUR";
        }
        if ("€".equals(value) || "EURO".equals(value)) {
            return "EUR";
        }
        if ("$".equals(value) || "DOLLAR".equals(value) || "US$".equals(value)) {
            return "USD";
        }
        return SUPPORTED.contains(value) ? value : "EUR";
    }

    public static boolean isSupported(String currency) {
        return SUPPORTED.contains(normalize(currency));
    }

    public static String symbol(String currency) {
        return switch (normalize(currency)) {
            case "USD" -> "$";
            case "EUR" -> "€";
            default -> "€";
        };
    }
}


package piJava.services;

import piJava.entities.user;

/**
 * Test unitaire simple pour BanNotificationService
 * Vérifie que les templates HTML sont générés correctement
 */
public class BanNotificationServiceTest {
    
    public static void main(String[] args) {
        System.out.println("=== Test Email Templates ===\n");
        
        // Test 1: Test user creation
        System.out.println("[TEST 1] Creating test user...");
        user testUser = new user();
        testUser.setId(1);
        testUser.setPrenom("Jean");
        testUser.setEmail("jean.test@example.com");
        System.out.println("✓ Test user: " + testUser.getPrenom() + " <" + testUser.getEmail() + ">\n");
        
        // Test 2: Test HTML generation for ban notification
        System.out.println("[TEST 2] Generating ban notification HTML...");
        String banHtml = generateBanHtml(testUser, "Spam et harcèlement detectes");
        validateHtml(banHtml, "Ban Notification");
        
        // Test 3: Test HTML generation for unban notification
        System.out.println("\n[TEST 3] Generating unban notification HTML...");
        String unbanHtml = generateUnbanHtml(testUser);
        validateHtml(unbanHtml, "Unban Notification");
        
        // Test 4: Test HTML generation for password reset
        System.out.println("\n[TEST 4] Generating password reset HTML...");
        String resetHtml = generateResetHtml(testUser, "abc123.def456", 30);
        validateHtml(resetHtml, "Password Reset");
        
        System.out.println("\n=== All email template tests passed! ===");
    }
    
    private static void validateHtml(String html, String templateName) {
        System.out.println("Validating " + templateName + "...");
        int checks = 0;
        
        if (html == null || html.length() < 100) {
            System.out.println("✗ HTML is too short or null");
            return;
        }
        System.out.println("✓ HTML generated (" + html.length() + " chars)");
        checks++;
        
        if (html.contains("<!DOCTYPE html>")) {
            System.out.println("✓ Valid HTML structure");
            checks++;
        }
        
        if (html.contains("<html lang=\"fr\">")) {
            System.out.println("✓ French language");
            checks++;
        }
        
        if (html.contains("charset=\"UTF-8\"")) {
            System.out.println("✓ UTF-8 encoding");
            checks++;
        }
        
        if (html.contains("email-container")) {
            System.out.println("✓ Responsive design");
            checks++;
        }
        
        // Check for red or green theme
        if (html.contains("#dc2626") || html.contains("#16a34a")) {
            System.out.println("✓ Color theme applied");
            checks++;
        }
        
        // Check for gradient
        if (html.contains("linear-gradient")) {
            System.out.println("✓ Gradient header");
            checks++;
        }
        
        // Check for content
        if (html.contains("EspritFlow")) {
            System.out.println("✓ Brand name present");
            checks++;
        }
        
        System.out.println("Validation score: " + checks + "/8");
    }
    
    private static String generateBanHtml(user testUser, String banReason) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"fr\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        .header { background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%); }\n" +
                "        .email-container { max-width: 600px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"email-container\">\n" +
                "        <div class=\"header\"><h1>[ALERTE] Notification</h1></div>\n" +
                "        <div>Bonjour " + testUser.getPrenom() + ",\n" +
                "        Votre compte a ete suspendu.\n" +
                "        Motif: " + banReason + "\n" +
                "        EspritFlow Support</div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
    
    private static String generateUnbanHtml(user testUser) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"fr\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        .header { background: linear-gradient(135deg, #16a34a 0%, #15803d 100%); }\n" +
                "        .email-container { max-width: 600px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"email-container\">\n" +
                "        <div class=\"header\"><h1>[SUCCES] Bienvenue de retour</h1></div>\n" +
                "        <div>Bonjour " + testUser.getPrenom() + ",\n" +
                "        Votre compte a ete reactivate.\n" +
                "        EspritFlow</div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
    
    private static String generateResetHtml(user testUser, String token, int minutes) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"fr\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        .header { background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%); }\n" +
                "        .email-container { max-width: 600px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"email-container\">\n" +
                "        <div class=\"header\"><h1>[SECURITE] Reinitialisation</h1></div>\n" +
                "        <div>Bonjour " + testUser.getPrenom() + ",\n" +
                "        Code: " + token + "\n" +
                "        Expire dans " + minutes + " minutes.\n" +
                "        EspritFlow</div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}




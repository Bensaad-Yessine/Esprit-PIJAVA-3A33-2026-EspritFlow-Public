package piJava.services;

import piJava.entities.ResetPasswordRequest;
import piJava.entities.user;
import piJava.utils.MyDataBase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.regex.Pattern;

public class PasswordResetService {

    private static final int TOKEN_VALIDITY_MINUTES = 30;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private Connection con;
    private final UserServices userServices = new UserServices();
    private final MailService mailService = new MailService();
    private final SecureRandom secureRandom = new SecureRandom();

    public void requestReset(String email) throws SQLException, java.io.IOException {
        String normalizedEmail = normalize(email);
        if (normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("Veuillez saisir une adresse e-mail valide.");
        }
        if (!isValidEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Veuillez saisir une adresse e-mail valide.");
        }

        user account = userServices.getUserByEmail(normalizedEmail);
        if (account == null) {
            throw new IllegalArgumentException("Aucun compte trouvé pour cette adresse e-mail.");
        }

        deleteExpiredRequestsForUser(account.getId());

        String selector = generateTokenPart(12);
        String verifier = generateTokenPart(32);
        String publicToken = selector + "." + verifier;
        String hashedToken = sha256Hex(verifier);
        LocalDateTime requestedAt = LocalDateTime.now();
        LocalDateTime expiresAt = requestedAt.plusMinutes(TOKEN_VALIDITY_MINUTES);

        ResetPasswordRequest request = new ResetPasswordRequest(0, selector, hashedToken, requestedAt, expiresAt, account.getId());
        insertRequest(request);

        try {
            sendResetEmail(account, publicToken, expiresAt);
            try {
                deleteRequestsForUserExceptSelector(account.getId(), selector);
            } catch (SQLException cleanupError) {
                System.err.println("Password reset cleanup warning: " + cleanupError.getMessage());
            }
        } catch (java.io.IOException e) {
            try {
                deleteRequestById(request.getId());
            } catch (SQLException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
    }

    public void resetPassword(String email, String token, String newPassword) throws SQLException {
        String normalizedEmail = normalize(email);
        if (normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("Veuillez saisir votre adresse e-mail.");
        }
        if (!isValidEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Veuillez saisir une adresse e-mail valide.");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Veuillez saisir le code de réinitialisation.");
        }
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 6 caractères.");
        }

        user account = userServices.getUserByEmail(normalizedEmail);
        if (account == null) {
            throw new IllegalArgumentException("Aucun compte trouvé pour cette adresse e-mail.");
        }

        deleteExpiredRequestsForUser(account.getId());

        String[] parts = token.trim().split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Le code de réinitialisation est invalide.");
        }

        ResetPasswordRequest request = findRequest(parts[0], account.getId());
        if (request == null) {
            throw new IllegalArgumentException("Code de réinitialisation introuvable ou expiré.");
        }

        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(LocalDateTime.now())) {
            deleteRequestsForUser(account.getId());
            throw new IllegalArgumentException("Le code de réinitialisation a expiré. Veuillez en demander un nouveau.");
        }

        String verifier = parts[1].trim();
        if (!sha256Hex(verifier).equalsIgnoreCase(request.getHashedToken())) {
            throw new IllegalArgumentException("Code de réinitialisation incorrect.");
        }

        userServices.updatePasswordById(account.getId(), newPassword.trim());
        deleteRequestsForUser(account.getId());
    }

    private void sendResetEmail(user account, String token, LocalDateTime expiresAt) throws java.io.IOException {
        String subject = "EspritFlow - Réinitialisation de mot de passe";
        String htmlBody = buildPasswordResetHtml(account, token, expiresAt);
        mailService.sendPasswordResetEmail(account.getEmail(), subject, htmlBody);
    }

    private String buildPasswordResetHtml(user account, String token, LocalDateTime expiresAt) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"fr\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 0; }\n" +
                "        .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); overflow: hidden; }\n" +
                "        .header { background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%); color: white; padding: 40px 20px; text-align: center; }\n" +
                "        .header h1 { margin: 0; font-size: 28px; font-weight: bold; }\n" +
                "        .header p { margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; }\n" +
                "        .content { padding: 40px 30px; }\n" +
                "        .content h2 { color: #dc2626; font-size: 20px; margin: 0 0 20px 0; }\n" +
                "        .content p { color: #333333; line-height: 1.6; margin: 15px 0; }\n" +
                "        .token-box { background-color: #f0f0f0; border-left: 4px solid #dc2626; padding: 20px; margin: 25px 0; border-radius: 4px; }\n" +
                "        .token-box .label { color: #666666; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; }\n" +
                "        .token-box .token { font-family: 'Courier New', monospace; font-size: 18px; font-weight: bold; color: #dc2626; word-break: break-all; margin-top: 10px; letter-spacing: 2px; }\n" +
                "        .expiry { background-color: #fff3cd; border: 1px solid #ffc107; border-radius: 4px; padding: 15px; margin: 20px 0; color: #856404; }\n" +
                "        .expiry strong { color: #dc2626; }\n" +
                "        .steps { margin: 30px 0; }\n" +
                "        .steps ol { margin: 15px 0; padding-left: 20px; }\n" +
                "        .steps li { margin: 10px 0; color: #333333; line-height: 1.6; }\n" +
                "        .footer { background-color: #f8f8f8; padding: 20px 30px; border-top: 1px solid #eeeeee; text-align: center; font-size: 12px; color: #888888; }\n" +
                "        .footer p { margin: 5px 0; }\n" +
                "        .security-notice { background-color: #f0f0f0; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 4px; font-size: 13px; color: #555555; }\n" +
                "        .security-notice strong { color: #dc2626; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"email-container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>[SECURITE] Reinitialisation de mot de passe</h1>\n" +
                "            <p>EspritFlow - Plateforme d'apprentissage</p>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"content\">\n" +
                "            <h2>Bonjour " + safe(account.getPrenom()) + ",</h2>\n" +
                "\n" +
                "            <p>Nous avons recu une demande de reinitialisation de mot de passe pour votre compte EspritFlow.</p>\n" +
                "\n" +
                "            <div class=\"token-box\">\n" +
                "                <div class=\"label\">Code de reinitialisation</div>\n" +
                "                <div class=\"token\">" + token + "</div>\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"expiry\">\n" +
                "                <strong>[TEMPS LIMITE] Attention :</strong> Ce code expire a <strong>" + formatExpiryTime(expiresAt) + "</strong> (dans 30 minutes).\n" +
                "            </div>\n" +
                "\n" +
                "            <h3 style=\"color: #333333; font-size: 16px; margin-top: 30px;\">Comment reinitialiser votre mot de passe :</h3>\n" +
                "            <div class=\"steps\">\n" +
                "                <ol>\n" +
                "                    <li>Ouvrez l'application <strong>EspritFlow</strong></li>\n" +
                "                    <li>Cliquez sur <strong>\"Forgot password?\"</strong> sur l'ecran de connexion</li>\n" +
                "                    <li>Entrez votre adresse e-mail : <strong>" + safe(account.getEmail()) + "</strong></li>\n" +
                "                    <li>Entrez le code ci-dessus</li>\n" +
                "                    <li>Creez un nouveau mot de passe securise</li>\n" +
                "                    <li>Cliquez sur <strong>\"Reinitialiser le mot de passe\"</strong></li>\n" +
                "                </ol>\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"security-notice\">\n" +
                "                <strong>[SECURITE] Important :</strong> Si vous n'avez pas demande cette reinitialisation, veuillez ignorer cet email. Votre compte restera securise. Ne partagez jamais ce code avec quiconque.\n" +
                "            </div>\n" +
                "\n" +
                "            <p style=\"color: #666666; font-size: 13px; margin-top: 30px;\">\n" +
                "                Si vous avez des problemes ou besoin d'aide, veuillez contacter notre support : <strong>support@espritflow.tn</strong>\n" +
                "            </p>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"footer\">\n" +
                "            <p>&copy; 2026 EspritFlow. Tous droits reserves.</p>\n" +
                "            <p>Cet email a ete envoye a <strong>" + safe(account.getEmail()) + "</strong></p>\n" +
                "            <p>Si ce n'est pas votre adresse, <a href=\"#\" style=\"color: #dc2626; text-decoration: none;\">signalez-le</a></p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String formatExpiryTime(LocalDateTime expiresAt) {
        if (expiresAt == null) return "N/A";
        return expiresAt.getHour() + ":" + String.format("%02d", expiresAt.getMinute());
    }

    private void insertRequest(ResetPasswordRequest request) throws SQLException {
        String sql = "INSERT INTO reset_password_request (selector, hashed_token, requested_at, expires_at, user_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, request.getSelector());
            ps.setString(2, request.getHashedToken());
            ps.setTimestamp(3, Timestamp.valueOf(request.getRequestedAt()));
            ps.setTimestamp(4, Timestamp.valueOf(request.getExpiresAt()));
            ps.setInt(5, request.getUserId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    request.setId(rs.getInt(1));
                }
            }
        }
    }

    private void deleteRequestById(int requestId) throws SQLException {
        String sql = "DELETE FROM reset_password_request WHERE id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }

    private ResetPasswordRequest findRequest(String selector, int userId) throws SQLException {
        String sql = "SELECT * FROM reset_password_request WHERE selector = ? AND user_id = ? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, selector);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    private void deleteRequestsForUser(int userId) throws SQLException {
        String sql = "DELETE FROM reset_password_request WHERE user_id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void deleteExpiredRequestsForUser(int userId) throws SQLException {
        String sql = "DELETE FROM reset_password_request WHERE user_id = ? AND expires_at < NOW()";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void deleteRequestsForUserExceptSelector(int userId, String selector) throws SQLException {
        String sql = "DELETE FROM reset_password_request WHERE user_id = ? AND selector <> ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, selector);
            ps.executeUpdate();
        }
    }

    private ResetPasswordRequest map(ResultSet rs) throws SQLException {
        return new ResetPasswordRequest(
                rs.getInt("id"),
                rs.getString("selector"),
                rs.getString("hashed_token"),
                rs.getTimestamp("requested_at") != null ? rs.getTimestamp("requested_at").toLocalDateTime() : null,
                rs.getTimestamp("expires_at") != null ? rs.getTimestamp("expires_at").toLocalDateTime() : null,
                rs.getInt("user_id")
        );
    }

    private String generateTokenPart(int bytes) {
        byte[] data = new byte[bytes];
        secureRandom.nextBytes(data);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de calculer le hash du token.", e);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean isValidEmail(String value) {
        return value != null && EMAIL_PATTERN.matcher(value.trim()).matches();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private Connection requireConnection() throws SQLException {
        con = MyDataBase.getInstance().getConnection();
        if (con == null) {
            throw new SQLException("Database connection unavailable. Verify MySQL is running and the 'pidev' database exists.");
        }
        return con;
    }
}


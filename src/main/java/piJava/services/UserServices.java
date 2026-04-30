package piJava.services;

import org.mindrot.jbcrypt.BCrypt;
import piJava.entities.user;
import piJava.utils.MyDataBase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class UserServices implements ICrud<user> {

    private Connection con;
    private String lastErrorMessage;
    private final BanNotificationService banNotificationService = new BanNotificationService();

    public UserServices() {
        con = MyDataBase.getInstance().getConnection();
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    @Override
    public List<user> show() {
        List<user> users = new ArrayList<>();
        String sql = "SELECT * FROM `user` ORDER BY id";
        try (Statement st = requireConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    @Override
    public void add(user u) {
        lastErrorMessage = null;
        String sql = "INSERT INTO `user` (email, roles, password, is_verified, nom, prenom, "
                + "num_tel, date_de_naissance, sexe, profile_pic, created_at, "
                + "is_banned, ban_reason, banned_at, verification_token, classe_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getRoles() != null ? u.getRoles() : "[\"ROLE_USER\"]");
            ps.setString(3, BCrypt.hashpw(u.getPassword(), BCrypt.gensalt()));
            ps.setInt(4, u.getIs_verified());
            ps.setString(5, u.getNom());
            ps.setString(6, u.getPrenom());
            ps.setString(7, u.getNum_tel());
            ps.setDate(8, Date.valueOf(normalizeBirthDate(u.getDate_de_naissance())));
            ps.setString(9, u.getSexe());
            ps.setString(10, u.getProfile_pic());
            ps.setInt(11, u.getIs_banned());
            ps.setString(12, u.getBan_reason());
            ps.setString(13, u.getBanned_at());
            ps.setString(14, u.getVerification_token());
            if (u.getClasse_id() != null) {
                ps.setInt(15, u.getClasse_id());
            } else {
                ps.setNull(15, Types.INTEGER);
            }

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    u.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            System.err.println("Failed to add user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String normalizeBirthDate(String date) {
        String fallback = "2000-01-01";
        if (date == null || date.isBlank()) {
            return fallback;
        }

        try {
            LocalDate parsed = LocalDate.parse(date.trim());
            return parsed.toString();
        } catch (DateTimeParseException e) {
            return fallback;
        }
    }

    @Override
    public void delete(int id) {
        try {
            Connection connection = requireConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM reset_password_request WHERE user_id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM `user` WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Failed to delete user: " + e.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    public void edit(user u) {
        String sql = "UPDATE `user` SET email=?, roles=?, nom=?, prenom=?, "
                + "num_tel=?, date_de_naissance=?, sexe=?, classe_id=?, profile_pic=? WHERE id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getRoles());
            ps.setString(3, u.getNom());
            ps.setString(4, u.getPrenom());
            ps.setString(5, u.getNum_tel());
            if (u.getDate_de_naissance() != null && !u.getDate_de_naissance().isEmpty()) {
                ps.setDate(6, Date.valueOf(u.getDate_de_naissance()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setString(7, u.getSexe());
            if (u.getClasse_id() != null) {
                ps.setInt(8, u.getClasse_id());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setString(9, u.getProfile_pic());
            ps.setInt(10, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to edit user: " + e.getMessage());
            e.printStackTrace();
        }

        if (u.getPassword() != null && !u.getPassword().isEmpty()) {
            String pwd = u.getPassword();
            if (!pwd.startsWith("$2a$") && !pwd.startsWith("$2y$") && !pwd.startsWith("$2b$")) {
                try (PreparedStatement ps = requireConnection().prepareStatement(
                        "UPDATE `user` SET password=? WHERE id=?")) {
                    ps.setString(1, BCrypt.hashpw(u.getPassword(), BCrypt.gensalt()));
                    ps.setInt(2, u.getId());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Failed to update password: " + e.getMessage());
                }
            }
        }
    }

    public void updatePasswordById(int id, String rawPassword) throws SQLException {
        String sql = "UPDATE `user` SET password=? WHERE id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void banUser(int id, String reason) {
        String sql = "UPDATE `user` SET is_banned=1, ban_reason=?, banned_at=NOW() WHERE id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, id);
            ps.executeUpdate();

            // Send ban notification email
            user bannedUser = getById(id);
            if (bannedUser != null) {
                try {
                    banNotificationService.notifyUserBanned(bannedUser, reason);
                } catch (java.io.IOException e) {
                    System.err.println("Failed to send ban notification email: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to ban user: " + e.getMessage());
        }
    }

    public void unbanUser(int id) {
        String sql = "UPDATE `user` SET is_banned=0, ban_reason=NULL, banned_at=NULL WHERE id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            // Send unban notification email
            user unbanUser = getById(id);
            if (unbanUser != null) {
                try {
                    sendUnbanNotificationEmail(unbanUser);
                } catch (java.io.IOException e) {
                    System.err.println("Failed to send unban notification email: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to unban user: " + e.getMessage());
        }
    }

    private void sendUnbanNotificationEmail(user user) throws java.io.IOException {
        MailService mailService = new MailService();
        String subject = "✅ Votre compte a été réactivé";
        String htmlBody = buildUnbanNotificationHtml(user);
        mailService.sendPasswordResetEmail(user.getEmail(), subject, htmlBody);
    }

    private String buildUnbanNotificationHtml(user user) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"fr\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 0; }\n" +
                "        .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); overflow: hidden; }\n" +
                "        .header { background: linear-gradient(135deg, #16a34a 0%, #15803d 100%); color: white; padding: 40px 20px; text-align: center; }\n" +
                "        .header h1 { margin: 0; font-size: 28px; font-weight: bold; }\n" +
                "        .header p { margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; }\n" +
                "        .success-banner { background-color: #dcfce7; border-left: 4px solid #16a34a; padding: 20px; margin: 0; color: #166534; }\n" +
                "        .success-banner strong { color: #16a34a; font-size: 16px; }\n" +
                "        .content { padding: 40px 30px; }\n" +
                "        .content h2 { color: #16a34a; font-size: 20px; margin: 0 0 20px 0; }\n" +
                "        .content p { color: #333333; line-height: 1.6; margin: 15px 0; }\n" +
                "        .success-box { background-color: #f0fdf4; border-left: 4px solid #16a34a; padding: 20px; margin: 25px 0; border-radius: 4px; }\n" +
                "        .success-box p { color: #166534; margin: 10px 0; }\n" +
                "        .actions { margin: 30px 0; padding: 20px; background-color: #f8f8f8; border-radius: 4px; text-align: center; }\n" +
                "        .actions p { margin: 10px 0; color: #333333; }\n" +
                "        .actions a { background-color: #16a34a; color: white; padding: 10px 25px; text-decoration: none; border-radius: 4px; display: inline-block; font-weight: bold; }\n" +
                "        .footer { background-color: #f8f8f8; padding: 20px 30px; border-top: 1px solid #eeeeee; text-align: center; font-size: 12px; color: #888888; }\n" +
                "        .footer p { margin: 5px 0; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"email-container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>[SUCCES] Bienvenue de retour !</h1>\n" +
                "            <p>EspritFlow - Plateforme d'apprentissage</p>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"success-banner\">\n" +
                "            <strong>Votre compte a ete reactivate avec succes</strong>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"content\">\n" +
                "            <h2>Bonjour " + (user.getPrenom() != null ? user.getPrenom().trim() : "") + ",</h2>\n" +
                "\n" +
                "            <p>Bonne nouvelle ! Votre compte EspritFlow a ete reactivate et vous pouvez a nouveau acceder a la plateforme.</p>\n" +
                "\n" +
                "            <div class=\"success-box\">\n" +
                "                <p><strong>[OK]</strong> Vous pouvez maintenant vous connecter</p>\n" +
                "                <p><strong>[OK]</strong> Acceder a tous vos cours et ressources</p>\n" +
                "                <p><strong>[OK]</strong> Soumettre vos travaux</p>\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"actions\">\n" +
                "                <p><strong>Pret a continuer ?</strong></p>\n" +
                "                <p><a href=\"#\">Se connecter a EspritFlow</a></p>\n" +
                "            </div>\n" +
                "\n" +
                "            <p style=\"color: #666666; font-size: 13px; margin-top: 30px;\">\n" +
                "                Si vous avez des questions, n'hesitez pas a <a href=\"mailto:support@espritflow.tn\" style=\"color: #16a34a; text-decoration: none;\">contacter notre support</a>.\n" +
                "            </p>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"footer\">\n" +
                "            <p>&copy; 2026 EspritFlow. Tous droits reserves.</p>\n" +
                "            <p>Cet email a ete envoye a <strong>" + (user.getEmail() != null ? user.getEmail().trim() : "") + "</strong></p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    public void Register(user u) throws SQLException {
        String sql = "INSERT INTO `user` (email, roles, password, is_verified, nom, prenom, "
                + "num_tel, date_de_naissance, sexe, created_at, is_banned, classe_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, "[\"ROLE_USER\"]");
            ps.setString(3, BCrypt.hashpw(u.getPassword(), BCrypt.gensalt()));
            ps.setInt(4, 1);
            ps.setString(5, u.getNom());
            ps.setString(6, u.getPrenom());
            ps.setString(7, u.getNum_tel());
            ps.setDate(8, Date.valueOf(u.getDate_de_naissance()));
            ps.setString(9, u.getSexe());
            ps.setInt(10, 0);
            ps.setInt(11, u.getClasse_id());
            ps.executeUpdate();
        }
    }

    public user getById(int id) {
        String sql = "SELECT * FROM `user` WHERE id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("getById error: " + e.getMessage());
        }
        return null;
    }

    public user getUserByEmail(String email) {
        String sql = "SELECT * FROM `user` WHERE email=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("getUserByEmail error: " + e.getMessage());
        }
        return null;
    }

    private Connection requireConnection() throws SQLException {
        con = MyDataBase.getInstance().getConnection();
        if (con == null) {
            throw new SQLException("Database connection unavailable. Verify MySQL is running and the 'pidev' database exists.");
        }
        return con;
    }

    private user mapResultSet(ResultSet rs) throws SQLException {
        return new user(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("roles"),
                rs.getString("password"),
                rs.getInt("is_verified"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("num_tel"),
                rs.getString("date_de_naissance"),
                rs.getString("sexe"),
                rs.getString("profile_pic"),
                rs.getString("created_at"),
                rs.getInt("is_banned"),
                rs.getString("ban_reason"),
                rs.getString("banned_at"),
                rs.getString("verification_token"),
                (Integer) rs.getObject("classe_id")
        );
    }
}

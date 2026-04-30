package piJava.services;

import piJava.entities.user;
import piJava.utils.MyDataBase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class loginService {

    private Connection con;

    public loginService() {
        con = MyDataBase.getInstance().getConnection();
    }

    public user login(String email, String rawPassword) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String sql = "SELECT * FROM `user` WHERE email=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // ❌ Check banned
                if (rs.getInt("is_banned") == 1) {
                    System.out.println("❌ Account banned: " + rs.getString("ban_reason"));
                    return null;
                }

                // ❌ Check verified
                if (rs.getInt("is_verified") == 0) {
                    System.out.println("❌ Email not verified yet!");
                    return null;
                }

                // 🔥 Symfony $2y$ fix
                String hash = rs.getString("password");
                if (hash.startsWith("$2y$")) hash = hash.replace("$2y$", "$2a$");

                // ✅ Check password
                if (BCrypt.checkpw(rawPassword, hash)) {
                    int userId = rs.getInt("id");

                    // ── Record last_login timestamp ──────────────────────────
                    try (PreparedStatement upd = requireConnection()
                            .prepareStatement("UPDATE `user` SET last_login = NOW() WHERE id = ?")) {
                        upd.setInt(1, userId);
                        upd.executeUpdate();
                    } catch (SQLException ex) {
                        System.err.println("⚠️ Could not update last_login: " + ex.getMessage());
                    }

                    return new user(
                            userId,
                            rs.getString("email"),
                            rs.getString("roles"),
                            hash,
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
                } else {
                    System.out.println("❌ Wrong password!");
                }

            } else {
                System.out.println("❌ Email not found!");
            }

        } catch (SQLException e) {
            System.err.println("❌ Login failed: " + e.getMessage());
            e.printStackTrace();
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
}

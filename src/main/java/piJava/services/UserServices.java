package piJava.services;

import piJava.entities.user;
import piJava.utils.MyDataBase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserServices implements ICrud<user> {

    private Connection con;

    public UserServices() {
        con = MyDataBase.getInstance().getConnection();
    }

    // ── SHOW ALL ──────────────────────────────────────────────
    @Override
    public List<user> show() {
        List<user> users = new ArrayList<>();
        String sql = "SELECT * FROM `user` ORDER BY id";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) users.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("❌ Error loading users: " + e.getMessage());
        }
        return users;
    }

    // ── ADD (uses full constructor — password hashed with BCrypt) ─
    @Override
    public void add(user u) {
        String sql = "INSERT INTO `user` (email, roles, password, is_verified, nom, prenom, "
                + "num_tel, date_de_naissance, sexe, profile_pic, created_at, "
                + "is_banned, ban_reason, banned_at, verification_token, classe_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,  u.getEmail());
            ps.setString(2,  u.getRoles() != null ? u.getRoles() : "[\"ROLE_USER\"]");
            ps.setString(3,  BCrypt.hashpw(u.getPassword(), BCrypt.gensalt())); // always hash
            ps.setInt(4,     u.getIs_verified());
            ps.setString(5,  u.getNom());
            ps.setString(6,  u.getPrenom());
            ps.setString(7,  u.getNum_tel());
            // date_de_naissance is stored as String "yyyy-MM-dd" in your entity
            if (u.getDate_de_naissance() != null && !u.getDate_de_naissance().isEmpty())
                ps.setDate(8, Date.valueOf(u.getDate_de_naissance()));
            else
                ps.setNull(8, Types.DATE);
            ps.setString(9,  u.getSexe());
            ps.setString(10, u.getProfile_pic());       // profile_pic (nullable)
            ps.setInt(11,    u.getIs_banned());
            ps.setString(12, u.getBan_reason());        // ban_reason (nullable)
            ps.setString(13, u.getBanned_at());         // banned_at (nullable)
            ps.setString(14, u.getVerification_token());// token (nullable)
            if (u.getClasse_id() != null) ps.setInt(15, u.getClasse_id());
            else                          ps.setNull(15, Types.INTEGER);

            ps.executeUpdate();

            // Set generated ID back on the object
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getInt(1));
            }
            System.out.println("✅ User ajouté : " + u.getPrenom() + " " + u.getNom());
        } catch (SQLException e) {
            System.err.println("❌ Failed to add user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── DELETE (cascades reset_password_request first) ────────
    @Override
    public void delete(int id) {
        try {
            con.setAutoCommit(false);

            // Remove FK-constrained child rows first
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM reset_password_request WHERE user_id=?")) {
                ps.setInt(1, id); ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM `user` WHERE id=?")) {
                ps.setInt(1, id); ps.executeUpdate();
            }

            con.commit();
            System.out.println("✅ User supprimé (id=" + id + ")");
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("❌ Failed to delete user: " + e.getMessage());
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ── EDIT ──────────────────────────────────────────────────
    @Override
    public void edit(user u) {
        String sql = "UPDATE `user` SET email=?, roles=?, nom=?, prenom=?, "
                + "num_tel=?, date_de_naissance=?, sexe=?, classe_id=?, profile_pic=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getRoles());
            ps.setString(3, u.getNom());
            ps.setString(4, u.getPrenom());
            ps.setString(5, u.getNum_tel());
            if (u.getDate_de_naissance() != null && !u.getDate_de_naissance().isEmpty())
                ps.setDate(6, Date.valueOf(u.getDate_de_naissance()));
            else
                ps.setNull(6, Types.DATE);
            ps.setString(7, u.getSexe());
            if (u.getClasse_id() != null) ps.setInt(8, u.getClasse_id());
            else                          ps.setNull(8, Types.INTEGER);
            ps.setString(9, u.getProfile_pic());
            ps.setInt(10, u.getId());
            ps.executeUpdate();
            System.out.println("✅ User modifié : " + u.getPrenom() + " " + u.getNom());
        } catch (SQLException e) {
            System.err.println("❌ Failed to edit user: " + e.getMessage());
            e.printStackTrace();
        }

        // Update password only if a new one was provided AND it's not already a BCrypt hash
        if (u.getPassword() != null && !u.getPassword().isEmpty()) {
            // BCrypt hashes from jBCrypt/PHP start with $2a$, $2y$, or $2b$
            String pwd = u.getPassword();
            if (!pwd.startsWith("$2a$") && !pwd.startsWith("$2y$") && !pwd.startsWith("$2b$")) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE `user` SET password=? WHERE id=?")) {
                    ps.setString(1, BCrypt.hashpw(u.getPassword(), BCrypt.gensalt()));
                    ps.setInt(2, u.getId());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("❌ Failed to update password: " + e.getMessage());
                }
            }
        }
    }

    // ── BAN ───────────────────────────────────────────────────
    public void banUser(int id, String reason) {
        String sql = "UPDATE `user` SET is_banned=1, ban_reason=?, banned_at=NOW() WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("🚫 User banni (id=" + id + ")");
        } catch (SQLException e) {
            System.err.println("❌ Failed to ban user: " + e.getMessage());
        }
    }

    // ── UNBAN ─────────────────────────────────────────────────
    public void unbanUser(int id) {
        String sql = "UPDATE `user` SET is_banned=0, ban_reason=NULL, banned_at=NULL WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ User débanni (id=" + id + ")");
        } catch (SQLException e) {
            System.err.println("❌ Failed to unban user: " + e.getMessage());
        }
    }

    // ── REGISTER (with classe_id) ─────────────────────────────
    public void Register(user u) {
        String sql = "INSERT INTO `user` (email, roles, password, is_verified, nom, prenom, "
                + "num_tel, date_de_naissance, sexe, created_at, is_banned, classe_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
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
            System.out.println("✅ Inscription réussie : " + u.getEmail());
        } catch (SQLException e) {
            System.err.println("❌ Failed to register: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── GET BY ID ─────────────────────────────────────────────
    public user getById(int id) {
        String sql = "SELECT * FROM `user` WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ getById error: " + e.getMessage());
        }
        return null;
    }

    // ── PRIVATE MAPPER ────────────────────────────────────────
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
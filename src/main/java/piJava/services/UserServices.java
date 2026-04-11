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

    // ─── DISPLAY ALL USERS ───────────────────────────────
    @Override
    public List<user> show() {
        List<user> users = new ArrayList<>();
        String sql = "SELECT * FROM `user`";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                user u = new user(
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
                users.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error displaying users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    // ─── ADD USER ───────────────────────────────────────
    @Override
    public void add(user u) {
        String sql = "INSERT INTO `user` (email, roles, password, is_verified, nom, prenom, num_tel, date_de_naissance, sexe, created_at, is_banned) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getEmail());
            ps.setString(2, "[\"ROLE_USER\"]");  // JSON role
            ps.setString(3, BCrypt.hashpw(u.getPassword(), BCrypt.gensalt())); // hash password
            ps.setInt(4, 1); // verified by default
            ps.setString(5, u.getNom());
            ps.setString(6, u.getPrenom());
            ps.setString(7, u.getNum_tel());
            ps.setDate(8, Date.valueOf(u.getDate_de_naissance())); // yyyy-MM-dd
            ps.setString(9, u.getSexe());
            ps.setInt(10, 0); // not banned

            ps.executeUpdate();
            System.out.println("✅ User ajouté avec succès");
        } catch (SQLException e) {
            System.err.println("❌ Failed to add user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─── DELETE USER ────────────────────────────────────
    @Override
    public void delete(int id) {
        try {
            con.setAutoCommit(false); // transaction

            // 1. delete child first
            String sql1 = "DELETE FROM reset_password_request WHERE user_id=?";
            try (PreparedStatement ps1 = con.prepareStatement(sql1)) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
            }

            // 2. delete user
            String sql2 = "DELETE FROM `user` WHERE id=?";
            try (PreparedStatement ps2 = con.prepareStatement(sql2)) {
                ps2.setInt(1, id);
                ps2.executeUpdate();
            }

            con.commit();
            System.out.println("✅ User supprimé avec succès");

        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("❌ Failed to delete user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ─── UPDATE USER ────────────────────────────────────
    @Override
    public void edit(user u) {
        String sql = "UPDATE `user` SET email=?, nom=?, prenom=?, num_tel=?, date_de_naissance=?, sexe=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getNom());
            ps.setString(3, u.getPrenom());
            ps.setString(4, u.getNum_tel());
            ps.setDate(5, Date.valueOf(u.getDate_de_naissance()));
            ps.setString(6, u.getSexe());
            ps.setInt(7, u.getId());

            ps.executeUpdate();
            System.out.println("✅ User modifié avec succès");

        } catch (SQLException e) {
            System.err.println("❌ Failed to update user: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void Register(user u) {
        String sql = "INSERT INTO `user` (email, roles, password, is_verified, nom, prenom, num_tel, date_de_naissance, sexe, created_at, is_banned,classe_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getEmail());
            ps.setString(2, "[\"ROLE_USER\"]");  // JSON role
            ps.setString(3, BCrypt.hashpw(u.getPassword(), BCrypt.gensalt())); // hash password
            ps.setInt(4, 1); // verified by default
            ps.setString(5, u.getNom());
            ps.setString(6, u.getPrenom());
            ps.setString(7, u.getNum_tel());
            ps.setDate(8, Date.valueOf(u.getDate_de_naissance())); // yyyy-MM-dd
            ps.setString(9, u.getSexe());
            ps.setInt(10, 0); // not banned
            ps.setInt(11, u.getClasse_id());

            ps.executeUpdate();
            System.out.println("✅ User ajouté avec succès");
        } catch (SQLException e) {
            System.err.println("❌ Failed to add user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
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
import java.util.ArrayList;
import java.util.List;

public class UserServices implements ICrud<user> {

    private Connection con;

    public UserServices() {
        con = MyDataBase.getInstance().getConnection();
    }

    @Override
    public List<user> show() {
        List<user> users = new ArrayList<>();
        String sql = "SELECT * FROM `user`";
        try (Statement st = requireConnection().createStatement();
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

    @Override
    public void add(user u) {
        String sql = "INSERT INTO `user` (email, roles, password, is_verified, nom, prenom, num_tel, date_de_naissance, sexe, created_at, is_banned) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(u.getEmail()));
            ps.setString(2, "[\"ROLE_USER\"]");
            ps.setString(3, BCrypt.hashpw(u.getPassword(), BCrypt.gensalt()));
            ps.setInt(4, 1);
            ps.setString(5, safeTrim(u.getNom()));
            ps.setString(6, safeTrim(u.getPrenom()));
            ps.setString(7, safeTrim(u.getNum_tel()));
            ps.setDate(8, Date.valueOf(u.getDate_de_naissance()));
            ps.setString(9, safeTrim(u.getSexe()));
            ps.setInt(10, 0);

            ps.executeUpdate();
            System.out.println("User ajoute avec succes");
        } catch (SQLException e) {
            System.err.println("Failed to add user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        try {
            Connection connection = requireConnection();
            connection.setAutoCommit(false);

            String sql1 = "DELETE FROM reset_password_request WHERE user_id=?";
            try (PreparedStatement ps1 = connection.prepareStatement(sql1)) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
            }

            String sql2 = "DELETE FROM `user` WHERE id=?";
            try (PreparedStatement ps2 = connection.prepareStatement(sql2)) {
                ps2.setInt(1, id);
                ps2.executeUpdate();
            }

            connection.commit();
            System.out.println("User supprime avec succes");
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Failed to delete user: " + e.getMessage());
            e.printStackTrace();
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
        String sql = "UPDATE `user` SET email=?, nom=?, prenom=?, num_tel=?, date_de_naissance=?, sexe=? WHERE id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(u.getEmail()));
            ps.setString(2, safeTrim(u.getNom()));
            ps.setString(3, safeTrim(u.getPrenom()));
            ps.setString(4, safeTrim(u.getNum_tel()));
            ps.setDate(5, Date.valueOf(u.getDate_de_naissance()));
            ps.setString(6, safeTrim(u.getSexe()));
            ps.setInt(7, u.getId());

            ps.executeUpdate();
            System.out.println("User modifie avec succes");
        } catch (SQLException e) {
            System.err.println("Failed to update user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void Register(user u) throws SQLException {
        String normalizedEmail = normalizeEmail(u.getEmail());
        if (emailExists(normalizedEmail)) {
            throw new SQLException("Cet email existe deja.");
        }

        String sql = "INSERT INTO `user` (email, roles, password, is_verified, nom, prenom, num_tel, date_de_naissance, sexe, created_at, is_banned, classe_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            ps.setString(2, "[\"ROLE_USER\"]");
            ps.setString(3, BCrypt.hashpw(u.getPassword(), BCrypt.gensalt()));
            ps.setInt(4, 1);
            ps.setString(5, safeTrim(u.getNom()));
            ps.setString(6, safeTrim(u.getPrenom()));
            ps.setString(7, safeTrim(u.getNum_tel()));
            ps.setDate(8, Date.valueOf(u.getDate_de_naissance()));
            ps.setString(9, safeTrim(u.getSexe()));
            ps.setInt(10, 0);
            ps.setInt(11, u.getClasse_id());
            ps.executeUpdate();
        }
    }

    private boolean emailExists(String email) throws SQLException {
        String sql = "SELECT id FROM `user` WHERE email = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Connection requireConnection() throws SQLException {
        con = MyDataBase.getInstance().getConnection();
        if (con == null) {
            throw new SQLException("Database connection unavailable. Verify MySQL is running and the 'pidev' database exists.");
        }
        return con;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }
}

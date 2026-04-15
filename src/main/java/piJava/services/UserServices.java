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
            if (u.getDate_de_naissance() != null && !u.getDate_de_naissance().isEmpty()) {
                ps.setDate(8, Date.valueOf(u.getDate_de_naissance()));
            } else {
                ps.setNull(8, Types.DATE);
            }
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
            System.err.println("Failed to add user: " + e.getMessage());
            e.printStackTrace();
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

    public void banUser(int id, String reason) {
        String sql = "UPDATE `user` SET is_banned=1, ban_reason=?, banned_at=NOW() WHERE id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to ban user: " + e.getMessage());
        }
    }

    public void unbanUser(int id) {
        String sql = "UPDATE `user` SET is_banned=0, ban_reason=NULL, banned_at=NULL WHERE id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to unban user: " + e.getMessage());
        }
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

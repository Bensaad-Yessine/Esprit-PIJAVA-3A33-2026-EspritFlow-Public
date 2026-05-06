package piJava.services;


import piJava.entities.Notification;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class NotificationService implements ICrud<Notification>{

    Connection con;
    public NotificationService() {
        con = MyDataBase.getInstance().getConnection();
    }

    @Override
    public List<Notification> show() throws SQLException {

        return List.of();
    }

    @Override
    public void add(Notification notification) throws SQLException {
        String sql = "INSERT INTO `notification`( `message`, `type`, `is_read`, `created_at`, `user_id`, `tache_id`, `email`) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement sp = con.prepareStatement(sql);
        sp.setString(1, notification.getMessage());
        sp.setString(2, notification.getType());
        sp.setBoolean(3, notification.isRead());
        sp.setTimestamp(4, Timestamp.valueOf(notification.getCreatedAt()));
        sp.setInt(5, notification.getUserId());
        if (notification.getTacheId() != null) {
            sp.setInt(6, notification.getTacheId());
        } else {
            sp.setNull(6, Types.INTEGER);
        }
        sp.setBoolean(7, notification.isEmail());
        sp.executeUpdate();
        sp.close();
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM notification WHERE `id`= ?";
        PreparedStatement sp = con.prepareStatement(sql);
        sp.setInt(1, id);
        sp.executeUpdate();
        sp.close();
    }

    @Override
    public void edit(Notification notification) throws SQLException {
        String sql = "UPDATE notification SET message=?, type=?, is_read=?, created_at=?, user_id=?, tache_id=?, email=? WHERE id=?";
        PreparedStatement sp = con.prepareStatement(sql);
        sp.setString(1, notification.getMessage());
        sp.setString(2, notification.getType());
        sp.setBoolean(3, notification.isRead());

        if (notification.getCreatedAt() != null) {
            sp.setTimestamp(4, Timestamp.valueOf(notification.getCreatedAt()));
        } else {
            sp.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
        }

        sp.setInt(5, notification.getUserId());

        if (notification.getTacheId() != null) {
            sp.setInt(6, notification.getTacheId());
        } else {
            sp.setNull(6, Types.INTEGER);
        }

        sp.setBoolean(7, notification.isEmail());
        sp.setInt(8, notification.getId());

        sp.executeUpdate();
        sp.close();
    }

    public List<Notification> showUserNotifs(int userId) throws SQLException {
        List<Notification> notifs = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE user_id = ?";
        PreparedStatement sp = con.prepareStatement(sql);
        sp.setInt(1, userId);
        ResultSet rs  = sp.executeQuery();
        while (rs.next()) {
            Notification notif = new Notification();
            notif.setId(rs.getInt("id"));
            notif.setMessage(rs.getString("message"));
            notif.setType(rs.getString("type"));
            notif.setRead(rs.getBoolean("is_read"));
            notif.setUserId(rs.getInt("user_id"));
            notif.setTacheId(rs.getInt("tache_id"));
            notif.setEmail(rs.getBoolean("email"));
            notif.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            notifs.add(notif);
        }
        return notifs;
    }
}
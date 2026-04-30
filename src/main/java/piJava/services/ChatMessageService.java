package piJava.services;

import piJava.entities.ChatMessage;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageService {

    private final Connection con = MyDataBase.getInstance().getConnection();

    public void save(ChatMessage message) throws SQLException {
        String sql = "INSERT INTO chat_message (group_id, sender_id, content, sent_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getGroupId());
            ps.setInt(2, message.getSenderId());
            ps.setString(3, message.getContent());
            ps.setTimestamp(4,
                    Timestamp.valueOf(message.getSentAt() != null ? message.getSentAt() : LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    message.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<ChatMessage> getByGroupId(int groupId) throws SQLException {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.nom, u.prenom FROM chat_message m LEFT JOIN user u ON m.sender_id = u.id WHERE m.group_id = ? ORDER BY m.sent_at ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSet(rs));
                }
            }
        }
        return messages;
    }

    public List<ChatMessage> getByGroupIdSince(int groupId, LocalDateTime since) throws SQLException {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.nom, u.prenom FROM chat_message m LEFT JOIN user u ON m.sender_id = u.id WHERE m.group_id = ? AND m.sent_at > ? ORDER BY m.sent_at ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setTimestamp(2, Timestamp.valueOf(since));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSet(rs));
                }
            }
        }
        return messages;
    }

    private ChatMessage mapResultSet(ResultSet rs) throws SQLException {
        ChatMessage msg = new ChatMessage();
        msg.setId(rs.getInt("id"));
        msg.setGroupId(rs.getInt("group_id"));
        msg.setSenderId(rs.getInt("sender_id"));
        
        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        msg.setSender((prenom != null ? prenom : "") + " " + (nom != null ? nom : ""));
        
        msg.setContent(rs.getString("content"));
        Timestamp ts = rs.getTimestamp("sent_at");
        msg.setSentAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        return msg;
    }
}

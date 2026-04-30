package piJava.Controllers.group;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GroupChatMessage {
    private static final String DELIMITER = "\\|";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final int groupId;
    private final String sender;
    private final String content;
    private final LocalDateTime sentAt;

    public GroupChatMessage(int groupId, String sender, String content) {
        this(groupId, sender, content, LocalDateTime.now());
    }

    public GroupChatMessage(int groupId, String sender, String content, LocalDateTime sentAt) {
        this.groupId = groupId;
        this.sender = sender;
        this.content = content;
        this.sentAt = sentAt;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getDisplayValue() {
        return "[" + sentAt.format(TIME_FORMATTER) + "] " + sender + ": " + content;
    }

    public String encode() {
        return groupId + "|" + escape(sender) + "|" + escape(content) + "|" + sentAt;
    }

    public static GroupChatMessage decode(String line) {
        String[] parts = line.split(DELIMITER, 4);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid chat message format");
        }

        int groupId = Integer.parseInt(parts[0]);
        String sender = unescape(parts[1]);
        String content = unescape(parts[2]);
        LocalDateTime sentAt = LocalDateTime.parse(parts[3]);
        return new GroupChatMessage(groupId, sender, content, sentAt);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("|", "\\p");
    }

    private static String unescape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        String result = value.replace("\\p", "|");
        return result.replace("\\\\", "\\");
    }
}

package piJava.entities;

import java.time.LocalDateTime;

public class ChatMessage {

    private int id;
    private int groupId;
    private int senderId;
    private String sender;
    private String content;
    private LocalDateTime sentAt;

    public ChatMessage() {
    }

    public ChatMessage(int groupId, String sender, String content) {
        this.groupId = groupId;
        this.sender = sender;
        this.content = content;
        this.sentAt = LocalDateTime.now();
    }

    public ChatMessage(int groupId, String sender, String content, LocalDateTime sentAt) {
        this.groupId = groupId;
        this.sender = sender;
        this.content = content;
        this.sentAt = sentAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}

package piJava.entities;

import java.time.LocalDateTime;


public class Notification {

    private int id;
    private String message;
    private String type; // INFO, WARNING, RISK
    private boolean isRead;
    private LocalDateTime createdAt;
    private int userId;
    private Integer tacheId; // nullable
    private boolean email;



    // Constructor
    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }
    public Notification(int id, String message, String type, boolean isRead,
                        LocalDateTime createdAt, int userId, Integer tacheId, boolean email) {
        this.id = id;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.userId = userId;
        this.tacheId = tacheId;
        this.email = email;
    }


    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) {
        if (!type.equals("INFO") && !type.equals("WARNING") && !type.equals("RISK")) {
            throw new IllegalArgumentException("Invalid notification type");
        }
        this.type = type;
    }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Integer getTacheId() { return tacheId; }
    public void setTacheId(Integer tacheId) { this.tacheId = tacheId; }
    public boolean isEmail() { return email; }
    public void setEmail(boolean email) { this.email = email; }
}
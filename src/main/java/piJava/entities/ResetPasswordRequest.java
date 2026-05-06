package piJava.entities;

import java.time.LocalDateTime;

public class ResetPasswordRequest {

    private int id;
    private String selector;
    private String hashedToken;
    private LocalDateTime requestedAt;
    private LocalDateTime expiresAt;
    private int userId;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(int id, String selector, String hashedToken,
                                LocalDateTime requestedAt, LocalDateTime expiresAt, int userId) {
        this.id = id;
        this.selector = selector;
        this.hashedToken = hashedToken;
        this.requestedAt = requestedAt;
        this.expiresAt = expiresAt;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getHashedToken() {
        return hashedToken;
    }

    public void setHashedToken(String hashedToken) {
        this.hashedToken = hashedToken;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}


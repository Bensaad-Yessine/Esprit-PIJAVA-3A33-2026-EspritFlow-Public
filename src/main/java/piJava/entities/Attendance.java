package piJava.entities;

import java.sql.Timestamp;

public class Attendance {
    private int id;
    private int seanceId;
    private int userId;
    private String status;
    private Timestamp scannedAt;

    public Attendance() {
    }

    public Attendance(int id, int seanceId, int userId, String status, Timestamp scannedAt) {
        this.id = id;
        this.seanceId = seanceId;
        this.userId = userId;
        this.status = status;
        this.scannedAt = scannedAt;
    }

    public Attendance(int seanceId, int userId, String status, Timestamp scannedAt) {
        this.seanceId = seanceId;
        this.userId = userId;
        this.status = status;
        this.scannedAt = scannedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSeanceId() {
        return seanceId;
    }

    public void setSeanceId(int seanceId) {
        this.seanceId = seanceId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(Timestamp scannedAt) {
        this.scannedAt = scannedAt;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", seanceId=" + seanceId +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                ", scannedAt=" + scannedAt +
                '}';
    }
}

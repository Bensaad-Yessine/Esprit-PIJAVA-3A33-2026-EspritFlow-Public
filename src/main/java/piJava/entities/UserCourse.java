package piJava.entities;

import java.time.LocalDateTime;

/**
 * UserCourse - Track user course completion and progress.
 * Prevents duplicate completions and stores completion metadata.
 */
public class UserCourse {
    private int id;
    private int userId;
    private int courseId;
    private String apiCourseId;
    private boolean isCompleted;
    private LocalDateTime completionDate;
    private int progressPercentage;
    private LocalDateTime enrolledDate;
    private LocalDateTime lastAccessed;
    private String notes;

    // ─── Constructors ───────────────────────────────────────
    public UserCourse() {}

    public UserCourse(int userId, int courseId, String apiCourseId) {
        this.userId = userId;
        this.courseId = courseId;
        this.apiCourseId = apiCourseId;
        this.isCompleted = false;
        this.progressPercentage = 0;
        this.enrolledDate = LocalDateTime.now();
    }

    public UserCourse(int userId, int courseId, boolean isCompleted, 
                      LocalDateTime completionDate, int progressPercentage) {
        this(userId, courseId, null);
        this.isCompleted = isCompleted;
        this.completionDate = completionDate;
        this.progressPercentage = progressPercentage;
    }

    // ─── Getters & Setters ──────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getApiCourseId() { return apiCourseId; }
    public void setApiCourseId(String apiCourseId) { this.apiCourseId = apiCourseId; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public LocalDateTime getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDateTime completionDate) { this.completionDate = completionDate; }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }

    public LocalDateTime getEnrolledDate() { return enrolledDate; }
    public void setEnrolledDate(LocalDateTime enrolledDate) { this.enrolledDate = enrolledDate; }

    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // ─── Utility Methods ────────────────────────────────────
    public void markAsCompleted() {
        this.isCompleted = true;
        this.completionDate = LocalDateTime.now();
        this.progressPercentage = 100;
    }

    public boolean isRecent(int daysBack) {
        if (completionDate == null) return false;
        return completionDate.isAfter(LocalDateTime.now().minusDays(daysBack));
    }

    @Override
    public String toString() {
        return "UserCourse{" +
                "id=" + id +
                ", userId=" + userId +
                ", courseId=" + courseId +
                ", isCompleted=" + isCompleted +
                ", progress=" + progressPercentage + "%}";
    }
}


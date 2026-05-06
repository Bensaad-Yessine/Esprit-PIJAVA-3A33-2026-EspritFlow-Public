package piJava.entities;

import java.time.LocalDate;

/**
 * Course entity - represents a course from the Udemy API or cached locally.
 */
public class Course {
    private int id;
    private String apiId;
    private String title;
    private String description;
    private String category;
    private String couponCode;
    private LocalDate expirationDate;
    private String courseUrl;
    private String instructor;
    private double rating;
    private int studentsEnrolled;
    private String thumbnailUrl;
    private long cachedAt;

    // ─── Constructors ───────────────────────────────────────
    public Course() {}

    public Course(String apiId, String title, String description, String category,
                  String couponCode, LocalDate expirationDate, String courseUrl) {
        this.apiId = apiId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.couponCode = couponCode;
        this.expirationDate = expirationDate;
        this.courseUrl = courseUrl;
        this.cachedAt = System.currentTimeMillis();
    }

    public Course(int id, String apiId, String title, String description, String category,
                  String couponCode, LocalDate expirationDate, String courseUrl,
                  String instructor, double rating, int studentsEnrolled, String thumbnailUrl) {
        this(apiId, title, description, category, couponCode, expirationDate, courseUrl);
        this.id = id;
        this.instructor = instructor;
        this.rating = rating;
        this.studentsEnrolled = studentsEnrolled;
        this.thumbnailUrl = thumbnailUrl;
    }

    // ─── Getters & Setters ──────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getApiId() { return apiId; }
    public void setApiId(String apiId) { this.apiId = apiId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public String getCourseUrl() { return courseUrl; }
    public void setCourseUrl(String courseUrl) { this.courseUrl = courseUrl; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getStudentsEnrolled() { return studentsEnrolled; }
    public void setStudentsEnrolled(int studentsEnrolled) { this.studentsEnrolled = studentsEnrolled; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }

    // ─── Utility Methods ────────────────────────────────────
    public boolean isCouponValid() {
        return expirationDate != null && !LocalDate.now().isAfter(expirationDate);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", rating=" + rating +
                ", isCouponValid=" + isCouponValid() +
                '}';
    }
}


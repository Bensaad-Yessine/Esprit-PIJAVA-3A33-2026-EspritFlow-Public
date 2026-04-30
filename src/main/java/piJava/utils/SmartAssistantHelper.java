package piJava.utils;

import piJava.entities.*;
import piJava.services.*;

import java.util.List;

/**
 * SmartAssistantHelper - Wrapper class to simplify integration in JavaFX controllers.
 * 
 * Usage:
 *   SmartAssistantHelper helper = new SmartAssistantHelper(userId);
 *   helper.searchCourses("Python");
 *   helper.enrollInCourse(courseId);
 *   helper.getRecommendations();
 */
public class SmartAssistantHelper {

    private final int userId;
    private final CourseService courseService;
    private final UserCourseService userCourseService;
    private final StreakService streakService;
    private final RecommendationService recommendationService;
    private final AchievementService achievementService;
    private final CourseCacheService cacheService;
    private final RateLimitService rateLimitService;

    public SmartAssistantHelper(int userId) {
        this.userId = userId;
        this.courseService = new CourseService();
        this.userCourseService = new UserCourseService();
        this.streakService = new StreakService();
        this.recommendationService = new RecommendationService();
        this.achievementService = new AchievementService();
        this.cacheService = new CourseCacheService();
        this.rateLimitService = new RateLimitService();
    }

    // ── Course Discovery ────────────────────────────────────

    /**
     * Search for courses by keyword.
     */
    public List<Course> searchCourses(String query) throws Exception {
        checkRateLimit("search");
        return courseService.searchCourses(query, 1, 20);
    }

    /**
     * Search with pagination.
     */
    public List<Course> searchCourses(String query, int page, int pageSize) throws Exception {
        checkRateLimit("search");
        return courseService.searchCourses(query, page, pageSize);
    }

    /**
     * Browse courses by category.
     */
    public List<Course> getCoursesByCategory(String category) throws Exception {
        checkRateLimit("browse");
        return courseService.getCoursesByCategory(category, 1);
    }

    /**
     * Get trending courses.
     */
    public List<Course> getTrendingCourses() throws Exception {
        checkRateLimit("trending");
        return courseService.getTrendingCourses(1);
    }

    /**
     * Get cached course details.
     */
    public Course getCourseDetails(int courseId) {
        return cacheService.getCachedCourse(courseId);
    }

    // ── User Course Management ──────────────────────────────

    /**
     * Enroll in a course.
     * Automatically caches the course and creates enrollment.
     */
    public boolean enrollInCourse(Course course) {
        try {
            // Cache the course
            int cachedId = cacheService.cacheourse(course);
            
            // Create enrollment
            UserCourse uc = userCourseService.enrollUserInCourse(
                userId, cachedId, course.getApiId()
            );
            
            if (uc != null) {
                // Update streak on enrollment (max 1 per day)
                streakService.updateStreakOnEnrollment(userId);
                
                // Record interest
                recommendationService.recordInterest(userId, course.getCategory());
                return true;
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("[SmartAssistant] Error enrolling user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mark a course as completed.
     * Automatically triggers streak update and achievement checks.
     */
    public boolean completeCourse(int courseId) {
        try {
            // Check rate limit
            if (!rateLimitService.canCompleteMoreCourses(userId)) {
                System.err.println("[SmartAssistant] You have completed max courses today");
                return false;
            }

            // Mark complete
            boolean marked = userCourseService.markCourseAsCompleted(userId, courseId);
            
            if (marked) {
                // Update streak (core logic)
                streakService.updateStreakOnCompletion(userId);
                
                // Log the request
                rateLimitService.recordRequest(userId, "completion");
                
                return true;
            }
        } catch (Exception e) {
            System.err.println("[SmartAssistant] Error completing course: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get user's enrolled courses.
     */
    public List<UserCourse> getEnrolledCourses() {
        return userCourseService.getUserCourses(userId);
    }

    /**
     * Get user's completed courses.
     */
    public List<UserCourse> getCompletedCourses() {
        return userCourseService.getCompletedCourses(userId);
    }

    /**
     * Update progress on a course (0-100%).
     */
    public boolean updateProgress(int courseId, int percentage) {
        return userCourseService.updateProgress(userId, courseId, percentage);
    }

    // ── Streak & Gamification ──────────────────────────────

    /**
     * Get current daily streak.
     */
    public int getCurrentStreak() {
        return streakService.getCurrentStreak(userId);
    }

    /**
     * Get all-time longest streak.
     */
    public int getLongestStreak() {
        return streakService.getLongestStreak(userId);
    }

    /**
     * Check if user's streak is at risk.
     * Returns true if they didn't complete a course yesterday.
     */
    public boolean isStreakAtRisk() {
        return streakService.isStreakAtRisk(userId);
    }

    /**
     * Get days remaining until streak is lost.
     * Returns 0 if no active streak, 1 if must complete today, etc.
     */
    public int getDaysUntilStreakLost() {
        return streakService.getDaysUntilStreakLost(userId);
    }

    /**
     * Manually reset streak (admin only).
     */
    public boolean resetStreak() {
        return streakService.resetStreak(userId);
    }

    // ── Recommendations ────────────────────────────────────

    /**
     * Get personalized course recommendations.
     */
    public List<Course> getRecommendations(int limit) {
        return recommendationService.getRecommendations(userId, limit);
    }

    /**
     * Get similar courses to a given course.
     */
    public List<Course> getSimilarCourses(Course course, int limit) {
        return recommendationService.getSimilarCourses(userId, course, limit);
    }

    /**
     * Record interest in a category.
     */
    public void recordInterest(String category) {
        recommendationService.recordInterest(userId, category);
    }

    // ── Achievements ───────────────────────────────────────

    /**
     * Get all user achievements.
     */
    public List<Achievement> getAchievements() {
        return achievementService.getUserAchievements(userId);
    }

    /**
     * Check if user has earned an achievement.
     */
    public boolean hasAchievement(Achievement.AchievementType type) {
        return achievementService.hasEarnedAchievement(userId, type.getTypeId());
    }

    /**
     * Get count of achievements earned.
     */
    public int getAchievementCount() {
        return getAchievements().size();
    }

    // ── Statistics & Dashboard ─────────────────────────────

    /**
     * Get user's learning statistics.
     */
    public LearningStats getStats() {
        return new LearningStats(
            userCourseService.getTotalCompletedCount(userId),
            streakService.getCurrentStreak(userId),
            streakService.getLongestStreak(userId),
            getAchievements().size()
        );
    }

    /**
     * Get learning statistics.
     */
    public static class LearningStats {
        public final int totalCoursesCompleted;
        public final int currentStreak;
        public final int longestStreak;
        public final int achievementsEarned;

        public LearningStats(int total, int current, int longest, int achievements) {
            this.totalCoursesCompleted = total;
            this.currentStreak = current;
            this.longestStreak = longest;
            this.achievementsEarned = achievements;
        }

        @Override
        public String toString() {
            return "Stats{" +
                    "completed=" + totalCoursesCompleted +
                    ", streak=" + currentStreak + "/" + longestStreak +
                    ", badges=" + achievementsEarned +
                    '}';
        }
    }

    // ── Admin/Support ──────────────────────────────────────

    /**
     * Check API health.
     */
    public boolean isAPIHealthy() {
        return courseService.isAPIHealthy();
    }

    /**
     * Clear cache (admin only).
     */
    public void clearCache() {
        courseService.clearCache();
    }

    /**
     * Clean old cached courses from DB.
     */
    public int cleanOldCache() {
        return cacheService.cleanOldCache();
    }

    // ── Internal Helpers ───────────────────────────────────

    private void checkRateLimit(String requestType) throws Exception {
        if (rateLimitService.isRateLimited(userId, requestType)) {
            throw new Exception("Rate limit exceeded for " + requestType);
        }
        rateLimitService.recordRequest(userId, requestType);
    }

    /**
     * Get all service instances (for advanced usage).
     */
    public CourseService getCourseService() { return courseService; }
    public UserCourseService getUserCourseService() { return userCourseService; }
    public StreakService getStreakService() { return streakService; }
    public RecommendationService getRecommendationService() { return recommendationService; }
    public AchievementService getAchievementService() { return achievementService; }
}


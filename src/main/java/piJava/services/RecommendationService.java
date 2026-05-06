package piJava.services;

import piJava.entities.Course;
import piJava.entities.UserInterest;
import piJava.utils.MyDataBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RecommendationService - Behavior-based course recommendations.
 *
 * Strategy:
 *  1. Track user interests by category
 *  2. Recommend courses from top 3 categories
 *  3. Exclude already completed/enrolled courses
 *  4. Sort by rating and recency
 */
public class RecommendationService {

    private final MyDataBase db;
    private final CourseService courseService;
    private final UserInterestService userInterestService;
    private final UserCourseService userCourseService;

    public RecommendationService() {
        this.db = MyDataBase.getInstance();
        this.courseService = new CourseService();
        this.userInterestService = new UserInterestService();
        this.userCourseService = new UserCourseService();
    }

    /**
     * Get personalized course recommendations for a user.
     */
    public List<Course> getRecommendations(int userId, int limit) {
        try {
            List<UserInterest> topInterests = userInterestService.getTopInterests(userId, 5);

            if (topInterests.isEmpty()) {
                return courseService.getTrendingCourses(1);
            }

            List<Course> recommendations = new ArrayList<>();
            Set<Integer> usedCourseIds = new HashSet<>();

            for (UserInterest interest : topInterests) {
                try {
                    List<Course> categoryCourses = courseService.getCoursesByCategory(interest.getCategory(), 1);

                    for (Course course : categoryCourses) {
                        if (usedCourseIds.contains(course.getId())) {
                            continue;
                        }

                        if (userCourseService.getUserCourse(userId, course.getId()) == null) {
                            recommendations.add(course);
                            usedCourseIds.add(course.getId());

                            if (recommendations.size() >= limit) {
                                break;
                            }
                        }
                    }

                    if (recommendations.size() >= limit) {
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("[RecommendationService] Error fetching " + interest.getCategory() + ": " + e.getMessage());
                }
            }

            return recommendations.stream().limit(limit).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("[RecommendationService] Error generating recommendations: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Record user interest when they view/enroll in a course.
     */
    public void recordInterest(int userId, String category) {
        userInterestService.getOrCreateInterest(userId, category);
    }

    /**
     * Get trending courses across all users.
     */
    public List<Course> getTrendingCourses(int limit) {
        try {
            return courseService.getTrendingCourses(1).stream().limit(limit).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("[RecommendationService] Error fetching trending: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get similar courses to a given course.
     */
    public List<Course> getSimilarCourses(int userId, Course baseCourse, int limit) {
        try {
            recordInterest(userId, baseCourse.getCategory());
            return courseService.getCoursesByCategory(baseCourse.getCategory(), 1)
                    .stream()
                    .filter(c -> c.getId() != baseCourse.getId())
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("[RecommendationService] Error fetching similar courses: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}

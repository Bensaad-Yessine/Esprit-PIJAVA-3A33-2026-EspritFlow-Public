# 🎓 Smart Assistant for Students - Complete Architecture

> **An intelligent learning assistant system integrated with Udemy free courses and built-in streak/achievement tracking**.

---

## 📋 Table of Contents

1. [System Overview](#system-overview)
2. [Database Schema](#database-schema)
3. [Architecture & Design](#architecture--design)
4. [API Integration](#api-integration)
5. [Core Features](#core-features)
6. [Implementation Guide](#implementation-guide)
7. [Testing & Deployment](#testing--deployment)
8. [Example Flows](#example-flows)

---

## System Overview

### What is it?

A complete **learning management system** that:
- Fetches **free Udemy courses** via an external API
- Tracks user **course completions** locally
- Manages a **daily streak system** (gamification)
- Recommends courses **based on user behavior**
- Awards **achievements & badges**

### Key Components

```
┌─────────────────────────────────────────────────────────────┐
│                    ESPRIT Flow App (JavaFX)                 │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Smart Assistant UI                      │   │
│  │  - Courses Catalog                                   │   │
│  │  - My Progress                                       │   │
│  │  - Streak Dashboard                                  │   │
│  │  - Achievements                                      │   │
│  └──────────────────────────────────────────────────────┘   │
│                           ▼                                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         Service Layer (Java Classes)                 │   │
│  │  - CourseService (API)                               │   │
│  │  - UserCourseService (Tracking)                      │   │
│  │  - StreakService (Gamification)                      │   │
│  │  - RecommendationService (Behavior)                  │   │
│  │  - AchievementService (Badges)                       │   │
│  │  - CourseCacheService (Local Cache)                  │   │
│  │  - RateLimitService (Anti-spam)                      │   │
│  └──────────────────────────────────────────────────────┘   │
│                           ▼                                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │           Data Access (MySQL + JDBC)                 │   │
│  │  - user_course table                                 │   │
│  │  - user_interest table                               │   │
│  │  - course_cache table                                │   │
│  │  - achievement table                                 │   │
│  │  - api_request_log table                             │   │
│  └──────────────────────────────────────────────────────┘   │
│                           ▼                                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │    External Udemy API                                │   │
│  │  https://udemy-paid-courses-for-free-api.p...        │   │
│  │  (via RapidAPI)                                      │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## Database Schema

### 1. User Table (Modified)

```sql
ALTER TABLE user ADD COLUMN IF NOT EXISTS current_streak INT DEFAULT 0;
ALTER TABLE user ADD COLUMN IF NOT EXISTS longest_streak INT DEFAULT 0;
ALTER TABLE user ADD COLUMN IF NOT EXISTS last_streak_date DATE DEFAULT NULL;
ALTER TABLE user ADD COLUMN IF NOT EXISTS total_courses_completed INT DEFAULT 0;
ALTER TABLE user ADD COLUMN IF NOT EXISTS learning_points INT DEFAULT 0;
```

### 2. course_cache Table

```sql
CREATE TABLE IF NOT EXISTS course_cache (
    id INT AUTO_INCREMENT PRIMARY KEY,
    api_id VARCHAR(255) UNIQUE NOT NULL,        -- ID from Udemy API
    title VARCHAR(500) NOT NULL,
    description LONGTEXT,
    category VARCHAR(255),
    coupon_code VARCHAR(100),
    expiration_date DATE,
    course_url VARCHAR(1024),
    instructor VARCHAR(255),
    rating DECIMAL(3, 2) DEFAULT 0,
    students_enrolled INT DEFAULT 0,
    thumbnail_url VARCHAR(500),
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX (category),
    INDEX (title)
);
```

### 3. user_course Table

```sql
CREATE TABLE IF NOT EXISTS user_course (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    api_course_id VARCHAR(255),
    is_completed BOOLEAN DEFAULT FALSE,
    completion_date DATETIME DEFAULT NULL,
    progress_percentage INT DEFAULT 0,
    enrolled_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_accessed DATETIME DEFAULT NULL,
    notes LONGTEXT,
    UNIQUE KEY unique_user_course (user_id, course_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course_cache(id) ON DELETE CASCADE
);
```

### 4. user_interest Table

```sql
CREATE TABLE IF NOT EXISTS user_interest (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category VARCHAR(255) NOT NULL,
    interest_score INT DEFAULT 1,
    last_engagement DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_category (user_id, category),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
```

### 5. achievement Table

```sql
CREATE TABLE IF NOT EXISTS achievement (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    achievement_type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    badge_icon VARCHAR(1024),
    earned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    metadata JSON,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
```

### 6. api_request_log Table

```sql
CREATE TABLE IF NOT EXISTS api_request_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    request_type VARCHAR(100),
    endpoint VARCHAR(500),
    request_count INT DEFAULT 1,
    last_request TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_request (user_id, request_type, DATE(last_request))
);
```

---

## Architecture & Design

### Service Layer Architecture

#### 1. **CourseService** - API Integration
- **Responsibility**: Fetch courses from Udemy API
- **Methods**:
  - `searchCourses(query, page, pageSize)` – Search courses
  - `getTrendingCourses(page)` – Get trending courses
  - `getCoursesByCategory(category, page)` – Filter by category
  - `isAPIHealthy()` – Check API availability
- **Caching**: 1-hour in-memory cache
- **Error Handling**: Graceful fallback to cached results

#### 2. **UserCourseService** - Tracking & Progress
- **Responsibility**: Manage user course enrollment and completion
- **Methods**:
  - `enrollUserInCourse(userId, courseId, apiCourseId)` – Register user
  - `markCourseAsCompleted(userId, courseId)` – Mark done
  - `getCompletedCourses(userId)` – Get finished courses
  - `getRecentCompletions(userId, daysBack)` – Recent activity
  - `getCompletionsOnDate(userId, date)` – For streak calculation
- **Anti-Spam**: Prevents duplicate completion on same day

#### 3. **StreakService** - Gamification (CORE 🔥)
- **Responsibility**: Compute and update daily streaks
- **Rules**:
  1. +1 streak ONLY if ≥1 course completed per day
  2. Max 1 increment per day (anti-spam)
  3. Consecutive days → streak increases
  4. Miss 1 day → streak resets to 1
  5. Timezone: Africa/Tunis
- **Methods**:
  - `updateStreakOnCompletion(userId)` – Trigger after completion
  - `getCurrentStreak(userId)` – Get active streak
  - `getLongestStreak(userId)` – Get all-time best
  - `isStreakAtRisk(userId)` – Alert user
  - `resetStreak(userId)` – Manual reset
- **Achievement Integration**: Awards badges at 7, 30 days

#### 4. **RecommendationService** - Smart Suggestions
- **Strategy**:
  1. Track user interests by category (frequency-based)
  2. Recommend from top 3 categories
  3. Exclude completed/enrolled courses
  4. Sort by rating + recency
- **Methods**:
  - `getRecommendations(userId, limit)` – Personalized list
  - `getTrendingCourses(limit)` – For new users
  - `getSimilarCourses(userId, course, limit)` – Related courses

#### 5. **AchievementService** - Badges & Rewards
- **Achievement Types**:
  - `FIRST_COURSE` – Complete first course
  - `STREAK_7` – 7-day streak
  - `STREAK_30` – 30-day streak
  - `COURSE_MASTER` – 10 courses completed
  - `CATEGORY_EXPERT` – 5 courses in one category
- **Methods**:
  - `awardAchievementIfNotEarned(userId, type)` – Grant badge
  - `getUserAchievements(userId)` – List all badges
  - `hasEarnedAchievement(userId, type)` – Check status

#### 6. **CourseCacheService** - Local Storage
- **Responsibility**: Store API responses locally for offline access
- **Methods**:
  - `cacheourse(course)` – Save to DB
  - `getCachedCourse(id)` – Retrieve by ID
  - `searchCourses(query)` – Full-text search on title/description
  - `cleanOldCache()` – Delete expired entries

#### 7. **RateLimitService** - anti-Spam Protection
- **Limits**:
  - 100 API requests per day per user
  - 50 course completions per day per user
- **Methods**:
  - `isRateLimited(userId, requestType)` – Check limit
  - `recordRequest(userId, requestType)` – Log request
  - `canCompleteMoreCourses(userId)` – Verify completion quota

---

## API Integration

### Udemy Free Courses API (RapidAPI)

**Endpoint**: `https://udemy-paid-courses-for-free-api.p.rapidapi.com/rapidapi/courses/search`

**Parameters**:
```
page=1                    -- Page number
page_size=20              -- Items per page
query=python              -- Search keyword
```

**Response Example**:
```json
{
  "courses": [
    {
      "id": "1234",
      "title": "Python for Beginners",
      "description": "...",
      "category": "Programming",
      "coupon_code": "FREE50",
      "coupon_expiry": "2025-12-31",
      "course_id": "1234",
      "url": "https://udemy.com/...",
      "rating": 4.5,
      "students_enrolled": 10000,
      "thumbnail": "..."
    }
  ]
}
```

**Setup Steps**:

1. Go to [RapidAPI](https://rapidapi.com/)
2. Search for "Udemy Paid Courses for Free"
3. Subscribe to free tier
4. Copy your **X-RapidAPI-Key**
5. Update `.env`:
   ```
   UDEMY_API_KEY=your_key_here
   ```

---

## Core Features

### 1. Course Discovery

```java
// Search courses
CourseService courseService = new CourseService();
List<Course> results = courseService.searchCourses("Python", 1, 20);

// Browse by category
List<Course> webDev = courseService.getCoursesByCategory("Web Development", 1);

// Get trending
List<Course> trending = courseService.getTrendingCourses(1);
```

### 2. Course Enrollment & Completion

```java
UserCourseService ucService = new UserCourseService();

// Enroll user
int userId = 42;
int courseId = 100;
UserCourse enrollment = ucService.enrollUserInCourse(userId, courseId, "api-123");

// Mark as completed
boolean success = ucService.markCourseAsCompleted(userId, courseId);

// Trigger streak update
StreakService streakService = new StreakService();
streakService.updateStreakOnCompletion(userId);  // Auto-awards badges
```

### 3. Streak Management

```java
StreakService streakService = new StreakService();

// Get current streak
int streak = streakService.getCurrentStreak(userId);      // e.g., 7

// Get all-time best
int best = streakService.getLongestStreak(userId);        // e.g., 45

// Check risk status
if (streakService.isStreakAtRisk(userId)) {
    // Alert: User didn't complete course yesterday
    // Streak will reset if no completion today
}

// Manual reset (for admins)
streakService.resetStreak(userId);
```

### 4. Recommendation Engine

```java
RecommendationService recService = new RecommendationService();

// Get personalized recommendations
List<Course> recommendations = recService.getRecommendations(userId, 5);

// Record interest
recService.recordInterest(userId, "Data Science");

// Get similar courses
Course baseCourse = courseService.searchCourses("Python").get(0);
List<Course> similar = recService.getSimilarCourses(userId, baseCourse, 10);
```

### 5. Achievements

```java
AchievementService achService = new AchievementService();

// Award badge
achService.awardAchievementIfNotEarned(userId, AchievementType.STREAK_7);

// Get all achievements
List<Achievement> badges = achService.getUserAchievements(userId);

// Check if earned
boolean hasBadge = achService.hasEarnedAchievement(userId, "first_course");
```

---

## Implementation Guide

### Setup Steps

#### 1. **Run Database Migration**

```sql
-- Execute migration_smart_courses.sql
SOURCE /path/to/migration_smart_courses.sql;
```

If using MySQL Workbench:
- Open `migration_smart_courses.sql`
- Execute all queries

#### 2. **Update `.env` File**

```dotenv
# Udemy API
UDEMY_API_KEY=YOUR_RAPIDAPI_KEY_HERE
UDEMY_API_HOST=udemy-paid-courses-for-free-api.p.rapidapi.com
```

#### 3. **Add to `user.java` Entity**

```java
// Add these fields
private int currentStreak;
private int longestStreak;
private String lastStreakDate;
private int totalCoursesCompleted;

// Add getters/setters
public int getCurrentStreak() { return currentStreak; }
public void setCurrentStreak(int val) { this.currentStreak = val; }
// ... etc
```

#### 4. **Integrate into Sidebar Navigation**

```java
// In FrontSidebarController.java
@FXML private HBox coursesBtn;

@FXML
public void goToCourses() {
    setActiveButton(coursesBtn);
    loadView("/frontoffice/courses/courses-content.fxml");
}
```

#### 5. **Add to FXML Sidebar**

```xml
<!-- In sidebar.fxml -->
<HBox fx:id="coursesBtn" ... onMouseClicked="#goToCourses">
    <Label text="📚 Cours"/>
</HBox>
```

---

## Testing & Deployment

### Unit Tests

```java
// Example: Test streak calculation
@Test
public void testStreakIncrement() {
    StreakService streakService = new StreakService();
    boolean updated = streakService.updateStreakOnCompletion(userId);
    assertTrue(updated);
    assertEquals(2, streakService.getCurrentStreak(userId));
}

// Example: Test anti-spam
@Test
public void testDuplicateCompletionBlocked() {
    UserCourseService ucService = new UserCourseService();
    boolean first = ucService.markCourseAsCompleted(userId, courseId);
    boolean second = ucService.markCourseAsCompleted(userId, courseId);
    
    assertTrue(first);
    assertFalse(second);  // Blocked
}
```

### Integration Testing

```bash
# Test API connectivity
mvn test -Dtest=CourseServiceTest

# Test database operations
mvn test -Dtest=UserCourseServiceTest

# Full integration test
mvn test
```

### Deployment Checklist

- [ ] Database migrations applied
- [ ] `.env` updated with UDEMY_API_KEY
- [ ] `user.java` updated with streak fields
- [ ] All services compiled without errors
- [ ] JavaFX UI controllers created
- [ ] Navigation integrated into sidebar
- [ ] API health check passes
- [ ] Sample data loaded
- [ ] User testing completed

---

## Example Flows

### Flow 1: User Completes a Course

```
User opens app
  ▼
Navigates to "Courses" section
  ▼
Searches for "Python" or browses category
  ▼
Clicks a course → CourseService fetches from API
  ▼
Clicks "Enroll" → UserCourseService.enrollUserInCourse()
  ▼
User watches course...
  ▼
Clicks "Mark Complete" → UserCourseService.markCourseAsCompleted()
  ▼
StreakService.updateStreakOnCompletion() triggered
  ▼
Checks completion on today's date
  ▼
Calculates new streak (consecutive or reset)
  ▼
Updates user.current_streak and user.last_streak_date
  ▼
Checks for achievement (7-day? 30-day?)
  ▼
Awards badge if earned
  ▼
RecommendationService.recordInterest() logs category
  ▼
UI updates to show: new streak + badge + recommendations
```

### Flow 2: Get Personalized Recommendations

```
User opens dashboard
  ▼
RecommendationService.getRecommendations(userId, 5)
  ▼
Queries user_interest table
  ▼
Finds top 3 categories by score (e.g., "Python", "Web Dev", "AI")
  ▼
For each category:
  - CourseService.getCoursesByCategory()
  - Filters out already completed
  - Filters out already enrolled
  ▼
Returns 5 recommended courses
  ▼
UI displays as cards with "Enroll" button
```

### Flow 3: Check if Streak is at Risk

```
User logs in
  ▼
StreakService.isStreakAtRisk(userId)
  ▼
Gets last_streak_date from DB
  ▼
Checks if last completion was < 1 day ago
  ▼
If at risk:
  - Show warning: "Complete a course today to maintain streak!"
  - Highlight recommended courses
  ▼
If safe:
  - Show: "You have X days to maintain your streak"
```

---

## Configuration & Environment

### .env Variables

```dotenv
# Udemy API
UDEMY_API_KEY=sk_test_xxxx...        # From RapidAPI dashboard
UDEMY_API_HOST=udemy-paid-courses-for-free-api.p.rapidapi.com

# Timezone (for streak system)
TIMEZONE=Africa/Tunis                 # UTC+1

# Rate Limiting
MAX_API_REQUESTS_PER_DAY=100
MAX_COMPLETIONS_PER_DAY=50

# Cache Duration
CACHE_VALIDITY_HOURS=24
```

---

## Troubleshooting

### Issue: API Returns 401 (Invalid Key)
**Solution**: Check UDEMY_API_KEY in `.env` is correct

### Issue: Streak not updating
**Solution**: 
1. Check database has user_course entry
2. Verify `is_completed = TRUE` was set
3. Check timezone setting

### Issue: No recommendations showing
**Solution**:
1. User might have no interest history
2. Try completing a course first
3. Check user_interest table has records

---

## Support & Contribution

- **Questions?** Check the code comments
- **Found a bug?** Create an issue
- **Want to contribute?** Submit a PR

---

*Built with ❤️ for ESPRIT Flow - Smart Learning Assistant*


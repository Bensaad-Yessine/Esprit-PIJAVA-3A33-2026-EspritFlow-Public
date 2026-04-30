# 🚀 Smart Assistant for Students - Quick Integration Guide

## ⏱️ 5-Minute Setup

### Step 1: Run Database Migration (1 min)

**Using MySQL CLI:**
```bash
mysql -u root -p pidev < migration_smart_courses.sql
```

**Using MySQL Workbench:**
1. Open `migration_smart_courses.sql`
2. Click "Execute" (lightning bolt icon)
3. Wait for all statements to complete ✓

**Using IntelliJ:**
1. Right-click on `migration_smart_courses.sql`
2. Select "Run" or "Execute"
3. Choose your database connection

### Step 2: Get Udemy API Key (1 min)

1. Go to [RapidAPI - Udemy Courses API](https://rapidapi.com/tutorialindex-tutorialindex-default/api/udemy-paid-courses-for-free-api)
2. Click **"Subscribe to Free"** button
3. Accept the terms
4. Go to **"Code Snippets"** tab
5. Copy your **X-RapidAPI-Key** (looks like: `abc123def456...`)

### Step 3: Update .env (1 min)

```env
# Add this to your .env file:
UDEMY_API_KEY=paste_your_key_here
UDEMY_API_HOST=udemy-paid-courses-for-free-api.p.rapidapi.com
```

### Step 4: Update user.java Entity (1 min)

Add these fields to `user.java`:

```java
private int currentStreak;
private int longestStreak;
private String lastStreakDate;
private int totalCoursesCompleted;

// Getters
public int getCurrentStreak() { return currentStreak; }
public int getLongestStreak() { return longestStreak; }
public String getLastStreakDate() { return lastStreakDate; }
public int getTotalCoursesCompleted() { return totalCoursesCompleted; }

// Setters
public void setCurrentStreak(int val) { this.currentStreak = val; }
public void setLongestStreak(int val) { this.longestStreak = val; }
public void setLastStreakDate(String val) { this.lastStreakDate = val; }
public void setTotalCoursesCompleted(int val) { this.totalCoursesCompleted = val; }
```

### Step 5: Add Navigation (1 min)

**In `FrontSidebarController.java`:**

```java
@FXML private HBox coursesBtn;

@FXML
private void goToCourses() {
    setActiveButton(coursesBtn);
    loadView("/frontoffice/courses/courses-content.fxml");
}
```

**In `sidebar.fxml`:**

```xml
<!-- Add this button after other menu items -->
<HBox fx:id="coursesBtn" 
      spacing="12"
      alignment="CENTER_LEFT"
      styleClass="sidebar-item"
      onMouseClicked="#goToCourses">
    <Label text="📚 Courses"/>
</HBox>
```

---

## 📝 Usage Examples

### Basic Usage in a Controller

```java
// Get current user ID
int userId = SessionManager.getInstance().getCurrentUser().getId();

// Create helper
SmartAssistantHelper helper = new SmartAssistantHelper(userId);

// === SEARCH & BROWSE ===
List<Course> results = helper.searchCourses("Python");
List<Course> trending = helper.getTrendingCourses();
List<Course> byCategory = helper.getCoursesByCategory("Web Development");

// === ENROLL & TRACK ===
boolean enrolled = helper.enrollInCourse(course);
boolean completed = helper.completeCourse(courseId);

// === GET STATS ===
int streak = helper.getCurrentStreak();                  // e.g., 7
int longestStreak = helper.getLongestStreak();          // e.g., 45
boolean atRisk = helper.isStreakAtRisk();                // true/false
int daysLeft = helper.getDaysUntilStreakLost();          // 1 if must complete today

// === ACHIEVEMENTS ===
List<Achievement> badges = helper.getAchievements();     // All earned badges
boolean has7DayBadge = helper.hasAchievement(STREAK_7);

// === RECOMMENDATIONS ===
List<Course> recommended = helper.getRecommendations(5); // Top 5 for user
List<Course> similar = helper.getSimilarCourses(course, 10);

// === STATS ===
SmartAssistantHelper.LearningStats stats = helper.getStats();
System.out.println(stats);  // Output: Stats{completed=12, streak=7/45, badges=3}
```

### Example: Dashboard Widget

```java
@FXML private Label streakLabel;
@FXML private Label achievementsLabel;
@FXML private Label recommendedLabel;

@FXML
void initialize() {
    int userId = SessionManager.getInstance().getCurrentUser().getId();
    SmartAssistantHelper helper = new SmartAssistantHelper(userId);

    // Update streak display
    int streak = helper.getCurrentStreak();
    streakLabel.setText("🔥 " + streak + "-day streak!");
    
    // Show achievements
    int badges = helper.getAchievementCount();
    achievementsLabel.setText("🏆 " + badges + " badges earned");
    
    // Load recommendations
    List<Course> recommended = helper.getRecommendations(3);
    recommendedLabel.setText("Based on your interests: " + recommended.get(0).getTitle());
}
```

### Example: Course Completion Button

```java
@FXML
void handleCompleteButtonClick(ActionEvent event) {
    int userId = SessionManager.getInstance().getCurrentUser().getId();
    SmartAssistantHelper helper = new SmartAssistantHelper(userId);
    
    boolean success = helper.completeCourse(courseId);
    
    if (success) {
        // Show success message
        int newStreak = helper.getCurrentStreak();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Completed!");
        alert.setContentText("Great job! Your streak is now " + newStreak + " days.");
        alert.showAndWait();
        
        // Refresh UI
        refreshStreakDisplay();
        refreshAchievementsDisplay();
        refreshRecommendations();
    }
}
```

---

## 🗂️ File Structure

```
pijava/
├── src/main/java/piJava/
│   ├── entities/
│   │   ├── Course.java              ✅ NEW
│   │   ├── UserCourse.java          ✅ NEW
│   │   ├── UserInterest.java        ✅ NEW
│   │   └── Achievement.java         ✅ NEW
│   │
│   ├── services/
│   │   ├── CourseService.java               ✅ NEW - API Integration
│   │   ├── UserCourseService.java           ✅ NEW - Tracking
│   │   ├── StreakService.java               ✅ NEW - Gamification 🔥
│   │   ├── RecommendationService.java       ✅ NEW - Smart Suggestions
│   │   ├── CourseCacheService.java          ✅ NEW - Local Cache
│   │   └── (UserInterestService & AchievementService in RecommendationService.java)
│   │
│   ├── Controllers/frontoffice/courses/
│   │   └── CoursesContentController.java    ✅ NEW - UI Controller
│   │
│   └── utils/
│       └── SmartAssistantHelper.java        ✅ NEW - Integration Helper
│
├── src/main/resources/
│   └── frontoffice/courses/
│       ├── courses-content.fxml             ✅ NEW - UI Layout
│       └── courses.css                      ✅ NEW - Styling
│
└── migration_smart_courses.sql              ✅ NEW - DB Schema
```

---

## ✅ Verification Checklist

Before running the app:

- [ ] Database migration completed (`migration_smart_courses.sql` executed)
- [ ] `.env` updated with `UDEMY_API_KEY`
- [ ] `user.java` has streak fields added
- [ ] All new `.java` files compiled (no errors in IDE)
- [ ] Navigation button added to sidebar
- [ ] `SmartAssistantHelper` imports are available in controllers

---

## 🐛 Troubleshooting

### Error: "Cannot find symbol: class Course"
**Solution**: Make sure `Course.java` is in `src/main/java/piJava/entities/`

### Error: "API rate limit exceeded"
**Solution**: Increase `UDEMY_API_KEY` quota on RapidAPI dashboard

### Error: "Column 'current_streak' doesn't exist"
**Solution**: Run migration SQL (`migration_smart_courses.sql`)

### No courses appearing
**Solution**:
1. Check API health: `helper.isAPIHealthy()`
2. Verify API key is set: `EnvConfig.get("UDEMY_API_KEY")`
3. Check internet connection
4. Try `helper.clearCache()` to force refresh

### Streak not updating
**Solution**:
1. Verify `is_completed = TRUE` in database
2. Check timezone setting: `StreakService.TIMEZONE`
3. Ensure `updateStreakOnCompletion()` is called after completion

---

## 📖 Next Steps

1. **Test the system**:
   ```bash
   mvn clean javafx:run
   ```

2. **Search for courses**: Try "Python", "Web Development", etc.

3. **Enroll in a course**: Click any course's "Enroll Now" button

4. **Mark complete**: Click "Mark as Completed" (in course detail)

5. **Check streak**: View your current streak in dashboard

6. **Earn badges**: Complete courses on consecutive days!

---

## 📞 Support

- **Bug?** Check the console output and logs
- **Need help?** Review `SMART_ASSISTANT_ARCHITECTURE.md`
- **Questions?** Check code comments in service classes

---

*Always test in development before deploying to production!* 🚀


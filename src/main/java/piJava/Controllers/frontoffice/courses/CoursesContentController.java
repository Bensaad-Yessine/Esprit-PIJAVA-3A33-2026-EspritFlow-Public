package piJava.Controllers.frontoffice.courses;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.Course;
import piJava.utils.SessionManager;
import piJava.utils.SmartAssistantHelper;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * CoursesContentController - Display and manage course catalog.
 * 
 * Features:
 *  - Search courses by keyword
 *  - Browse by category
 *  - View trending courses
 *  - Enroll in courses
 *  - View personalized recommendations
 */
public class CoursesContentController implements Initializable {

    // ── FXML Components ────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Button trendingBtn;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private FlowPane coursesContainer;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private ScrollPane coursesScroll;
    @FXML private Label streakLabel;
    
    // Chips
    @FXML private Button chipAll, chipDev, chipDesign, chipBusiness, chipData;

    // ── Layout Components for Alternatives ──────────────────
    @FXML private TabPane tabPane;           // Optional: for tabs (Catalog, My Courses, Recommendations)
    @FXML private Tab catalogTab;
    @FXML private Tab myCoursesTab;
    @FXML private Tab recommendationsTab;

    // ── State ──────────────────────────────────────────────
    private SmartAssistantHelper helper;
    private List<Course> currentResults;
    private FrontSidebarController sidebarController;
    private javafx.scene.layout.StackPane contentArea;

    // ── Setters for injection ──────────────────────────────
    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    public void setContentArea(javafx.scene.layout.StackPane contentArea) {
        this.contentArea = contentArea;
    }

    // ── Lifecycle ──────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize helper for current user
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        this.helper = new SmartAssistantHelper(userId);

        setupUI();
        loadInitialData();
    }

    private void setupUI() {
        // Setup search field
        searchField.setPromptText("Search courses (Python, Web Development, etc.)");
        searchField.setOnAction(e -> handleSearch());

        // Setup buttons
        searchBtn.setOnAction(e -> handleSearch());
        trendingBtn.setOnAction(e -> loadTrendingCourses());

        // Setup category combo box
        setupCategoryCombo();

        // Setup loading spinner
        loadingSpinner.setVisible(false);

        // Setup courses container (grid layout)
        coursesContainer.setHgap(20);
        coursesContainer.setVgap(20);
        coursesContainer.setPrefWrapLength(0); // Auto-wrap based on container width
    }

    private void setupCategoryCombo() {
        categoryCombo.getItems().addAll(
            "Tous",
            "Programming",
            "Web Development",
            "Data Science",
            "Machine Learning",
            "Business",
            "Design",
            "Marketing",
            "Personal Development",
            "Health & Fitness"
        );

        categoryCombo.setValue("Tous");
        categoryCombo.setOnAction(e -> loadCoursesByCategory());
    }

    private void loadInitialData() {
        // Check API health
        new Thread(() -> {
            boolean healthy = helper.isAPIHealthy();
            Platform.runLater(() -> {
                if (!healthy) {
                    statusLabel.setText("⚠️ API temporarily unavailable, showing cached courses");
                    statusLabel.setStyle("-fx-text-fill: #ff9800;");
                }
            });
        }).start();

        // Initialize Streak
        updateStreakDisplay();

        // Load trending courses on startup
        loadTrendingCourses();
    }

    private void updateStreakDisplay() {
        if (streakLabel != null) {
            Platform.runLater(() -> {
                int streak = helper.getCurrentStreak();
                streakLabel.setText("🔥 " + streak + " Day Streak!");
            });
        }
    }

    // ── Search & Filter ────────────────────────────────────

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        
        if (query.isEmpty()) {
            statusLabel.setText("Please enter a search term");
            return;
        }

        loadingSpinner.setVisible(true);
        coursesContainer.getChildren().clear();

        Task<List<Course>> task = new Task<>() {
            @Override protected List<Course> call() throws Exception {
                return helper.searchCourses(query);
            }
        };

        task.setOnSucceeded(e -> {
            currentResults = task.getValue();
            displayCourses(currentResults);
            loadingSpinner.setVisible(false);
            statusLabel.setText("Found " + currentResults.size() + " courses");
        });

        task.setOnFailed(e -> {
            loadingSpinner.setVisible(false);
            statusLabel.setText("Search failed: " + task.getException().getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        });

        new Thread(task, "course-search").start();
    }

    @FXML
    private void handleTrending(javafx.event.ActionEvent event) {
        loadTrendingCourses();
    }

    @FXML
    private void handleChip(javafx.event.ActionEvent event) {
        Button source = (Button) event.getSource();
        String category = source.getText();
        
        // Map chip text to combo box values
        if (category.equals("All")) {
            categoryCombo.setValue("Tous");
        } else if (category.equals("Development")) {
            categoryCombo.setValue("Web Development");
        } else if (category.equals("Data Science")) {
            categoryCombo.setValue("Data Science");
        } else {
            categoryCombo.setValue(category);
        }
        
        loadCoursesByCategory();
        
        // Update chip active states
        Button[] chips = {chipAll, chipDev, chipDesign, chipBusiness, chipData};
        for (Button c : chips) {
            if (c != null) {
                c.getStyleClass().remove("chip-active");
                if (!c.getStyleClass().contains("chip")) {
                    c.getStyleClass().add("chip");
                }
            }
        }
        source.getStyleClass().remove("chip");
        source.getStyleClass().add("chip-active");
    }

    @FXML
    private void loadTrendingCourses() {
        loadingSpinner.setVisible(true);
        coursesContainer.getChildren().clear();
        searchField.clear();
        categoryCombo.setValue("Tous");

        Task<List<Course>> task = new Task<>() {
            @Override protected List<Course> call() throws Exception {
                return helper.getTrendingCourses();
            }
        };

        task.setOnSucceeded(e -> {
            currentResults = task.getValue();
            displayCourses(currentResults);
            loadingSpinner.setVisible(false);
            statusLabel.setText("Trending courses loaded");
        });

        task.setOnFailed(e -> {
            loadingSpinner.setVisible(false);
            statusLabel.setText("Failed to load trending");
        });

        new Thread(task, "trending-loader").start();
    }

    @FXML
    private void loadCoursesByCategory() {
        String category = categoryCombo.getValue();
        if (category.equals("Tous")) {
            loadTrendingCourses();
            return;
        }

        loadingSpinner.setVisible(true);
        coursesContainer.getChildren().clear();

        Task<List<Course>> task = new Task<>() {
            @Override protected List<Course> call() throws Exception {
                return helper.getCoursesByCategory(category);
            }
        };

        task.setOnSucceeded(e -> {
            currentResults = task.getValue();
            displayCourses(currentResults);
            loadingSpinner.setVisible(false);
            statusLabel.setText(category + ": " + currentResults.size() + " courses");
        });

        task.setOnFailed(e -> {
            loadingSpinner.setVisible(false);
            statusLabel.setText("Failed to load category");
        });

        new Thread(task, "category-loader").start();
    }

    // ── Course Display ────────────────────────────────────

    private void displayCourses(List<Course> courses) {
        Platform.runLater(() -> {
            coursesContainer.getChildren().clear();

            if (courses.isEmpty()) {
                Label empty = new Label("No courses found. Try a different search.");
                empty.setStyle("-fx-font-size: 14; -fx-text-fill: #999;");
                coursesContainer.getChildren().add(empty);
                return;
            }

            for (Course course : courses) {
                VBox card = createCourseCard(course);
                coursesContainer.getChildren().add(card);
            }
        });
    }

    /**
     * Create a visual course card.
     */
    private VBox createCourseCard(Course course) {
        VBox card = new VBox();
        card.setPrefWidth(300);
        card.setMinHeight(380);
        card.getStyleClass().add("course-card");

        // Thumbnail Placeholder (Gradient with First Letter)
        javafx.scene.layout.StackPane thumbnail = new javafx.scene.layout.StackPane();
        thumbnail.setPrefHeight(140);
        thumbnail.setMinHeight(140);
        thumbnail.getStyleClass().add("course-thumbnail");
        
        String firstLetter = course.getTitle() != null && !course.getTitle().isEmpty() 
                             ? course.getTitle().substring(0, 1).toUpperCase() : "C";
        Label initialLabel = new Label(firstLetter);
        initialLabel.setStyle("-fx-text-fill: white; -fx-font-size: 50; -fx-font-weight: bold;");
        thumbnail.getChildren().add(initialLabel);

        // Content Box (Padding applied here so thumbnail touches card edges)
        VBox contentBox = new VBox(8);
        contentBox.setPadding(new javafx.geometry.Insets(16));
        VBox.setVgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);

        // Title
        Label titleLabel = new Label(course.getTitle());
        titleLabel.getStyleClass().add("course-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(40); // ensure title takes 2 lines

        // Category / Institution
        Label categoryLabel = new Label("🏫 " + (course.getCategory() != null ? 
                                        course.getCategory() : "General"));
        categoryLabel.getStyleClass().add("course-category");

        // Description (truncated)
        Label descLabel = new Label(truncateText(course.getDescription(), 90));
        descLabel.getStyleClass().add("course-description");
        descLabel.setWrapText(true);
        descLabel.setMinHeight(50);

        // Spacer to push buttons to bottom
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));

        Button detailsBtn = new Button("Details");
        detailsBtn.getStyleClass().add("btn-secondary");
        detailsBtn.setPrefWidth(90);
        detailsBtn.setOnAction(e -> showCourseDetails(course));

        Button enrollBtn = new Button("Enroll Now");
        enrollBtn.getStyleClass().add("btn-primary");
        enrollBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(enrollBtn, javafx.scene.layout.Priority.ALWAYS);
        enrollBtn.setOnAction(e -> handleEnroll(course, card));

        buttonBox.getChildren().addAll(detailsBtn, enrollBtn);

        // Assemble Content Box
        contentBox.getChildren().addAll(
            titleLabel,
            categoryLabel,
            descLabel,
            spacer,
            buttonBox
        );

        // Assemble Card
        card.getChildren().addAll(thumbnail, contentBox);

        return card;
    }

    private void handleEnroll(Course course, VBox card) {
        // Play a nice "pop" animation on the card
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(150), card);
        st.setByX(0.04);
        st.setByY(0.04);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();

        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                return helper.enrollInCourse(course);
            }
        };

        task.setOnSucceeded(e -> {
            boolean success = task.getValue();
            if (success) {
                int currentStreak = helper.getCurrentStreak();
                updateStreakDisplay();
                
                if (course.getCourseUrl() != null && !course.getCourseUrl().isEmpty()) {
                    openURL(course.getCourseUrl());
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Enrollment Successful");
                alert.setHeaderText("🎉 Enrolled in " + course.getTitle());
                alert.setContentText("🔥 Streak Updated!\nYour current learning streak is now: " + currentStreak + " day(s).\n\nThe course has been opened in your browser so you can start learning!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Enrollment Failed");
                alert.setContentText("Failed to enroll. Please try again.");
                alert.showAndWait();
            }
        });

        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Enrollment error: " + task.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    private void showCourseDetails(Course course) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(course.getTitle());
        alert.setHeaderText(course.getCategory());
        alert.setContentText(course.getDescription() + "\n\n" +
                            "Instructor: " + (course.getInstructor() != null ? 
                                            course.getInstructor() : "Unknown") + "\n" +
                            "Rating: " + course.getRating() + "/5\n" +
                            "Students: " + course.getStudentsEnrolled() + "\n" +
                            (course.isCouponValid() ? "Coupon: " + course.getCouponCode() : 
                                                    "Coupon expired"));
        
        ButtonType openBtn = new ButtonType("Open in Browser");
        ButtonType closeBtn = new ButtonType("Close");
        alert.getButtonTypes().setAll(openBtn, closeBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == openBtn && course.getCourseUrl() != null) {
                openURL(course.getCourseUrl());
            }
        });
    }

    private void openURL(String url) {
        try {
            // Clean up the URL just in case there are escaped slashes
            String cleanUrl = url.replace("\\/", "/").trim();
            
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + cleanUrl);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + cleanUrl);
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec("xdg-open " + cleanUrl);
            } else {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(cleanUrl));
            }
        } catch (Exception e) {
            System.err.println("Error opening URL: " + e.getMessage());
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Browser Error");
                a.setHeaderText("Could not automatically open browser");
                a.setContentText("Please manually navigate to:\n" + url);
                a.show();
            });
        }
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}

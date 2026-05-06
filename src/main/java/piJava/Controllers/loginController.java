package piJava.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.application.Platform;
import piJava.entities.user;
import piJava.services.loginService;
import piJava.utils.SessionManager;

import java.io.IOException;
import java.util.prefs.Preferences;

public class loginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheck;

    private final loginService loginService = new loginService();

    private static final String PREF_EMAIL    = "remembered_email";
    private static final String PREF_REMEMBER = "remember_me";

    private final String[] MOTIVATIONAL_QUOTES = {
        "\"The beautiful thing about learning is nobody can take it away from you.\" - B.B. King",
        "\"Education is the most powerful weapon which you can use to change the world.\" - Nelson Mandela",
        "\"Learning never exhausts the mind.\" - Leonardo da Vinci",
        "\"Develop a passion for learning. If you do, you will never cease to grow.\" - Anthony J. D'Angelo",
        "\"The more that you read, the more things you will know. The more that you learn, the more places you'll go.\" - Dr. Seuss",
        "\"An investment in knowledge pays the best interest.\" - Benjamin Franklin",
        "\"You don't have to be great to start, but you have to start to be great.\" - Zig Ziglar"
    };

    private String getRandomQuote() {
        return MOTIVATIONAL_QUOTES[new java.util.Random().nextInt(MOTIVATIONAL_QUOTES.length)];
    }

    @FXML
    public void initialize() {
        Preferences prefs = Preferences.userNodeForPackage(loginController.class);
        if (prefs.getBoolean(PREF_REMEMBER, false)) {
            emailField.setText(prefs.get(PREF_EMAIL, ""));
            rememberMeCheck.setSelected(true);
        }
    }

    @FXML
    void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "Missing Fields", "Please enter both email and password.");
            return;
        }

        user loggedInUser;
        try {
            loggedInUser = loginService.login(email, password);
        } catch (Exception e) {
            String dbMessage = piJava.utils.MyDataBase.getInstance().getLastErrorMessage();
            System.err.println("Login failed: " + e.getMessage());
            showAlert(AlertType.ERROR, "Database Error",
                    "Connexion à la base impossible. Vérifiez que MySQL est démarré et que la base configurée existe.\n"
                            + (dbMessage != null ? "Détail: " + dbMessage : ""));
            return;
        }

        System.out.println("Login result: " + loggedInUser);


        if (loggedInUser == null) {
            showAlert(AlertType.ERROR, "Login Failed",
                    "Invalid credentials, unverified email, banned account, or database unavailable.");
            passwordField.clear();
            return;
        }

        // ✅ Save to global session — accessible from every controller
        SessionManager.getInstance().login(loggedInUser);

        // Handle Remember Me
        Preferences prefs = Preferences.userNodeForPackage(loginController.class);
        if (rememberMeCheck.isSelected()) {
            prefs.put(PREF_EMAIL, email);
            prefs.putBoolean(PREF_REMEMBER, true);
        } else {
            prefs.remove(PREF_EMAIL);
            prefs.putBoolean(PREF_REMEMBER, false);
        }

        // Route by role
        // ✅ Temporary fix until you create home.fxml
        if (SessionManager.getInstance().isAdmin()) {
            showAlert(AlertType.INFORMATION, "Welcome Admin",
                    "Logged in as: " + SessionManager.getInstance().getFullName() + "\n\n💡 " + getRandomQuote());
            navigateTo("/backoffice/main.fxml");

        } else {
            showAlert(AlertType.INFORMATION, "Welcome",
                    "Logged in as: " + SessionManager.getInstance().getFullName() + "\n\n💡 " + getRandomQuote());
            navigateTo("/frontoffice/main.fxml");
        }
    }

    @FXML
    void handleSignUp() {
        navigateTo("/AjouterUser.fxml");
    }

    @FXML
    void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forgot-password.fxml"));
            Parent root = loader.load();
            piJava.Controllers.ForgotPasswordController controller = loader.getController();
            controller.setInitialEmail(emailField.getText().trim());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Navigation Error: Could not load forgot-password.fxml: " + e.getMessage());
            showAlert(AlertType.ERROR, "Navigation Error", "Could not load forgot-password.fxml\nCause: " + e.getMessage());
        }
    }

    @FXML
    void handleGmailLogin() {
        piJava.services.GoogleAuthService googleAuthService = new piJava.services.GoogleAuthService();
        googleAuthService.authenticate(new piJava.services.GoogleAuthService.AuthCallback() {
            @Override
            public void onSuccess(user loggedInUser) {
                // Save to global session
                SessionManager.getInstance().login(loggedInUser);

                // Route by role
                if (SessionManager.getInstance().isAdmin()) {
                    Platform.runLater(() -> {
                        showAlert(AlertType.INFORMATION, "Welcome Admin",
                                "Logged in via Google as: " + SessionManager.getInstance().getFullName() + "\n\n💡 " + getRandomQuote());
                        navigateTo("/backoffice/main.fxml");
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert(AlertType.INFORMATION, "Welcome",
                                "Logged in via Google as: " + SessionManager.getInstance().getFullName() + "\n\n💡 " + getRandomQuote());
                        navigateTo("/frontoffice/main.fxml");
                    });
                }
            }

            @Override
            public void onError(String message) {
                Platform.runLater(() -> showAlert(AlertType.ERROR, "Google Login Error", message));
            }
        });
    }

    @FXML
    void handleFaceID() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/face-login.fxml"));
            Parent root = loader.load();
            
            // Load CSS and create scene
            Scene scene = new Scene(root);
            String css = getClass().getResource("/face-login.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Navigation Error: Could not load face-login.fxml: " + e.getMessage());
            showAlert(AlertType.ERROR, "Navigation Error", "Could not load face-login.fxml\nCause: " + e.getMessage());
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Navigation Error: Could not load the next screen: " + fxmlPath + " | " + e.getMessage());
            showAlert(AlertType.ERROR, "Navigation Error", "Could not load the next screen: " + fxmlPath + "\nCause: " + e.getCause() + "\nMessage: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Navigation Error: Unexpected error while loading " + fxmlPath + ": " + e.getMessage());
            showAlert(AlertType.ERROR, "Navigation Error", "Unexpected error: \nCause: " + e.getCause() + "\nMessage: " + e.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

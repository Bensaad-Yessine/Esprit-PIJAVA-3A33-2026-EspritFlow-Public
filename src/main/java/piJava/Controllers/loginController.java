package piJava.Controllers;

import javafx.event.ActionEvent;
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

    @FXML
    public void initialize() {
        Preferences prefs = Preferences.userNodeForPackage(loginController.class);
        if (prefs.getBoolean(PREF_REMEMBER, false)) {
            emailField.setText(prefs.get(PREF_EMAIL, ""));
            rememberMeCheck.setSelected(true);
        }
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "Missing Fields", "Please enter both email and password.");
            return;
        }

        user loggedInUser = loginService.login(email, password);

        System.out.println("Login result: " + loggedInUser); // ← add this


        if (loggedInUser == null) {
            showAlert(AlertType.ERROR, "Login Failed",
                    "Invalid credentials, unverified email, or banned account.");
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
                    "Logged in as: " + SessionManager.getInstance().getFullName());
            navigateTo(event, "/backoffice/main.fxml");

        } else {
            showAlert(AlertType.INFORMATION, "Welcome",
                    "Logged in as: " + SessionManager.getInstance().getFullName());
            navigateTo(event, "/frontoffice/main.fxml");
        }
    }

    @FXML
    void handleSignUp(ActionEvent event) {
        navigateTo(event, "/AjouterUser.fxml");
    }

    @FXML
    void handleForgotPassword(ActionEvent event) {
        showAlert(AlertType.INFORMATION, "Coming Soon", "ForgetPassword login is not yet available.");
    }

    @FXML
    void handleGmailLogin(ActionEvent event) {
        showAlert(AlertType.INFORMATION, "Coming Soon", "Gmail login is not yet available.");
    }

    @FXML
    void handleFaceID(ActionEvent event) {
        showAlert(AlertType.INFORMATION, "Coming Soon", "FaceID login is not yet available.");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Navigation Error", "Could not load the next screen.");
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

package piJava.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import piJava.services.PasswordResetService;

import java.io.IOException;

public class ResetPasswordController {

    @FXML private TextField emailField;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private final PasswordResetService passwordResetService = new PasswordResetService();

    @FXML
    public void initialize() {
        messageLabel.setText("");
    }

    public void setInitialEmail(String email) {
        if (emailField != null && email != null && !email.isBlank()) {
            emailField.setText(email);
        }
    }

    @FXML
    void handleResetPassword() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String token = tokenField.getText() == null ? "" : tokenField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty() || token.isEmpty() || newPassword == null || newPassword.isBlank()) {
            showAlert(AlertType.WARNING, "Missing fields", "Veuillez remplir l'e-mail, le code et le nouveau mot de passe.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showAlert(AlertType.WARNING, "Mismatch", "Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            passwordResetService.resetPassword(email, token, newPassword);
            messageLabel.setText("Votre mot de passe a été réinitialisé avec succès.");
            showAlert(AlertType.INFORMATION, "Success", "Mot de passe réinitialisé avec succès.");
            navigateToLogin();
        } catch (IllegalArgumentException e) {
            showAlert(AlertType.WARNING, "Validation", e.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Impossible de réinitialiser le mot de passe.\n" + e.getMessage());
        }
    }

    @FXML
    void handleBackToLogin() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Could not load login.fxml: " + e.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}


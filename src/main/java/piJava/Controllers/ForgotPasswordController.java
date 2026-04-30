package piJava.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import piJava.services.PasswordResetService;

import java.io.IOException;

public class ForgotPasswordController {

    @FXML private TextField emailField;
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
    void handleSendResetMail() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (email.isEmpty()) {
            showAlert(AlertType.WARNING, "Missing field", "Veuillez saisir votre adresse e-mail.");
            return;
        }

        try {
            passwordResetService.requestReset(email);
            messageLabel.setText("Un code de réinitialisation a été envoyé à votre adresse e-mail.");
            openResetPasswordScreen(email);
        } catch (IllegalArgumentException e) {
            showAlert(AlertType.WARNING, "Demande refusée", e.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Impossible d'envoyer le lien de réinitialisation.\n" + e.getMessage());
        }
    }

    @FXML
    void handleBackToLogin() {
        navigateTo("/login.fxml");
    }

    private void openResetPasswordScreen(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reset-password.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            controller.getClass().getMethod("setInitialEmail", String.class).invoke(controller, email);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (ReflectiveOperationException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Could not initialize reset-password.fxml: " + e.getMessage());
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Could not load reset-password.fxml: " + e.getMessage());
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Could not load: " + fxmlPath + "\n" + e.getMessage());
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


package piJava.utils;

import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import java.util.List;

public class ValidationHelper {

    public static void initializeErrorLabel(Label label) {
        if (label != null) {
            label.setText("");
            label.setVisible(false);
            label.setManaged(false);
            label.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
        }
    }

    public static void showFieldErrors(TextInputControl field, Label errorLabel, List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            // Clear error state
            if (errorLabel != null) {
                errorLabel.setText("");
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
            if (field != null) {
                field.setStyle("");
            }
        } else {
            // Show error state
            if (errorLabel != null) {
                errorLabel.setText(String.join("\n", errors));
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
            if (field != null) {
                field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1px; -fx-border-radius: 4px;");
            }
        }
    }
}
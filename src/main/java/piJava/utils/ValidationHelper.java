package piJava.utils;

import javafx.scene.control.Control;
import javafx.scene.control.Label;

import java.util.List;

public class ValidationHelper {

    private static final String ERROR_STYLE = "-fx-border-color: #d32f2f; -fx-border-width: 2px; -fx-border-radius: 3px;";
    private static final String ERROR_LABEL_STYLE = "-fx-text-fill: #d32f2f; -fx-font-size: 11px;";

    /**
     * Display validation errors for a field
     * @param control The JavaFX control to highlight
     * @param errorLabel The label to show error message
     * @param errors List of validation errors
     * @return true if there are errors, false otherwise
     */
    public static boolean showFieldErrors(Control control, Label errorLabel, List<String> errors) {
        if (errors.isEmpty()) {
            clearFieldError(control, errorLabel);
            return false;
        }

        // Highlight control with red border
        control.setStyle(ERROR_STYLE);

        // Show first error in label
        if (errorLabel != null) {
            errorLabel.setText(errors.get(0));
            errorLabel.setStyle(ERROR_LABEL_STYLE);
            errorLabel.setVisible(true);
        }

        return true;
    }

    /**
     * Clear validation error for a field
     */
    public static void clearFieldError(Control control, Label errorLabel) {
        // Remove red border
        control.setStyle("");

        // Hide error label
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
    }

    /**
     * Initialize error label styling
     */
    public static void initializeErrorLabel(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setStyle(ERROR_LABEL_STYLE);
        }
    }
}
package piJava.Controllers.frontoffice.preferencealerte;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.preferenceAlerte;
import piJava.services.AlerteService;
import piJava.utils.PreferenceAlerteValidator;
import piJava.utils.ValidationHelper;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class AlerteEditController implements Initializable {

    // Static variable to pass alerte from AlertesController
    public static preferenceAlerte currentAlerte = null;

    private preferenceAlerte alerte;

    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private TextField delaiField;
    @FXML private TextField debutField;
    @FXML private TextField finField;

    @FXML private Label nomErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label delaiErrorLabel;
    @FXML private Label debutErrorLabel;
    @FXML private Label finErrorLabel;

    @FXML private CheckBox activeCheck;
    @FXML private CheckBox defaultCheck;
    @FXML private CheckBox emailCheck;
    @FXML private CheckBox pushCheck;
    @FXML private CheckBox siteCheck;

    private final AlerteService service = new AlerteService();
    public void setAlerte(preferenceAlerte alerte) {
        this.alerte = alerte;
        fillForm(); // 🔥 auto-fill when received
    }

    private FrontSidebarController sidebarController;
    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFields();
        // Retrieve the alerte that was set by AlertesController before loading this view
        if (currentAlerte != null) {
            setAlerte(currentAlerte);
            currentAlerte = null; // Clear it after use
        }
    }

    @FXML
    private void initializeFields() {
        // Initialize error labels
        ValidationHelper.initializeErrorLabel(nomErrorLabel);
        ValidationHelper.initializeErrorLabel(descriptionErrorLabel);
        ValidationHelper.initializeErrorLabel(delaiErrorLabel);
        ValidationHelper.initializeErrorLabel(debutErrorLabel);
        ValidationHelper.initializeErrorLabel(finErrorLabel);

        // Add listeners for real-time validation
        nomField.textProperty().addListener((obs, oldVal, newVal) -> validateNomField());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateDescriptionField());
        delaiField.textProperty().addListener((obs, oldVal, newVal) -> validateDelaiField());
        debutField.textProperty().addListener((obs, oldVal, newVal) -> validateDebutField());
        finField.textProperty().addListener((obs, oldVal, newVal) -> validateFinField());
    }

    // ✅ Fill fields with existing data
    private void fillForm() {
        if (alerte == null) return;

        nomField.setText(alerte.getNom());
        descriptionField.setText(alerte.getDescription());
        delaiField.setText(String.valueOf(alerte.getDelai_rappel_min()));

        if (alerte.getHeure_silence_debut() != null)
            debutField.setText(alerte.getHeure_silence_debut().toString());

        if (alerte.getHeure_silence_fin() != null)
            finField.setText(alerte.getHeure_silence_fin().toString());

        activeCheck.setSelected(alerte.getIs_active());
        defaultCheck.setSelected(alerte.getIs_default());
        emailCheck.setSelected(alerte.getEmail_actif());
        pushCheck.setSelected(alerte.getPush_actif());
        siteCheck.setSelected(alerte.getSite_notif_active());
    }

    @FXML
    private void handleUpdate() {
        try {
            // Create a temporary object for validation
            preferenceAlerte temp = new preferenceAlerte();
            temp.setNom(nomField.getText());
            temp.setDescription(descriptionField.getText());
            temp.setDelai_rappel_min(delaiField.getText().isEmpty() ? 0 : Integer.parseInt(delaiField.getText()));

            try {
                temp.setHeure_silence_debut(debutField.getText().isEmpty() ? null : LocalTime.parse(debutField.getText()));
                temp.setHeure_silence_fin(finField.getText().isEmpty() ? null : LocalTime.parse(finField.getText()));
            } catch (Exception e) {
                // Ignore parsing errors here, validator will catch them
            }
            temp.setUser_id(alerte.getUser_id()); // Use existing user_id for validation

            // Validate
            List<String> errors = PreferenceAlerteValidator.validate(temp);
            if (!errors.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", String.join("\n", errors));
                return;
            }

            // If validation passes, update the actual object
            alerte.setNom(nomField.getText());
            alerte.setDescription(descriptionField.getText());
            alerte.setDelai_rappel_min(Integer.parseInt(delaiField.getText()));

            alerte.setIs_active(activeCheck.isSelected());
            alerte.setIs_default(defaultCheck.isSelected());
            alerte.setEmail_actif(emailCheck.isSelected());
            alerte.setPush_actif(pushCheck.isSelected());
            alerte.setSite_notif_active(siteCheck.isSelected());

            alerte.setHeure_silence_debut(LocalTime.parse(debutField.getText()));
            alerte.setHeure_silence_fin(LocalTime.parse(finField.getText()));

            alerte.setDate_mise_ajour(new Date());

            service.edit(alerte);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Préférence mise à jour ✔");

            this.handleBack();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le délai doit être un nombre !");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour !");
        }
    }

    private void handleBack() {
        if (sidebarController != null) {
            sidebarController.goToNotifications();
        }
    }

    @FXML
    private void handleCancel() {
        this.handleBack();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Validation methods
    private void validateNomField() {
        List<String> errors = PreferenceAlerteValidator.validateNom(nomField.getText());
        ValidationHelper.showFieldErrors(nomField, nomErrorLabel, errors);
    }

    private void validateDescriptionField() {
        List<String> errors = PreferenceAlerteValidator.validateDescription(descriptionField.getText());
        ValidationHelper.showFieldErrors(descriptionField, descriptionErrorLabel, errors);
    }

    private void validateDelaiField() {
        List<String> errors = PreferenceAlerteValidator.validateDelaiText(delaiField.getText());
        ValidationHelper.showFieldErrors(delaiField, delaiErrorLabel, errors);
    }

    private void validateDebutField() {
        List<String> errors = PreferenceAlerteValidator.validateHeureText(debutField.getText());
        // Also check relationship if both fields have values
        if (!debutField.getText().isEmpty() && !finField.getText().isEmpty()) {
            try {
                LocalTime debut = LocalTime.parse(debutField.getText());
                LocalTime fin = LocalTime.parse(finField.getText());
                if (debut.equals(fin)) {
                    errors.add("L'heure de début et de fin doivent être différentes.");
                }
            } catch (Exception e) {
                // Parsing errors already handled above
            }
        }
        ValidationHelper.showFieldErrors(debutField, debutErrorLabel, errors);
    }

    private void validateFinField() {
        List<String> errors = PreferenceAlerteValidator.validateHeureText(finField.getText());
        // Also check relationship if both fields have values
        if (!debutField.getText().isEmpty() && !finField.getText().isEmpty()) {
            try {
                LocalTime debut = LocalTime.parse(debutField.getText());
                LocalTime fin = LocalTime.parse(finField.getText());
                if (debut.equals(fin)) {
                    errors.add("L'heure de début et de fin doivent être différentes.");
                }
            } catch (Exception e) {
                // Parsing errors already handled above
            }
        }
        ValidationHelper.showFieldErrors(finField, finErrorLabel, errors);
    }
}

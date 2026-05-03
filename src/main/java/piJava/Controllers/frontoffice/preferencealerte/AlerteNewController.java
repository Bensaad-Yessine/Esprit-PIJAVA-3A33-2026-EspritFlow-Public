package piJava.Controllers.frontoffice.preferencealerte;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.preferenceAlerte;
import piJava.services.AlerteService;
import piJava.utils.PreferenceAlerteValidator;
import piJava.utils.SessionManager;
import piJava.utils.ValidationHelper;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class AlerteNewController implements Initializable {

    private final int currentUserId = SessionManager.getInstance().getCurrentUser().getId();

    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private TextField delaiField;
    @FXML private TextField debutField;
    @FXML
    private TextField finField;

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


    private FrontSidebarController sidebarController;
    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    @FXML
    private void initialize() {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFields();
    }

    @FXML
    private void initializeFields() {

    }

    private void handleBack() {
        if (sidebarController != null) {
            sidebarController.goToNotifications();
        }
    }

    @FXML
    public void handleCancel(ActionEvent actionEvent) {
        this.handleBack();
    }

    @FXML
    private void handleAdd() {
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
            temp.setUser_id(currentUserId); // Set user_id for validation

            // Validate
            List<String> errors = PreferenceAlerteValidator.validate(temp);
            if (!errors.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", String.join("\n", errors));
                return;
            }

            // If validation passes, create the object
            preferenceAlerte a = new preferenceAlerte();

            a.setNom(nomField.getText());
            a.setDescription(descriptionField.getText());
            a.setIs_active(activeCheck.isSelected());
            a.setIs_default(defaultCheck.isSelected());
            a.setEmail_actif(emailCheck.isSelected());
            a.setPush_actif(pushCheck.isSelected());
            a.setSite_notif_active(siteCheck.isSelected());
            a.setDelai_rappel_min(Integer.parseInt(delaiField.getText()));
            a.setHeure_silence_debut(LocalTime.parse(debutField.getText()));
            a.setHeure_silence_fin(LocalTime.parse(finField.getText()));
            a.setDate_creation(new Date());
            a.setDate_mise_ajour(new Date());
            a.setUser_id(currentUserId);
            service.add(a);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Préférence ajoutée avec succès ✔");

            this.handleBack();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le délai doit être un nombre !");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout !");
        }
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

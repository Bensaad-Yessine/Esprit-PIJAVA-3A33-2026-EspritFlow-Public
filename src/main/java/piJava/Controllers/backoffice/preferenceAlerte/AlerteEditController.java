package piJava.Controllers.backoffice.preferenceAlerte;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import piJava.Controllers.backoffice.SidebarController;
import piJava.entities.preferenceAlerte;
import piJava.entities.user;
import piJava.services.AlerteService;
import piJava.services.UserServices;
import piJava.utils.PreferenceAlerteValidator;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.*;

public class AlerteEditController {

    @FXML private ComboBox<user> cbUser;
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private TextField delaiField;
    @FXML private TextField debutField;
    @FXML private TextField finField;

    @FXML private CheckBox activeCheck;
    @FXML private CheckBox defaultCheck;
    @FXML private CheckBox emailCheck;
    @FXML private CheckBox pushCheck;
    @FXML private CheckBox siteCheck;

    // ERROR LABELS
    @FXML private Label userError;
    @FXML private Label nomError;
    @FXML private Label descriptionError;
    @FXML private Label delaiError;
    @FXML private Label timeError;

    private final AlerteService service = new AlerteService();
    private SidebarController sidebarController;
    private preferenceAlerte alerte;

    public void setSidebarController(SidebarController sidebarController) {
        this.sidebarController = sidebarController;
        if (AlertesController.getCurrentAlerteForEdit() != null) {
            setAlerte(AlertesController.getCurrentAlerteForEdit());
            AlertesController.setCurrentAlerteForEdit(null);
        }
    }

    public void setAlerte(preferenceAlerte alerte) {
        this.alerte = alerte;
        fillForm();
    }

    @FXML
    public void initialize() {
        // Setup live validation listeners
        nomField.textProperty().addListener((o, a, b) -> validateNom());
        descriptionField.textProperty().addListener((o, a, b) -> validateDescription());
        delaiField.textProperty().addListener((o, a, b) -> validateDelai());
        cbUser.valueProperty().addListener((o, a, b) -> validateUser());
        debutField.textProperty().addListener((o, a, b) -> validateTimes());
        finField.textProperty().addListener((o, a, b) -> validateTimes());
        chargerUsers();
    }

    private void chargerUsers() {
        UserServices userServices = new UserServices();
        List<user> users = userServices.show();
        cbUser.getItems().addAll(users);

        cbUser.setConverter(new StringConverter<user>() {
            @Override
            public String toString(user u) {
                return u == null ? "" : u.getEmail();
            }

            @Override
            public user fromString(String string) {
                return null;
            }
        });
    }

    // ═══════════════════════════════════════════════
    // FILL FORM
    // ═══════════════════════════════════════════════

    private void fillForm() {
        if (alerte == null) return;

        nomField.setText(alerte.getNom());
        descriptionField.setText(alerte.getDescription());
        
        // Find the user object with the matching ID
        for (user u : cbUser.getItems()) {
            if (u.getId() == alerte.getUser_id()) {
                cbUser.setValue(u);
                break;
            }
        }
        
        delaiField.setText(String.valueOf(alerte.getDelai_rappel_min()));

        if (alerte.getHeure_silence_debut() != null)
            debutField.setText(alerte.getHeure_silence_debut().toString());

        if (alerte.getHeure_silence_fin() != null)
            finField.setText(alerte.getHeure_silence_fin().toString());

        activeCheck.setSelected(Boolean.TRUE.equals(alerte.getIs_active()));
        defaultCheck.setSelected(Boolean.TRUE.equals(alerte.getIs_default()));
        emailCheck.setSelected(Boolean.TRUE.equals(alerte.getEmail_actif()));
        pushCheck.setSelected(Boolean.TRUE.equals(alerte.getPush_actif()));
        siteCheck.setSelected(Boolean.TRUE.equals(alerte.getSite_notif_active()));
    }

    // ═══════════════════════════════════════════════
    // VALIDATION METHODS (LIVE)
    // ═══════════════════════════════════════════════

    private void validateNom() {
        List<String> errors = PreferenceAlerteValidator.validateNom(nomField.getText());
        displayError(nomField, nomError, errors);
    }

    private void validateDescription() {
        List<String> errors = PreferenceAlerteValidator.validateDescription(descriptionField.getText());
        displayError(descriptionField, descriptionError, errors);
    }

    private void validateDelai() {
        try {
            if (delaiField.getText().isEmpty()) {
                displayError(delaiField, delaiError, List.of("Le délai est obligatoire."));
            } else {
                int delai = Integer.parseInt(delaiField.getText());
                List<String> errors = PreferenceAlerteValidator.validateDelaiRappel(delai);
                displayError(delaiField, delaiError, errors);
            }
        } catch (NumberFormatException e) {
            displayError(delaiField, delaiError, List.of("Le délai doit être un nombre."));
        }
    }

    private void validateUser() {
        user u = cbUser.getValue();
        int userId = (u == null) ? 0 : u.getId();
        List<String> errors = PreferenceAlerteValidator.validateUser(userId);
        displayError(cbUser, userError, errors);
    }

    private void validateTimes() {
        try {
            LocalTime debut = null;
            LocalTime fin = null;

            if (!debutField.getText().isEmpty()) {
                debut = LocalTime.parse(debutField.getText());
            }
            if (!finField.getText().isEmpty()) {
                fin = LocalTime.parse(finField.getText());
            }

            List<String> errors = PreferenceAlerteValidator.validateSilenceTimes(debut, fin);
            displayError(debutField, timeError, errors);
        } catch (Exception e) {
            displayError(debutField, timeError, List.of("Format heure invalide (HH:mm)."));
        }
    }

    // ═══════════════════════════════════════════════
    // DISPLAY ERROR METHOD
    // ═══════════════════════════════════════════════

    private void displayError(Control control, Label errorLabel, List<String> errors) {
        if (errors.isEmpty()) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            control.getStyleClass().remove("error");
        } else {
            errorLabel.setText(errors.get(0));
            errorLabel.setVisible(true);
            if (!control.getStyleClass().contains("error")) {
                control.getStyleClass().add("error");
            }
        }
    }

    // ═══════════════════════════════════════════════
    // HANDLE UPDATE
    // ═══════════════════════════════════════════════

    @FXML
    private void handleUpdate() {
        // Clear previous errors
        clearAllErrors();

        try {
            alerte.setNom(nomField.getText());
            alerte.setDescription(descriptionField.getText());
            alerte.setIs_active(activeCheck.isSelected());
            alerte.setIs_default(defaultCheck.isSelected());
            alerte.setEmail_actif(emailCheck.isSelected());
            alerte.setPush_actif(pushCheck.isSelected());
            alerte.setSite_notif_active(siteCheck.isSelected());
            alerte.setDelai_rappel_min(Integer.parseInt(delaiField.getText()));
            alerte.setHeure_silence_debut(LocalTime.parse(debutField.getText()));
            alerte.setHeure_silence_fin(LocalTime.parse(finField.getText()));
            if (cbUser.getValue() != null) {
                alerte.setUser_id(cbUser.getValue().getId());
            } else {
                alerte.setUser_id(0);
            }
            alerte.setDate_mise_ajour(new Date());

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Format invalide ! Vérifiez les nombres et l'heure (HH:mm).");
            return;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
            return;
        }

        // 🔥 FULL VALIDATION
        List<String> errors = PreferenceAlerteValidator.validate(alerte);

        if (!errors.isEmpty()) {
            mapErrorsToUI(errors);
            return;
        }

        try {
            service.edit(alerte);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Préférence mise à jour ✔");
            handleBack();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════
    // ERROR MAPPING
    // ═══════════════════════════════════════════════

    private void mapErrorsToUI(List<String> errors) {
        for (String error : errors) {
            if (error.toLowerCase().contains("nom")) {
                displayError(nomField, nomError, List.of(error));
            } else if (error.toLowerCase().contains("description")) {
                displayError(descriptionField, descriptionError, List.of(error));
            } else if (error.toLowerCase().contains("délai") || error.toLowerCase().contains("delai")) {
                displayError(delaiField, delaiError, List.of(error));
            } else if (error.toLowerCase().contains("heure")) {
                displayError(debutField, timeError, List.of(error));
            } else if (error.toLowerCase().contains("utilisateur")) {
                displayError(cbUser, userError, List.of(error));
            }
        }
    }

    private void clearAllErrors() {
        nomError.setText("");
        nomError.setVisible(false);
        nomField.getStyleClass().remove("error");

        descriptionError.setText("");
        descriptionError.setVisible(false);
        descriptionField.getStyleClass().remove("error");

        delaiError.setText("");
        delaiError.setVisible(false);
        delaiField.getStyleClass().remove("error");

        timeError.setText("");
        timeError.setVisible(false);
        debutField.getStyleClass().remove("error");
        finField.getStyleClass().remove("error");

        userError.setText("");
        userError.setVisible(false);
        cbUser.getStyleClass().remove("error");
    }

    // ═══════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleBack() {
        if (sidebarController != null) {
            sidebarController.goToNotifications();
        } else {
            System.err.println("Sidebar controller not set!");
        }
    }

    @FXML
    private void handleCancel() {
        this.handleBack();
    }
}
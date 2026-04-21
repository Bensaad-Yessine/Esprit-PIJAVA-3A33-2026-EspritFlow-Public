package piJava.Controllers.backoffice.taches;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import piJava.Controllers.backoffice.SidebarController;
import piJava.entities.suiviTache;
import piJava.entities.tache;
import piJava.services.SuiviTacheService;
import piJava.services.TacheService;
import piJava.utils.TacheValidator;
import piJava.utils.ValidationHelper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class TacheNewController {

    @FXML private ComboBox<Integer> cbUser;
    @FXML private TextField txtTitre;
    @FXML private ComboBox<String> cbType;
    @FXML private DatePicker dpDateDebut;
    @FXML private TextField txtHeureDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private TextField txtHeureFin;
    @FXML private ComboBox<String> cbPriorite;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtDuree;

    // Error labels
    @FXML private Label lblTitreError;
    @FXML private Label lblDescriptionError;
    @FXML private Label lblTypeError;
    @FXML private Label lblPrioriteError;
    @FXML private Label lblStatutError;
    @FXML private Label lblUserError;
    @FXML private Label lblDateDebutError;
    @FXML private Label lblDateFinError;
    @FXML private Label lblDureeError;

    private SidebarController sidebarController;
    private boolean hasValidationErrors = false;

    @FXML
    public void initialize() {
        // Initialize all error labels
        ValidationHelper.initializeErrorLabel(lblTitreError);
        ValidationHelper.initializeErrorLabel(lblDescriptionError);
        ValidationHelper.initializeErrorLabel(lblTypeError);
        ValidationHelper.initializeErrorLabel(lblPrioriteError);
        ValidationHelper.initializeErrorLabel(lblStatutError);
        ValidationHelper.initializeErrorLabel(lblUserError);
        ValidationHelper.initializeErrorLabel(lblDateDebutError);
        ValidationHelper.initializeErrorLabel(lblDateFinError);
        ValidationHelper.initializeErrorLabel(lblDureeError);

        // Setup real-time validation
        setupValidationListeners();
        initComboBoxes();
        cbUser.getItems().addAll(1, 2, 3, 4, 5);
    }

    private void initComboBoxes() {

        // TYPE (from Symfony entity)
        cbType.getItems().addAll(
                "MANUEL",
                "REUNION",
                "REVISION",
                "SANTE",
                "EMPLOI"
        );

        // PRIORITE
        cbPriorite.getItems().addAll(
                "FAIBLE",
                "MOYEN",
                "ELEVEE"
        );

        // STATUT
        cbStatut.getItems().addAll(
                "A_FAIRE",
                "EN_COURS",
                "TERMINE",
                "EN_RETARD",
                "PAUSED",
                "ABANDON"
        );
        cbStatut.setPromptText("Statut (ex: EN_COURS)");
        cbPriorite.setPromptText("Priorité");
        cbType.setPromptText("Type de tâche");
    }

    private void setupValidationListeners() {
        // Titre validation - on text change
        txtTitre.textProperty().addListener((obs, oldVal, newVal) -> {
            validateTitreField();
        });

        // Description validation - on text change
        txtDescription.textProperty().addListener((obs, oldVal, newVal) -> {
            validateDescriptionField();
        });

        // Type validation
        cbType.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateTypeField();
        });

        // Priorite validation
        cbPriorite.valueProperty().addListener((obs, oldVal, newVal) -> {
            validatePrioriteField();
        });

        // Statut validation
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateStatutField();
        });

        // User validation
        cbUser.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateUserField();
        });

        // Date debut validation
        dpDateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDateDebutField();
        });

        txtHeureDebut.textProperty().addListener((obs, oldVal, newVal) -> {
            validateDateDebutField();
        });

        txtHeureDebut.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                validateDateDebutField();
            }
        });

        // Date fin validation
        dpDateFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDateFinField();
        });

        txtHeureFin.textProperty().addListener((obs, oldVal, newVal) -> {
            validateDateFinField();
        });

        txtHeureFin.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                validateDateFinField();
            }
        });

        // Duree validation
        txtDuree.textProperty().addListener((obs, oldVal, newVal) -> {
            validateDureeField();
        });
    }

    // ═══════════════════════════════════════════════════════════
    // FIELD VALIDATION METHODS (using centralized validator)
    // ═══════════════════════════════════════════════════════════

    private void validateTitreField() {
        List<String> errors = TacheValidator.validateTitre(txtTitre.getText());
        ValidationHelper.showFieldErrors(txtTitre, lblTitreError, errors);
    }

    private void validateDescriptionField() {
        List<String> errors = TacheValidator.validateDescription(txtDescription.getText());
        ValidationHelper.showFieldErrors(txtDescription, lblDescriptionError, errors);
    }

    private void validateTypeField() {
        List<String> errors = TacheValidator.validateType(cbType.getValue());
        ValidationHelper.showFieldErrors(cbType, lblTypeError, errors);
    }

    private void validatePrioriteField() {
        List<String> errors = TacheValidator.validatePriorite(cbPriorite.getValue());
        ValidationHelper.showFieldErrors(cbPriorite, lblPrioriteError, errors);
    }

    private void validateStatutField() {
        List<String> errors = TacheValidator.validateStatut(cbStatut.getValue());
        ValidationHelper.showFieldErrors(cbStatut, lblStatutError, errors);
    }

    private void validateUserField() {
        Integer userId = cbUser.getValue();
        List<String> errors = TacheValidator.validateUser(userId == null ? 0 : userId);
        ValidationHelper.showFieldErrors(cbUser, lblUserError, errors);
    }

    private void validateDateDebutField() {
        try {
            Date dateDebut = parseDateDebut();
            List<String> errors = TacheValidator.validateDateDebut(dateDebut);
            ValidationHelper.showFieldErrors(dpDateDebut, lblDateDebutError, errors);

            // Also revalidate date fin to check duration
            validateDateFinField();
        } catch (Exception e) {
            ValidationHelper.showFieldErrors(txtHeureDebut, lblDateDebutError,
                    List.of("Format d'heure invalide (HH:mm)."));
        }
    }

    private void validateDateFinField() {
        try {
            Date dateDebut = parseDateDebut();
            Date dateFin = parseDateFin();

            // Validate both dates together (includes duration check)
            List<String> errors = TacheValidator.validateDates(dateDebut, dateFin);

            // Filter to show only errors related to date fin
            List<String> dateFinErrors = errors.stream()
                    .filter(e -> e.contains("date de fin") || e.contains("durer plus"))
                    .toList();

            ValidationHelper.showFieldErrors(dpDateFin, lblDateFinError, dateFinErrors);
        } catch (Exception e) {
            ValidationHelper.showFieldErrors(txtHeureFin, lblDateFinError,
                    List.of("Format d'heure invalide (HH:mm)."));
        }
    }

    private void validateDureeField() {
        List<String> errors = TacheValidator.validateDureeEstimeeText(txtDuree.getText());
        ValidationHelper.showFieldErrors(txtDuree, lblDureeError, errors);
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    private Date parseDateDebut() throws Exception {
        LocalDate startDate = dpDateDebut.getValue();
        if (startDate == null) return null;

        String heureText = txtHeureDebut.getText();
        if (heureText == null || heureText.trim().isEmpty()) return null;

        LocalTime startTime = LocalTime.parse(heureText);
        LocalDateTime startLdt = LocalDateTime.of(startDate, startTime);
        return Date.from(startLdt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private Date parseDateFin() throws Exception {
        LocalDate endDate = dpDateFin.getValue();
        if (endDate == null) return null;

        String heureText = txtHeureFin.getText();
        if (heureText == null || heureText.trim().isEmpty()) return null;

        LocalTime endTime = LocalTime.parse(heureText);
        LocalDateTime endLdt = LocalDateTime.of(endDate, endTime);
        return Date.from(endLdt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private boolean validateAllFields() {
        validateTitreField();
        validateDescriptionField();
        validateTypeField();
        validatePrioriteField();
        validateStatutField();
        validateUserField();
        validateDateDebutField();
        validateDateFinField();
        validateDureeField();

        // Check if any error label is visible
        return !lblTitreError.isVisible() &&
                !lblDescriptionError.isVisible() &&
                !lblTypeError.isVisible() &&
                !lblPrioriteError.isVisible() &&
                !lblStatutError.isVisible() &&
                !lblUserError.isVisible() &&
                !lblDateDebutError.isVisible() &&
                !lblDateFinError.isVisible() &&
                !lblDureeError.isVisible();
    }

    // ═══════════════════════════════════════════════════════════
    // SUBMIT HANDLER
    // ═══════════════════════════════════════════════════════════

    @FXML
    private void handleAdd() {
        // Validate all fields
        if (!validateAllFields()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreurs de validation");
            alert.setHeaderText("Veuillez corriger les erreurs avant de continuer.");
            alert.setContentText("Des champs contiennent des erreurs (surlignés en rouge).");
            alert.showAndWait();
            return;
        }

        try {
            // Parse dates
            Date dateDebut = parseDateDebut();
            Date dateFin = parseDateFin();

            // Create task object
            tache t = new tache();
            t.setTitre(txtTitre.getText());
            t.setType(cbType.getValue());
            t.setDate_debut(dateDebut);
            t.setDate_fin(dateFin);
            t.setPriorite(cbPriorite.getValue());
            t.setStatut(cbStatut.getValue());
            t.setDescription(txtDescription.getText());
            t.setDuree_estimee(txtDuree.getText().isEmpty() ? 0 : Integer.parseInt(txtDuree.getText()));
            t.setCreated_at(new Date());
            t.setUpdated_at(new Date());
            t.setUser_id(cbUser.getValue());
            t.setPrediction(0.5);

            // Double-check with full validation (safety net)
            List<String> errors = TacheValidator.validate(t);
            if (!errors.isEmpty()) {
                showValidationErrors(errors);
                return;
            }

            // Add to database
            TacheService ts = new TacheService();
            ts.add(t);

            SuiviTacheService st = new SuiviTacheService();
            suiviTache s = new suiviTache();
            s.setTache(t);
            s.setAncienStatut("");
            s.setNouveauStatut(t.getStatut());
            s.setCommentaire("");
            s.setDateAction(new Date());
            st.add(s);

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Succès");
            info.setHeaderText("Tache créée");
            info.setContentText("La tache a été ajoutée avec succès.");
            info.showAndWait();
            this.handleBack();

        } catch (Exception e) {
            Alert info = new Alert(Alert.AlertType.ERROR);
            info.setTitle("Échec");
            info.setHeaderText("Erreur lors de la création de la tache");
            info.setContentText(e.getMessage());
            info.showAndWait();
        }
    }

    private void showValidationErrors(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de saisie");
        alert.setHeaderText("Veuillez corriger les erreurs suivantes :");

        StringBuilder sb = new StringBuilder();
        for (String err : errors) {
            sb.append("• ").append(err).append("\n");
        }

        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    public void setSidebarController(SidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    public void handleBack() {
        if (sidebarController != null) {
            sidebarController.goToTaches();
        } else {
            System.err.println("Sidebar controller not set!");
        }
    }
}
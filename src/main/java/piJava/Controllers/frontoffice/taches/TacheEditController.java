package piJava.Controllers.frontoffice.taches;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.tache;
import piJava.services.TacheService;
import piJava.utils.TacheValidator;
import piJava.utils.ValidationHelper;

import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TacheEditController {

    private tache currentTache;
    private final TacheService service = new TacheService();

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

    // error labels
    @FXML private Label lblTitreError;
    @FXML private Label lblTypeError;
    @FXML private Label lblDateDebutError;
    @FXML private Label lblDateFinError;
    @FXML private Label lblPrioriteError;
    @FXML private Label lblStatutError;
    @FXML private Label lblDescriptionError;
    @FXML private Label lblDureeError;

    @FXML private Label lblNotification;

    private FrontSidebarController sidebarController;

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
        // Retrieve the task that was set before loading this view
        if (TachesController.getCurrentTaskForEdit() != null) {
            setTache(TachesController.getCurrentTaskForEdit());
            TachesController.setCurrentTaskForEdit(null); // Clear it after use
        }
    }

    public void setTache(tache t) {
        this.currentTache = t;
        fillForm();
    }

    @FXML
    public void initialize() {

        // combos
        cbType.getItems().addAll("MANUEL", "REUNION", "REVISION", "SANTE", "EMPLOI");
        cbPriorite.getItems().addAll("FAIBLE", "MOYEN", "ELEVEE");
        cbStatut.getItems().addAll("A_FAIRE", "EN_COURS", "TERMINE", "EN_RETARD", "PAUSED", "ABANDON");

        ValidationHelper.initializeErrorLabel(lblTitreError);
        ValidationHelper.initializeErrorLabel(lblTypeError);
        ValidationHelper.initializeErrorLabel(lblDateDebutError);
        ValidationHelper.initializeErrorLabel(lblDateFinError);
        ValidationHelper.initializeErrorLabel(lblPrioriteError);
        ValidationHelper.initializeErrorLabel(lblStatutError);
        ValidationHelper.initializeErrorLabel(lblDescriptionError);
        ValidationHelper.initializeErrorLabel(lblDureeError);

        // live validation
        txtTitre.textProperty().addListener((o, a, b) -> validateTitre());
        txtDescription.textProperty().addListener((o, a, b) -> validateDescription());
        txtDuree.textProperty().addListener((o, a, b) -> validateDuree());

        cbType.valueProperty().addListener((o, a, b) -> validateType());
        cbPriorite.valueProperty().addListener((o, a, b) -> validatePriorite());
        cbStatut.valueProperty().addListener((o, a, b) -> validateStatut());

        dpDateDebut.valueProperty().addListener((o, a, b) -> validateDates());
        dpDateFin.valueProperty().addListener((o, a, b) -> validateDates());

        txtHeureDebut.textProperty().addListener((o, a, b) -> validateDates());
        txtHeureFin.textProperty().addListener((o, a, b) -> validateDates());
    }

    // ---------------- FILL ----------------
    private void fillForm() {
        if (currentTache == null) return;

        txtTitre.setText(currentTache.getTitre());
        cbType.setValue(currentTache.getType());

        LocalDateTime start = LocalDateTime.ofInstant(
                currentTache.getDate_debut().toInstant(),
                ZoneId.systemDefault()
        );

        LocalDateTime end = LocalDateTime.ofInstant(
                currentTache.getDate_fin().toInstant(),
                ZoneId.systemDefault()
        );

        dpDateDebut.setValue(start.toLocalDate());
        txtHeureDebut.setText(start.toLocalTime().toString());

        dpDateFin.setValue(end.toLocalDate());
        txtHeureFin.setText(end.toLocalTime().toString());

        cbPriorite.setValue(currentTache.getPriorite());
        cbStatut.setValue(currentTache.getStatut());

        txtDescription.setText(currentTache.getDescription());
        txtDuree.setText(String.valueOf(currentTache.getDuree_estimee()));
    }

    // ---------------- VALIDATION ----------------

    private void validateTitre() {
        List<String> errors = TacheValidator.validateTitre(txtTitre.getText());
        ValidationHelper.showFieldErrors(txtTitre, lblTitreError, errors);
    }

    private void validateType() {
        List<String> errors = TacheValidator.validateType(cbType.getValue());
        ValidationHelper.showFieldErrors(cbType, lblTypeError, errors);
    }

    private void validatePriorite() {
        List<String> errors = TacheValidator.validatePriorite(cbPriorite.getValue());
        ValidationHelper.showFieldErrors(cbPriorite, lblPrioriteError, errors);
    }

    private void validateStatut() {
        List<String> errors = TacheValidator.validateStatut(cbStatut.getValue());
        ValidationHelper.showFieldErrors(cbStatut, lblStatutError, errors);
    }

    private void validateDescription() {
        List<String> errors = TacheValidator.validateDescription(txtDescription.getText());
        ValidationHelper.showFieldErrors(txtDescription, lblDescriptionError, errors);
    }

    private void validateDuree() {
        List<String> errors = TacheValidator.validateDureeEstimeeText(txtDuree.getText());
        ValidationHelper.showFieldErrors(txtDuree, lblDureeError, errors);
    }

    private void validateDates() {
        try {
            Date d1 = parseDate(dpDateDebut, txtHeureDebut);
            Date d2 = parseDate(dpDateFin, txtHeureFin);

            ValidationHelper.showFieldErrors(dpDateDebut, lblDateDebutError,
                    TacheValidator.validateDateDebut(d1));

            ValidationHelper.showFieldErrors(dpDateFin, lblDateFinError,
                    TacheValidator.validateDates(d1, d2));

        } catch (Exception ignored) {}
    }

    private Date parseDate(DatePicker dp, TextField tf) {
        LocalDate d = dp.getValue();
        LocalTime t = LocalTime.parse(tf.getText());
        return Date.from(LocalDateTime.of(d, t)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    // ---------------- UPDATE ----------------

    @FXML
    private void handleUpdate() {

        // Validate all form fields
        List<String> errors = new ArrayList<>();

        errors.addAll(TacheValidator.validateTitre(txtTitre.getText()));
        errors.addAll(TacheValidator.validateDescription(txtDescription.getText()));
        errors.addAll(TacheValidator.validateType(cbType.getValue()));
        errors.addAll(TacheValidator.validatePriorite(cbPriorite.getValue()));
        errors.addAll(TacheValidator.validateStatut(cbStatut.getValue()));

        try {
            Date d1 = parseDate(dpDateDebut, txtHeureDebut);
            Date d2 = parseDate(dpDateFin, txtHeureFin);
            errors.addAll(TacheValidator.validateDates(d1, d2));
        } catch (Exception e) {
            errors.add("Format heure invalide (HH:mm)");
        }

        errors.addAll(TacheValidator.validateDureeEstimeeText(txtDuree.getText()));

        if (!errors.isEmpty()) {
            return;
        }

        try {
            currentTache.setTitre(txtTitre.getText());
            currentTache.setDescription(txtDescription.getText());
            currentTache.setType(cbType.getValue());
            currentTache.setPriorite(cbPriorite.getValue());
            currentTache.setStatut(cbStatut.getValue());

            Date d1 = parseDate(dpDateDebut, txtHeureDebut);
            Date d2 = parseDate(dpDateFin, txtHeureFin);

            currentTache.setDate_debut(d1);
            currentTache.setDate_fin(d2);

            currentTache.setDuree_estimee(Integer.parseInt(txtDuree.getText()));

            service.edit(currentTache);

            lblNotification.setText("✔ Mise à jour réussie");
            handleBack();

        } catch (Exception e) {
            showError();
        }
    }

    private void handleBack() {
        try {
            if (sidebarController != null) {
                sidebarController.goToTaches();
            }
        } catch (Exception e) {
            System.err.println("Error going back: " + e.getMessage());
        }
    }

    private void showError() {
        lblNotification.setStyle("-fx-text-fill: red;");
        lblNotification.setText("Erreur lors de la mise à jour");
    }

    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Annulation");
        alert.setHeaderText("Annuler les modifications ?");
        alert.setContentText("Les changements non enregistrés seront perdus.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            handleBack();
        }
    }
}

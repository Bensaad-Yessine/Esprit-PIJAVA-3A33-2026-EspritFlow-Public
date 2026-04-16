package piJava.Controllers.backoffice.taches;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import piJava.Controllers.backoffice.SidebarController;
import piJava.entities.tache;
import piJava.services.TacheService;
import piJava.utils.TacheValidator;
import piJava.utils.ValidationHelper;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TacheEditController {

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
    @FXML private ComboBox<Integer> cbUser;

    // error labels (IMPORTANT)
    @FXML private Label lblTitreError;
    @FXML private Label lblDescriptionError;
    @FXML private Label lblTypeError;
    @FXML private Label lblPrioriteError;
    @FXML private Label lblStatutError;
    @FXML private Label lblUserError;
    @FXML private Label lblDateDebutError;
    @FXML private Label lblDateFinError;
    @FXML private Label lblDureeError;

    private tache currentTache;
    private final TacheService service = new TacheService();
    private SidebarController sidebarController;

    public void setSidebarController(SidebarController sidebarController) {
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

        // combos (same as create screen)
        cbType.getItems().addAll("MANUEL", "REUNION", "REVISION", "SANTE", "EMPLOI");
        cbPriorite.getItems().addAll("FAIBLE", "MOYEN", "ELEVEE");
        cbStatut.getItems().addAll("A_FAIRE", "EN_COURS", "TERMINE", "EN_RETARD", "PAUSED", "ABANDON");

        cbUser.getItems().addAll(1, 2, 3, 4, 5);

        // init error labels
        ValidationHelper.initializeErrorLabel(lblTitreError);
        ValidationHelper.initializeErrorLabel(lblDescriptionError);
        ValidationHelper.initializeErrorLabel(lblTypeError);
        ValidationHelper.initializeErrorLabel(lblPrioriteError);
        ValidationHelper.initializeErrorLabel(lblStatutError);
        ValidationHelper.initializeErrorLabel(lblUserError);
        ValidationHelper.initializeErrorLabel(lblDateDebutError);
        ValidationHelper.initializeErrorLabel(lblDateFinError);
        ValidationHelper.initializeErrorLabel(lblDureeError);

        setupValidation();
    }

    private void setupValidation() {

        txtTitre.textProperty().addListener((o, a, b) -> validateTitre());
        txtDescription.textProperty().addListener((o, a, b) -> validateDescription());

        cbType.valueProperty().addListener((o, a, b) -> validateType());
        cbPriorite.valueProperty().addListener((o, a, b) -> validatePriorite());
        cbStatut.valueProperty().addListener((o, a, b) -> validateStatut());
        cbUser.valueProperty().addListener((o, a, b) -> validateUser());

        dpDateDebut.valueProperty().addListener((o, a, b) -> validateDates());
        dpDateFin.valueProperty().addListener((o, a, b) -> validateDates());

        txtHeureDebut.textProperty().addListener((o, a, b) -> validateDates());
        txtHeureFin.textProperty().addListener((o, a, b) -> validateDates());

        txtDuree.textProperty().addListener((o, a, b) -> validateDuree());
    }

    // VALIDATION METHODS
    private void validateTitre() {
        ValidationHelper.showFieldErrors(txtTitre, lblTitreError,
                TacheValidator.validateTitre(txtTitre.getText()));
    }

    private void validateDescription() {
        ValidationHelper.showFieldErrors(txtDescription, lblDescriptionError,
                TacheValidator.validateDescription(txtDescription.getText()));
    }

    private void validateType() {
        ValidationHelper.showFieldErrors(cbType, lblTypeError,
                TacheValidator.validateType(cbType.getValue()));
    }

    private void validatePriorite() {
        ValidationHelper.showFieldErrors(cbPriorite, lblPrioriteError,
                TacheValidator.validatePriorite(cbPriorite.getValue()));
    }

    private void validateStatut() {
        ValidationHelper.showFieldErrors(cbStatut, lblStatutError,
                TacheValidator.validateStatut(cbStatut.getValue()));
    }

    private void validateUser() {
        Integer id = cbUser.getValue();
        ValidationHelper.showFieldErrors(cbUser, lblUserError,
                TacheValidator.validateUser(id == null ? 0 : id));
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

    private void validateDuree() {
        ValidationHelper.showFieldErrors(txtDuree, lblDureeError,
                TacheValidator.validateDureeEstimeeText(txtDuree.getText()));
    }

    private Date parseDate(DatePicker dp, TextField tf) {
        if (dp.getValue() == null || tf.getText().isEmpty()) return null;
        LocalTime t = LocalTime.parse(tf.getText());
        LocalDateTime dt = LocalDateTime.of(dp.getValue(), t);
        return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private void fillForm() {
        if (currentTache == null) return;

        txtTitre.setText(currentTache.getTitre());
        txtDescription.setText(currentTache.getDescription());

        cbType.setValue(currentTache.getType());
        cbPriorite.setValue(currentTache.getPriorite());
        cbStatut.setValue(currentTache.getStatut());
        cbUser.setValue(currentTache.getUser_id());

        LocalDateTime d1 = LocalDateTime.ofInstant(currentTache.getDate_debut().toInstant(), ZoneId.systemDefault());
        LocalDateTime d2 = LocalDateTime.ofInstant(currentTache.getDate_fin().toInstant(), ZoneId.systemDefault());

        dpDateDebut.setValue(d1.toLocalDate());
        txtHeureDebut.setText(d1.toLocalTime().toString());

        dpDateFin.setValue(d2.toLocalDate());
        txtHeureFin.setText(d2.toLocalTime().toString());

        txtDuree.setText(String.valueOf(currentTache.getDuree_estimee()));
    }

    @FXML
    private void handleUpdate() {

        // Validate all form fields
        List<String> errors = new ArrayList<>();

        errors.addAll(TacheValidator.validateTitre(txtTitre.getText()));
        errors.addAll(TacheValidator.validateDescription(txtDescription.getText()));
        errors.addAll(TacheValidator.validateType(cbType.getValue()));
        errors.addAll(TacheValidator.validatePriorite(cbPriorite.getValue()));
        errors.addAll(TacheValidator.validateStatut(cbStatut.getValue()));
        errors.addAll(TacheValidator.validateUser(cbUser.getValue() == null ? 0 : cbUser.getValue()));

        Date d1 = parseDate(dpDateDebut, txtHeureDebut);
        Date d2 = parseDate(dpDateFin, txtHeureFin);
        errors.addAll(TacheValidator.validateDates(d1, d2));

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
            currentTache.setUser_id(cbUser.getValue());


            currentTache.setDate_debut(d1);
            currentTache.setDate_fin(d2);

            currentTache.setDuree_estimee(Integer.parseInt(txtDuree.getText()));

            service.edit(currentTache);

            handleBack();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleBack() {
        if (sidebarController != null) {
            sidebarController.goToTaches();
        } else {
            System.err.println("Sidebar controller not set!");
        }
    }

    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Annulation");
        alert.setHeaderText("Annuler les modifications ?");
        alert.setContentText("Les changements non sauvegardés seront perdus.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            handleBack();
        }
    }
}
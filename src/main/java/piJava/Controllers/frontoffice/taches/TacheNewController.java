package piJava.Controllers.frontoffice.taches;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.suiviTache;
import piJava.entities.tache;
import piJava.services.SuiviTacheService;
import piJava.services.TacheService;
import piJava.utils.SessionManager;
import piJava.utils.TacheValidator;
import piJava.utils.ValidationHelper;

import java.time.*;
import java.util.Date;
import java.util.List;

public class TacheNewController {

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

    private int current_userId = SessionManager.getInstance().getCurrentUser().getId();

    // error labels (same system as backoffice)
    @FXML private Label lblTitreError;
    @FXML private Label lblDescriptionError;
    @FXML private Label lblTypeError;
    @FXML private Label lblPrioriteError;
    @FXML private Label lblStatutError;
    @FXML private Label lblDateDebutError;
    @FXML private Label lblDateFinError;
    @FXML private Label lblDureeError;

    private FrontSidebarController sidebarController;

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    @FXML
    public void initialize() {

        // initialize error labels
        ValidationHelper.initializeErrorLabel(lblTitreError);
        ValidationHelper.initializeErrorLabel(lblDescriptionError);
        ValidationHelper.initializeErrorLabel(lblTypeError);
        ValidationHelper.initializeErrorLabel(lblPrioriteError);
        ValidationHelper.initializeErrorLabel(lblStatutError);
        ValidationHelper.initializeErrorLabel(lblDateDebutError);
        ValidationHelper.initializeErrorLabel(lblDateFinError);
        ValidationHelper.initializeErrorLabel(lblDureeError);

        // validation listeners (same behavior as backoffice)
        setupValidation();
    }

    private void setupValidation() {

        txtTitre.textProperty().addListener((o, a, b) -> validateTitre());
        txtDescription.textProperty().addListener((o, a, b) -> validateDescription());

        cbType.valueProperty().addListener((o, a, b) -> validateType());
        cbPriorite.valueProperty().addListener((o, a, b) -> validatePriorite());
        cbStatut.valueProperty().addListener((o, a, b) -> validateStatut());

        dpDateDebut.valueProperty().addListener((o, a, b) -> validateDates());
        dpDateFin.valueProperty().addListener((o, a, b) -> validateDates());

        txtHeureDebut.textProperty().addListener((o, a, b) -> validateDates());
        txtHeureFin.textProperty().addListener((o, a, b) -> validateDates());

        txtDuree.textProperty().addListener((o, a, b) -> validateDuree());
    }

    // ───────────────────────── VALIDATION METHODS ─────────────────────────

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

    private void validateDates() {
        try {
            Date d1 = parseDate(dpDateDebut, txtHeureDebut);
            Date d2 = parseDate(dpDateFin, txtHeureFin);

            ValidationHelper.showFieldErrors(dpDateDebut, lblDateDebutError,
                    TacheValidator.validateDateDebut(d1));

            ValidationHelper.showFieldErrors(dpDateFin, lblDateFinError,
                    TacheValidator.validateDates(d1, d2));

        } catch (Exception e) {
            // ignore parsing errors (handled visually)
        }
    }

    private void validateDuree() {
        ValidationHelper.showFieldErrors(txtDuree, lblDureeError,
                TacheValidator.validateDureeEstimeeText(txtDuree.getText()));
    }

    // ───────────────────────── HELPERS ─────────────────────────

    private Date parseDate(DatePicker dp, TextField tf) {
        if (dp.getValue() == null || tf.getText().isEmpty()) return null;

        LocalTime time = LocalTime.parse(tf.getText());
        LocalDateTime dt = LocalDateTime.of(dp.getValue(), time);

        return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
    }

    // ───────────────────────── ADD TASK ─────────────────────────

    @FXML
    private void handleAdd() {

        try {
            // build object
            tache t = new tache();
            t.setTitre(txtTitre.getText());
            t.setType(cbType.getValue());
            t.setPriorite(cbPriorite.getValue());
            t.setStatut(cbStatut.getValue());
            t.setDescription(txtDescription.getText());

            t.setDate_debut(parseDate(dpDateDebut, txtHeureDebut));
            t.setDate_fin(parseDate(dpDateFin, txtHeureFin));

            t.setDuree_estimee(
                    txtDuree.getText().isEmpty() ? 0 : Integer.parseInt(txtDuree.getText())
            );

            t.setCreated_at(new Date());
            t.setUpdated_at(new Date());

            // 🚨 no user selection in frontoffice
            t.setUser_id(current_userId); // replace later with logged-in user
            t.setPrediction(0);

            // FULL VALIDATION (same as backoffice)
            List<String> errors = TacheValidator.validate(t);

            if (!errors.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de validation");
                alert.setHeaderText("Veuillez corriger les erreurs du formulaire");
                alert.setContentText(String.join("\n", errors));
                alert.showAndWait();
                return;
            }

            // save
            TacheService ts = new TacheService();
            ts.add(t);

            // suivi
            SuiviTacheService st = new SuiviTacheService();
            suiviTache s = new suiviTache();
            s.setTache(t);
            s.setAncienStatut("");
            s.setNouveauStatut(t.getStatut());
            s.setCommentaire("");
            s.setDateAction(new Date());
            st.add(s);

            // Success alert
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Succès");
            successAlert.setHeaderText("Tâche ajoutée avec succès");
            successAlert.setContentText("La tâche '" + t.getTitre() + "' a été ajoutée.");
            successAlert.showAndWait();

            // Redirect back to tasks page
            if (sidebarController != null) {
                sidebarController.goToTaches();
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Une erreur est survenue");
            alert.setContentText("Détails de l'erreur: " + e.getMessage());
            alert.showAndWait();
            System.err.println("Error adding task: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            if (sidebarController != null) {
                sidebarController.goToTaches();
            }
        } catch (Exception e) {
            System.err.println("Error going back: " + e.getMessage());
        }
    }
}
package piJava.Controllers.backoffice.objectifsante;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import piJava.Controllers.backoffice.SidebarController;
import piJava.entities.ObjectifSante;
import piJava.services.ObjectifSanteService;
import piJava.utils.SessionManager;
import java.sql.Date;

public class AjouterObjectifController {

    @FXML
    private TextField txtTitre;

    @FXML
    private ComboBox<String> cbType;

    @FXML
    private TextField txtValeurCible;

    @FXML
    private DatePicker dpDateDebut;

    @FXML
    private DatePicker dpDateFin;

    @FXML
    private ComboBox<String> cbPriorite;

    @FXML
    private Label lblUnite;

    @FXML
    private Label errTitre;

    @FXML
    private Label errType;

    @FXML
    private Label errValeurCible;

    @FXML
    private Label errDateDebut;

    @FXML
    private Label errDateFin;

    @FXML
    private Label errPriorite;

    private AfficherObjectifsController afficherObjectifsController;
    private SidebarController sidebarController;
    private StackPane contentArea;

    public void setAfficherObjectifsController(AfficherObjectifsController afficherObjectifsController) {
        this.afficherObjectifsController = afficherObjectifsController;
    }

    public void setSidebarController(SidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        limiterChampNumerique(txtValeurCible);
        cbType.valueProperty().addListener((obs, oldValue, newValue) -> mettreAJourUnite(newValue));
    }

    @FXML
    public void retourObjectifs() {
        try {
            if (sidebarController != null) {
                sidebarController.goToObjectifsSante();
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du retour vers les objectifs : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ajouterObjectif() {
        if (!validerSaisieObjectif()) {
            return;
        }

        try {
            String titre = txtTitre.getText().trim();
            String type = cbType.getValue();
            int valeurCible = Integer.parseInt(txtValeurCible.getText().trim());
            Date dateDebut = Date.valueOf(dpDateDebut.getValue());
            Date dateFin = Date.valueOf(dpDateFin.getValue());
            String priorite = cbPriorite.getValue();
            String statut = "EN_COURS";

            if (SessionManager.getInstance().getCurrentUser() == null) {
                errTitre.setText("Aucun utilisateur connecté.");
                return;
            }

            int currentUserId = SessionManager.getInstance().getCurrentUser().getId();

            ObjectifSante objectif = new ObjectifSante(
                    titre,
                    type,
                    valeurCible,
                    dateDebut,
                    dateFin,
                    priorite,
                    statut,
                    currentUserId
            );

            ObjectifSanteService service = new ObjectifSanteService();
            service.ajouter(objectif);

            if (afficherObjectifsController != null) {
                afficherObjectifsController.chargerObjectifs();
            }

            if (sidebarController != null) {
                sidebarController.goToObjectifsSante();
            }

        } catch (Exception e) {
            errTitre.setText("Erreur lors de l'ajout.");
            e.printStackTrace();
        }
    }

    private void mettreAJourUnite(String type) {
        if (type == null) {
            lblUnite.setText("Unité");
            txtValeurCible.setPromptText("Ex: 8");
            return;
        }

        switch (type) {
            case "SOMMEIL":
                lblUnite.setText("heures / nuit");
                txtValeurCible.setPromptText("0 à 15");
                break;
            case "ALIMENTATION":
                lblUnite.setText("repas / jour");
                txtValeurCible.setPromptText("0 à 6");
                break;
            case "SPORT":
                lblUnite.setText("minutes / jour");
                txtValeurCible.setPromptText("0 à 300");
                break;
            default:
                lblUnite.setText("Unité");
                txtValeurCible.setPromptText("Ex: 8");
                break;
        }
    }

    private boolean validerSaisieObjectif() {
        viderErreurs();
        boolean valide = true;

        String titre = txtTitre.getText() == null ? "" : txtTitre.getText().trim();
        String type = cbType.getValue();
        String valeurText = txtValeurCible.getText() == null ? "" : txtValeurCible.getText().trim();

        if (titre.isEmpty()) {
            errTitre.setText("Le titre est obligatoire.");
            ajouterStyleErreur(txtTitre);
            valide = false;
        } else if (titre.length() < 3) {
            errTitre.setText("Minimum 3 caractères.");
            ajouterStyleErreur(txtTitre);
            valide = false;
        }

        if (type == null) {
            errType.setText("Veuillez sélectionner un type.");
            ajouterStyleErreur(cbType);
            valide = false;
        }

        if (valeurText.isEmpty()) {
            errValeurCible.setText("La valeur cible est obligatoire.");
            ajouterStyleErreur(txtValeurCible);
            valide = false;
        } else {
            try {
                int valeurCible = Integer.parseInt(valeurText);

                if (type != null) {
                    if (type.equals("SOMMEIL") && (valeurCible < 0 || valeurCible > 15)) {
                        errValeurCible.setText("Entre 0 et 15 heures / nuit.");
                        ajouterStyleErreur(txtValeurCible);
                        valide = false;
                    }

                    if (type.equals("ALIMENTATION") && (valeurCible < 0 || valeurCible > 6)) {
                        errValeurCible.setText("Entre 0 et 6 repas / jour.");
                        ajouterStyleErreur(txtValeurCible);
                        valide = false;
                    }

                    if (type.equals("SPORT") && (valeurCible < 0 || valeurCible > 300)) {
                        errValeurCible.setText("Entre 0 et 300 minutes / jour.");
                        ajouterStyleErreur(txtValeurCible);
                        valide = false;
                    }
                }
            } catch (NumberFormatException e) {
                errValeurCible.setText("La valeur doit être un entier.");
                ajouterStyleErreur(txtValeurCible);
                valide = false;
            }
        }

        if (dpDateDebut.getValue() == null) {
            errDateDebut.setText("Choisissez la date de début.");
            ajouterStyleErreur(dpDateDebut);
            valide = false;
        }

        if (dpDateFin.getValue() == null) {
            errDateFin.setText("Choisissez la date de fin.");
            ajouterStyleErreur(dpDateFin);
            valide = false;
        }

        if (dpDateDebut.getValue() != null && dpDateFin.getValue() != null &&
                dpDateFin.getValue().isBefore(dpDateDebut.getValue())) {
            errDateFin.setText("La date fin doit être après la date début.");
            ajouterStyleErreur(dpDateFin);
            valide = false;
        }

        if (cbPriorite.getValue() == null) {
            errPriorite.setText("Veuillez sélectionner une priorité.");
            ajouterStyleErreur(cbPriorite);
            valide = false;
        }

        return valide;
    }

    private void viderErreurs() {
        errTitre.setText("");
        errType.setText("");
        errValeurCible.setText("");
        errDateDebut.setText("");
        errDateFin.setText("");
        errPriorite.setText("");

        retirerStyleErreur(txtTitre);
        retirerStyleErreur(cbType);
        retirerStyleErreur(txtValeurCible);
        retirerStyleErreur(dpDateDebut);
        retirerStyleErreur(dpDateFin);
        retirerStyleErreur(cbPriorite);
    }

    private void limiterChampNumerique(TextField textField) {
        textField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void ajouterStyleErreur(javafx.scene.control.Control control) {
        if (!control.getStyleClass().contains("field-error")) {
            control.getStyleClass().add("field-error");
        }
    }

    private void retirerStyleErreur(javafx.scene.control.Control control) {
        control.getStyleClass().remove("field-error");
    }
}
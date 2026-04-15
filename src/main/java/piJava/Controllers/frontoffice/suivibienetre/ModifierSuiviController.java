package piJava.Controllers.frontoffice.suivibienetre;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.ObjectifSante;
import piJava.entities.SuiviBienEtre;
import piJava.entities.user;
import piJava.services.ObjectifSanteService;
import piJava.services.SuiviBienEtreService;
import piJava.utils.SessionManager;

import java.sql.Date;
import java.time.LocalDate;

public class ModifierSuiviController {

    @FXML
    private DatePicker dpDateSaisie;

    @FXML
    private ComboBox<String> cbHumeur;

    @FXML
    private TextField txtQualiteSommeil;

    @FXML
    private TextField txtNiveauEnergie;

    @FXML
    private TextField txtNiveauStress;

    @FXML
    private TextField txtQualiteAlimentation;

    @FXML
    private TextField txtNotesLibres;

    @FXML
    private Label errDateSaisie;

    @FXML
    private Label errHumeur;

    @FXML
    private Label errQualiteSommeil;

    @FXML
    private Label errNiveauEnergie;

    @FXML
    private Label errNiveauStress;

    @FXML
    private Label errQualiteAlimentation;

    @FXML
    private Label errNotesLibres;

    private SuiviBienEtre suivi;
    private AfficherSuivisController afficherSuivisController;
    private FrontSidebarController sidebarController;
    private StackPane contentArea;

    public void setAfficherSuivisController(AfficherSuivisController afficherSuivisController) {
        this.afficherSuivisController = afficherSuivisController;
    }

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void setSuivi(SuiviBienEtre suivi) {
        this.suivi = suivi;

        dpDateSaisie.setValue(suivi.getDateSaisie().toLocalDate());
        cbHumeur.setValue(suivi.getHumeur());
        txtQualiteSommeil.setText(String.valueOf(suivi.getQualiteSommeil()));
        txtNiveauEnergie.setText(String.valueOf(suivi.getNiveauEnergie()));
        txtNiveauStress.setText(String.valueOf(suivi.getNiveauStress()));
        txtQualiteAlimentation.setText(String.valueOf(suivi.getQualiteAlimentation()));
        txtNotesLibres.setText(suivi.getNotesLibres());
    }

    @FXML
    public void initialize() {
        cbHumeur.getItems().addAll("EXCELLENT", "BIEN", "MOYEN", "MAUVAIS");

        limiterChampNumerique(txtQualiteSommeil);
        limiterChampNumerique(txtNiveauEnergie);
        limiterChampNumerique(txtNiveauStress);
        limiterChampNumerique(txtQualiteAlimentation);
    }

    private double convertirHumeurEnNote(String humeur) {
        if (humeur == null) return 0;

        switch (humeur) {
            case "EXCELLENT":
                return 10;
            case "BIEN":
                return 8;
            case "MOYEN":
                return 5;
            case "MAUVAIS":
                return 2;
            default:
                return 0;
        }
    }

    private double calculerScore(String typeObjectif,
                                 String humeur,
                                 int qualiteSommeil,
                                 int niveauEnergie,
                                 int niveauStress,
                                 int qualiteAlimentation) {

        double stressInverse = 10 - niveauStress;
        double humeurNote = convertirHumeurEnNote(humeur);
        double score;

        switch (typeObjectif) {
            case "SOMMEIL":
                score = (
                        qualiteSommeil * 0.50 +
                                niveauEnergie * 0.15 +
                                qualiteAlimentation * 0.15 +
                                stressInverse * 0.15 +
                                humeurNote * 0.05
                ) * 10;
                break;

            case "SPORT":
                score = (
                        niveauEnergie * 0.50 +
                                qualiteSommeil * 0.15 +
                                qualiteAlimentation * 0.15 +
                                stressInverse * 0.15 +
                                humeurNote * 0.05
                ) * 10;
                break;

            case "ALIMENTATION":
                score = (
                        qualiteAlimentation * 0.50 +
                                qualiteSommeil * 0.15 +
                                niveauEnergie * 0.15 +
                                stressInverse * 0.15 +
                                humeurNote * 0.05
                ) * 10;
                break;

            default:
                score = (
                        qualiteSommeil +
                                niveauEnergie +
                                stressInverse +
                                qualiteAlimentation +
                                humeurNote
                ) / 5.0 * 10;
                break;
        }

        return Math.round(score * 100.0) / 100.0;
    }

    @FXML
    public void modifierSuivi() {
        if (!validerSaisieSuivi()) {
            return;
        }

        try {
            if (suivi == null) {
                errDateSaisie.setText("Aucun suivi à modifier.");
                return;
            }

            user currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                errDateSaisie.setText("Aucun utilisateur connecté.");
                return;
            }

            int qualiteSommeil = Integer.parseInt(txtQualiteSommeil.getText().trim());
            int niveauEnergie = Integer.parseInt(txtNiveauEnergie.getText().trim());
            int niveauStress = Integer.parseInt(txtNiveauStress.getText().trim());
            int qualiteAlimentation = Integer.parseInt(txtQualiteAlimentation.getText().trim());

            ObjectifSanteService objectifService = new ObjectifSanteService();
            ObjectifSante objectif = objectifService.recupererParIdEtUser(suivi.getObjectifId(), currentUser.getId());

            if (objectif == null) {
                errDateSaisie.setText("Objectif introuvable ou non autorisé.");
                return;
            }

            double score = calculerScore(
                    objectif.getType(),
                    cbHumeur.getValue(),
                    qualiteSommeil,
                    niveauEnergie,
                    niveauStress,
                    qualiteAlimentation
            );

            suivi.setDateSaisie(Date.valueOf(dpDateSaisie.getValue()));
            suivi.setHumeur(cbHumeur.getValue());
            suivi.setQualiteSommeil(qualiteSommeil);
            suivi.setNiveauEnergie(niveauEnergie);
            suivi.setNiveauStress(niveauStress);
            suivi.setQualiteAlimentation(qualiteAlimentation);
            suivi.setNotesLibres(txtNotesLibres.getText() == null ? "" : txtNotesLibres.getText().trim());
            suivi.setScore(score);

            SuiviBienEtreService service = new SuiviBienEtreService();
            boolean success = service.modifierParUser(suivi, currentUser.getId());

            if (!success) {
                errDateSaisie.setText("Modification refusée.");
                return;
            }

            if (afficherSuivisController != null) {
                afficherSuivisController.chargerSuivisParObjectif();
            }

            if (sidebarController != null && afficherSuivisController != null) {
                afficherSuivisController.rechargerPageSuivis(suivi.getObjectifId(), sidebarController);
            }

        } catch (Exception e) {
            errDateSaisie.setText("Erreur lors de la modification du suivi.");
            e.printStackTrace();
        }
    }

    @FXML
    public void retourSuivis() {
        try {
            if (afficherSuivisController != null && sidebarController != null && suivi != null) {
                afficherSuivisController.rechargerPageSuivis(suivi.getObjectifId(), sidebarController);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du retour vers les suivis : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validerSaisieSuivi() {
        viderErreurs();
        boolean valide = true;

        if (dpDateSaisie.getValue() == null) {
            errDateSaisie.setText("Veuillez choisir la date de saisie.");
            ajouterStyleErreur(dpDateSaisie);
            valide = false;
        } else if (dpDateSaisie.getValue().isAfter(LocalDate.now())) {
            errDateSaisie.setText("La date de saisie ne doit pas être dans le futur.");
            ajouterStyleErreur(dpDateSaisie);
            valide = false;
        }

        if (cbHumeur.getValue() == null) {
            errHumeur.setText("Veuillez sélectionner l'humeur.");
            ajouterStyleErreur(cbHumeur);
            valide = false;
        }

        if (!validerNote(txtQualiteSommeil, errQualiteSommeil, "La qualité du sommeil")) {
            valide = false;
        }

        if (!validerNote(txtNiveauEnergie, errNiveauEnergie, "Le niveau d'énergie")) {
            valide = false;
        }

        if (!validerNote(txtNiveauStress, errNiveauStress, "Le niveau de stress")) {
            valide = false;
        }

        if (!validerNote(txtQualiteAlimentation, errQualiteAlimentation, "La qualité de l'alimentation")) {
            valide = false;
        }

        String notes = txtNotesLibres.getText() == null ? "" : txtNotesLibres.getText().trim();
        if (notes.length() > 2000) {
            errNotesLibres.setText("Les notes libres ne doivent pas dépasser 2000 caractères.");
            ajouterStyleErreur(txtNotesLibres);
            valide = false;
        }

        return valide;
    }

    private boolean validerNote(TextField champ, Label labelErreur, String nomChamp) {
        String valeur = champ.getText() == null ? "" : champ.getText().trim();

        if (valeur.isEmpty()) {
            labelErreur.setText(nomChamp + " est obligatoire.");
            ajouterStyleErreur(champ);
            return false;
        }

        try {
            int note = Integer.parseInt(valeur);

            if (note < 0 || note > 10) {
                labelErreur.setText(nomChamp + " doit être entre 0 et 10.");
                ajouterStyleErreur(champ);
                return false;
            }
        } catch (NumberFormatException e) {
            labelErreur.setText(nomChamp + " doit être un nombre entier.");
            ajouterStyleErreur(champ);
            return false;
        }

        return true;
    }

    private void viderErreurs() {
        errDateSaisie.setText("");
        errHumeur.setText("");
        errQualiteSommeil.setText("");
        errNiveauEnergie.setText("");
        errNiveauStress.setText("");
        errQualiteAlimentation.setText("");
        errNotesLibres.setText("");

        retirerStyleErreur(dpDateSaisie);
        retirerStyleErreur(cbHumeur);
        retirerStyleErreur(txtQualiteSommeil);
        retirerStyleErreur(txtNiveauEnergie);
        retirerStyleErreur(txtNiveauStress);
        retirerStyleErreur(txtQualiteAlimentation);
        retirerStyleErreur(txtNotesLibres);
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
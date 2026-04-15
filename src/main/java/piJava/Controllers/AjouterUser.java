package piJava.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import piJava.entities.Classe;
import piJava.entities.user;
import piJava.services.UserServices;
import piJava.services.ClasseService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AjouterUser implements Initializable {

    @FXML private CheckBox acceptTerms;
    @FXML private ComboBox<Integer> anneeNaissance;
    @FXML private ComboBox<Integer> jourNaissance;
    @FXML private ComboBox<String>  moisNaissance;
    @FXML private ToggleButton btnFemme;
    @FXML private ToggleButton btnHomme;
    @FXML private ComboBox<Classe> classe;  // ← int ID, matches classe_id
    @FXML private TextField email;
    @FXML private PasswordField motDePasse;
    @FXML private TextField nomFamille;
    @FXML private TextField prenom;
    @FXML private TextField telephone;
    @FXML private Label passwordStrengthLabel;

    private final ToggleGroup sexeGroup = new ToggleGroup();
    private final ClasseService classeService = new ClasseService();
    private final UserServices userServices = new UserServices();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Gender toggle group
        btnHomme.setToggleGroup(sexeGroup);
        btnFemme.setToggleGroup(sexeGroup);

        // Date dropdowns
        moisNaissance.getItems().addAll(
                "01","02","03","04","05","06",
                "07","08","09","10","11","12"
        );
        for (int d = 1;  d <= 31;  d++) jourNaissance.getItems().add(d);
        for (int y = 1950; y <= 2015; y++) anneeNaissance.getItems().add(y);

        // Class IDs — replace with a DB call if needed e.g. userServices.getClasseIds()
        classe.getItems().addAll(classeService.getAllClasses());;

        // Password strength live feedback
        motDePasse.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                passwordStrengthLabel.setText("Password strength: None");
                passwordStrengthLabel.setStyle("-fx-text-fill: #ee5555; -fx-font-size: 11px;");
            } else if (newVal.length() < 6) {
                passwordStrengthLabel.setText("Password strength: Weak");
                passwordStrengthLabel.setStyle("-fx-text-fill: #ee5555; -fx-font-size: 11px;");
            } else if (newVal.length() < 10) {
                passwordStrengthLabel.setText("Password strength: Medium");
                passwordStrengthLabel.setStyle("-fx-text-fill: #ffaa00; -fx-font-size: 11px;");
            } else {
                passwordStrengthLabel.setText("Password strength: Strong");
                passwordStrengthLabel.setStyle("-fx-text-fill: #44cc44; -fx-font-size: 11px;");
            }
        });
    }

    @FXML
    void handleRegister(ActionEvent event) {
        // ── Validation ──────────────────────────────────────────
        if (!acceptTerms.isSelected()) {
            showAlert(Alert.AlertType.WARNING,
                    "Vous devez accepter les conditions d'utilisation.");
            return;
        }
        if (prenom.getText().isBlank() || nomFamille.getText().isBlank()
                || email.getText().isBlank() || motDePasse.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING,
                    "Veuillez remplir tous les champs obligatoires.");
            return;
        }
        if (motDePasse.getText().length() < 6) {
            showAlert(Alert.AlertType.WARNING,
                    "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        if (moisNaissance.getValue() == null
                || jourNaissance.getValue() == null
                || anneeNaissance.getValue() == null) {
            showAlert(Alert.AlertType.WARNING,
                    "Veuillez sélectionner votre date de naissance.");
            return;
        }
        if (classe.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez choisir une classe.");
            return;
        }

        // ── Build values ─────────────────────────────────────────
        String sexe = btnHomme.isSelected() ? "Homme"
                : btnFemme.isSelected() ? "Femme" : "";

        // Format: "YYYY-MM-DD" to match typical DB date format
        String dateNaissance = anneeNaissance.getValue()
                + "-" + moisNaissance.getValue()
                + "-" + String.format("%02d", jourNaissance.getValue());

        int classeId = classe.getValue().getId();

        // ── Matches constructor: user(nom, prenom, email, num_tel,
        //                             date_de_naissance, sexe, classe_id, password)
        user newUser = new user(
                nomFamille.getText(),
                prenom.getText(),
                email.getText(),
                telephone.getText(),
                dateNaissance,
                sexe,
                classeId,
                motDePasse.getText()
        );

        userServices.Register(newUser);
        showAlert(Alert.AlertType.INFORMATION, "Compte créé avec succès !");
    }

    @FXML void handleGmailRegister(ActionEvent event) { }
    @FXML void handleSignIn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) email.getScene().getWindow(); // grab current stage
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML void handleTerms(ActionEvent event) { }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
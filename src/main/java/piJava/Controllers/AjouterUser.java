package piJava.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.application.Platform;
import piJava.entities.Classe;
import piJava.entities.user;
import piJava.services.ClasseService;
import piJava.services.UserServices;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AjouterUser implements Initializable {

    @FXML private CheckBox acceptTerms;
    @FXML private ComboBox<Integer> anneeNaissance;
    @FXML private ComboBox<Integer> jourNaissance;
    @FXML private ComboBox<String> moisNaissance;
    @FXML private ToggleButton btnFemme;
    @FXML private ToggleButton btnHomme;
    @FXML private ComboBox<Classe> classe;
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
        btnHomme.setToggleGroup(sexeGroup);
        btnFemme.setToggleGroup(sexeGroup);

        moisNaissance.getItems().addAll(
                "01", "02", "03", "04", "05", "06",
                "07", "08", "09", "10", "11", "12"
        );
        for (int d = 1; d <= 31; d++) {
            jourNaissance.getItems().add(d);
        }
        for (int y = 1950; y <= 2015; y++) {
            anneeNaissance.getItems().add(y);
        }

        classe.getItems().addAll(classeService.getAllClasses());

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
        if (!acceptTerms.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Vous devez accepter les conditions d'utilisation.");
            return;
        }
        if (prenom.getText().isBlank() || nomFamille.getText().isBlank()
                || email.getText().isBlank() || motDePasse.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs obligatoires.");
            return;
        }
        if (motDePasse.getText().length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Le mot de passe doit contenir au moins 6 caracteres.");
            return;
        }
        if (moisNaissance.getValue() == null
                || jourNaissance.getValue() == null
                || anneeNaissance.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez selectionner votre date de naissance.");
            return;
        }
        if (classe.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez choisir une classe.");
            return;
        }

        String sexe = btnHomme.isSelected() ? "Homme" : btnFemme.isSelected() ? "Femme" : "";
        String dateNaissance = anneeNaissance.getValue()
                + "-" + moisNaissance.getValue()
                + "-" + String.format("%02d", jourNaissance.getValue());

        user newUser = new user(
                nomFamille.getText().trim(),
                prenom.getText().trim(),
                email.getText().trim().toLowerCase(),
                telephone.getText() == null ? "" : telephone.getText().trim(),
                dateNaissance,
                sexe,
                classe.getValue().getId(),
                motDePasse.getText()
        );

        try {
            userServices.Register(newUser);
            showAlert(Alert.AlertType.INFORMATION, "Compte cree avec succes.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Echec de creation du compte : " + e.getMessage());
            return;
        }

        handleSignIn(event);
    }

    @FXML
    void handleGmailRegister(ActionEvent event) {
        piJava.services.GoogleAuthService googleAuthService = new piJava.services.GoogleAuthService();
        googleAuthService.authenticate(new piJava.services.GoogleAuthService.AuthCallback() {
            @Override
            public void onSuccess(user loggedInUser) {
                // Save to global session
                piJava.utils.SessionManager.getInstance().login(loggedInUser);

                // Route to appropriate view
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Inscription reussie via Google !");
                    // Route by role
                    if (piJava.utils.SessionManager.getInstance().isAdmin()) {
                        navigateTo(event, "/backoffice/main.fxml");
                    } else {
                        navigateTo(event, "/frontoffice/main.fxml");
                    }
                });
            }

            @Override
            public void onError(String message) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, message));
            }
        });
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) email.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error: " + e.getMessage());
        }
    }

    @FXML
    void handleSignIn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) email.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleTerms(ActionEvent event) {
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

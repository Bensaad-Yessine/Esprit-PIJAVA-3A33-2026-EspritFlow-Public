package piJava.Controllers.backoffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import java.net.URL;

public class SidebarController {

    @FXML private Button dashboardBtn;
    @FXML private Button tachesBtn;
    @FXML private Button classesBtn;
    @FXML private Button matieresBtn;
    @FXML private Button sallesBtn;
    @FXML private Button seancesBtn;
    @FXML private Button enseignantsBtn;
    @FXML private Button emploiBtn;
    @FXML private Button notesBtn;
    @FXML private Button notificationsBtn;

    private StackPane contentArea;

    // Called by MainController after fx:include injection
    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void setActivePage(String page) {
        Button[] all = { dashboardBtn, tachesBtn, classesBtn, matieresBtn,
                         sallesBtn, seancesBtn, enseignantsBtn, emploiBtn, notesBtn, notificationsBtn };
        for (Button b : all) {
            if (b != null) b.getStyleClass().remove("menu-item-active");
        }

        switch (page) {
            case "dashboard"     -> dashboardBtn.getStyleClass().add("menu-item-active");
            case "taches"        -> tachesBtn.getStyleClass().add("menu-item-active");
            case "classes"       -> classesBtn.getStyleClass().add("menu-item-active");
            case "matieres"      -> matieresBtn.getStyleClass().add("menu-item-active");
            case "salles"        -> sallesBtn.getStyleClass().add("menu-item-active");
            case "seances"       -> seancesBtn.getStyleClass().add("menu-item-active");
            case "enseignants"   -> enseignantsBtn.getStyleClass().add("menu-item-active");
            case "emploi"        -> emploiBtn.getStyleClass().add("menu-item-active");
            case "notes"         -> notesBtn.getStyleClass().add("menu-item-active");
            case "notifications" -> notificationsBtn.getStyleClass().add("menu-item-active");
        }
    }

    // ── Core loader ──────────────────────────────────────────
    private void loadPage(String fxmlPath, String activePage) {
        try {
            if (contentArea == null) {
                showNavigationError("La zone de contenu principale n'est pas initialisee.");
                return;
            }

            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                showNavigationError("Page indisponible: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);
            setActivePage(activePage);
        } catch (Exception e) {
            System.err.println("Failed to load: " + fxmlPath);
            e.printStackTrace();
            showNavigationError("Impossible de charger la page demandee.");
        }
    }

    // ── Nav actions ──────────────────────────────────────────
    @FXML public void goToDashboard()   { loadPage("/backoffice/dashboard/dashboard-content.fxml", "dashboard"); }
    @FXML public void goToTaches()      { showNotImplemented("Taches"); }

    // ↓ This path must match where ClassesContent.fxml sits in your resources folder
    @FXML public void goToClasses()     { loadPage("/backoffice/Classe/ClassesContent.fxml",       "classes"); }

    @FXML public void goToMatieres()    { loadPage("/backoffice/Matiere/MatiereContent.fxml",            "matieres"); }
    
    @FXML public void goToSalles()      { loadPage("/backoffice/Salle/SallesContent.fxml", "salles"); }
    
    @FXML public void goToSeances()     { loadPage("/backoffice/Seance/SeanceContent.fxml", "seances"); }
    
    @FXML public void goToEnseignants() { showNotImplemented("Enseignants"); }
    @FXML public void goToEmploi()      { showNotImplemented("Emploi du temps"); }
    @FXML public void goToNotes()       { showNotImplemented("Notes"); }
    @FXML public void goToNotifications(){ showNotImplemented("Notifications"); }

    @FXML
    public void logout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showNavigationError("Impossible de revenir a l'ecran de connexion.");
        }
    }

    private void showNotImplemented(String pageName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Module indisponible");
        alert.setHeaderText(null);
        alert.setContentText(pageName + " n'est pas encore implemente dans ce projet.");
        alert.showAndWait();
    }

    private void showNavigationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de navigation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

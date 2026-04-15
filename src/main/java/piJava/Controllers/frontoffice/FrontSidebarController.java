package piJava.Controllers.frontoffice;

import javafx.animation.ParallelTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import javafx.scene.control.Alert;
import piJava.utils.SessionManager;
public class FrontSidebarController {

    @FXML private Button dashboardBtn;
    @FXML private Button tachesBtn;
    @FXML private Button classesBtn;
    @FXML private Button matieresBtn;
    @FXML private Button enseignantsBtn;
    @FXML private Button emploiBtn;
    @FXML private Button notesBtn;
    @FXML private Button notificationsBtn;

    // Reference to the main content area (set by MainController)
    private StackPane contentArea;


    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }
    public StackPane getContentArea() {return contentArea;}


    public void setActivePage(String page) {
        // Remove active class from all buttons
        dashboardBtn.getStyleClass().remove("menu-item-active");
        tachesBtn.getStyleClass().remove("menu-item-active");
        classesBtn.getStyleClass().remove("menu-item-active");
        matieresBtn.getStyleClass().remove("menu-item-active");
        enseignantsBtn.getStyleClass().remove("menu-item-active");
        emploiBtn.getStyleClass().remove("menu-item-active");
        notesBtn.getStyleClass().remove("menu-item-active");
        notificationsBtn.getStyleClass().remove("menu-item-active");

        // Add active class to the selected button
        switch (page) {
            case "dashboard"      -> dashboardBtn.getStyleClass().add("menu-item-active");
            case "taches"       -> tachesBtn.getStyleClass().add("menu-item-active");
            case "classes"        -> classesBtn.getStyleClass().add("menu-item-active");
            case "matieres"       -> matieresBtn.getStyleClass().add("menu-item-active");
            case "enseignants"    -> enseignantsBtn.getStyleClass().add("menu-item-active");
            case "emploi"         -> emploiBtn.getStyleClass().add("menu-item-active");
            case "notes"          -> notesBtn.getStyleClass().add("menu-item-active");
            case "alertes"  -> notificationsBtn.getStyleClass().add("menu-item-active");
        }
    }

    public void loadPage(String fxmlFile, String activePage) {
        try {
            if (contentArea == null) {
                showNavigationError("La zone de contenu principale n'est pas initialisee.");
                return;
            }

            URL resource = getClass().getResource(fxmlFile);
            if (resource == null) {
                showNavigationError("Page indisponible: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent page = loader.load();

            Object controller = loader.getController();

            // 🔥 inject THIS sidebar into next controller
            if (controller instanceof piJava.Controllers.frontoffice.taches.TachesController tc) {
                tc.setSidebarController(this);
            }
            if (controller instanceof piJava.Controllers.frontoffice.preferencealerte.AlertesController ac) {
                ac.setSidebarController(this);
            }

            contentArea.getChildren().setAll(page);
            setActivePage(activePage);

        } catch (Exception e) {
            e.printStackTrace();
            showNavigationError("Impossible de charger la page demandee.");
        }
    }

    @FXML public void goToDashboard()    { loadPage("/frontoffice/dashboard/dashboard-content.fxml", "dashboard"); }
    @FXML void goToTaches()            { loadPage("/frontoffice/taches/taches-content.fxml",  "taches"); }
    @FXML void goToClasses()             { showNotImplemented("Groupes et Classes"); }
    @FXML void goToMatieres()            { showNotImplemented("Cours et Programmes"); }
    @FXML void goToEnseignants()         { showNotImplemented("Intervenants"); }
    @FXML void goToEmploi()              { loadPage("/frontoffice/emploi/EmploiContent.fxml", "emploi"); }
    @FXML void goToNotes()               { showNotImplemented("Evaluations"); }
    @FXML void goToNotifications()       { loadPage("/frontoffice/preferenceAlerte/alerte-content.fxml","alertes"); }
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

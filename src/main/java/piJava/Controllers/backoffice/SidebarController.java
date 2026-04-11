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
import java.net.URL;

public class SidebarController {

    @FXML private Button dashboardBtn;
    @FXML private Button tachesBtn;
    @FXML private Button classesBtn;
    @FXML private Button matieresBtn;
    @FXML private Button enseignantsBtn;
    @FXML private Button emploiBtn;
    @FXML private Button notesBtn;
    @FXML private Button notificationsBtn;

    private StackPane contentArea;

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

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
            case "notifications"  -> notificationsBtn.getStyleClass().add("menu-item-active");
        }
    }

    private void loadPage(String fxmlFile, String activePage) {
        try {
            URL resource = getClass().getResource(fxmlFile);
            System.out.println("Loading: " + resource);
            FXMLLoader loader = new FXMLLoader(resource);
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);
            setActivePage(activePage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void goToDashboard()    { loadPage("/backoffice/dashboard/dashboard-content.fxml", "dashboard"); }
    @FXML void goToTaches()            { loadPage("/backoffice/taches/taches-content.fxml",  "taches"); }
    @FXML void goToClasses()             { loadPage("/backoffice/classes-content.fxml",   "classes"); }
    @FXML void goToMatieres()            { loadPage("/backoffice/matieres-content.fxml",  "matieres"); }
    @FXML void goToEnseignants()         { loadPage("/backoffice/enseignants-content.fxml","enseignants"); }
    @FXML void goToEmploi()              { loadPage("/backoffice/emploi-content.fxml",    "emploi"); }
    @FXML void goToNotes()               { loadPage("/backoffice/notes-content.fxml",     "notes"); }
    @FXML void goToNotifications()       { loadPage("/backoffice/notifications-content.fxml","notifications"); }
    @FXML
    public void logout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login/login.fxml"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
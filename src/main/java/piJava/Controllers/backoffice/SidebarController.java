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

    // Called by MainController after fx:include injection
    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void setActivePage(String page) {
        Button[] all = { dashboardBtn, tachesBtn, classesBtn, matieresBtn,
                         enseignantsBtn, emploiBtn, notesBtn, notificationsBtn };
        for (Button b : all) b.getStyleClass().remove("menu-item-active");

        switch (page) {
            case "dashboard"     -> dashboardBtn.getStyleClass().add("menu-item-active");
            case "taches"        -> tachesBtn.getStyleClass().add("menu-item-active");
            case "classes"       -> classesBtn.getStyleClass().add("menu-item-active");
            case "matieres"      -> matieresBtn.getStyleClass().add("menu-item-active");
            case "enseignants"   -> enseignantsBtn.getStyleClass().add("menu-item-active");
            case "emploi"        -> emploiBtn.getStyleClass().add("menu-item-active");
            case "notes"         -> notesBtn.getStyleClass().add("menu-item-active");
            case "notifications" -> notificationsBtn.getStyleClass().add("menu-item-active");
        }
    }

    // ── Core loader ──────────────────────────────────────────
    private void loadPage(String fxmlPath, String activePage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);
            setActivePage(activePage);
        } catch (Exception e) {
            System.err.println("Failed to load: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ── Nav actions ──────────────────────────────────────────
    @FXML public void goToDashboard()   { loadPage("/backoffice/dashboard/dashboard-content.fxml", "dashboard"); }
    @FXML public void goToTaches()      { loadPage("/backoffice/taches/taches-content.fxml",       "taches"); }

    // ↓ This path must match where ClassesContent.fxml sits in your resources folder
    @FXML public void goToClasses()     { loadPage("/backoffice/Classe/ClassesContent.fxml",       "classes"); }

    @FXML public void goToMatieres()    { loadPage("/backoffice/Matiere/MatiereContent.fxml",            "matieres"); }
    @FXML public void goToEnseignants() { loadPage("/backoffice/enseignants-content.fxml",         "enseignants"); }
    @FXML public void goToEmploi()      { loadPage("/backoffice/emploi-content.fxml",              "emploi"); }
    @FXML public void goToNotes()       { loadPage("/backoffice/notes-content.fxml",               "notes"); }
    @FXML public void goToNotifications(){ loadPage("/backoffice/notifications-content.fxml",      "notifications"); }

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

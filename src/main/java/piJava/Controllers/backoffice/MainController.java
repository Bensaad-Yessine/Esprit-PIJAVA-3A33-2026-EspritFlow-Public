package piJava.Controllers.backoffice;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // The StackPane from main.fxml — content is swapped here
    @FXML private StackPane contentArea;

    // JavaFX auto-injects this because fx:id="sidebar" in main.fxml
    // Convention: fx:id + "Controller"  →  sidebarController
    @FXML private SidebarController sidebarController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Give the sidebar a reference to the content area
        sidebarController.setContentArea(contentArea);

        // 2. Load the default page (dashboard) on startup
        sidebarController.goToDashboard();
    }
}

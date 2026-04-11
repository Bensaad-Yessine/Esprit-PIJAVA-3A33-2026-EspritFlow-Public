package piJava.Controllers.backoffice;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;

    // IntelliJ auto-injects this because sidebar.fxml has fx:id="sidebar"
    // The naming convention is: fx:id + "Controller" = sidebarController
    @FXML private SidebarController sidebarController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Give the sidebar access to the content area so it can swap pages
        sidebarController.setContentArea(contentArea);

        // Load dashboard as the default page on startup
        sidebarController.goToDashboard();
    }
}

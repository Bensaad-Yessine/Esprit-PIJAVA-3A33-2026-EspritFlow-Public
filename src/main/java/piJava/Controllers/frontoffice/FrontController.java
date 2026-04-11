package piJava.Controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class FrontController implements Initializable {

    @FXML private StackPane contentArea;

    // IntelliJ auto-injects this because sidebar.fxml has fx:id="sidebar"
    // The naming convention is: fx:id + "Controller" = sidebarController
    @FXML private piJava.Controllers.frontoffice.FrontSidebarController frontsidebarController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Give the sidebar access to the content area so it can swap pages
        frontsidebarController.setContentArea(contentArea);

        // Load dashboard as the default page on startup
        frontsidebarController.goToDashboard();
    }
}

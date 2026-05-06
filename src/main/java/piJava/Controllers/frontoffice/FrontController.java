package piJava.Controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class FrontController implements Initializable {

    // ── Content area (inner StackPane inside the chat overlay wrapper) ──────
    @FXML private StackPane contentArea;

    // ── Sidebar controller (auto-injected by FXML: fx:id="frontsidebar" → frontsidebarController)
    @FXML private FrontSidebarController frontsidebarController;

    // ── Chat widget controller (auto-injected by FXML: fx:id="chatWidget" → chatWidgetController)
    // We don't need to call methods on it — it initializes itself.
    // Declared to avoid potential FXML injection warnings.
    @FXML private ChatController chatWidgetController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Give the sidebar access to the content area so it can swap pages
        frontsidebarController.setContentArea(contentArea);

        // Load dashboard as the default page on startup
        frontsidebarController.goToDashboard();
    }
}

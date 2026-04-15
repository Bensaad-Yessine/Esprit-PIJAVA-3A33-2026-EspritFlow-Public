package piJava.Controllers.frontoffice.preferencealerte;

import piJava.Controllers.frontoffice.preferencealerte.AlertesController;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.preferenceAlerte;
import piJava.services.AlerteService;
import piJava.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AlertesController implements Initializable {

    private int currentUserId;

    @FXML private VBox  alertesContainer;
    @FXML private Label lblAlertesCount;
    @FXML private Label notificationLabel;

    private FrontSidebarController sidebarController;

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ✅ Session instead of hardcoded id
        if (SessionManager.getInstance().isLoggedIn()) {
            currentUserId = SessionManager.getInstance().getCurrentUser().getId();
        } else {
            showNotification("❌ Session expirée. Veuillez vous reconnecter.");
            return;
        }
        loadAlertes();
    }

    // ── Load ───────────────────────────────────────────────────────────────────

    private void loadAlertes() {
        try {
            List<preferenceAlerte> alertes = new AlerteService().showUserAlertes(currentUserId);
            alertesContainer.getChildren().clear();
            for (preferenceAlerte alerte : alertes) {
                alertesContainer.getChildren().add(createAlerteCard(alerte));
            }
            lblAlertesCount.setText(alertes.size() + " préférence(s)");
        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("❌ Erreur lors du chargement des alertes");
        }
    }

    // ── Card builder ───────────────────────────────────────────────────────────

    private HBox createAlerteCard(preferenceAlerte alerte) {
        HBox root = new HBox(20);
        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add("alerte-item");

        // Status bar
        VBox statusBar = new VBox();
        statusBar.setPrefWidth(8);
        statusBar.getStyleClass().add(alerte.getIs_active() ? "status-bar-active" : "status-bar-inactive");

        // Content
        VBox content = new VBox(10);
        HBox.setHgrow(content, Priority.ALWAYS);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(alerte.getNom());
        name.getStyleClass().add("alerte-name");

        Label activeStatus = new Label(alerte.getIs_active() ? "✅ Active" : "⏸️ Inactive");
        activeStatus.getStyleClass().add(alerte.getIs_active() ? "badge-active" : "badge-inactive");

        topRow.getChildren().addAll(name, activeStatus);
        if (alerte.getIs_default()) {
            Label defaultBadge = new Label("⭐ Par défaut");
            defaultBadge.getStyleClass().add("badge-default");
            topRow.getChildren().add(defaultBadge);
        }

        // Channels
        HBox channels = new HBox(10);
        channels.setAlignment(Pos.CENTER_LEFT);
        if (alerte.getEmail_actif()) {
            Label lbl = new Label("📧 Email"); lbl.getStyleClass().add("channel-badge");
            channels.getChildren().add(lbl);
        }
        if (alerte.getPush_actif()) {
            Label lbl = new Label("🔔 Push"); lbl.getStyleClass().add("channel-badge");
            channels.getChildren().add(lbl);
        }
        if (alerte.getSite_notif_active()) {
            Label lbl = new Label("🌐 Site"); lbl.getStyleClass().add("channel-badge");
            channels.getChildren().add(lbl);
        }

        // Silence hours
        HBox meta = new HBox(20);
        meta.setAlignment(Pos.CENTER_LEFT);
        if (alerte.getHeure_silence_debut() != null && alerte.getHeure_silence_fin() != null) {
            Label metaLabel = new Label("🔇 Silence: "
                    + alerte.getHeure_silence_debut() + " - " + alerte.getHeure_silence_fin());
            metaLabel.getStyleClass().add("alerte-meta");
            meta.getChildren().add(metaLabel);
        }

        content.getChildren().addAll(topRow, channels, meta);

        // Actions
        VBox actions = new VBox(10);
        actions.setAlignment(Pos.CENTER);

        Button btnDetails = new Button("👁 Détails");
        Button btnEdit    = new Button("✏️ Modifier");
        Button btnDelete  = new Button("🗑 Supprimer");

        btnDetails.getStyleClass().add("btn-details");
        btnEdit.getStyleClass().add("btn-edit");
        btnDelete.getStyleClass().add("btn-delete");

        btnDelete.setOnAction(e -> deleteAlerte(alerte, root));
        btnEdit.setOnAction(e -> editAlerte(alerte));
        btnDetails.setOnAction(e -> showDetails(alerte));

        actions.getChildren().addAll(btnDetails, btnEdit, btnDelete);
        root.getChildren().addAll(statusBar, content, actions);
        return root;
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    private void showDetails(preferenceAlerte alerte) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/frontoffice/preferenceAlerte/alerte-details.fxml"));
            Parent r = loader.load();
            AlerteDetailsController ctrl = loader.getController();
            ctrl.setAlerte(alerte);
            ctrl.setSidebarController(sidebarController);

        } catch (IOException e) {
            showNotification("❌ Erreur ouverture détails");
        }
    }

    private void editAlerte(preferenceAlerte alerte) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/frontoffice/preferenceAlerte/alerte-edit.fxml"));
            Parent r = loader.load();
            AlerteEditController ctrl = loader.getController();
            ctrl.setAlerte(alerte);
            ctrl.setSidebarController(sidebarController);

        } catch (IOException e) {
            showNotification("❌ Erreur lors de l'ouverture de l'éditeur");
        }
    }

    @FXML
    public void addAlerte() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/frontoffice/preferenceAlerte/alerte-new.fxml"));
            Parent r = loader.load();
            AlerteNewController ctrl = loader.getController();
            ctrl.setSidebarController(sidebarController);

        } catch (IOException e) {
            showNotification("❌ Erreur lors de l'ouverture du formulaire");
        }
    }

    private void deleteAlerte(preferenceAlerte alerte, HBox card) {
        try {
            new AlerteService().delete(alerte.getId());
            ((VBox) card.getParent()).getChildren().remove(card);
            int count = parseCount(lblAlertesCount.getText()) - 1;
            lblAlertesCount.setText(count + " préférence(s)");
            showNotification("✅ Préférence supprimée.");
        } catch (SQLException ex) {
            showNotification("❌ Suppression échouée : " + ex.getMessage());
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private int parseCount(String text) {
        try { return Integer.parseInt(text.split(" ")[0]); }
        catch (NumberFormatException e) { return 0; }
    }

    private void showNotification(String message) {
        notificationLabel.setText(message);
        notificationLabel.setOpacity(1);
        FadeTransition ft = new FadeTransition(Duration.seconds(4), notificationLabel);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.play();
    }
}

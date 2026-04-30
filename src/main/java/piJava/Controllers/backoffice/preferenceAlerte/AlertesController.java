package piJava.Controllers.backoffice.preferenceAlerte;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import piJava.Controllers.backoffice.SidebarController;
import piJava.Controllers.backoffice.preferenceAlerte.AlerteDetailsController;
import piJava.Controllers.backoffice.preferenceAlerte.AlerteEditController;
import piJava.Controllers.backoffice.preferenceAlerte.AlerteNewController;
import piJava.entities.preferenceAlerte;
import piJava.services.AlerteService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AlertesController implements Initializable {


    @FXML
    private ListView<preferenceAlerte> alertesListView;

    @FXML
    private Label lblAlertesCount;

    @FXML
    private Label lblActiveCount;

    @FXML
    private Label lblInactiveCount;

    @FXML
    private Label lblDefaultCount;

    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private ComboBox<String> sortCombo;

    private SidebarController sidebarController;
    private ObservableList<preferenceAlerte> allAlertes = FXCollections.observableArrayList();
    private ObservableList<preferenceAlerte> filteredAlertes = FXCollections.observableArrayList();

    private static preferenceAlerte currentAlerteForDetails;
    private static preferenceAlerte currentAlerteForEdit;

    public void setSidebarController(SidebarController sidebarcontroller) {
        this.sidebarController = sidebarcontroller;
    }

    public static void setCurrentAlerteForDetails(preferenceAlerte alerte) {
        currentAlerteForDetails = alerte;
    }

    public static void setCurrentAlerteForEdit(preferenceAlerte alerte) {
        currentAlerteForEdit = alerte;
    }

    public static preferenceAlerte getCurrentAlerteForEdit() {
        return currentAlerteForEdit;
    }

    public static preferenceAlerte getCurrentAlerteForDetails() {
        return currentAlerteForDetails;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListView();
        loadAlertes();
        setupFilters();
    }

    private void setupListView() {
        alertesListView.setItems(filteredAlertes);
        alertesListView.setFixedCellSize(200);
        alertesListView.setCellFactory(param -> new AlerteListCell());
    }

    private void loadAlertes() {
        try {
            AlerteService alerteService = new AlerteService();
            List<preferenceAlerte> alertes = alerteService.show();

            allAlertes.setAll(alertes);
            updateStats();
            applyFilters();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("❌ Erreur lors du chargement de la liste des preferences");
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private HBox createAlerteCard(preferenceAlerte alerte) {
        HBox root = new HBox(20);
        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add("alerte-item");
        root.setStyle("-fx-background-color: #2D3A46; -fx-background-radius: 20; -fx-border-color: #3B4A5A; -fx-border-radius: 20; -fx-border-width: 1; -fx-padding: 22; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 10, 0, 0, 4);");

        // ─── Status bar ───────────────────────────────
        VBox statusBar = new VBox();
        statusBar.setPrefWidth(8);
        statusBar.getStyleClass().add(alerte.getIs_active() ? "status-bar-active" : "status-bar-inactive");

        // ─── Content VBox ───────────────────────────────
        VBox content = new VBox(10);
        HBox.setHgrow(content, Priority.ALWAYS);

        // Top row: name, status badges
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(alerte.getNom());
        name.getStyleClass().add("alerte-name");

        Label activeStatus = new Label(alerte.getIs_active() ? "✅ Active" : "⏸️ Inactive");
        activeStatus.getStyleClass().add(alerte.getIs_active() ? "badge-active" : "badge-inactive");

        Label userId = new Label("User #" + alerte.getUser_id());
        userId.getStyleClass().add("alerte-user");

        if (alerte.getIs_default()) {
            Label defaultBadge = new Label("⭐ Par défaut");
            defaultBadge.getStyleClass().add("badge-default");
            topRow.getChildren().addAll(name, activeStatus, defaultBadge);
        } else {
            topRow.getChildren().addAll(name, activeStatus, userId);
        }

        // Notification channels
        HBox channels = new HBox(10);
        channels.setAlignment(Pos.CENTER_LEFT);
        if (alerte.getEmail_actif()) {
            Label email = new Label("📧 Email");
            email.getStyleClass().add("channel-badge");
            channels.getChildren().add(email);
        }
        if (alerte.getPush_actif()) {
            Label push = new Label("🔔 Push");
            push.getStyleClass().add("channel-badge");
            channels.getChildren().add(push);
        }
        if (alerte.getSite_notif_active()) {
            Label site = new Label("🌐 Site");
            site.getStyleClass().add("channel-badge");
            channels.getChildren().add(site);
        }

        // Meta info
        HBox meta = new HBox(20);
        meta.setAlignment(Pos.CENTER_LEFT);

        StringBuilder metaText = new StringBuilder();

        if (alerte.getHeure_silence_debut() != null && alerte.getHeure_silence_fin() != null) {
            if (metaText.length() > 0) metaText.append(" | ");
            metaText.append("🔇 Silence: ")
                    .append(alerte.getHeure_silence_debut())
                    .append(" - ")
                    .append(alerte.getHeure_silence_fin());
        }

        if (metaText.length() > 0) {
            Label metaLabel = new Label(metaText.toString());
            metaLabel.getStyleClass().add("alerte-meta");
            meta.getChildren().add(metaLabel);
        }

        content.getChildren().addAll(topRow, channels, meta);

        // ─── Action buttons ─────────────────────────────
        VBox actions = new VBox(10);
        actions.setAlignment(Pos.CENTER);

        Button details = new Button("👁 Détails");
        Button edit = new Button("✏️ Modifier");
        Button delete = new Button("🗑 Supprimer");

        details.getStyleClass().add("btn-details");
        edit.getStyleClass().add("btn-edit");
        delete.getStyleClass().add("btn-delete");

        // actions
        delete.setOnAction(e -> deleteAlerte(alerte));
        edit.setOnAction(e -> editAlerte(alerte));
        details.setOnAction(e -> showDetails(alerte));

        actions.getChildren().addAll(details, edit, delete);

        root.getChildren().addAll(statusBar, content, actions);

        return root;
    }

    private void showDetails(preferenceAlerte alerte) {
        try {
            AlertesController.setCurrentAlerteForDetails(alerte);
            if (sidebarController != null) {
                sidebarController.loadView("/backoffice/preferenceAlerte/alerte-details.fxml");
            }
        } catch (Exception e) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Error");
            info.setHeaderText("❌ Erreur lors de l'ouverture des détails");
            info.setContentText("Impossible d'afficher les détails de la preference d'alerte");
            e.printStackTrace();
        }
    }

    private void deleteAlerte(preferenceAlerte alerte) {
        try {
            AlerteService alerteService = new AlerteService();
            alerteService.delete(alerte.getId());

            // Remove from lists
            allAlertes.remove(alerte);
            updateStats();
            applyFilters();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("✔ Succès");
            alert.setHeaderText("Preference d'alerte supprimée");
            alert.setContentText("La preference d'alerte a été supprimée avec succès.");
            alert.showAndWait();
        } catch (SQLException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("❌ Erreur");
            alert.setHeaderText("Suppression échouée");
            alert.setContentText("La preference d'alerte n'a pas pu être supprimée. Veuillez réessayer. \n"+ex.getMessage());
            alert.showAndWait();
        }
    }

    private void editAlerte(preferenceAlerte alerte) {
        try {
            AlertesController.setCurrentAlerteForEdit(alerte);
            if (sidebarController != null) {
                sidebarController.loadView("/backoffice/preferenceAlerte/alerte-edit.fxml");
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("❌ Erreur lors de l'ouverture du formulaire de modification");
            alert.showAndWait();
            ex.printStackTrace();
        }
    }

    public void addAlerte() {
        try {
            if (sidebarController != null) {
                sidebarController.loadView("/backoffice/preferenceAlerte/alerte-new.fxml");
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("❌ Erreur lors de l'ouverture du formulaire de création");
            alert.showAndWait();
            ex.printStackTrace();
        }
    }

    private void setupFilters() {
        statusFilter.getItems().addAll("Tous", "Actif", "Inactif");
        sortCombo.getItems().addAll("Nom (A-Z)", "Nom (Z-A)", "Date création (récent)", "Date création (ancien)");
        statusFilter.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        sortCombo.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        txtSearch.textProperty().addListener((obs, old, newVal) -> applyFilters());
    }

    private void updateStats() {
        long total = allAlertes.size();
        long active = allAlertes.stream().filter(a -> a.getIs_active()).count();
        long inactive = total - active;
        long defaults = allAlertes.stream().filter(a -> a.getIs_default()).count();

        lblAlertesCount.setText(total + " préférence(s)");
        lblActiveCount.setText(String.valueOf(active));
        lblInactiveCount.setText(String.valueOf(inactive));
        lblDefaultCount.setText(String.valueOf(defaults));
    }

    private void applyFilters() {
        List<preferenceAlerte> filtered = allAlertes.stream()
            .filter(a -> {
                String query = txtSearch.getText().toLowerCase();
                if (!query.isEmpty() && !a.getNom().toLowerCase().contains(query)) return false;

                String status = statusFilter.getValue();
                if (status != null && !"Tous".equals(status)) {
                    boolean isActive = "Actif".equals(status);
                    if (a.getIs_active() != isActive) return false;
                }

                return true;
            })
            .collect(Collectors.toList());

        // Sort
        String sort = sortCombo.getValue();
        if (sort != null) {
            switch (sort) {
                case "Nom (A-Z)":
                    filtered.sort(Comparator.comparing(preferenceAlerte::getNom));
                    break;
                case "Nom (Z-A)":
                    filtered.sort(Comparator.comparing(preferenceAlerte::getNom).reversed());
                    break;
                case "Date création (récent)":
                    filtered.sort(Comparator.comparing(preferenceAlerte::getDate_creation).reversed());
                    break;
                case "Date création (ancien)":
                    filtered.sort(Comparator.comparing(preferenceAlerte::getDate_creation));
                    break;
            }
        }

        filteredAlertes.setAll(filtered);
    }

    private class AlerteListCell extends ListCell<preferenceAlerte> {
        @Override
        protected void updateItem(preferenceAlerte alerte, boolean empty) {
            super.updateItem(alerte, empty);

            if (empty || alerte == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            setGraphic(createAlerteCard(alerte));
            setText(null);
        }
    }

}

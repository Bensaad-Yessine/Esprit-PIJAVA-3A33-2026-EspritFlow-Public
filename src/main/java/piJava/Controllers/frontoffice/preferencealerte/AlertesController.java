package piJava.Controllers.frontoffice.preferencealerte;

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
import javafx.util.Callback;
import javafx.util.Duration;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.preferenceAlerte;
import piJava.services.AlerteService;
import piJava.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AlertesController implements Initializable {

    private final int currentUserId = SessionManager.getInstance().getCurrentUser().getId();

    @FXML
    private ListView<preferenceAlerte> alertesListView;

    @FXML
    private Label lblAlertesCount;

    @FXML
    private Label lblEmailCount;

    @FXML
    private Label lblPushCount;


    @FXML
    private Label lblSiteCount;

    @FXML
    private Label taskCountLabel;

    @FXML
    private Label notificationLabel;

    @FXML
    private TextField searchField;

    private FrontSidebarController sidebarController;
    private ObservableList<preferenceAlerte> allAlertes = FXCollections.observableArrayList();
    private ObservableList<preferenceAlerte> filteredAlertes = FXCollections.observableArrayList();

    public void setSidebarController(FrontSidebarController sidebarcontroller) {
        this.sidebarController = sidebarcontroller;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListView();
        loadAlertes();
    }

    private void setupListView() {
        alertesListView.setItems(filteredAlertes);
        alertesListView.setFixedCellSize(200); // Set fixed height for cells
        alertesListView.setCellFactory(new Callback<ListView<preferenceAlerte>, ListCell<preferenceAlerte>>() {
            @Override
            public ListCell<preferenceAlerte> call(ListView<preferenceAlerte> param) {
                return new AlerteListCell();
            }
        });
    }

    private void loadAlertes() {
        try {
            AlerteService alerteService = new AlerteService();
            List<preferenceAlerte> alertes = alerteService.showUserAlertes(currentUserId);

            allAlertes.setAll(alertes);
            filteredAlertes.setAll(alertes);

            updateStats(alertes);

        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("❌ Erreur lors du chargement des alertes");
        }
    }

    private void updateStats(List<preferenceAlerte> alertes) {
        int total = alertes.size();
        int emailCount = 0;
        int pushCount = 0;
        int siteCount = 0;

        for (preferenceAlerte alerte : alertes) {
            if (alerte.getEmail_actif()) emailCount++;
            if (alerte.getPush_actif()) pushCount++;
            if (alerte.getSite_notif_active()) siteCount++;
        }

        lblAlertesCount.setText(String.valueOf(total));
        lblEmailCount.setText(String.valueOf(emailCount));
        lblPushCount.setText(String.valueOf(pushCount));
        lblSiteCount.setText(String.valueOf(siteCount));
        taskCountLabel.setText(total + " préférence(s)");
    }

    @FXML
    private void refreshList() {
        loadAlertes();
        showNotification("🔄 Liste actualisée");
    }

    @FXML
    private void filterAll() {
        filteredAlertes.setAll(allAlertes);
        updateFilterButtons("Toutes");
    }

    @FXML
    private void filterActive() {
        filteredAlertes.setAll(allAlertes.stream()
                .filter(a -> a.getIs_active())
                .collect(Collectors.toList()));
        updateFilterButtons("Actives");
    }

    @FXML
    private void filterInactive() {
        filteredAlertes.setAll(allAlertes.stream()
                .filter(a -> !a.getIs_active())
                .collect(Collectors.toList()));
        updateFilterButtons("Inactives");
    }

    @FXML
    private void filterEmail() {
        filteredAlertes.setAll(allAlertes.stream()
                .filter(a -> a.getEmail_actif())
                .collect(Collectors.toList()));
        updateFilterButtons("Email");
    }

    @FXML
    private void filterPush() {
        filteredAlertes.setAll(allAlertes.stream()
                .filter(a -> a.getPush_actif())
                .collect(Collectors.toList()));
        updateFilterButtons("Push");
    }

    @FXML
    private void filterSite() {
        filteredAlertes.setAll(allAlertes.stream()
                .filter(a -> a.getSite_notif_active())
                .collect(Collectors.toList()));
        updateFilterButtons("Site");
    }

    @FXML
    private void sortByDateCreation() {
        FXCollections.sort(filteredAlertes, Comparator.comparing(preferenceAlerte::getDate_creation).reversed());
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        if (query.isEmpty()) {
            filteredAlertes.setAll(allAlertes);
        } else {
            filteredAlertes.setAll(allAlertes.stream()
                    .filter(a -> a.getNom().toLowerCase().contains(query))
                    .collect(Collectors.toList()));
        }
    }

    private void updateFilterButtons(String activeFilter) {
        // This would update button styles - implementation depends on your needs
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

    private HBox createAlerteCard(preferenceAlerte alerte) {
        HBox root = new HBox(16);
        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add("alerte-item");
        root.setPrefWidth(0); // Allow stretching
        root.setPrefHeight(200);
        root.setMinHeight(200);
        root.setMaxHeight(200);

        // Status indicator
        VBox statusIndicator = new VBox();
        statusIndicator.getStyleClass().add("status-indicator");
        statusIndicator.getStyleClass().add(alerte.getIs_active() ? "status-indicator-active" : "status-indicator-inactive");

        // Content
        VBox content = new VBox(8);
        HBox.setHgrow(content, Priority.ALWAYS);

        // Header row
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icon container
        HBox iconContainer = new HBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.getStyleClass().add("alerte-icon-container");

        // Determine icon based on primary channel
        Label iconLabel = new Label();
        iconLabel.getStyleClass().add("alerte-icon");

        if (alerte.getEmail_actif()) {
            iconContainer.getStyleClass().add("alerte-icon-container-red");
            iconLabel.setText("📧");
            iconLabel.getStyleClass().add("alerte-icon-red");
        } else if (alerte.getPush_actif()) {
            iconContainer.getStyleClass().add("alerte-icon-container-blue");
            iconLabel.setText("🔔");
            iconLabel.getStyleClass().add("alerte-icon-blue");
        } else {
            iconContainer.getStyleClass().add("alerte-icon-container-green");
            iconLabel.setText("🌐");
            iconLabel.getStyleClass().add("alerte-icon-green");
        }
        iconContainer.getChildren().add(iconLabel);

        // Name and badges
        VBox nameBox = new VBox(4);
        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(alerte.getNom());
        name.getStyleClass().add("alerte-name");

        // Priority badge (simulated based on is_default or other logic)
        Label priorityBadge = new Label(alerte.getIs_default() ? "PAR DÉFAUT" : "NORMALE");
        priorityBadge.getStyleClass().add(alerte.getIs_default() ? "priority-high" : "priority-medium");

        nameRow.getChildren().addAll(name, priorityBadge);

        // Description
        Label desc = new Label(getAlerteDescription(alerte));
        desc.getStyleClass().add("alerte-description");
        desc.setWrapText(true);

        nameBox.getChildren().addAll(nameRow, desc);

        header.getChildren().addAll(iconContainer, nameBox);

        // Meta row
        HBox metaRow = new HBox(16);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.getStyleClass().add("alerte-meta-box");

        // Creation date (if available)
        if (alerte.getDate_creation() != null) {
            HBox dateBox = new HBox(6);
            dateBox.setAlignment(Pos.CENTER_LEFT);
            Label dateIcon = new Label("📅");
            dateIcon.getStyleClass().add("alerte-meta-icon");
            Label dateLabel = new Label(alerte.getDate_creation().toString());
            dateLabel.getStyleClass().add("alerte-meta");
            dateBox.getChildren().addAll(dateIcon, dateLabel);
            metaRow.getChildren().add(dateBox);
        }

        // Silence hours if set
        if (alerte.getHeure_silence_debut() != null && alerte.getHeure_silence_fin() != null) {
            HBox silenceBox = new HBox(6);
            silenceBox.setAlignment(Pos.CENTER_LEFT);
            Label silenceIcon = new Label("🔇");
            silenceIcon.getStyleClass().add("alerte-meta-icon");
            Label silenceLabel = new Label(alerte.getHeure_silence_debut() + " - " + alerte.getHeure_silence_fin());
            silenceLabel.getStyleClass().add("alerte-meta");
            silenceBox.getChildren().addAll(silenceIcon, silenceLabel);
            metaRow.getChildren().add(silenceBox);
        }

        // Channel badges
        HBox channels = new HBox(8);
        channels.setAlignment(Pos.CENTER_RIGHT);
        if (alerte.getEmail_actif()) {
            Label email = new Label("EMAIL");
            email.getStyleClass().add("channel-badge");
            channels.getChildren().add(email);
        }
        if (alerte.getPush_actif()) {
            Label push = new Label("PUSH");
            push.getStyleClass().add("channel-badge");
            push.getStyleClass().add("channel-badge-push");
            channels.getChildren().add(push);
        }
        if (alerte.getSite_notif_active()) {
            Label site = new Label("SITE");
            site.getStyleClass().add("channel-badge");
            site.getStyleClass().add("channel-badge-sms");
            channels.getChildren().add(site);
        }

        HBox metaWrapper = new HBox();
        HBox.setHgrow(metaWrapper, Priority.ALWAYS);
        metaWrapper.getChildren().add(metaRow);

        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.getChildren().addAll(metaWrapper, channels);

        content.getChildren().addAll(header, bottomRow);

        // Actions
        VBox actions = new VBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMinWidth(120);

        // Status badge
        Label statusBadge = new Label(alerte.getIs_active() ? "ACTIF" : "INACTIF");
        statusBadge.getStyleClass().add(alerte.getIs_active() ? "badge-active" : "badge-inactive");

        // Action buttons
        HBox btnBox = new HBox(8);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.getStyleClass().add("action-buttons");

        Button edit = new Button("✏️");
        edit.getStyleClass().add("btn-edit");
        edit.setTooltip(new Tooltip("Modifier"));
        edit.setOnAction(e -> editAlerte(alerte));

        Button details = new Button("👁");
        details.getStyleClass().add("btn-details");
        details.setTooltip(new Tooltip("Détails"));
        details.setOnAction(e -> showDetails(alerte));

        Button delete = new Button("🗑");
        delete.getStyleClass().add("btn-delete");
        delete.setTooltip(new Tooltip("Supprimer"));
        delete.setOnAction(e -> deleteAlerte(alerte));

        btnBox.getChildren().addAll(edit, details, delete);
        actions.getChildren().addAll(statusBadge, btnBox);

        root.getChildren().addAll(statusIndicator, content, actions);

        return root;
    }

    private String getAlerteDescription(preferenceAlerte alerte) {
        if (alerte.getEmail_actif() && alerte.getPush_actif() && alerte.getSite_notif_active()) {
            return "Notifications par email, push et sur site";
        } else if (alerte.getEmail_actif() && alerte.getPush_actif()) {
            return "Notifications par email et push";
        } else if (alerte.getEmail_actif()) {
            return "Notifications par email uniquement";
        } else if (alerte.getPush_actif()) {
            return "Notifications push uniquement";
        } else if (alerte.getSite_notif_active()) {
            return "Notifications sur site uniquement";
        }
        return "Aucun canal activé";
    }

    private void showDetails(preferenceAlerte alerte) {
        try {
            // Store the alerte temporarily so AlerteDetailsController can access it
            AlerteDetailsController.currentAlerte = alerte;
            sidebarController.loadView("/frontoffice/preferenceAlerte/alerte-details.fxml");
        } catch (Exception e) {
            showNotification("❌ Erreur ouverture détails");
            e.printStackTrace();
        }
    }

    private void deleteAlerte(preferenceAlerte alerte) {
        // Confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la préférence ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" + alerte.getNom() + "\" ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    AlerteService alerteService = new AlerteService();
                    alerteService.delete(alerte.getId());

                    allAlertes.remove(alerte);
                    filteredAlertes.remove(alerte);
                    updateStats(allAlertes);

                    showNotification("✅ Préférence supprimée avec succès");

                } catch (SQLException ex) {
                    showNotification("❌ Erreur lors de la suppression");
                    ex.printStackTrace();
                }
            }
        });
    }

    private void editAlerte(preferenceAlerte alerte) {
        try {
            // Store the alerte temporarily so AlerteEditController can access it
            AlerteEditController.currentAlerte = alerte;
            sidebarController.loadView("/frontoffice/preferenceAlerte/alerte-edit.fxml");
        } catch (Exception ex) {
            showNotification("❌ Erreur lors de l'ouverture de l'éditeur");
            ex.printStackTrace();
        }
    }

    @FXML
    public void addAlerte() {
        try {
            sidebarController.loadView("/frontoffice/preferenceAlerte/alerte-new.fxml");
        } catch (Exception ex) {
            showNotification("❌ Erreur lors de l'ouverture du formulaire");
            ex.printStackTrace();
        }
    }

    private void showNotification(String message) {
        notificationLabel.setText(message);
        notificationLabel.setOpacity(1);
        FadeTransition ft = new FadeTransition(Duration.seconds(3), notificationLabel);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.play();
    }
}


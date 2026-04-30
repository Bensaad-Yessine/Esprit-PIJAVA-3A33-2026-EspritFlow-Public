package piJava.Controllers.backoffice.group;

import javafx.collections.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import piJava.entities.Groupe;
import piJava.entities.PropositionReunion;


import piJava.Controllers.group.GroupChatClient;
import piJava.Controllers.group.GroupChatListener;
import piJava.Controllers.group.GroupChatMessage;
import piJava.Controllers.group.GroupChatServer;

import piJava.services.PropositionReunionService;


import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PropositionReunionController implements Initializable, GroupChatListener {

    @FXML private Label groupNameBreadcrumb;
    @FXML private Label pageTitle;
    @FXML private TextField searchField;
    @FXML private Button addPropositionBtn;

    @FXML private Label totalPropositionsLabel;
    @FXML private Label acceptedPropositionsLabel;
    @FXML private Label pendingPropositionsLabel;

    @FXML private FlowPane propositionsContainer;

    @FXML private Label footerLabel;
    @FXML private Button backBtn;
    @FXML private TextField chatHostField;
    @FXML private TextField chatPortField;
    @FXML private TextField chatUsernameField;
    @FXML private ListView<String> chatMessagesListView;
    @FXML private TextField chatMessageField;
    @FXML private Label chatStatusLabel;

    private StackPane contentArea;
    private Groupe currentGroupe;
    private final PropositionReunionService propositionService = new PropositionReunionService();


    private final GroupChatClient groupChatClient = new GroupChatClient();

    private ObservableList<PropositionReunion> allPropositions = FXCollections.observableArrayList();
    private ObservableList<PropositionReunion> filtered = FXCollections.observableArrayList();
    private GroupContentController parentController;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupListView();
        setupSearch();
        setupChatUI();
    }

    private void setupChatUI() {
        if (chatMessagesListView != null) {
            chatMessagesListView.setItems(FXCollections.observableArrayList());
        }
        if (chatStatusLabel != null) {
            chatStatusLabel.setText("Chat hors ligne");
        }
    }

    private void setupListView() {
        propositionsListView.setCellFactory(param -> new ListCell<PropositionReunion>() {
            @Override
            protected void updateItem(PropositionReunion prop, boolean empty) {
                super.updateItem(prop, empty);
                if (empty || prop == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(createPropositionCard(prop));
            }
        });
    }

    private VBox createPropositionCard(PropositionReunion prop) {
        VBox card = new VBox();
        card.setSpacing(14);
        card.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #2D3A46, #1F2937); "
                    + "-fx-background-radius: 14; -fx-padding: 18 20; "
                    + "-fx-border-color: linear-gradient(from 0% 0% to 100% 100%, #3B4A5A, #2D3A46); "
                    + "-fx-border-radius: 14; -fx-border-width: 1; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 4), "
                    + "dropshadow(gaussian, rgba(59,130,246,0.15), 20, 0, 0, 2); "
                    + "-fx-fill-width: true; -fx-max-width: Infinity;");
        card.setPadding(new Insets(18, 20, 18, 20));

        HBox header = new HBox();
        header.setSpacing(14);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox priorityBar = new VBox();
        String barColor = "En attente".equalsIgnoreCase(prop.getStatut()) ? 
                         "linear-gradient(from 0% 100% to 0% 0%, #F59E0B, #D97706)" :
                         "Acceptée".equalsIgnoreCase(prop.getStatut()) ?
                         "linear-gradient(from 0% 100% to 0% 0%, #10B981, #059669)" :
                         "linear-gradient(from 0% 100% to 0% 0%, #EF4444, #DC2626)";
        priorityBar.setStyle("-fx-background-color: " + barColor + "; "
                            + "-fx-pref-width: 6; -fx-background-radius: 3;");

        VBox info = new VBox();
        info.setSpacing(4);

        Label titre = new Label(prop.getTitre());
        titre.setStyle("-fx-text-fill: #F3F4F6; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label dateTime = new Label((prop.getDateReunion() != null ? prop.getDateReunion().toString() : "—") + 
                                  " | " + (prop.getHeureDebut() != null ? prop.getHeureDebut().toString() : "—") + 
                                  " - " + (prop.getHeureFin() != null ? prop.getHeureFin().toString() : "—"));
        dateTime.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 13px;");

        info.getChildren().addAll(titre, dateTime);
        header.getChildren().addAll(priorityBar, info);

        HBox statsRow = new HBox();
        statsRow.setSpacing(20);

        VBox lieuBox = new VBox();
        lieuBox.setSpacing(2);
        Label lieuLabel = new Label("Lieu");
        lieuLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        Label lieuValue = new Label(nvl(prop.getLieu(), "—"));
        lieuValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #60A5FA;");
        lieuBox.getChildren().addAll(lieuLabel, lieuValue);

        VBox descBox = new VBox();
        descBox.setSpacing(2);
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        Label descValue = new Label(nvl(prop.getDescription(), "—"));
        descValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #CBD5E1; -fx-wrap-text: true;");
        descBox.getChildren().addAll(descLabel, descValue);

        VBox statusBox = new VBox();
        statusBox.setSpacing(2);
        Label statusLabel = new Label("Statut");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        Label statusValue = new Label(nvl(prop.getStatut(), "—"));
        String statusColor = "En attente".equalsIgnoreCase(prop.getStatut()) ? 
                            "linear-gradient(from 0% 0% to 100% 100%, #F59E0B, #D97706)" :
                            "Acceptée".equalsIgnoreCase(prop.getStatut()) ?
                            "linear-gradient(from 0% 0% to 100% 100%, #10B981, #059669)" :
                            "linear-gradient(from 0% 0% to 100% 100%, #EF4444, #DC2626)";
        statusValue.setStyle("-fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold; "
                           + "-fx-text-fill: #FFFFFF; -fx-background-color: " + statusColor + ";");
        statusBox.getChildren().addAll(statusLabel, statusValue);

        statsRow.getChildren().addAll(lieuBox, descBox, statusBox);

        HBox actions = new HBox();
        actions.setSpacing(8);
        actions.setAlignment(Pos.CENTER_RIGHT);



        Separator sep1 = new Separator();
        sep1.setPrefHeight(1);
        sep1.setStyle("-fx-background-color: #3B4A5A;");
        actions.getChildren().add(sep1);

        Button editBtn = new Button("✎ Modifier");
        editBtn.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #F59E0B, #D97706); "
                        + "-fx-text-fill: #FFFFFF; -fx-background-radius: 8; -fx-padding: 8 16; "
                        + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
        editBtn.setOnAction(e -> handleEdit(prop));

        Button delBtn = new Button("🗑 Supprimer");
        delBtn.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #EF4444, #DC2626); "
                      + "-fx-text-fill: #FFFFFF; -fx-background-radius: 8; -fx-padding: 8 16; "
                      + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
        delBtn.setOnAction(e -> handleDelete(prop));

        actions.getChildren().addAll(editBtn, delBtn);

        card.getChildren().addAll(header, statsRow, actions);

        return card;
    }

    public void setCurrentGroupe(Groupe groupe) {
        this.currentGroupe = groupe;
        if (groupe != null) {
            groupNameBreadcrumb.setText(groupe.getNom());
            pageTitle.setText("📋 Propositions Réunion - " + groupe.getNom());
            if (chatUsernameField != null && (chatUsernameField.getText() == null || chatUsernameField.getText().isBlank())) {
                chatUsernameField.setText("Backoffice-" + groupe.getId());
            }
            loadData();
        }
    }

    public void setParentController(GroupContentController parent) {
        this.parentController = parent;
    }
    
    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    // ── Data Loading ───────────────────────────────────────────
    private void loadData() {
        if (propositionsContainer == null || footerLabel == null) {
            return;
        }
        
        try {
            System.out.println("[DEBUG] Loading propositions for groupe: " + currentGroupe.getId());
allPropositions.setAll(propositionService.getByGroupeId(currentGroupe.getId()));
            filtered.setAll(allPropositions);
            updateStats();
            updateDisplay();
        } catch (SQLException e) {
            System.err.println("[ERROR] SQL Error loading propositions: " + e.getMessage());
            e.printStackTrace();
            allPropositions.clear();
            filtered.clear();
            updateStats();
            updateList();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement: " + e.getMessage());
        }
    }

    private void updateStats() {
        int total = allPropositions.size();
        int accepted = (int) allPropositions.stream()
                .filter(p -> "Acceptée".equalsIgnoreCase(p.getStatut()))
                .count();
        int pending = (int) allPropositions.stream()
                .filter(p -> "En attente".equalsIgnoreCase(p.getStatut()))
                .count();

        totalPropositionsLabel.setText(String.valueOf(total));
        acceptedPropositionsLabel.setText(String.valueOf(accepted));
        pendingPropositionsLabel.setText(String.valueOf(pending));
    }

    private void updateDisplay() {
        propositionsContainer.getChildren().clear();
        for (PropositionReunion prop : filtered) {
            propositionsContainer.getChildren().add(createPropositionCard(prop));
        }
        footerLabel.setText("Affichage de " + filtered.size() + " proposition(s)");
    }
    
    private VBox createPropositionCard(PropositionReunion prop) {
        VBox card = new VBox();
        card.setSpacing(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color: #1e1e2d; -fx-background-radius: 12; -fx-border-color: #2a2a36; -fx-border-radius: 12; -fx-border-width: 1;");

        Label titleLabel = new Label("Proposition #" + prop.getId());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #ffffff;");

        Label dateLabel = new Label("📅 " + (prop.getDateReunion() != null ? prop.getDateReunion() : "—"));
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8fa6;");

        String timeRange = (prop.getHeureDebut() != null ? prop.getHeureDebut() : "") + 
                         (prop.getHeureFin() != null ? " - " + prop.getHeureFin() : "");
        Label timeLabel = new Label("🕐 " + (timeRange.isEmpty() ? "—" : timeRange));
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8fa6;");

        Label lieuLabel = new Label("📍 " + (prop.getLieu() != null ? prop.getLieu() : "—"));
        lieuLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8fa6;");

        String status = prop.getStatut() != null ? prop.getStatut() : "En attente";
        Label statusBadge = new Label(status);
        String statusColor = "En attente".equalsIgnoreCase(status) ? "#f59e0b" : 
                          "Acceptée".equalsIgnoreCase(status) ? "#10b981" : "#ef4444";
        statusBadge.setStyle("-fx-padding: 6 14; -fx-background-color: " + statusColor + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 16; -fx-font-size: 12px;");

        Label descLabel = new Label(prop.getDescription() != null ? prop.getDescription() : "Aucune description");
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #b8bcc8;");
        descLabel.setWrapText(true);

        HBox actions = new HBox(12);
        Button editBtn = new Button("✎ Modifier");
        editBtn.setStyle("-fx-background-color: #6c5ce7; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        editBtn.setOnAction(e -> handleEdit(prop));

        Button deleteBtn = new Button("✕ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> handleDelete(prop));

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(titleLabel, dateLabel, timeLabel, lieuLabel, statusBadge, descLabel, actions);
        return card;
    }

    // ── Search ─────────────────────────────────────────────────
    private void setupSearch() {
        searchField.textProperty().addListener((o, old, neu) -> applyFilters());
    }

    private void applyFilters() {
        String search = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        filtered.setAll(allPropositions.stream()
                .filter(p -> (p.getDescription() != null && p.getDescription().toLowerCase().contains(search)) ||
                            (p.getLieu() != null && p.getLieu().toLowerCase().contains(search))))
                .collect(Collectors.toList()));
        updateDisplay();
    }

    // ── Actions ────────────────────────────────────────────────
    @FXML
    private void handleAdd() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Proposition de Réunion");
        dialog.setHeaderText("Ajouter une nouvelle proposition pour " + currentGroupe.getNom());
        
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(16));
        
        TextField titreField = new TextField();
        titreField.setPromptText("Titre de la proposition");
        titreField.setPrefWidth(300);
        
        DatePicker dateField = new DatePicker();
        dateField.setValue(java.time.LocalDate.now());
        
        ComboBox<String> debStart = createTimeCombo();
        ComboBox<String> debEnd = createTimeCombo();
        debEnd.setValue("10:00");
        
        TextField lieuField = new TextField();
        lieuField.setPromptText("Lieu de la réunion");
        lieuField.setPrefWidth(300);
        
        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefWidth(300);
        descField.setPrefHeight(80);
        descField.setWrapText(true);
        
        form.add(new Label("Titre:"), 0, 0);
        form.add(titreField, 1, 0);
        
        form.add(new Label("Date:"), 0, 1);
        form.add(dateField, 1, 1);
        
        form.add(new Label("Heure Début:"), 0, 2);
        form.add(debStart, 1, 2);
        
        form.add(new Label("Heure Fin:"), 0, 3);
        form.add(debEnd, 1, 3);
        
        form.add(new Label("Lieu:"), 0, 4);
        form.add(lieuField, 1, 4);
        
        form.add(new Label("Description:"), 0, 5);
        form.add(descField, 1, 5);
        
        ComboBox<String> statusField = new ComboBox<>();
        statusField.getItems().addAll("En attente", "Acceptée", "Refusée");
        statusField.setValue("En attente");
        
        form.add(new Label("Statut:"), 0, 6);
        form.add(statusField, 1, 6);
        
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        styleAlert(dialog);
        
        dialog.showAndWait().ifPresent((ButtonType response) -> {
            if (response == ButtonType.OK) {
                if (titreField.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Le titre est requis");
                    return;
                }
                
                String titre = titreField.getText().trim();
                if (!Character.isUpperCase(titre.charAt(0))) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Le titre doit commencer par une majuscule.");
                    return;
                }
                
                java.time.LocalDate selectedDate = dateField.getValue();
                java.time.LocalDate today = java.time.LocalDate.now();
                if (selectedDate == null || !selectedDate.isAfter(today)) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "La date doit être un jour supérieur à aujourd'hui.");
                    return;
                }
                
                java.time.LocalTime startTime = timeStringToLocalTime(debStart.getValue());
                java.time.LocalTime endTime = timeStringToLocalTime(debEnd.getValue());
                if (startTime != null && endTime != null) {
                    long minutesDiff = java.time.Duration.between(startTime, endTime).toMinutes();
                    if (minutesDiff < 0) {
                        minutesDiff += 24 * 60;
                    }
                    if (minutesDiff > 270) {
                        showAlert(Alert.AlertType.WARNING, "Validation", "La date fin doit être maximum 4 heures et demie après la date début.");
                        return;
                    }
                }
                
                String description = descField.getText();
                if (description != null && !description.isEmpty() && description.trim().length() < 10) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "La description doit contenir au moins 10 caractères.");
                    return;
                }
                
                try {
                    PropositionReunion prop = new PropositionReunion();
                    prop.setTitre(titreField.getText());
                    prop.setDateReunion(dateField.getValue());
                    prop.setHeureDebut(timeStringToLocalTime(debStart.getValue()));
                    prop.setHeureFin(timeStringToLocalTime(debEnd.getValue()));
                    prop.setLieu(lieuField.getText());
                    prop.setDescription(descField.getText());
                    prop.setIdGroupeId(currentGroupe.getId());
                    prop.setStatut(statusField.getValue());
                    
                    propositionService.add(prop);
                    loadData();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Proposition ajoutée avec succès!");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
                }
            }
        });
    }

    private void handleEdit(PropositionReunion proposition) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier Proposition de Réunion");
        dialog.setHeaderText("Éditer: " + proposition.getTitre());
        
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(16));
        
        TextField titreField = new TextField();
        titreField.setText(proposition.getTitre());
        titreField.setPrefWidth(300);
        
        DatePicker dateField = new DatePicker();
        dateField.setValue(proposition.getDateReunion() != null ? proposition.getDateReunion() : java.time.LocalDate.now());
        
        ComboBox<String> debStart = createTimeCombo();
        debStart.setValue(proposition.getHeureDebut() != null ? proposition.getHeureDebut().toString() : "09:00");
        
        ComboBox<String> debEnd = createTimeCombo();
        debEnd.setValue(proposition.getHeureFin() != null ? proposition.getHeureFin().toString() : "10:00");
        
        TextField lieuField = new TextField();
        lieuField.setText(nvl(proposition.getLieu(), ""));
        lieuField.setPrefWidth(300);
        
        TextArea descField = new TextArea();
        descField.setText(nvl(proposition.getDescription(), ""));
        descField.setPrefWidth(300);
        descField.setPrefHeight(80);
        descField.setWrapText(true);
        
        form.add(new Label("Titre:"), 0, 0);
        form.add(titreField, 1, 0);
        
        form.add(new Label("Date:"), 0, 1);
        form.add(dateField, 1, 1);
        
        form.add(new Label("Heure Début:"), 0, 2);
        form.add(debStart, 1, 2);
        
        form.add(new Label("Heure Fin:"), 0, 3);
        form.add(debEnd, 1, 3);
        
        form.add(new Label("Lieu:"), 0, 4);
        form.add(lieuField, 1, 4);
        
        form.add(new Label("Description:"), 0, 5);
        form.add(descField, 1, 5);
        
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        styleAlert(dialog);
        
        dialog.showAndWait().ifPresent((ButtonType response) -> {
            if (response == ButtonType.OK) {
                if (titreField.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Le titre est requis");
                    return;
                }
                
                String titre = titreField.getText().trim();
                if (!Character.isUpperCase(titre.charAt(0))) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Le titre doit commencer par une majuscule.");
                    return;
                }
                
                java.time.LocalDate selectedDate = dateField.getValue();
                java.time.LocalDate today = java.time.LocalDate.now();
                if (selectedDate == null || !selectedDate.isAfter(today)) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "La date doit être un jour supérieur à aujourd'hui.");
                    return;
                }
                
                java.time.LocalTime startTime = timeStringToLocalTime(debStart.getValue());
                java.time.LocalTime endTime = timeStringToLocalTime(debEnd.getValue());
                if (startTime != null && endTime != null) {
                    long minutesDiff = java.time.Duration.between(startTime, endTime).toMinutes();
                    if (minutesDiff < 0) {
                        minutesDiff += 24 * 60;
                    }
                    if (minutesDiff > 270) {
                        showAlert(Alert.AlertType.WARNING, "Validation", "La date fin doit être maximum 4 heures et demie après la date début.");
                        return;
                    }
                }
                
                String description = descField.getText();
                if (description != null && !description.isEmpty() && description.trim().length() < 10) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "La description doit contenir au moins 10 caractères.");
                    return;
                }
                
                try {
                    proposition.setTitre(titreField.getText());
                    proposition.setDateReunion(dateField.getValue());
                    proposition.setHeureDebut(timeStringToLocalTime(debStart.getValue()));
                    proposition.setHeureFin(timeStringToLocalTime(debEnd.getValue()));
                    proposition.setLieu(lieuField.getText());
                    proposition.setDescription(descField.getText());
                    
                    propositionService.edit(proposition);
                    loadData();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Proposition modifiée avec succès!");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
                }
            }
        });
    }

    private void handleDelete(PropositionReunion proposition) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer « " + proposition.getTitre() + " » ?");
        confirm.setContentText("Cette action est irréversible.");
        styleAlert(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    propositionService.delete(proposition.getId());
                    loadData();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/backoffice/group/GroupContent.fxml")
            );
            javafx.scene.Parent view = loader.load();
            
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Back navigation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Utilities ──────────────────────────────────────────────
    private static String nvl(String s, String fallback) {
        return (s != null && !s.isBlank()) ? s : fallback;
    }

    private ComboBox<String> createTimeCombo() {
        ComboBox<String> combo = new ComboBox<>();
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m += 15) {
                combo.getItems().add(String.format("%02d:%02d", h, m));
            }
        }
        combo.setValue("09:00");
        return combo;
    }

    private java.time.LocalTime timeStringToLocalTime(String time) {
        if (time == null || time.isEmpty()) return java.time.LocalTime.of(9, 0);
        String[] parts = time.split(":");
        return java.time.LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        pane.setStyle("-fx-background-color: #111318; -fx-border-color: #1e2130; -fx-border-width: 1;");
    }

    private void styleAlert(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #111318; -fx-border-color: #1e2130; -fx-border-width: 1;");
    }







}
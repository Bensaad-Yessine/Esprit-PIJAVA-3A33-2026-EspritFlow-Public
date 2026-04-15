package piJava.Controllers.backoffice.group;

import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import piJava.entities.Groupe;
import piJava.entities.PropositionReunion;
import piJava.services.PropositionReunionService;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class PropositionReunionController implements Initializable {

    @FXML private Label groupNameBreadcrumb;
    @FXML private Label pageTitle;
    @FXML private TextField searchField;
    @FXML private Button addPropositionBtn;

    @FXML private Label totalPropositionsLabel;
    @FXML private Label acceptedPropositionsLabel;
    @FXML private Label pendingPropositionsLabel;

    @FXML private TableView<PropositionReunion> propositionTable;
    @FXML private TableColumn<PropositionReunion, String> idCol;
    @FXML private TableColumn<PropositionReunion, String> dateCol;
    @FXML private TableColumn<PropositionReunion, String> heureCol;
    @FXML private TableColumn<PropositionReunion, String> lieuCol;
    @FXML private TableColumn<PropositionReunion, String> statusCol;
    @FXML private TableColumn<PropositionReunion, String> descCol;
    @FXML private TableColumn<PropositionReunion, Void> actionsCol;

    @FXML private Label footerLabel;
    @FXML private Button backBtn;

    private Groupe currentGroupe;
    private final PropositionReunionService propositionService = new PropositionReunionService();
    private ObservableList<PropositionReunion> allPropositions = FXCollections.observableArrayList();
    private ObservableList<PropositionReunion> filtered = FXCollections.observableArrayList();
    private GroupContentController parentController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupSearch();
    }

    public void setCurrentGroupe(Groupe groupe) {
        this.currentGroupe = groupe;
        if (groupe != null) {
            groupNameBreadcrumb.setText(groupe.getNom());
            pageTitle.setText("Propositions Réunion - " + groupe.getNom());
            loadData();
        }
    }

    public void setParentController(GroupContentController parent) {
        this.parentController = parent;
    }

    // ── Column Setup ───────────────────────────────────────────
    private void setupColumns() {
        // ID
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));

        // DATE
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateReunion() != null ? d.getValue().getDateReunion().toString() : "—"
        ));

        // HEURE (显示开始和结束时间)
        heureCol.setCellValueFactory(d -> {
            PropositionReunion prop = d.getValue();
            String debut = prop.getHeureDebut() != null ? prop.getHeureDebut().toString() : "—";
            String fin = prop.getHeureFin() != null ? prop.getHeureFin().toString() : "—";
            return new SimpleStringProperty(debut + " - " + fin);
        });

        // LIEU
        lieuCol.setCellValueFactory(d -> new SimpleStringProperty(nvl(d.getValue().getLieu(), "—")));

        // STATUT
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                String color = "En attente".equalsIgnoreCase(item) ? "#f59e0b" : 
                              "Acceptée".equalsIgnoreCase(item) ? "#10b981" : "#ef4444";
                badge.setStyle("-fx-padding:4px 12px; -fx-background-color:" + color + "; " +
                              "-fx-text-fill:white; -fx-border-radius:12;");
                setGraphic(badge);
            }
        });

        // DESCRIPTION
        descCol.setCellValueFactory(d -> new SimpleStringProperty(nvl(d.getValue().getDescription(), "—")));

        // ACTIONS
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = createActionButton("✎", "#8b5cf6");
            private final Button delBtn = createActionButton("✕", "#ef4444");
            private final HBox actions = new HBox(6, editBtn, delBtn);

            {
                actions.setAlignment(Pos.CENTER_LEFT);
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) {
                    setGraphic(null);
                    return;
                }
                PropositionReunion prop = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> handleEdit(prop));
                delBtn.setOnAction(e -> handleDelete(prop));
                setGraphic(actions);
            }
        });
    }

    // ── Data Loading ───────────────────────────────────────────
    private void loadData() {
        // Safety check - ensure UI elements are initialized before loading data
        if (propositionTable == null || footerLabel == null) {
            System.err.println("[WARNING] UI elements not initialized yet, deferring loadData");
            return;
        }
        
        try {
            System.out.println("[DEBUG] Loading propositions for groupe: " + currentGroupe.getId());
            allPropositions.setAll(propositionService.getByGroupeId(currentGroupe.getId()));
            System.out.println("[DEBUG] Loaded " + allPropositions.size() + " propositions");
            filtered.setAll(allPropositions);
            updateStats();
            updateTable();
        } catch (SQLException e) {
            System.err.println("[ERROR] SQL Error loading propositions: " + e.getMessage());
            e.printStackTrace();
            allPropositions.clear();
            filtered.clear();
            updateStats();
            updateTable();
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

    private void updateTable() {
        propositionTable.setItems(filtered);
        footerLabel.setText(String.format("Affichage de %d proposition(s)", filtered.size()));
    }

    // ── Search ─────────────────────────────────────────────────
    private void setupSearch() {
        searchField.textProperty().addListener((o, old, neu) -> applyFilters());
    }

    private void applyFilters() {
        String search = searchField.getText().toLowerCase();
        filtered.setAll(allPropositions.stream()
                .filter(p -> p.getTitre().toLowerCase().contains(search) ||
                            (p.getLieu() != null && p.getLieu().toLowerCase().contains(search)))
                .collect(Collectors.toList()));
        updateTable();
    }

    // ── Actions ────────────────────────────────────────────────
    @FXML
    private void handleAdd() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Proposition de Réunion");
        dialog.setHeaderText("Ajouter une nouvelle proposition pour " + currentGroupe.getNom());
        
        // Create form fields
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
        
        // Add to form
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
                
                // Validation: Titre doit commencer par une majuscule
                String titre = titreField.getText().trim();
                if (!Character.isUpperCase(titre.charAt(0))) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Le titre doit commencer par une majuscule.");
                    return;
                }
                
                // Validation: Date doit être supérieure à aujourd'hui
                java.time.LocalDate selectedDate = dateField.getValue();
                java.time.LocalDate today = java.time.LocalDate.now();
                if (selectedDate == null || !selectedDate.isAfter(today)) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "La date doit être un jour supérieur à aujourd'hui.");
                    return;
                }
                
                // Validation: Date fin max 3 heures après début
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
                
                // Validation: Description minimum 10 caractères
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
        
        // Create form fields
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
        
        // Add to form
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
                
                // Validation: Titre doit commencer par une majuscule
                String titre = titreField.getText().trim();
                if (!Character.isUpperCase(titre.charAt(0))) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Le titre doit commencer par une majuscule.");
                    return;
                }
                
                // Validation: Date doit être supérieure à aujourd'hui
                java.time.LocalDate selectedDate = dateField.getValue();
                java.time.LocalDate today = java.time.LocalDate.now();
                if (selectedDate == null || !selectedDate.isAfter(today)) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "La date doit être un jour supérieur à aujourd'hui.");
                    return;
                }
                
                // Validation: Date fin max 3 heures après début
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
                
                // Validation: Description minimum 10 caractères
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
        if (parentController != null) {
            parentController.showGroupsView();
        } else {
            System.err.println("[WARNING] Parent controller not set - cannot navigate back");
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

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size:11px; -fx-padding:4px 8px; " +
                   "-fx-background-color:" + color + "; -fx-text-fill:white; " +
                   "-fx-border-radius:4; -fx-cursor:hand;");
        return btn;
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(msg);
        styleAlert(alert);
        alert.showAndWait();
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

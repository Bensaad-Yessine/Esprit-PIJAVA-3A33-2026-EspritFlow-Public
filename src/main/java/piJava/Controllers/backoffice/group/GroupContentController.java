package piJava.Controllers.backoffice.group;

import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import piJava.Controllers.backoffice.SidebarController;
import piJava.entities.Groupe;
import piJava.services.GroupeService;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class GroupContentController implements Initializable {

    // ── Header ─────────────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private Button    addGroupBtn;

    // ── Mini stats ─────────────────────────────────────────────
    @FXML private Label totalGroupsLabel;
    @FXML private Label activeGroupsLabel;
    //@FXML private Label totalMembersLabel;
    @FXML private Label avgMembersLabel;

    // ── Filters ────────────────────────────────────────────────
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button           resetFilterBtn;
    @FXML private Label           resultCountLabel;

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Groupe>            groupTable;
    @FXML private TableColumn<Groupe, String>  idCol;
    @FXML private TableColumn<Groupe, String>  nameCol;
    @FXML private TableColumn<Groupe, String>  projectCol;
    @FXML private TableColumn<Groupe, String>  membersCol;
    @FXML private TableColumn<Groupe, String>  statusCol;
    @FXML private TableColumn<Groupe, String>  descCol;
    @FXML private TableColumn<Groupe, Void>    actionsCol;

    // ── Footer ─────────────────────────────────────────────────
    @FXML private Label  footerLabel;
    @FXML private Button prevBtn;
    @FXML private Label  pageLabel;
    @FXML private Button nextBtn;

    // ── State ──────────────────────────────────────────────────
    private final GroupeService groupeService = new GroupeService();
    private ObservableList<Groupe> allGroups = FXCollections.observableArrayList();
    private ObservableList<Groupe> filtered    = FXCollections.observableArrayList();

    private static final int PAGE_SIZE = 8;
    private int currentPage = 1;

    // ── Injected dependencies ──────────────────────────────────
    private StackPane contentArea;
    private SidebarController sidebarController;

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void setSidebarController(SidebarController sidebar) {
        this.sidebarController = sidebar;
    }

    // ───────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadData();
        setupSearch();
        setupFilters();
        animateEntrance();
    }

    // ── Column Setup ───────────────────────────────────────────
    private void setupColumns() {

        // # ID
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label("#" + item);
                lbl.setStyle("-fx-font-family:'Syne'; -fx-font-size:11px; "
                           + "-fx-text-fill:#3a4060; -fx-font-weight:700;");
                setGraphic(lbl);
            }
        });

        // NOM
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("-fx-font-size:14px; -fx-font-weight:600; -fx-text-fill:#eef0f8;");
                setGraphic(lbl);
            }
        });

        // PROJET
        projectCol.setCellValueFactory(d -> new SimpleStringProperty(nvl(d.getValue().getProjet(), "—")));

        // MEMBRES
        membersCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getNbreMembre())));
        membersCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("-fx-text-alignment:center; -fx-text-fill:#cbd5e0;");
                setGraphic(lbl);
            }
        });

        // STATUT
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(nvl(d.getValue().getStatut(), "—")));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setStyle("-fx-padding:4px 12px; -fx-border-radius:12; "
                           + ("Actif".equalsIgnoreCase(item)
                                ? "-fx-background-color:#10b981; -fx-text-fill:#ffffff;"
                                : "-fx-background-color:#6b7280; -fx-text-fill:#ffffff;"));
                setGraphic(badge);
            }
        });

        // DESCRIPTION
        descCol.setCellValueFactory(d -> new SimpleStringProperty(nvl(d.getValue().getDescription(), "—")));

        // ACTIONS
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn  = createActionButton("✎", "#8b5cf6");
            private final Button proposBtn = createActionButton("📋", "#f59e0b");
            private final Button delBtn   = createActionButton("✕", "#ef4444");
            private final HBox actions    = new HBox(6, editBtn, proposBtn, delBtn);

            {
                actions.setAlignment(Pos.CENTER_LEFT);
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) {
                    setGraphic(null);
                    return;
                }
                Groupe groupe = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> handleEdit(groupe));
                proposBtn.setOnAction(e -> handlePropositions(groupe));
                delBtn.setOnAction(e -> handleDelete(groupe));
                setGraphic(actions);
            }
        });
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size:11px; -fx-padding:4px 8px; "
                   + "-fx-background-color:" + color + "; -fx-text-fill:white; "
                   + "-fx-border-radius:4; -fx-cursor:hand;");
        return btn;
    }

    // ── Data Loading ───────────────────────────────────────────
    private void loadData() {
        try {
            System.out.println("[DEBUG] Loading groups data...");
            allGroups.setAll(groupeService.getAll());
            System.out.println("[DEBUG] Loaded " + allGroups.size() + " groups");
            filtered.setAll(allGroups);
            updateStats();
            updateTable();
        } catch (SQLException e) {
            System.err.println("[ERROR] SQL Error loading groups: " + e.getMessage());
            e.printStackTrace();
            // Set empty data so UI still shows even if DB fails
            allGroups.clear();
            filtered.clear();
            updateStats();
            updateTable();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des groupes: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error loading groups: " + e.getMessage());
            e.printStackTrace();
            allGroups.clear();
            filtered.clear();
            updateStats();
            updateTable();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur inattendue: " + e.getMessage());
        }
    }

    private void updateStats() {
        int total = allGroups.size();
        int active = (int) allGroups.stream()
                .filter(g -> "Actif".equalsIgnoreCase(g.getStatut()))
                .count();
        double avgMembers = allGroups.isEmpty() ? 0 : allGroups.stream()
                .mapToInt(Groupe::getNbreMembre)
                .average()
                .orElse(0);
        int totalMembers = allGroups.stream()
                .mapToInt(Groupe::getNbreMembre)
                .sum();

        totalGroupsLabel.setText(String.valueOf(total));
        activeGroupsLabel.setText(String.valueOf(active));
        //totalMembersLabel.setText(String.valueOf(totalMembers));       // ✅ total members
        avgMembersLabel.setText(String.format("%.1f", avgMembers)); // ✅ avg members
    }

    private void updateTable() {
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filtered.size());
        ObservableList<Groupe> page = FXCollections.observableArrayList(
                filtered.subList(start, end)
        );
        groupTable.setItems(page);
        pageLabel.setText(String.valueOf(currentPage));
        footerLabel.setText(String.format("Affichage de %d-%d sur %d entrées",
                filtered.isEmpty() ? 0 : start + 1, end, filtered.size()));
        resultCountLabel.setText(filtered.size() + " résultat(s)");
    }

    // ── Search ─────────────────────────────────────────────────
    private void setupSearch() {
        searchField.textProperty().addListener((o, old, neu) -> applyFilters());
    }

    // ── Filters ────────────────────────────────────────────────
    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("Actif", "Inactif"));
        statusFilter.valueProperty().addListener((o, old, neu) -> applyFilters());
    }

    private void applyFilters() {
        currentPage = 1;
        String search = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();

        filtered.setAll(allGroups.stream()
                .filter(g -> g.getNom().toLowerCase().contains(search))
                .filter(g -> status == null || status.isEmpty() || g.getStatut().equalsIgnoreCase(status))
                .collect(Collectors.toList()));

        resultCountLabel.setText(filtered.size() + " résultat(s)");
        updateTable();
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        statusFilter.setValue(null);
        currentPage = 1;
        filtered.setAll(allGroups);
        updateTable();
    }

    // ── Pagination ─────────────────────────────────────────────
    @FXML
    private void handlePrev() {
        if (currentPage > 1) {
            currentPage--;
            updateTable();
        }
    }

    @FXML
    private void handleNext() {
        int maxPage = (int) Math.ceil((double) filtered.size() / PAGE_SIZE);
        if (currentPage < maxPage) {
            currentPage++;
            updateTable();
        }
    }

    // ── Actions ────────────────────────────────────────────────
    @FXML
    private void handleAdd() {
        showGroupeDialog(null);
    }

    private void handleEdit(Groupe groupe) {
        showGroupeDialog(groupe);
    }

    private void handlePropositions(Groupe groupe) {
        try {
            System.out.println("[DEBUG] Loading propositions view for groupe: " + groupe.getNom());
            
            // Load the PropositionReunion view
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/backoffice/group/PropositionReunion.fxml")
            );
            if (loader.getLocation() == null) {
                System.err.println("[ERROR] FXML file not found at /backoffice/group/PropositionReunion.fxml");
            }
            
            javafx.scene.Parent view = loader.load();
            System.out.println("[DEBUG] FXML loaded successfully");
            
            PropositionReunionController controller = loader.getController();
            System.out.println("[DEBUG] Controller obtained: " + (controller != null ? "OK" : "NULL"));
            
            controller.setCurrentGroupe(groupe);
            controller.setParentController(this);
            controller.setSidebarController(sidebarController);
            controller.setContentArea(contentArea);
            
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
                System.out.println("[DEBUG] View set to contentArea");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error loading propositions: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des propositions:\n" + e.getMessage());
        }
    }

    public void showGroupsView() {
        loadData();
    }

    private void handleDelete(Groupe groupe) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer « " + groupe.getNom() + " » ?");
        confirm.setContentText("Cette action est irréversible.");
        styleAlert(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    groupeService.delete(groupe.getId());
                    loadData();
                    showToast("Groupe supprimé avec succès");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    // ── Dialog for Add / Edit ──────────────────────────────────
    private void showGroupeDialog(Groupe existing) {
        boolean isEdit = existing != null;

        Dialog<Groupe> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier le groupe" : "Nouveau groupe");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #111318; -fx-border-color: #1e2130; -fx-border-width: 1;");
        pane.getButtonTypes().addAll(
                new ButtonType(isEdit ? "Enregistrer" : "Créer", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );

        // Style dialog buttons
        Button okBtn = (Button) pane.lookupButton(pane.getButtonTypes().get(0));
        okBtn.setStyle("-fx-background-color: #00e5c8; -fx-text-fill: #0d0f14; "
                     + "-fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 8 20;");

        Button cancelBtn = (Button) pane.lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle("-fx-background-color: #1e2130; -fx-text-fill: #6b7394; "
                         + "-fx-background-radius: 8; -fx-border-color: #272c3d; "
                         + "-fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 8 20;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(24, 28, 16, 28));
        grid.setStyle("-fx-background-color: #111318;");

        TextField nomField    = dialogField("ex: Groupe A");
        TextField projetField = dialogField("ex: Application Web");
        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.setItems(FXCollections.observableArrayList("Actif", "Inactif"));
        statutCombo.setPrefWidth(260);
        statutCombo.setStyle(dialogFieldStyle());
        
        Spinner<Integer> membersSpinner = new Spinner<>(1, 20, 1);
        membersSpinner.setPrefWidth(260);
        membersSpinner.setStyle(dialogFieldStyle());
        
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description du groupe...");
        descArea.setPrefRowCount(3);
        descArea.setStyle(dialogFieldStyle());
        descArea.setWrapText(true);

        if (isEdit) {
            nomField.setText(existing.getNom());
            projetField.setText(nvl(existing.getProjet(), ""));
            statutCombo.setValue(existing.getStatut());
            membersSpinner.getValueFactory().setValue(existing.getNbreMembre());
            descArea.setText(nvl(existing.getDescription(), ""));
        }

        grid.add(dialogLabel("Nom du groupe *"),   0, 0); grid.add(nomField,     1, 0);
        grid.add(dialogLabel("Projet *"),          0, 1); grid.add(projetField,  1, 1);
        grid.add(dialogLabel("Nombre de membres"), 0, 2); grid.add(membersSpinner, 1, 2);
        grid.add(dialogLabel("Statut"),            0, 3); grid.add(statutCombo,  1, 3);
        grid.add(dialogLabel("Description"),       0, 4); grid.add(descArea,     1, 4);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Groupe g = isEdit ? existing : new Groupe();
                g.setNom(nomField.getText().trim());
                g.setProjet(projetField.getText().trim());
                g.setNbreMembre(membersSpinner.getValue());
                g.setStatut(statutCombo.getValue() != null ? statutCombo.getValue() : "Actif");
                g.setDescription(descArea.getText().trim());
                return g;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(g -> {
            if (g.getNom().isEmpty() || g.getProjet().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champs requis", "Le nom et projet sont obligatoires.");
                return;
            }
            
            // Validation: Nom doit avoir minimum 3 caractères
            if (g.getNom().length() < 3) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le nom doit contenir au moins 3 caractères.");
                return;
            }
            
            // Validation: Projet doit commencer par une majuscule
            if (!g.getProjet().isEmpty() && !Character.isUpperCase(g.getProjet().charAt(0))) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le projet doit commencer par une majuscule.");
                return;
            }
            
            // Validation: Description minimum 10 caractères
            if (g.getDescription() != null && g.getDescription().length() > 0 && g.getDescription().length() < 10) {
                showAlert(Alert.AlertType.WARNING, "Validation", "La description doit contenir au moins 10 caractères.");
                return;
            }
            
            try {
                if (isEdit) {
                    groupeService.edit(g);
                    showToast("Groupe modifié avec succès");
                } else {
                    groupeService.add(g);
                    showToast("Groupe créé avec succès");
                }
                loadData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        });
    }

    // ── Animation ──────────────────────────────────────────────
    private void animateEntrance() {
        FadeTransition fade = new FadeTransition(Duration.millis(300), groupTable);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    // ── Utilities ──────────────────────────────────────────────
    private static String nvl(String s, String fallback) {
        return (s != null && !s.isBlank()) ? s : fallback;
    }

    private void showToast(String message) {
        System.out.println("[TOAST] " + message);
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

    private TextField dialogField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(260);
        tf.setStyle(dialogFieldStyle());
        return tf;
    }

    private String dialogFieldStyle() {
        return "-fx-background-color: #161921; -fx-border-color: #1e2130; "
             + "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; "
             + "-fx-text-fill: #c8cfe8; -fx-prompt-text-fill: #3a4060; "
             + "-fx-font-size: 13px; -fx-padding: 8 12;";
    }

    private Label dialogLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #8a91b5; -fx-font-size: 12px; -fx-font-weight: 600;");
        return lbl;
    }
}

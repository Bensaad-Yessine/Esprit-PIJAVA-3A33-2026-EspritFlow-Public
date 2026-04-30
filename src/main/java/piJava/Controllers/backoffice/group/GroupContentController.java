package piJava.Controllers.backoffice.group;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import piJava.Controllers.backoffice.SidebarController;
import piJava.entities.Groupe;
import piJava.services.GroupeService;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class GroupContentController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button    addGroupBtn;

    @FXML private Label totalGroupsLabel;
    @FXML private Label activeGroupsLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label coveredSubjectsLabel;

    @FXML private ComboBox<String> statusFilter;
    @FXML private Button           resetFilterBtn;
    @FXML private Label           resultCountLabel;

    @FXML private ListView<Groupe> groupsListView;

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupListView();
        loadData();
        setupSearch();
        setupFilters();
    }

    private void setupListView() {
        groupsListView.setCellFactory(param -> new ListCell<Groupe>() {
            @Override
            protected void updateItem(Groupe groupe, boolean empty) {
                super.updateItem(groupe, empty);
                if (empty || groupe == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(createGroupCard(groupe));
            }
        });
    }

    private VBox createGroupCard(Groupe groupe) {
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
        priorityBar.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 0% 0%, #3B82F6, #1D4ED8); "
                            + "-fx-pref-width: 6; -fx-background-radius: 3;");

        VBox info = new VBox();
        info.setSpacing(4);

        Label name = new Label(groupe.getNom());
        name.setStyle("-fx-text-fill: #F3F4F6; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label project = new Label(nvl(groupe.getProjet(), "—"));
        project.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 13px;");

        info.getChildren().addAll(name, project);
        header.getChildren().addAll(priorityBar, info);

        HBox statsRow = new HBox();
        statsRow.setSpacing(20);

        VBox membersBox = new VBox();
        membersBox.setSpacing(2);
        Label membersLabel = new Label("Membres");
        membersLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        Label membersValue = new Label(String.valueOf(groupe.getNbreMembre()));
        membersValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #60A5FA;");
        membersBox.getChildren().addAll(membersLabel, membersValue);

        VBox descBox = new VBox();
        descBox.setSpacing(2);
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        Label descValue = new Label(nvl(groupe.getDescription(), "—"));
        descValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #CBD5E1; -fx-wrap-text: true;");
        descBox.getChildren().addAll(descLabel, descValue);

        VBox statusBox = new VBox();
        statusBox.setSpacing(2);
        Label statusLabel = new Label("Statut");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        Label statusValue = new Label(nvl(groupe.getStatut(), "—"));
        String statusColor = "Actif".equalsIgnoreCase(groupe.getStatut()) ? 
                            "linear-gradient(from 0% 0% to 100% 100%, #10B981, #059669)" :
                            "linear-gradient(from 0% 0% to 100% 100%, #6B7280, #4B5563)";
        statusValue.setStyle("-fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold; "
                           + "-fx-text-fill: #FFFFFF; -fx-background-color: " + statusColor + ";");
        statusBox.getChildren().addAll(statusLabel, statusValue);

        statsRow.getChildren().addAll(membersBox, descBox, statusBox);

        HBox actions = new HBox();
        actions.setSpacing(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("✎ Modifier");
        editBtn.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #F59E0B, #D97706); "
                        + "-fx-text-fill: #FFFFFF; -fx-background-radius: 8; -fx-padding: 8 16; "
                        + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
        editBtn.setOnAction(e -> handleEdit(groupe));

        Button proposBtn = new Button("📋 Propositions");
        proposBtn.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3B82F6, #2563EB); "
                          + "-fx-text-fill: #FFFFFF; -fx-background-radius: 8; -fx-padding: 8 16; "
                          + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
        proposBtn.setOnAction(e -> handlePropositions(groupe));

        Button delBtn = new Button("🗑 Supprimer");
        delBtn.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #EF4444, #DC2626); "
                      + "-fx-text-fill: #FFFFFF; -fx-background-radius: 8; -fx-padding: 8 16; "
                      + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
        delBtn.setOnAction(e -> handleDelete(groupe));

        actions.getChildren().addAll(editBtn, proposBtn, delBtn);

        card.getChildren().addAll(header, statsRow, actions);

        return card;
    }

    private void loadData() {
        try {
            System.out.println("[DEBUG] Loading groups data...");
            allGroups.setAll(groupeService.getAll());
            System.out.println("[DEBUG] Loaded " + allGroups.size() + " groups");
            filtered.setAll(allGroups);
            updateStats();
            updateList();
        } catch (SQLException e) {
            System.err.println("[ERROR] SQL Error loading groups: " + e.getMessage());
            e.printStackTrace();
            allGroups.clear();
            filtered.clear();
            updateStats();
            updateList();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des groupes: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error loading groups: " + e.getMessage());
            e.printStackTrace();
            allGroups.clear();
            filtered.clear();
            updateStats();
            updateList();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur inattendue: " + e.getMessage());
        }
    }

    private void updateStats() {
        int total = allGroups.size();
        int active = (int) allGroups.stream().filter(g -> "Actif".equalsIgnoreCase(g.getStatut())).count();
        double avgMembers = allGroups.isEmpty() ? 0 : allGroups.stream()
                .mapToInt(Groupe::getNbreMembre)
                .average()
                .orElse(0);

        totalGroupsLabel.setText(String.valueOf(total));
        activeGroupsLabel.setText(String.valueOf(active));
        avgMembersLabel.setText(String.format("%.1f", avgMembers));
    }

    private void updateList() {
        groupsListView.setItems(filtered);
        footerCountLabel.setText(filtered.size() + " groupes");
    }

    private void setupSearch() {
        searchField.textProperty().addListener((o, old, neu) -> applyFilters());
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("Actif", "Inactif"));
        statusFilter.valueProperty().addListener((o, old, neu) -> applyFilters());
    }

    private void applyFilters() {
        String search = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();

        filtered.setAll(allGroups.stream()
                .filter(g -> g.getNom().toLowerCase().contains(search))
                .filter(g -> status == null || status.isEmpty() || g.getStatut().equalsIgnoreCase(status))
                .collect(Collectors.toList()));

        updateList();
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        statusFilter.setValue(null);
        filtered.setAll(allGroups);
        updateList();
    }

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
        if (contentArea != null) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/backoffice/group/GroupContent.fxml")
                );
                javafx.scene.Parent view = loader.load();
                
                GroupContentController controller = loader.getController();
                controller.setContentArea(contentArea);
                controller.setSidebarController(sidebarController);
                
                contentArea.getChildren().setAll(view);
            } catch (Exception e) {
                System.err.println("[ERROR] Error navigating back: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            loadData();
        }
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
            
            if (g.getNom().length() < 3) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le nom doit contenir au moins 3 caractères.");
                return;
            }
            
            if (!g.getProjet().isEmpty() && !Character.isUpperCase(g.getProjet().charAt(0))) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le projet doit commencer par une majuscule.");
                return;
            }
            
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
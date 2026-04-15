package piJava.Controllers.frontoffice.group;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GroupContentController implements Initializable {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/pidev";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @FXML private TextField searchField;
    @FXML private Button addGroupBtn;
    @FXML private Label totalGroupsLabel;
    @FXML private Label activeGroupsLabel;
    @FXML private Label pendingGroupsLabel;
    @FXML private VBox groupsContainer;
    @FXML private Label footerLabel;

    private List<GroupRecord> userGroups = new ArrayList<>();
    private Object sidebarController;
    private StackPane contentArea;

    public void setSidebarController(Object sidebarController) {
        this.sidebarController = sidebarController;
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupSearch();
        loadData();
    }

    @FXML
    private void handleAdd() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau groupe");
        dialog.setHeaderText("Creer un nouveau groupe de projet");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom du groupe");
        nomField.setPrefWidth(300);

        TextField projetField = new TextField();
        projetField.setPromptText("Nom du projet");
        projetField.setPrefWidth(300);

        Spinner<Integer> membersSpinner = new Spinner<>(1, 20, 1);
        membersSpinner.setPrefWidth(300);

        TextArea descField = new TextArea();
        descField.setPromptText("Description du groupe");
        descField.setPrefWidth(300);
        descField.setPrefHeight(80);
        descField.setWrapText(true);

        form.add(new Label("Nom du groupe:"), 0, 0);
        form.add(nomField, 1, 0);
        form.add(new Label("Projet:"), 0, 1);
        form.add(projetField, 1, 1);
        form.add(new Label("Membres:"), 0, 2);
        form.add(membersSpinner, 1, 2);
        form.add(new Label("Description:"), 0, 3);
        form.add(descField, 1, 3);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }

            String nom = nomField.getText() == null ? "" : nomField.getText().trim();
            String projet = projetField.getText() == null ? "" : projetField.getText().trim();
            String description = descField.getText() == null ? "" : descField.getText().trim();

            if (nom.isEmpty() || projet.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le nom et le projet sont obligatoires.");
                return;
            }

            try (Connection connection = openConnection()) {
                int groupId = insertGroup(connection, nom, projet, membersSpinner.getValue(), description);
                Integer currentUserId = resolveCurrentUserId();
                if (currentUserId != null) {
                    addUserToGroup(connection, groupId, currentUserId);
                }
                loadData();
                showAlert(Alert.AlertType.INFORMATION, "Succes", "Groupe cree avec succes.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la creation: " + e.getMessage());
            }
        });
    }

    private void loadData() {
        Integer currentUserId = resolveCurrentUserId();
        if (currentUserId == null) {
            userGroups = new ArrayList<>();
            updateStats();
            displayGroups(userGroups);
            showAlert(Alert.AlertType.WARNING, "Erreur", "Aucun utilisateur connecte.");
            return;
        }

        List<GroupRecord> loaded = new ArrayList<>();
        String sql = "SELECT g.id, g.nom_projet, g.matiere, g.nbr_membre, g.statut, g.description " +
                "FROM groupe_projet g " +
                "INNER JOIN groupe_projet_user ug ON g.id = ug.groupe_projet_id " +
                "WHERE ug.user_id = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    loaded.add(new GroupRecord(
                            resultSet.getInt("id"),
                            resultSet.getString("nom_projet"),
                            resultSet.getString("matiere"),
                            resultSet.getInt("nbr_membre"),
                            resultSet.getString("statut"),
                            resultSet.getString("description")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        userGroups = loaded;
        updateStats();
        displayGroups(userGroups);
    }

    private void updateStats() {
        int total = userGroups.size();
        int active = (int) userGroups.stream().filter(g -> isActiveStatus(g.statut)).count();
        int pending = total - active;

        totalGroupsLabel.setText(String.valueOf(total));
        activeGroupsLabel.setText(String.valueOf(active));
        pendingGroupsLabel.setText(String.valueOf(pending));
        footerLabel.setText(total + " groupes");
    }

    private void displayGroups(List<GroupRecord> groups) {
        groupsContainer.getChildren().clear();
        for (GroupRecord groupe : groups) {
            groupsContainer.getChildren().add(createGroupCard(groupe));
        }
    }

    private VBox createGroupCard(GroupRecord groupe) {
        VBox card = new VBox();
        card.setSpacing(18);
        card.setPadding(new Insets(22));
        card.setPrefWidth(400);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 24; -fx-border-color: rgba(255,255,255,0.75); -fx-border-radius: 24; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 16, 0, 0, 8);");

        HBox header = new HBox();
        header.setSpacing(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(50, 50);
        iconBadge.setStyle("-fx-background-color: linear-gradient(to right, #ef4444, #be123c); -fx-background-radius: 16;");

        Label icon = new Label("G");
        icon.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: white;");
        iconBadge.getChildren().add(icon);

        VBox titleBox = new VBox();
        titleBox.setSpacing(4);
        Label nameLabel = new Label(nvl(groupe.nom, "Groupe"));
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        Label projetLabel = new Label(nvl(groupe.projet, "Projet"));
        projetLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        titleBox.getChildren().addAll(nameLabel, projetLabel);

        Label topBadge = new Label(isActiveStatus(groupe.statut) ? "Actif" : nvl(groupe.statut, "Inactif"));
        topBadge.setStyle(isActiveStatus(groupe.statut)
                ? "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-background-radius: 999; -fx-padding: 6 12; -fx-font-size: 11px; -fx-font-weight: 800;"
                : "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-background-radius: 999; -fx-padding: 6 12; -fx-font-size: 11px; -fx-font-weight: 800;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(iconBadge, titleBox, spacer, topBadge);

        HBox infoRow = new HBox();
        infoRow.setSpacing(14);
        VBox membersBox = createInfoBox("Membres", String.valueOf(groupe.nbMembres));
        VBox projectBox = createInfoBox("Projet", nvl(groupe.projet, "-"));
        HBox.setHgrow(membersBox, Priority.ALWAYS);
        HBox.setHgrow(projectBox, Priority.ALWAYS);
        infoRow.getChildren().addAll(membersBox, projectBox);

        Label descLabel = new Label(nvl(groupe.description, "Aucune description"));
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-line-spacing: 3px;");
        descLabel.setWrapText(true);

        HBox actionRow = new HBox();
        actionRow.setSpacing(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        Button viewBtn = new Button("Voir details");
        viewBtn.setStyle("-fx-background-color: linear-gradient(to right, #ef4444, #b91c1c); -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 12px; -fx-padding: 10 18 10 18; -fx-background-radius: 14; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> handleView(groupe));
        actionRow.getChildren().add(viewBtn);

        card.getChildren().addAll(header, infoRow, descLabel, actionRow);
        return card;
    }

    private VBox createInfoBox(String title, String value) {
        VBox box = new VBox();
        box.setSpacing(4);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #fff7f7, #ffe9ea); -fx-background-radius: 18;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 700;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #111827; -fx-font-weight: 800;");
        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    private void setupSearch() {
        searchField.textProperty().addListener((o, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        if (search.isEmpty()) {
            displayGroups(userGroups);
            return;
        }

        List<GroupRecord> filtered = userGroups.stream()
                .filter(g -> nvl(g.nom, "").toLowerCase().contains(search) || nvl(g.projet, "").toLowerCase().contains(search))
                .collect(Collectors.toList());
        displayGroups(filtered);
    }

    private void handleView(GroupRecord groupe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/group/group-details.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            Method method = controller.getClass().getMethod("setGroupData", int.class, String.class, String.class, int.class, String.class, String.class);
            method.invoke(controller, groupe.id, groupe.nom, groupe.projet, groupe.nbMembres, groupe.statut, groupe.description);

            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.setTitle("Details du groupe - " + nvl(groupe.nom, "Groupe"));
            stage.setScene(new Scene(root, 1100, 760));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les details du groupe: " + e.getMessage());
        }
    }

    private Connection openConnection() throws Exception {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private int insertGroup(Connection connection, String nom, String projet, int nbMembres, String description) throws Exception {
        String sql = "INSERT INTO groupe_projet (nom_projet, matiere, nbr_membre, statut, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, nom);
            statement.setString(2, projet);
            statement.setInt(3, nbMembres);
            statement.setString(4, "Actif");
            statement.setString(5, description);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new IllegalStateException("Impossible de recuperer l'id du groupe cree.");
    }

    private void addUserToGroup(Connection connection, int groupId, int userId) throws Exception {
        String sql = "INSERT INTO groupe_projet_user (groupe_projet_id, user_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    private Integer resolveCurrentUserId() {
        try {
            Class<?> sessionManagerClass = Class.forName("piJava.utils.SessionManager");
            Method getInstance = sessionManagerClass.getMethod("getInstance");
            Object sessionManager = getInstance.invoke(null);
            Method getCurrentUser = sessionManagerClass.getMethod("getCurrentUser");
            Object currentUser = getCurrentUser.invoke(sessionManager);
            if (currentUser == null) {
                return null;
            }
            Method getId = currentUser.getClass().getMethod("getId");
            Object id = getId.invoke(currentUser);
            return id instanceof Integer ? (Integer) id : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isActiveStatus(String status) {
        return status != null && status.equalsIgnoreCase("Actif");
    }

    private static String nvl(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static final class GroupRecord {
        private final int id;
        private final String nom;
        private final String projet;
        private final int nbMembres;
        private final String statut;
        private final String description;

        private GroupRecord(int id, String nom, String projet, int nbMembres, String statut, String description) {
            this.id = id;
            this.nom = nom;
            this.projet = projet;
            this.nbMembres = nbMembres;
            this.statut = statut;
            this.description = description;
        }
    }
}

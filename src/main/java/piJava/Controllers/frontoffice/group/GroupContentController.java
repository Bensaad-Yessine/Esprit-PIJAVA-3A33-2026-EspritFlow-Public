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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import piJava.entities.Groupe;
import piJava.Controllers.frontoffice.group.FrontPropositionReunionController;
import piJava.Controllers.group.GroupChatClient;
import piJava.Controllers.group.GroupChatListener;
import piJava.Controllers.group.GroupChatMessage;
import piJava.Controllers.group.GroupChatServer;

public class GroupContentController implements Initializable {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/pidev";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @FXML private TextField searchField;
    @FXML private Button addGroupBtn;
    @FXML private Button invitationsBtn;
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
        updateInvitationBadge();
    }

    @FXML
    private void handleAdd() {
        ensureGroupTables();
        Integer currentUserId = resolveCurrentUserId();
        if (currentUserId == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Aucun utilisateur connecte.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau groupe");
        dialog.setHeaderText("Creer un nouveau groupe de projet");
        styleDialog(dialog.getDialogPane());

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom du groupe");
        nomField.setPrefWidth(300);
        styleField(nomField);

        TextField projetField = new TextField();
        projetField.setPromptText("Nom du projet");
        projetField.setPrefWidth(300);
        styleField(projetField);

        Spinner<Integer> membersSpinner = new Spinner<>(1, 20, 1);
        membersSpinner.setPrefWidth(300);

        TextArea descField = new TextArea();
        descField.setPromptText("Description du groupe");
        descField.setPrefWidth(300);
        descField.setPrefHeight(80);
        descField.setWrapText(true);
        styleArea(descField);

        ListView<SelectableUser> inviteesList = new ListView<>();
        inviteesList.setPrefHeight(150);
        inviteesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        inviteesList.setPlaceholder(new Label("Aucun utilisateur disponible"));
        inviteesList.setCellFactory(param -> new ListCell<SelectableUser>() {
            @Override
            protected void updateItem(SelectableUser item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.displayName);
            }
        });

        try (Connection connection = openConnection()) {
            inviteesList.getItems().setAll(loadInvitableUsers(connection, currentUserId));
        } catch (Exception e) {
            e.printStackTrace();
        }

        form.add(new Label("Nom du groupe:"), 0, 0);
        form.add(nomField, 1, 0);
        form.add(new Label("Projet:"), 0, 1);
        form.add(projetField, 1, 1);
        form.add(new Label("Membres:"), 0, 2);
        form.add(membersSpinner, 1, 2);
        form.add(new Label("Description:"), 0, 3);
        form.add(descField, 1, 3);
        form.add(new Label("Inviter des membres:"), 0, 4);
        form.add(inviteesList, 1, 4);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        styleDialogButtons(dialog.getDialogPane());

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
                int groupId = insertGroup(connection, nom, projet, membersSpinner.getValue(), description, currentUserId);
                addUserToGroup(connection, groupId, currentUserId);

                List<Integer> selectedInvitees = inviteesList.getSelectionModel().getSelectedItems()
                        .stream()
                        .map(user -> user.id)
                        .collect(Collectors.toList());
                int invitedCount = createInvitations(connection, groupId, currentUserId, selectedInvitees);

                loadData();
                updateInvitationBadge();
                showAlert(Alert.AlertType.INFORMATION, "Succes",
                        "Groupe cree avec succes.\nInvitations envoyees: " + invitedCount);
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
        String sqlWithCreator = "SELECT g.id, g.nom_projet, g.matiere, g.nbr_membre, g.statut, g.description, g.creator_user_id " +
                "FROM groupe_projet g " +
                "INNER JOIN groupe_projet_user ug ON g.id = ug.groupe_projet_id " +
                "WHERE ug.user_id = ?";
        String sqlFallback = "SELECT g.id, g.nom_projet, g.matiere, g.nbr_membre, g.statut, g.description " +
                "FROM groupe_projet g " +
                "INNER JOIN groupe_projet_user ug ON g.id = ug.groupe_projet_id " +
                "WHERE ug.user_id = ?";

        try (Connection connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sqlWithCreator)) {
                statement.setInt(1, currentUserId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        loaded.add(new GroupRecord(
                                resultSet.getInt("id"),
                                resultSet.getString("nom_projet"),
                                resultSet.getString("matiere"),
                                resultSet.getInt("nbr_membre"),
                                resultSet.getString("statut"),
                                resultSet.getString("description"),
                                resultSet.getObject("creator_user_id") != null ? resultSet.getInt("creator_user_id") : null
                        ));
                    }
                }
            } catch (Exception schemaException) {
                try (PreparedStatement fallbackStatement = connection.prepareStatement(sqlFallback)) {
                    fallbackStatement.setInt(1, currentUserId);
                    try (ResultSet resultSet = fallbackStatement.executeQuery()) {
                        while (resultSet.next()) {
                            loaded.add(new GroupRecord(
                                    resultSet.getInt("id"),
                                    resultSet.getString("nom_projet"),
                                    resultSet.getString("matiere"),
                                    resultSet.getInt("nbr_membre"),
                                    resultSet.getString("statut"),
                                    resultSet.getString("description"),
                                    null
                            ));
                        }
                    }
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

        FlowPane actionRow = new FlowPane();
        actionRow.setHgap(10);
        actionRow.setVgap(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Integer currentUserId = resolveCurrentUserId();
        Button viewBtn = new Button("Voir details");
        viewBtn.setStyle("-fx-background-color: linear-gradient(to right, #ef4444, #b91c1c); -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 12px; -fx-padding: 10 18 10 18; -fx-background-radius: 14; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> handleView(groupe));

        if (currentUserId != null && currentUserId.equals(groupe.creatorUserId)) {
            Button inviteBtn = new Button("➕ Inviter membres");
            inviteBtn.setStyle("-fx-background-color: linear-gradient(to right, #f59e0b, #d97706); -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 12px; -fx-padding: 10 18 10 18; -fx-background-radius: 14; -fx-cursor: hand;");
            inviteBtn.setOnAction(e -> handleInviteMembers(groupe));
            actionRow.getChildren().add(inviteBtn);
        }

        Button discussionBtn = new Button("💬 Discussion");
        discussionBtn.setStyle("-fx-background-color: linear-gradient(to right, #0ea5e9, #2563eb); -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 12px; -fx-padding: 10 18 10 18; -fx-background-radius: 14; -fx-cursor: hand;");
        discussionBtn.setOnAction(e -> handleOpenDiscussion(groupe));
        
        // Nouveau bouton pour les propositions de réunion (SYSTÈME DE VOTE)
        // Nouveau bouton pour les propositions de réunion (SYSTÈME DE VOTE)
        Button propositionsBtn = new Button("📋 Propositions Réunion");
        propositionsBtn.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #1d4ed8); -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 12px; -fx-padding: 10 18 10 18; -fx-background-radius: 14; -fx-cursor: hand;");
        propositionsBtn.setOnAction(e -> handleShowPropositions(groupe));
        
        // Groq AI Generate button
        Button aiBtn = new Button("🤖 Générer IA");
        aiBtn.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #7c3aed); -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 12px; -fx-padding: 10 18 10 18; -fx-background-radius: 14; -fx-cursor: hand;");
        aiBtn.setOnAction(e -> handleGenerateAIWithInput(groupe));
        
        actionRow.getChildren().add(viewBtn);
        actionRow.getChildren().add(discussionBtn);
        actionRow.getChildren().add(propositionsBtn);
        actionRow.getChildren().add(aiBtn);

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

    private void handleOpenDiscussion(GroupRecord groupe) {
        final String defaultHost = "localhost";
        final int defaultPort = 5050;
        String fullName = piJava.utils.SessionManager.getInstance().getFullName();
        final String defaultUsername = (fullName != null && !fullName.trim().isEmpty()) ? fullName : "Membre-" + (resolveCurrentUserId() != null ? resolveCurrentUserId() : "inv");

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Discussion du groupe - " + nvl(groupe.nom, "Groupe"));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #fff6f7, #ffecef);");

        Label titleLabel = new Label("Discussion en direct");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #be123c;");

        Label subtitleLabel = new Label("Groupe: " + nvl(groupe.nom, "Groupe"));
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #64748b;");

        ListView<String> messagesView = new ListView<>();
        messagesView.setPrefSize(550, 450);
        messagesView.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16; -fx-border-color: #fecdd3; -fx-border-radius: 16; -fx-padding: 6;");
        messagesView.setCellFactory(param -> new javafx.scene.control.ListCell<String>() {
            private final javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
            {
                imageView.setFitHeight(180);
                imageView.setFitWidth(300);
                imageView.setPreserveRatio(true);
                setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
                setGraphicTextGap(10);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item.contains("[GIF] ")) {
                        int urlIndex = item.indexOf("[GIF] ") + 6;
                        String url = item.substring(urlIndex).trim();
                        String textPrefix = item.substring(0, item.indexOf("[GIF] ")); 
                        
                        setText(textPrefix + "\n⏳ Chargement du GIF..."); 
                        setGraphic(null);
                        
                        new Thread(() -> {
                            javafx.scene.image.Image img = piJava.utils.GiphyService.fetchImage(url);
                            Platform.runLater(() -> {
                                if (img != null && !img.isError()) {
                                    setText(textPrefix);
                                    imageView.setImage(img);
                                    setGraphic(imageView);
                                } else {
                                    setText(textPrefix + "\n❌ Impossible de charger le GIF");
                                }
                            });
                        }).start();
                    } else {
                        setText(item);
                        setGraphic(null);
                    }
                }
            }
        });

        // Load chat history via API
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8087/api/messages/group/" + groupe.id))
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                com.google.gson.Gson gson = new com.google.gson.GsonBuilder().registerTypeAdapter(java.time.LocalDateTime.class, new com.google.gson.JsonDeserializer<java.time.LocalDateTime>() {
                    @Override
                    public java.time.LocalDateTime deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
                        return java.time.LocalDateTime.parse(json.getAsString());
                    }
                }).create();
                piJava.entities.ChatMessage[] history = gson.fromJson(response.body(), piJava.entities.ChatMessage[].class);
                if (history != null) {
                    for (piJava.entities.ChatMessage m : history) {
                        GroupChatMessage gcm = new GroupChatMessage(m.getGroupId(), m.getSender(), m.getContent(), m.getSentAt());
                        messagesView.getItems().add(gcm.getDisplayValue());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Erreur Historique", "Impossible de charger l'historique: " + e.getMessage());
            });
        }

        HBox sendRow = new HBox(8);
        TextField messageField = new TextField();
        messageField.setPromptText("Ecrivez un message au groupe...");
        HBox.setHgrow(messageField, Priority.ALWAYS);
        Button gifBtn = new Button("GIF");
        Button sendBtn = new Button("Envoyer");
        messageField.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-background-color: #ffffff; -fx-border-color: #fecdd3; -fx-padding: 9 12;");
        gifBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: 800; -fx-background-radius: 12; -fx-padding: 9 12; -fx-cursor: hand; -fx-border-color: #cbd5e1; -fx-border-radius: 12;");
        sendBtn.setStyle("-fx-background-color: linear-gradient(to right, #ef4444, #be123c); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 12; -fx-padding: 9 16; -fx-cursor: hand;");
        sendRow.getChildren().addAll(messageField, gifBtn, sendBtn);

        Label statusLabel = new Label("Chat hors ligne");
        statusLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-font-weight: 600;");
        root.getChildren().addAll(titleLabel, subtitleLabel, messagesView, sendRow, statusLabel);
        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().setStyle("-fx-background-color: #fff1f3;");

        GroupChatClient chatClient = new GroupChatClient();
        GroupChatListener listener = new GroupChatListener() {
            @Override
            public void onMessageReceived(GroupChatMessage message) {
                if (message.getGroupId() != groupe.id) {
                    return;
                }
                Platform.runLater(() -> messagesView.getItems().add(message.getDisplayValue()));
            }

            @Override
            public void onConnectionStatusChanged(String statusMessage) {
                Platform.runLater(() -> statusLabel.setText(statusMessage));
            }
        };

        Runnable connectAction = () -> {
            String host = defaultHost;
            int port = defaultPort;

            GroupChatServer.getInstance().start(port);
            try {
                chatClient.connect(host, port, listener);
                statusLabel.setText("Connecte au chat du groupe " + nvl(groupe.nom, "Groupe"));
                statusLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12px; -fx-font-weight: 700;");
            } catch (Exception e) {
                statusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 700;");
                showAlert(Alert.AlertType.ERROR, "Chat", "Connexion impossible: " + e.getMessage());
            }
        };

        Runnable sendAction = () -> {
            if (!chatClient.isConnected()) {
                showAlert(Alert.AlertType.WARNING, "Chat", "Connectez-vous au chat avant d'envoyer un message.");
                return;
            }
            String content = messageField.getText() == null ? "" : messageField.getText().trim();
            String username = defaultUsername;
            if (content.isBlank() || username.isBlank()) {
                return;
            }
            
            if (content.startsWith("/gif_url ")) {
                content = "[GIF] " + content.substring(9).trim();
            } else if (content.startsWith("/gif ")) {
                String query = content.substring(5).trim();
                String gifUrl = piJava.utils.GiphyService.getGifUrl(query);
                if (gifUrl != null) {
                    content = "[GIF] " + gifUrl;
                } else {
                    showAlert(Alert.AlertType.WARNING, "Giphy", "Aucun GIF trouve pour: " + query);
                    return;
                }
            }
            
            // Envoyer via API REST
            try {
                com.google.gson.Gson gson = new com.google.gson.GsonBuilder().registerTypeAdapter(java.time.LocalDateTime.class, new com.google.gson.JsonSerializer<java.time.LocalDateTime>() {
                    @Override
                    public com.google.gson.JsonElement serialize(java.time.LocalDateTime src, java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
                        return new com.google.gson.JsonPrimitive(src.toString());
                    }
                }).create();
                
                piJava.entities.ChatMessage msg = new piJava.entities.ChatMessage();
                msg.setGroupId(groupe.id);
                msg.setSender(username);
                Integer currentUserId = resolveCurrentUserId();
                msg.setSenderId(currentUserId != null ? currentUserId : 1);
                msg.setContent(content);
                
                String jsonBody = gson.toJson(msg);
                
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:8087/api/messages"))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();
                java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert(javafx.scene.control.Alert.AlertType.ERROR, "API Error", "Erreur: " + response.body());
                    });
                    return;
                }
                
                // Broadcast in real-time
                GroupChatMessage gcm = new GroupChatMessage(groupe.id, username, content);
                chatClient.sendMessage(gcm);
                
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Erreur d'envoi", e.getMessage());
                });
            }

            messageField.clear();
        };

        sendBtn.setOnAction(evt -> sendAction.run());
        messageField.setOnAction(evt -> sendAction.run());
        gifBtn.setOnAction(evt -> {
            Optional<String> chosenGif = showGifPicker();
            chosenGif.ifPresent(url -> {
                messageField.setText("/gif_url " + url);
                sendAction.run();
            });
        });

        dialog.setOnHidden(evt -> chatClient.disconnect());
        dialog.show();
        connectAction.run();
    }

    @FXML
    private void handleInvitations() {
        ensureGroupTables();
        Integer currentUserId = resolveCurrentUserId();
        if (currentUserId == null) {
            showAlert(Alert.AlertType.WARNING, "Invitations", "Aucun utilisateur connecte.");
            return;
        }

        ListView<PendingInvitation> invitationsView = new ListView<>();
        invitationsView.setStyle("-fx-background-color: transparent; -fx-border-color: #fecdd3; -fx-border-radius: 10; -fx-background-radius: 10; -fx-control-inner-background: #ffffff; -fx-selection-bar: #fee2e2; -fx-selection-bar-non-focused: #fee2e2;");
        invitationsView.setCellFactory(param -> new ListCell<PendingInvitation>() {
            @Override
            protected void updateItem(PendingInvitation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.displayValue);
                if (!empty) {
                    setStyle("-fx-padding: 12px 15px; -fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #334155; -fx-border-color: transparent transparent #f1f5f9 transparent; -fx-border-width: 1; -fx-background-color: transparent;");
                } else {
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        Button acceptBtn = new Button("✓ Accepter");
        acceptBtn.setStyle("-fx-background-color: linear-gradient(to right, #10b981, #059669); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 8 18; -fx-cursor: hand;");
        Button refuseBtn = new Button("✕ Refuser");
        refuseBtn.setStyle("-fx-background-color: linear-gradient(to right, #ef4444, #be123c); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 8 18; -fx-cursor: hand;");
        Button refreshBtn = new Button("↻ Actualiser");
        refreshBtn.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #475569; -fx-font-weight: 700; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-padding: 8 18; -fx-cursor: hand;");

        Runnable refreshAction = () -> {
            try (Connection connection = openConnection()) {
                invitationsView.getItems().setAll(loadPendingInvitations(connection, currentUserId));
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Invitations", "Erreur chargement invitations: " + e.getMessage());
            }
        };

        acceptBtn.setOnAction(event -> {
            PendingInvitation selected = invitationsView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try (Connection connection = openConnection()) {
                respondToInvitation(connection, selected.id, true);
                loadData();
                refreshAction.run();
                updateInvitationBadge();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Invitations", "Erreur acceptation: " + e.getMessage());
            }
        });

        refuseBtn.setOnAction(event -> {
            PendingInvitation selected = invitationsView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try (Connection connection = openConnection()) {
                respondToInvitation(connection, selected.id, false);
                refreshAction.run();
                updateInvitationBadge();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Invitations", "Erreur refus: " + e.getMessage());
            }
        });

        refreshBtn.setOnAction(event -> refreshAction.run());
        refreshAction.run();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Mes invitations de groupe");
        dialog.setHeaderText("Gérez vos invitations en attente");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        styleDialog(dialog.getDialogPane());
        styleDialogButtons(dialog.getDialogPane());

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        HBox actions = new HBox(10, acceptBtn, refuseBtn, refreshBtn);
        actions.setAlignment(javafx.geometry.Pos.CENTER);
        content.getChildren().addAll(invitationsView, actions);

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void handleInviteMembers(GroupRecord groupe) {
        ensureGroupTables();
        Integer currentUserId = resolveCurrentUserId();
        if (currentUserId == null) {
            showAlert(Alert.AlertType.WARNING, "Invitations", "Aucun utilisateur connecte.");
            return;
        }

        if (groupe.creatorUserId == null || !currentUserId.equals(groupe.creatorUserId)) {
            showAlert(Alert.AlertType.WARNING, "Invitations", "Seul le createur peut inviter des membres.");
            return;
        }

        ListView<SelectableUser> list = new ListView<>();
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        list.setPrefHeight(240);
        list.setStyle("-fx-background-color: transparent; -fx-border-color: #fecdd3; -fx-border-radius: 10; -fx-background-radius: 10; -fx-control-inner-background: #ffffff; -fx-selection-bar: #fee2e2; -fx-selection-bar-non-focused: #fee2e2;");
        list.setCellFactory(param -> new ListCell<SelectableUser>() {
            @Override
            protected void updateItem(SelectableUser item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.displayName);
                if (!empty) {
                    setStyle("-fx-padding: 10px 15px; -fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #334155; -fx-border-color: transparent transparent #f1f5f9 transparent; -fx-border-width: 1; -fx-background-color: transparent;");
                } else {
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        try (Connection connection = openConnection()) {
            list.getItems().setAll(loadInvitableUsersForGroup(connection, groupe.id, currentUserId));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Invitations", "Erreur chargement membres: " + e.getMessage());
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Inviter des membres");
        dialog.setHeaderText("Selectionnez les membres a inviter");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(list);
        styleDialog(dialog.getDialogPane());
        styleDialogButtons(dialog.getDialogPane());

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) {
                return;
            }
            List<Integer> selectedIds = list.getSelectionModel().getSelectedItems().stream()
                    .map(user -> user.id)
                    .collect(Collectors.toList());
            try (Connection connection = openConnection()) {
                int invitedCount = createInvitations(connection, groupe.id, currentUserId, selectedIds);
                updateInvitationBadge();
                showAlert(Alert.AlertType.INFORMATION, "Invitations", "Invitations envoyees: " + invitedCount);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Invitations", "Erreur envoi invitations: " + e.getMessage());
            }
        });
    }

    // ── SYSTÈME DE VOTE - PROPOSITIONS DE RÉUNION ──────────────
    private void handleShowPropositions(GroupRecord groupe) {
        final String fxmlPath = "/frontoffice/group/propositions-reunion.fxml";
        try {
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Erreur - ouverture Propositions Réunion",
                        "Impossible de trouver le fichier FXML.\n\n" +
                                "Chemin recherche: " + fxmlPath + "\n" +
                                "Cause probable: ressource manquante dans src/main/resources/frontoffice/group/"
                );
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            
            FrontPropositionReunionController controller = loader.getController();
            
            // Créer une instance Groupe et la passer au contrôleur
            piJava.entities.Groupe groupEntity = new piJava.entities.Groupe();
            groupEntity.setId(groupe.id);
            groupEntity.setNom(groupe.nom);
            groupEntity.setDescription(groupe.description);
            
            controller.setCurrentGroupe(groupEntity);

            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.setTitle("Propositions de Réunion - " + nvl(groupe.nom, "Groupe"));
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(
                    Alert.AlertType.ERROR,
                    "Erreur - ouverture Propositions Réunion",
                    buildDetailedErrorMessage(fxmlPath, e)
            );
        }
    }

    private String buildDetailedErrorMessage(String fxmlPath, Exception exception) {
        Throwable root = exception;
        while (root.getCause() != null) {
            root = root.getCause();
        }

        StringBuilder details = new StringBuilder();
        details.append("Impossible d'ouvrir l'ecran des propositions.\n\n");
        details.append("Point de defaillance: clic sur 'Propositions Réunion' d'un groupe\n");
        details.append("FXML vise: ").append(fxmlPath).append("\n");
        details.append("Type d'exception: ").append(exception.getClass().getSimpleName()).append("\n");
        details.append("Message: ").append(safeMessage(exception)).append("\n\n");
        details.append("Cause racine: ").append(root.getClass().getSimpleName()).append("\n");
        details.append("Message racine: ").append(safeMessage(root));
        return details.toString();
    }

    private String safeMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return "(aucun message detaille)";
        }
        return throwable.getMessage();
    }

    private void handleGenerateAI(GroupRecord groupe) {
        if (groupe == null) {
            showAlert(Alert.AlertType.WARNING, "Génération IA", "Groupe non valide.");
            return;
        }

        Alert loading = new Alert(Alert.AlertType.INFORMATION);
        loading.setTitle("Génération IA");
        loading.setHeaderText("Génération de proposition de réunion avec Groq...");
        loading.setContentText("Contact API...");
        loading.getButtonTypes().clear();
        ButtonType cancelBtn = new ButtonType("Annuler");
        loading.getButtonTypes().add(cancelBtn);
        loading.show();

        piJava.entities.PropositionReunion prop = piJava.utils.GroqService.generateProposition(
            nvl(groupe.nom, "Groupe"), 
            nvl(groupe.projet, "Projet"), 
            nvl(groupe.description, ""), 
            groupe.id
        );

        loading.close();

        if (prop != null) {
            try {
                piJava.services.PropositionReunionService service = new piJava.services.PropositionReunionService();
                service.add(prop);
                loadData(); // Refresh
                showAlert(Alert.AlertType.INFORMATION, "Succès IA", 
                    "Proposition IA créée !\n\n" +
                    "Titre: " + prop.getTitre() + "\n" +
                    "Date: " + prop.getDateReunion() + " " + prop.getHeureDebut() + "-" + prop.getHeureFin() + "\n" +
                    "Lieu: " + prop.getLieu());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur DB", "IA OK mais DB fail: " + e.getMessage());
            }
        } else {
            String details = piJava.utils.GroqService.getLastError();
            showAlert(Alert.AlertType.WARNING, "Echec IA",
                    "Impossible de generer la proposition IA.\n\n" +
                            (details.isBlank() ? "Verifiez internet et reessayez." : details));
            if (false && details == null) {
            showAlert(Alert.AlertType.WARNING, "Échec IA", 
                "Impossible de générer (API/JSON fail).\nVérifiez internet et réessayez.");
        }
    }

        }

    private void handleGenerateAIWithInput(GroupRecord groupe) {
        if (groupe == null) {
            showAlert(Alert.AlertType.WARNING, "Generation IA", "Groupe non valide.");
            return;
        }

        Optional<String> instructionResult = askAiMeetingInput(groupe);
        if (instructionResult.isEmpty()) {
            return;
        }
        String instruction = instructionResult.get();

        Alert loading = new Alert(Alert.AlertType.INFORMATION);
        loading.setTitle("Generation IA");
        loading.setHeaderText("Generation de proposition de reunion avec Groq...");
        loading.setContentText("Contact API...");
        loading.getButtonTypes().clear();
        loading.getButtonTypes().add(new ButtonType("Annuler"));
        loading.show();

        piJava.entities.PropositionReunion prop = piJava.utils.GroqService.generatePropositionFromMessage(
                nvl(groupe.nom, "Groupe"),
                nvl(groupe.projet, "Projet"),
                nvl(groupe.description, ""),
                groupe.id,
                instruction
        );

        loading.close();

        if (prop != null) {
            try {
                piJava.services.PropositionReunionService service = new piJava.services.PropositionReunionService();
                service.add(prop);
                loadData();
                showAlert(Alert.AlertType.INFORMATION, "Succes IA",
                        "Proposition IA creee !\n\n" +
                                "Titre: " + prop.getTitre() + "\n" +
                                "Date: " + prop.getDateReunion() + " " + prop.getHeureDebut() + "-" + prop.getHeureFin() + "\n" +
                                "Lieu: " + prop.getLieu());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur DB", "IA OK mais DB fail: " + e.getMessage());
            }
        } else {
            String details = piJava.utils.GroqService.getLastError();
            showAlert(Alert.AlertType.WARNING, "Echec IA",
                    "Impossible de generer la proposition IA.\n\n" +
                            (details.isBlank() ? "Verifiez internet et reessayez." : details));
        }
    }

    private Optional<String> askAiMeetingInput(GroupRecord groupe) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Assistant IA");
        dialog.setHeaderText("✨ Décrivez la réunion à créer");
        styleDialog(dialog.getDialogPane());

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        Label groupLabel = new Label("Groupe cible : " + nvl(groupe.nom, "Groupe"));
        groupLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        TextArea instructionField = new TextArea();
        instructionField.setPromptText("Ex: je veux une reunion le 5/2/2025 de 10:00 a 12:00 en esprit");
        instructionField.setText("je veux une reunion le 5/2/2025 de 10:00 a 12:00 en esprit");
        instructionField.setPrefWidth(460);
        instructionField.setPrefHeight(130);
        instructionField.setWrapText(true);
        styleArea(instructionField);
        content.getChildren().addAll(groupLabel, instructionField);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        styleDialogButtons(dialog.getDialogPane());

        Optional<ButtonType> response = dialog.showAndWait();
        if (response.isEmpty() || response.get() != ButtonType.OK) {
            return Optional.empty();
        }

        String instruction = instructionField.getText() == null ? "" : instructionField.getText().trim();
        if (instruction.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Ecrivez le message a envoyer a l'IA.");
            return Optional.empty();
        }

        return Optional.of(instruction);
    }

    private Optional<String> showGifPicker() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Sélecteur de GIF");
        dialog.setHeaderText("Recherchez et choisissez un GIF");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
        styleDialog(dialog.getDialogPane());
        styleDialogButtons(dialog.getDialogPane());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        HBox searchBox = new HBox(5);
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher (ex: cat, bravo)...");
        styleField(searchField);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        Button searchBtn = new Button("Chercher");
        searchBtn.setStyle("-fx-background-color: #cbd5e1; -fx-padding: 8 12; -fx-background-radius: 8;");
        searchBox.getChildren().addAll(searchField, searchBtn);

        javafx.scene.layout.FlowPane gifPane = new javafx.scene.layout.FlowPane(5, 5);
        gifPane.setPrefWrapLength(340);
        
        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(gifPane);
        scroll.setPrefSize(360, 300);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setFitToWidth(true);

        Label status = new Label("Chargement...");
        status.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");

        Runnable loadGifs = () -> {
            gifPane.getChildren().clear();
            status.setText("Chargement en cours...");
            new Thread(() -> {
                String query = searchField.getText().trim();
                java.util.List<piJava.utils.GiphyService.GifResult> results;
                if (query.isEmpty()) {
                    results = piJava.utils.GiphyService.getTrendingGifs(12);
                } else {
                    results = piJava.utils.GiphyService.searchGifs(query, 12);
                }
                
                Platform.runLater(() -> status.setText("Téléchargement des images..."));
                
                // Pre-fetch images to avoid JavaFX network issues
                class LoadedGif {
                    piJava.utils.GiphyService.GifResult result;
                    javafx.scene.image.Image image;
                    LoadedGif(piJava.utils.GiphyService.GifResult r, javafx.scene.image.Image i) { result = r; image = i; }
                }
                
                java.util.List<LoadedGif> loaded = new java.util.ArrayList<>();
                for (piJava.utils.GiphyService.GifResult gif : results) {
                    javafx.scene.image.Image img = piJava.utils.GiphyService.fetchImage(gif.previewUrl);
                    if (img != null && !img.isError()) {
                        loaded.add(new LoadedGif(gif, img));
                    }
                }
                
                Platform.runLater(() -> {
                    status.setText("");
                    gifPane.getChildren().clear();
                    
                    if (loaded.isEmpty()) {
                        status.setText("Aucun GIF trouve ou erreur de connexion.");
                        return;
                    }
                    
                    for (LoadedGif gif : loaded) {
                        try {
                            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(gif.image);
                            imgView.setFitHeight(100);
                            imgView.setPreserveRatio(true);
                            imgView.setStyle("-fx-cursor: hand;");
                            imgView.setOnMouseClicked(e -> {
                                dialog.setResult(gif.result.originalUrl);
                                dialog.close();
                            });
                            gifPane.getChildren().add(imgView);
                        } catch(Exception ignored) {}
                    }
                });
            }).start();
        };

        searchBtn.setOnAction(e -> loadGifs.run());
        searchField.setOnAction(e -> loadGifs.run());

        content.getChildren().addAll(searchBox, status, scroll);
        dialog.getDialogPane().setContent(content);

        loadGifs.run();
        return dialog.showAndWait();
    }

    private Connection openConnection() throws Exception {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void ensureGroupTables() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE groupe_projet ADD COLUMN creator_user_id INT NULL");
        } catch (Exception ignored) {
            // Column already exists or cannot be altered in this environment.
        }

        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS groupe_projet_invitation (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "groupe_projet_id INT NOT NULL, " +
                            "inviter_user_id INT NOT NULL, " +
                            "invited_user_id INT NOT NULL, " +
                            "statut VARCHAR(20) NOT NULL DEFAULT 'PENDING', " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                            ")"
            );
        } catch (Exception ignored) {
            // Table creation may fail due to permissions.
        }
    }

    private int insertGroup(Connection connection, String nom, String projet, int nbMembres, String description, int creatorUserId) throws Exception {
        String sql = "INSERT INTO groupe_projet (nom_projet, matiere, nbr_membre, statut, description, creator_user_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, nom);
            statement.setString(2, projet);
            statement.setInt(3, nbMembres);
            statement.setString(4, "Actif");
            statement.setString(5, description);
            statement.setInt(6, creatorUserId);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new IllegalStateException("Impossible de recuperer l'id du groupe cree.");
    }

    private List<SelectableUser> loadInvitableUsers(Connection connection, int currentUserId) throws Exception {
        String sql = "SELECT id, nom, prenom, email FROM user WHERE id <> ? ORDER BY nom, prenom";
        List<SelectableUser> users = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int userId = resultSet.getInt("id");
                    String nom = nvl(resultSet.getString("nom"), "");
                    String prenom = nvl(resultSet.getString("prenom"), "");
                    String email = nvl(resultSet.getString("email"), "");
                    users.add(new SelectableUser(userId, (nom + " " + prenom).trim() + " <" + email + ">"));
                }
            }
        }
        return users;
    }

    private List<SelectableUser> loadInvitableUsersForGroup(Connection connection, int groupId, int currentUserId) throws Exception {
        Set<Integer> excluded = new HashSet<>();
        String membersSql = "SELECT user_id FROM groupe_projet_user WHERE groupe_projet_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(membersSql)) {
            statement.setInt(1, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    excluded.add(resultSet.getInt("user_id"));
                }
            }
        }

        String pendingSql = "SELECT invited_user_id FROM groupe_projet_invitation WHERE groupe_projet_id = ? AND statut = 'PENDING'";
        try (PreparedStatement statement = connection.prepareStatement(pendingSql)) {
            statement.setInt(1, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    excluded.add(resultSet.getInt("invited_user_id"));
                }
            }
        }
        excluded.add(currentUserId);

        List<SelectableUser> allUsers = loadInvitableUsers(connection, currentUserId);
        return allUsers.stream()
                .filter(user -> !excluded.contains(user.id))
                .collect(Collectors.toList());
    }

    private int createInvitations(Connection connection, int groupId, int inviterUserId, List<Integer> userIds) throws Exception {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }

        int count = 0;
        String sql = "INSERT INTO groupe_projet_invitation (groupe_projet_id, inviter_user_id, invited_user_id, statut) VALUES (?, ?, ?, 'PENDING')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Integer invitedUserId : userIds) {
                if (invitedUserId == null || invitedUserId == inviterUserId) {
                    continue;
                }
                statement.setInt(1, groupId);
                statement.setInt(2, inviterUserId);
                statement.setInt(3, invitedUserId);
                statement.addBatch();
                count++;
            }
            statement.executeBatch();
        }
        return count;
    }

    private List<PendingInvitation> loadPendingInvitations(Connection connection, int userId) throws Exception {
        List<PendingInvitation> invitations = new ArrayList<>();
        String sql = "SELECT i.id, g.nom_projet, u.nom, u.prenom " +
                "FROM groupe_projet_invitation i " +
                "INNER JOIN groupe_projet g ON g.id = i.groupe_projet_id " +
                "INNER JOIN user u ON u.id = i.inviter_user_id " +
                "WHERE i.invited_user_id = ? AND i.statut = 'PENDING' " +
                "ORDER BY i.created_at DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int invitationId = resultSet.getInt("id");
                    String groupName = nvl(resultSet.getString("nom_projet"), "Groupe");
                    String inviter = (nvl(resultSet.getString("nom"), "") + " " + nvl(resultSet.getString("prenom"), "")).trim();
                    invitations.add(new PendingInvitation(invitationId, "Invitation de " + inviter + " pour le groupe: " + groupName));
                }
            }
        }
        return invitations;
    }

    private void respondToInvitation(Connection connection, int invitationId, boolean accepted) throws Exception {
        String selectSql = "SELECT groupe_projet_id, invited_user_id FROM groupe_projet_invitation WHERE id = ?";
        int groupId;
        int invitedUserId;
        try (PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setInt(1, invitationId);
            try (ResultSet resultSet = select.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Invitation introuvable.");
                }
                groupId = resultSet.getInt("groupe_projet_id");
                invitedUserId = resultSet.getInt("invited_user_id");
            }
        }

        String newStatus = accepted ? "ACCEPTED" : "REFUSED";
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE groupe_projet_invitation SET statut = ? WHERE id = ?")) {
            update.setString(1, newStatus);
            update.setInt(2, invitationId);
            update.executeUpdate();
        }

        if (!accepted) {
            return;
        }

        addUserToGroup(connection, groupId, invitedUserId);
        try (PreparedStatement updateCount = connection.prepareStatement(
                "UPDATE groupe_projet SET nbr_membre = nbr_membre + 1 WHERE id = ?")) {
            updateCount.setInt(1, groupId);
            updateCount.executeUpdate();
        }
    }

    private void updateInvitationBadge() {
        if (invitationsBtn == null) {
            return;
        }
        Integer currentUserId = resolveCurrentUserId();
        if (currentUserId == null) {
            invitationsBtn.setText("📩 Invitations");
            return;
        }
        try (Connection connection = openConnection()) {
            String sql = "SELECT COUNT(*) FROM groupe_projet_invitation WHERE invited_user_id = ? AND statut = 'PENDING'";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, currentUserId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    int pendingCount = resultSet.next() ? resultSet.getInt(1) : 0;
                    invitationsBtn.setText(pendingCount > 0
                            ? "📩 Invitations (" + pendingCount + ")"
                            : "📩 Invitations");
                }
            }
        } catch (Exception e) {
            invitationsBtn.setText("📩 Invitations");
        }
    }

    private void addUserToGroup(Connection connection, int groupId, int userId) throws Exception {
        String existsSql = "SELECT 1 FROM groupe_projet_user WHERE groupe_projet_id = ? AND user_id = ?";
        try (PreparedStatement exists = connection.prepareStatement(existsSql)) {
            exists.setInt(1, groupId);
            exists.setInt(2, userId);
            try (ResultSet resultSet = exists.executeQuery()) {
                if (resultSet.next()) {
                    return;
                }
            }
        }
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
        styleDialog(alert.getDialogPane());
        styleDialogButtons(alert.getDialogPane());
        alert.showAndWait();
    }

    private void styleDialog(DialogPane pane) {
        pane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #fecdd3; -fx-border-width: 1; -fx-background-radius: 14; -fx-border-radius: 14;");
        pane.setMinWidth(460);
    }

    private void styleDialogButtons(DialogPane pane) {
        for (ButtonType type : pane.getButtonTypes()) {
            Button button = (Button) pane.lookupButton(type);
            if (button == null) {
                continue;
            }
            boolean primary = type.getButtonData() == ButtonBar.ButtonData.OK_DONE || type == ButtonType.OK;
            button.setStyle(primary
                    ? "-fx-background-color: linear-gradient(to right, #ef4444, #be123c); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 8 18; -fx-cursor: hand;"
                    : "-fx-background-color: #f8fafc; -fx-text-fill: #475569; -fx-font-weight: 700; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-padding: 8 18; -fx-cursor: hand;");
        }
    }

    private void styleField(TextField field) {
        field.setStyle("-fx-background-color: #ffffff; -fx-border-color: #fecdd3; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 9 12; -fx-font-size: 13px;");
    }

    private void styleArea(TextArea area) {
        area.setStyle("-fx-background-color: #ffffff; -fx-border-color: #fecdd3; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 13px;");
    }

    private static final class GroupRecord {
        private final int id;
        private final String nom;
        private final String projet;
        private final int nbMembres;
        private final String statut;
        private final String description;
        private final Integer creatorUserId;

        private GroupRecord(int id, String nom, String projet, int nbMembres, String statut, String description, Integer creatorUserId) {
            this.id = id;
            this.nom = nom;
            this.projet = projet;
            this.nbMembres = nbMembres;
            this.statut = statut;
            this.description = description;
            this.creatorUserId = creatorUserId;
        }
    }

    private static final class SelectableUser {
        private final int id;
        private final String displayName;

        private SelectableUser(int id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
    }

    private static final class AiMeetingInput {
        private final LocalDate dateReunion;
        private final LocalTime heureDebut;
        private final LocalTime heureFin;
        private final String lieu;

        private AiMeetingInput(LocalDate dateReunion, LocalTime heureDebut, LocalTime heureFin, String lieu) {
            this.dateReunion = dateReunion;
            this.heureDebut = heureDebut;
            this.heureFin = heureFin;
            this.lieu = lieu;
        }
    }

    private static final class PendingInvitation {
        private final int id;
        private final String displayValue;

        private PendingInvitation(int id, String displayValue) {
            this.id = id;
            this.displayValue = displayValue;
        }
    }
}

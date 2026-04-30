package piJava.Controllers.frontoffice.group;

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
import piJava.entities.Vote;
import piJava.services.PropositionReunionService;
import piJava.services.VoteService;
import piJava.Controllers.group.GroupChatClient;
import piJava.Controllers.group.GroupChatListener;
import piJava.Controllers.group.GroupChatMessage;
import piJava.Controllers.group.GroupChatServer;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class FrontPropositionReunionController implements Initializable, GroupChatListener {

    @FXML private Label groupNameBreadcrumb;
    @FXML private Label pageTitle;
    @FXML private TextField searchField;

    @FXML private Label totalPropositionsLabel;
    @FXML private Label acceptedPropositionsLabel;
    @FXML private Label pendingPropositionsLabel;

    @FXML private ListView<PropositionReunion> propositionsListView;

    @FXML private Label footerLabel;
    @FXML private Button backBtn;
    @FXML private TextField chatHostField;
    @FXML private TextField chatPortField;
    @FXML private TextField chatUsernameField;
    @FXML private ListView<String> chatMessagesListView;
    @FXML private TextField chatMessageField;
    @FXML private Label chatStatusLabel;

    private Groupe currentGroupe;
    private final PropositionReunionService propositionService = new PropositionReunionService();
    private final VoteService voteService = new VoteService();
    private ObservableList<PropositionReunion> allPropositions = FXCollections.observableArrayList();
    private ObservableList<PropositionReunion> filtered = FXCollections.observableArrayList();
    private static final int CURRENT_USER_ID = 1; // Hardcoded user ID - À remplacer par userId authentifié
    private final GroupChatClient groupChatClient = new GroupChatClient();

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

        FlowPane actions = new FlowPane();
        actions.setHgap(8);
        actions.setVgap(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Vote is open until 2 hours before meeting when dateFinVote is not explicitly set.
        boolean isVotingOpen = isVotingStillOpen(prop);

        // Vote buttons - POUR LES MEMBRES DU FRONT OFFICE
        if (isVotingOpen) {
            Button voteForBtn = new Button("👍 Pour");
            voteForBtn.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #10B981, #059669); "
                              + "-fx-text-fill: #FFFFFF; -fx-background-radius: 8; -fx-padding: 8 14; "
                              + "-fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
            voteForBtn.setOnAction(e -> handleVote(prop, "pour"));

            Button voteAgainstBtn = new Button("👎 Contre");
            voteAgainstBtn.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #EF4444, #DC2626); "
                                   + "-fx-text-fill: #FFFFFF; -fx-background-radius: 8; -fx-padding: 8 14; "
                                   + "-fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
            voteAgainstBtn.setOnAction(e -> handleVote(prop, "contre"));

            Button abstainBtn = new Button("⊘ Abstention");
            abstainBtn.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #6B7280, #4B5563); "
                              + "-fx-text-fill: #FFFFFF; -fx-background-radius: 8; -fx-padding: 8 14; "
                              + "-fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
            abstainBtn.setOnAction(e -> handleVote(prop, "abstention"));

            Button voteStatsBtn = new Button("📊 Résultats");
            voteStatsBtn.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3B82F6, #1D4ED8); "
                                + "-fx-text-fill: #FFFFFF; -fx-background-radius: 8; -fx-padding: 8 14; "
                                + "-fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
            voteStatsBtn.setOnAction(e -> showVoteStats(prop));

            actions.getChildren().addAll(voteForBtn, voteAgainstBtn, abstainBtn, voteStatsBtn);
        } else {
            Label votingClosedLabel = new Label("🔒 Vote fermé");
            votingClosedLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
            actions.getChildren().add(votingClosedLabel);
        }

        card.getChildren().addAll(header, statsRow, actions);

        return card;
    }

    private boolean isVotingStillOpen(PropositionReunion proposition) {
        LocalDateTime now = LocalDateTime.now();

        if (proposition.getDateFinVote() != null) {
            // Inclusive check: vote still open on the end date.
            return !proposition.getDateFinVote().isBefore(now.toLocalDate());
        }

        if (proposition.getDateReunion() == null) {
            return false;
        }

        LocalTime meetingStart = proposition.getHeureDebut() != null
                ? proposition.getHeureDebut()
                : LocalTime.of(9, 0);

        LocalDateTime voteDeadline = LocalDateTime.of(proposition.getDateReunion(), meetingStart).minusHours(2);
        return now.isBefore(voteDeadline);
    }

    public void setCurrentGroupe(Groupe groupe) {
        this.currentGroupe = groupe;
        if (groupe != null) {
            groupNameBreadcrumb.setText(groupe.getNom());
            pageTitle.setText("📋 Propositions Réunion - " + groupe.getNom());
            if (chatUsernameField != null && (chatUsernameField.getText() == null || chatUsernameField.getText().isBlank())) {
                chatUsernameField.setText("Membre-" + CURRENT_USER_ID);
            }
            loadData();
        }
    }

    // ── Data Loading ───────────────────────────────────────────
    private void loadData() {
        if (propositionsListView == null || footerLabel == null) {
            System.err.println("[WARNING] UI elements not initialized yet, deferring loadData");
            return;
        }
        
        try {
            System.out.println("[DEBUG] Loading propositions for groupe: " + currentGroupe.getId());
            allPropositions.setAll(propositionService.getByGroupeId(currentGroupe.getId()));
            System.out.println("[DEBUG] Loaded " + allPropositions.size() + " propositions");
            filtered.setAll(allPropositions);
            updateStats();
            updateList();
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

    private void updateList() {
        propositionsListView.setItems(filtered);
        footerLabel.setText(filtered.size() + " propositions");
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
        updateList();
    }

    @FXML
    private void handleBack() {
        // Retourner à la liste des groupes
        groupChatClient.disconnect();
        System.out.println("[DEBUG] Navigating back from propositions");
    }

    @FXML
    private void handleConnectChat() {
        if (currentGroupe == null) {
            showAlert(Alert.AlertType.WARNING, "Chat", "Selectionnez un groupe avant de vous connecter.");
            return;
        }

        String host = chatHostField.getText() == null ? "localhost" : chatHostField.getText().trim();
        String user = chatUsernameField.getText() == null ? "" : chatUsernameField.getText().trim();
        int port;

        if (user.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Chat", "Veuillez entrer un pseudo.");
            return;
        }

        try {
            port = Integer.parseInt(chatPortField.getText().trim());
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Chat", "Le port doit etre numerique.");
            return;
        }

        GroupChatServer.getInstance().start(port);
        try {
            groupChatClient.connect(host, port, this);
            chatStatusLabel.setText("Connecte au chat du groupe " + currentGroupe.getNom());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chat", "Connexion impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendChatMessage() {
        if (currentGroupe == null) {
            return;
        }
        if (!groupChatClient.isConnected()) {
            showAlert(Alert.AlertType.WARNING, "Chat", "Connectez-vous au chat avant d'envoyer un message.");
            return;
        }

        String content = chatMessageField.getText() == null ? "" : chatMessageField.getText().trim();
        String username = chatUsernameField.getText() == null ? "" : chatUsernameField.getText().trim();
        if (content.isBlank() || username.isBlank()) {
            return;
        }

        groupChatClient.sendMessage(new GroupChatMessage(currentGroupe.getId(), username, content));
        chatMessageField.clear();
    }

    @Override
    public void onMessageReceived(GroupChatMessage message) {
        if (currentGroupe == null || message.getGroupId() != currentGroupe.getId()) {
            return;
        }
        Platform.runLater(() -> chatMessagesListView.getItems().add(message.getDisplayValue()));
    }

    @Override
    public void onConnectionStatusChanged(String statusMessage) {
        Platform.runLater(() -> chatStatusLabel.setText(statusMessage));
    }

    // ── VOTING SYSTEM ──────────────────────────────────────────
    private void handleVote(PropositionReunion proposition, String voteType) {
        try {
            Vote existingVote = voteService.getByUserAndProposition(CURRENT_USER_ID, proposition.getId());

            if (existingVote != null) {
                // Update existing vote
                existingVote.setType(voteType);
                existingVote.setUpdatedAt(java.time.LocalDateTime.now());
                voteService.edit(existingVote);
                showAlert(Alert.AlertType.INFORMATION, "Vote modifié", 
                         "Votre vote a été modifié en: " + voteType);
            } else {
                // Create new vote
                Vote newVote = new Vote(CURRENT_USER_ID, proposition.getId(), voteType);
                voteService.add(newVote);
                showAlert(Alert.AlertType.INFORMATION, "Vote enregistré", 
                         "Votre vote « " + voteType + " » a été enregistré!");
            }

            // Update proposition status based on votes
            updatePropositionStatus(proposition);
            loadData();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du vote: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePropositionStatus(PropositionReunion proposition) {
        try {
            VoteService.VoteStats stats = voteService.calculateVoteStats(proposition.getId());
            String newStatus = stats.checkProposalStatus();
            
            proposition.setStatut(newStatus);
            propositionService.edit(proposition);
            
            System.out.println("[VOTE] Proposition " + proposition.getId() + 
                             " updated to status: " + newStatus + 
                             " (Pour: " + stats.pour + ", Contre: " + stats.contre + 
                             ", Abstention: " + stats.abstention + ")");
        } catch (SQLException e) {
            System.err.println("[ERROR] Error updating proposition status: " + e.getMessage());
        }
    }

    private void showVoteStats(PropositionReunion proposition) {
        try {
            VoteService.VoteStats stats = voteService.calculateVoteStats(proposition.getId());
            Vote userVote = voteService.getByUserAndProposition(CURRENT_USER_ID, proposition.getId());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Résultats du Vote");
            alert.setHeaderText("Proposition: " + proposition.getTitre());

            StringBuilder content = new StringBuilder();
            content.append("Résultats du vote:\n\n");
            content.append("👍 Pour: ").append(stats.pour).append("\n");
            content.append("👎 Contre: ").append(stats.contre).append("\n");
            content.append("⊘ Abstention: ").append(stats.abstention).append("\n");
            content.append("━━━━━━━━━━━━━━━━━━\n");
            content.append("Total (excl. abstention): ").append(stats.getTotalVotants()).append("\n\n");

            if (stats.getTotalVotants() > 0) {
                double pourPercent = (double) stats.pour / stats.getTotalVotants() * 100;
                double contrePercent = (double) stats.contre / stats.getTotalVotants() * 100;
                content.append("Pour: ").append(String.format("%.1f%%", pourPercent)).append("\n");
                content.append("Contre: ").append(String.format("%.1f%%", contrePercent)).append("\n\n");
            }

            content.append("Statut: ").append(stats.checkProposalStatus()).append("\n");
            if (userVote != null) {
                content.append("\nVotre vote: ").append(userVote.getType());
            } else {
                content.append("\nVous n'avez pas encore voté");
            }

            VBox resultBox = new VBox(10);
            resultBox.setPadding(new Insets(8));
            resultBox.getChildren().addAll(
                    voteMetric("Pour", stats.pour, "#10B981"),
                    voteMetric("Contre", stats.contre, "#EF4444"),
                    voteMetric("Abstention", stats.abstention, "#6B7280"),
                    new Label("Total utile: " + stats.getTotalVotants()),
                    new Label("Statut: " + stats.checkProposalStatus()),
                    new Label(userVote != null ? "Votre vote: " + userVote.getType() : "Vous n'avez pas encore vote")
            );
            alert.getDialogPane().setContent(resultBox);
            styleAlert(alert);
            alert.showAndWait();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des résultats: " + e.getMessage());
        }
    }

    // ── Utilities ──────────────────────────────────────────────
    private static String nvl(String s, String fallback) {
        return (s != null && !s.isBlank()) ? s : fallback;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    private HBox voteMetric(String label, int value, String color) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(label);
        name.setMinWidth(90);
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");
        ProgressBar bar = new ProgressBar(Math.min(1.0, value / 10.0));
        bar.setPrefWidth(190);
        bar.setStyle("-fx-accent: " + color + ";");
        Label count = new Label(String.valueOf(value));
        count.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
        row.getChildren().addAll(name, bar, count);
        return row;
    }

    private void styleAlert(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        pane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #bfdbfe; -fx-border-width: 1; -fx-background-radius: 12; -fx-border-radius: 12;");
        pane.setMinWidth(420);
        for (ButtonType type : pane.getButtonTypes()) {
            Button button = (Button) pane.lookupButton(type);
            if (button != null) {
                button.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3B82F6, #1D4ED8); "
                        + "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 9; -fx-padding: 8 20; -fx-cursor: hand;");
            }
        }
    }
}

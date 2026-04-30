package piJava.Controllers.frontoffice.group;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GroupDetailsController implements Initializable {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/pidev";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @FXML private Label groupNameLabel;
    @FXML private Label projectLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label memberCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label totalPropositionsLabel;
    @FXML private Label acceptedPropositionsLabel;
    @FXML private Label pendingPropositionsLabel;
    @FXML private TextField searchField;
    @FXML private Button addPropositionBtn;
    @FXML private TableView<ProposalRecord> propositionTable;
    @FXML private TableColumn<ProposalRecord, String> titreCol;
    @FXML private TableColumn<ProposalRecord, String> dateCol;
    @FXML private TableColumn<ProposalRecord, String> heureCol;
    @FXML private TableColumn<ProposalRecord, String> lieuCol;
    @FXML private TableColumn<ProposalRecord, String> statutCol;
    @FXML private TableColumn<ProposalRecord, String> descriptionCol;
    @FXML private TableColumn<ProposalRecord, Void> actionsCol;
    @FXML private Label footerLabel;

    private final ObservableList<ProposalRecord> allPropositions = FXCollections.observableArrayList();
    private final ObservableList<ProposalRecord> filteredPropositions = FXCollections.observableArrayList();

    private int groupId;
    private String groupName;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        setupSearch();
    }

    public void setGroupData(int groupId, String nom, String projet, int nbMembres, String statut, String description) {
        this.groupId = groupId;
        this.groupName = nom;
        groupNameLabel.setText(nvl(nom, "Groupe"));
        projectLabel.setText("Projet: " + nvl(projet, "-"));
        descriptionLabel.setText(nvl(description, "Aucune description pour ce groupe."));
        memberCountLabel.setText(String.valueOf(nbMembres));
        statusLabel.setText(nvl(statut, "-"));
        statusLabel.setStyle(isActiveStatus(statut)
                ? "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-background-radius: 999; -fx-padding: 6 12; -fx-font-weight: 700;"
                : "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-background-radius: 999; -fx-padding: 6 12; -fx-font-weight: 700;");
        loadPropositions();
    }

    private void setupColumns() {
        titreCol.setCellValueFactory(data -> new SimpleStringProperty(nvl(data.getValue().titre, "-")));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(nvl(data.getValue().dateReunion, "-")));
        heureCol.setCellValueFactory(data -> new SimpleStringProperty(formatTimeRange(data.getValue())));
        lieuCol.setCellValueFactory(data -> new SimpleStringProperty(nvl(data.getValue().lieu, "-")));
        statutCol.setCellValueFactory(data -> new SimpleStringProperty(nvl(data.getValue().statut, "En attente")));
        descriptionCol.setCellValueFactory(data -> new SimpleStringProperty(nvl(data.getValue().description, "-")));

        statutCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(item);
                String normalized = normalize(item);
                String style;
                if ("acceptee".equals(normalized)) {
                    style = "-fx-background-color: #dcfce7; -fx-text-fill: #166534;";
                } else if ("refusee".equals(normalized)) {
                    style = "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
                } else {
                    style = "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
                }
                badge.setStyle(style + " -fx-background-radius: 999; -fx-padding: 6 10; -fx-font-size: 11px; -fx-font-weight: 700;");
                setGraphic(badge);
                setText(null);
            }
        });

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Supprimer");

            {
                deleteButton.getStyleClass().add("secondary-danger-btn");
                deleteButton.setOnAction(event -> {
                    ProposalRecord proposition = getTableView().getItems().get(getIndex());
                    handleDelete(proposition);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) {
                    setGraphic(null);
                    return;
                }
                setGraphic(deleteButton);
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void loadPropositions() {
        if (groupId <= 0) {
            return;
        }

        ObservableList<ProposalRecord> loaded = FXCollections.observableArrayList();
        String sql = "SELECT id, titre, date_reunion, heure_debut, heure_fin, lieu, description, status FROM proposition_reunion WHERE id_groupe_id = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String statut = resultSet.getString("status");
                    loaded.add(new ProposalRecord(
                            resultSet.getInt("id"),
                            resultSet.getString("titre"),
                            resultSet.getDate("date_reunion") != null ? resultSet.getDate("date_reunion").toLocalDate().toString() : null,
                            resultSet.getTime("heure_debut") != null ? resultSet.getTime("heure_debut").toLocalTime().toString() : null,
                            resultSet.getTime("heure_fin") != null ? resultSet.getTime("heure_fin").toLocalTime().toString() : null,
                            resultSet.getString("lieu"),
                            resultSet.getString("description"),
                            statut
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loaded.clear();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les propositions: " + e.getMessage());
        }

        allPropositions.setAll(loaded);
        applyFilters();
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filteredPropositions.setAll(allPropositions.stream()
                .filter(prop -> search.isEmpty()
                        || nvl(prop.titre, "").toLowerCase().contains(search)
                        || nvl(prop.lieu, "").toLowerCase().contains(search)
                        || nvl(prop.description, "").toLowerCase().contains(search))
                .collect(Collectors.toList()));

        propositionTable.setItems(filteredPropositions);
        updateStats();
        footerLabel.setText(filteredPropositions.size() + " proposition(s)");
    }

    private void updateStats() {
        int total = allPropositions.size();
        int accepted = (int) allPropositions.stream().filter(prop -> "acceptee".equals(normalize(prop.statut))).count();
        int pending = (int) allPropositions.stream().filter(prop -> "en attente".equals(normalize(prop.statut))).count();
        totalPropositionsLabel.setText(String.valueOf(total));
        acceptedPropositionsLabel.setText(String.valueOf(accepted));
        pendingPropositionsLabel.setText(String.valueOf(pending));
    }

    @FXML
    private void handleAddProposition() {
        if (groupId <= 0) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle proposition");
        dialog.setHeaderText("Creer une proposition pour " + nvl(groupName, "ce groupe"));

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(new ButtonType("Creer", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        styleDialog(pane);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(18));

        TextField titreField = new TextField();
        titreField.setPromptText("Titre");
        styleField(titreField);
        DatePicker dateField = new DatePicker(LocalDate.now().plusDays(1));
        ComboBox<String> startField = createTimeCombo();
        ComboBox<String> endField = createTimeCombo();
        startField.setValue("09:00");
        endField.setValue("10:00");
        TextField lieuField = new TextField();
        lieuField.setPromptText("Salle / lieu");
        styleField(lieuField);
        ComboBox<String> statusField = new ComboBox<>();
        statusField.getItems().addAll("En attente", "Acceptee", "Refusee");
        statusField.setValue("En attente");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefRowCount(4);
        descriptionField.setWrapText(true);
        styleArea(descriptionField);

        form.add(new Label("Titre"), 0, 0);
        form.add(titreField, 1, 0);
        form.add(new Label("Date"), 0, 1);
        form.add(dateField, 1, 1);
        form.add(new Label("Heure debut"), 0, 2);
        form.add(startField, 1, 2);
        form.add(new Label("Heure fin"), 0, 3);
        form.add(endField, 1, 3);
        form.add(new Label("Lieu"), 0, 4);
        form.add(lieuField, 1, 4);
        form.add(new Label("Statut"), 0, 5);
        form.add(statusField, 1, 5);
        form.add(new Label("Description"), 0, 6);
        form.add(descriptionField, 1, 6);
        pane.setContent(form);
        styleDialogButtons(pane);

        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK && result.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return;
            }

            String titre = titreField.getText() == null ? "" : titreField.getText().trim();
            if (titre.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le titre est obligatoire.");
                return;
            }

            if (dateField.getValue() == null || !dateField.getValue().isAfter(LocalDate.now())) {
                showAlert(Alert.AlertType.WARNING, "Validation", "La date doit etre apres aujourd'hui.");
                return;
            }

            LocalTime start = parseTime(startField.getValue());
            LocalTime end = parseTime(endField.getValue());
            if (start == null || end == null || !end.isAfter(start)) {
                showAlert(Alert.AlertType.WARNING, "Validation", "L'heure de fin doit etre apres l'heure de debut.");
                return;
            }

            if (Duration.between(start, end).toMinutes() > 270) {
                showAlert(Alert.AlertType.WARNING, "Validation", "La reunion ne peut pas depasser 4h30.");
                return;
            }

            String sql = "INSERT INTO proposition_reunion (proposition_id, titre, date_reunion, heure_debut, heure_fin, lieu, description, status, date_creation, nbr_vote_accept, id_groupe_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, 0);
                statement.setString(2, titre);
                statement.setDate(3, Date.valueOf(dateField.getValue()));
                statement.setTime(4, Time.valueOf(start));
                statement.setTime(5, Time.valueOf(end));
                statement.setString(6, lieuField.getText() == null ? "" : lieuField.getText().trim());
                statement.setString(7, descriptionField.getText() == null ? "" : descriptionField.getText().trim());
                statement.setString(8, statusField.getValue());
                statement.setDate(9, Date.valueOf(LocalDate.now()));
                statement.setInt(10, 0);
                statement.setInt(11, groupId);
                statement.executeUpdate();
                loadPropositions();
                showAlert(Alert.AlertType.INFORMATION, "Succes", "La proposition a ete creee.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de creer la proposition: " + e.getMessage());
            }
        });
    }

    private void handleDelete(ProposalRecord proposition) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText("Supprimer la proposition");
        confirm.setContentText("Voulez-vous supprimer \"" + nvl(proposition.titre, "cette proposition") + "\" ?");

        confirm.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) {
                return;
            }

            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM proposition_reunion WHERE id = ?")) {
                statement.setInt(1, proposition.id);
                statement.executeUpdate();
                loadPropositions();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Suppression impossible: " + e.getMessage());
            }
        });
    }

    private ComboBox<String> createTimeCombo() {
        ComboBox<String> combo = new ComboBox<>();
        for (int hour = 8; hour <= 20; hour++) {
            combo.getItems().add(String.format("%02d:00", hour));
            combo.getItems().add(String.format("%02d:30", hour));
        }
        return combo;
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalTime.parse(value);
    }

    private Connection openConnection() throws Exception {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private String formatTimeRange(ProposalRecord proposition) {
        return nvl(proposition.heureDebut, "-") + " - " + nvl(proposition.heureFin, "-");
    }

    private boolean isActiveStatus(String status) {
        return "actif".equals(normalize(status));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }

    private String nvl(String value, String fallback) {
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
        pane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #bfdbfe; -fx-border-width: 1; -fx-background-radius: 12; -fx-border-radius: 12;");
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
                    ? "-fx-background-color: linear-gradient(to right, #3b82f6, #1d4ed8); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 8 18; -fx-cursor: hand;"
                    : "-fx-background-color: #f8fafc; -fx-text-fill: #475569; -fx-font-weight: 700; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-padding: 8 18; -fx-cursor: hand;");
        }
    }

    private void styleField(TextField field) {
        field.setStyle("-fx-background-color: #ffffff; -fx-border-color: #bfdbfe; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 9 12; -fx-font-size: 13px;");
    }

    private void styleArea(TextArea area) {
        area.setStyle("-fx-background-color: #ffffff; -fx-border-color: #bfdbfe; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 13px;");
    }

    private static final class ProposalRecord {
        private final int id;
        private final String titre;
        private final String dateReunion;
        private final String heureDebut;
        private final String heureFin;
        private final String lieu;
        private final String description;
        private final String statut;

        private ProposalRecord(int id, String titre, String dateReunion, String heureDebut, String heureFin, String lieu, String description, String statut) {
            this.id = id;
            this.titre = titre;
            this.dateReunion = dateReunion;
            this.heureDebut = heureDebut;
            this.heureFin = heureFin;
            this.lieu = lieu;
            this.description = description;
            this.statut = statut;
        }
    }
}

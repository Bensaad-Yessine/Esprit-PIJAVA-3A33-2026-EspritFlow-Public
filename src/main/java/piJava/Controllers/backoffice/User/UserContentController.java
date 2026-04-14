package piJava.Controllers.backoffice.User;

import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import piJava.entities.Classe;
import piJava.entities.user;
import piJava.services.ClasseService;
import piJava.services.UserServices;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class UserContentController implements Initializable {

    // ── Header ─────────────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private Button    addUserBtn;

    // ── Mini Stats ─────────────────────────────────────────────
    @FXML private Label totalUsersLabel;
    @FXML private Label etudiantsLabel;
    @FXML private Label profsLabel;
    @FXML private Label bannedLabel;

    // ── Filters ────────────────────────────────────────────────
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> sexeFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button           resetFilterBtn;
    @FXML private Label            resultCountLabel;

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<user>            userTable;
    @FXML private TableColumn<user, String>  idCol;
    @FXML private TableColumn<user, Void>    avatarCol;
    @FXML private TableColumn<user, String>  nomCol;
    @FXML private TableColumn<user, String>  emailCol;
    @FXML private TableColumn<user, String>  roleCol;
    @FXML private TableColumn<user, String>  sexeCol;
    @FXML private TableColumn<user, String>  verifiedCol;
    @FXML private TableColumn<user, String>  bannedCol;
    @FXML private TableColumn<user, Void>    actionsCol;

    // ── Footer ─────────────────────────────────────────────────
    @FXML private Label  footerLabel;
    @FXML private Button prevBtn;
    @FXML private Label  pageLabel;
    @FXML private Button nextBtn;

    // ── State ──────────────────────────────────────────────────
    private final UserServices  userServices  = new UserServices();
    private final ClasseService classeService = new ClasseService();

    private ObservableList<user>   allUsers = FXCollections.observableArrayList();
    private ObservableList<user>   filtered = FXCollections.observableArrayList();
    private List<Classe>           classeList = new ArrayList<>();

    private static final int    PAGE_SIZE       = 10;
    private static final String UPLOAD_DIR      =
            "C:\\Users\\MSI\\Documents\\my_project_dev\\public\\uploads\\profile_pics\\";

    private int currentPage = 1;

    // ───────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadClasses();
        setupColumns();
        setupFilters();
        setupSearch();
        loadData();
        animateEntrance();
    }

    // ── Load classes from DB ───────────────────────────────────
    private void loadClasses() {
        try {
            classeList = classeService.getAllClasses();
        } catch (Exception e) {
            System.err.println("Failed to load classes: " + e.getMessage());
        }
    }

    // ── Column Setup ───────────────────────────────────────────
    private void setupColumns() {

        idCol.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label l = new Label("#" + item);
                l.setStyle("-fx-font-size:11px; -fx-text-fill:#3a4060; -fx-font-weight:700;");
                setGraphic(l);
            }
        });

        avatarCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                user u = getTableView().getItems().get(getIndex());

                // Try to show profile pic if available
                String pic = u.getProfile_pic();
                if (pic != null && !pic.isBlank()) {
                    File imgFile = new File(UPLOAD_DIR + pic);
                    if (imgFile.exists()) {
                        try {
                            ImageView iv = new ImageView(new Image(imgFile.toURI().toString(), 36, 36, true, true));
                            Circle clip = new Circle(18, 18, 18);
                            iv.setClip(clip);
                            setGraphic(iv);
                            return;
                        } catch (Exception ignored) {}
                    }
                }

                // Fallback: initials circle
                String initials = initials(u.getPrenom(), u.getNom());
                String[] colors = avatarColor(u.getRoles());
                Label circle = new Label(initials);
                circle.setStyle(
                        "-fx-background-color:" + colors[0] + "; "
                                + "-fx-text-fill:" + colors[1] + "; "
                                + "-fx-background-radius:18; "
                                + "-fx-min-width:36px; -fx-min-height:36px; "
                                + "-fx-max-width:36px; -fx-max-height:36px; "
                                + "-fx-font-size:12px; -fx-font-weight:700; -fx-alignment:CENTER;"
                );
                setGraphic(circle);
            }
        });

        nomCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPrenom() + " " + d.getValue().getNom()));
        nomCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label l = new Label(item);
                l.setStyle("-fx-font-size:13px; -fx-font-weight:600; -fx-text-fill:#eef0f8;");
                setGraphic(l);
            }
        });

        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        emailCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label l = new Label(item);
                l.setStyle("-fx-font-size:12px; -fx-text-fill:#6b7394;");
                setGraphic(l);
            }
        });

        roleCol.setCellValueFactory(d ->
                new SimpleStringProperty(formatRole(d.getValue().getRoles())));
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String[] s = roleBadgeStyle(item);
                Label badge = new Label(item);
                badge.setStyle(
                        "-fx-background-color:" + s[0] + "; -fx-border-color:" + s[1] + "; "
                                + "-fx-border-width:1; -fx-border-radius:6; -fx-background-radius:6; "
                                + "-fx-text-fill:" + s[2] + "; "
                                + "-fx-font-size:11px; -fx-font-weight:700; -fx-padding:3 10;"
                );
                setGraphic(badge);
            }
        });

        sexeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSexe()));
        sexeCol.setCellFactory(col -> badgeCell("#3b82f615", "#3b82f640", "#60a5fa"));

        verifiedCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getIs_verified() == 1 ? "✓ Vérifié" : "✗ Non vérifié"));
        verifiedCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                boolean ok = item.startsWith("✓");
                Label l = new Label(item);
                l.setStyle(ok
                        ? "-fx-background-color:#34d39915; -fx-border-color:#34d39940; -fx-border-width:1; "
                        + "-fx-border-radius:6; -fx-background-radius:6; -fx-text-fill:#34d399; "
                        + "-fx-font-size:11px; -fx-font-weight:700; -fx-padding:3 10;"
                        : "-fx-background-color:#ff4d6d15; -fx-border-color:#ff4d6d40; -fx-border-width:1; "
                        + "-fx-border-radius:6; -fx-background-radius:6; -fx-text-fill:#ff4d6d; "
                        + "-fx-font-size:11px; -fx-font-weight:700; -fx-padding:3 10;"
                );
                setGraphic(l);
            }
        });

        bannedCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getIs_banned() == 1 ? "Banni" : "Actif"));
        bannedCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                boolean banned = "Banni".equals(item);
                Label l = new Label(banned ? "🚫 Banni" : "✅ Actif");
                l.setStyle(banned
                        ? "-fx-background-color:#ff4d6d15; -fx-border-color:#ff4d6d40; -fx-border-width:1; "
                        + "-fx-border-radius:6; -fx-background-radius:6; -fx-text-fill:#ff4d6d; "
                        + "-fx-font-size:11px; -fx-font-weight:700; -fx-padding:3 10;"
                        : "-fx-background-color:#34d39915; -fx-border-color:#34d39940; -fx-border-width:1; "
                        + "-fx-border-radius:6; -fx-background-radius:6; -fx-text-fill:#34d399; "
                        + "-fx-font-size:11px; -fx-font-weight:700; -fx-padding:3 10;"
                );
                setGraphic(l);
            }
        });

        actionsCol.setCellFactory(col -> new TableCell<>() {
            final Button editBtn   = actionBtn("✏",           "btn-edit");
            final Button deleteBtn = actionBtn("🗑",           "btn-delete");
            final Button banBtn    = actionBtn("🚫 Bannir",   "btn-ban");
            final Button unbanBtn  = actionBtn("✅ Débannir", "btn-unban");
            final HBox   box       = new HBox(6, editBtn, deleteBtn, banBtn, unbanBtn);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                box.setPadding(new Insets(0, 4, 0, 4));
                editBtn.setOnAction(e   -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                banBtn.setOnAction(e    -> handleBan(getTableView().getItems().get(getIndex())));
                unbanBtn.setOnAction(e  -> handleUnban(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                user u = getTableView().getItems().get(getIndex());
                boolean isBanned = u.getIs_banned() == 1;
                banBtn.setVisible(!isBanned);  banBtn.setManaged(!isBanned);
                unbanBtn.setVisible(isBanned); unbanBtn.setManaged(isBanned);
                setGraphic(box);
            }
        });
    }

    // ── Data ───────────────────────────────────────────────────
    private void loadData() {
        try {
            allUsers.setAll(userServices.show());
            applyFilters();
            updateStats();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", e.getMessage());
        }
    }

    private void updateStats() {
        totalUsersLabel.setText(String.valueOf(allUsers.size()));
        long etudiants = allUsers.stream()
                .filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_USER")
                        && !u.getRoles().contains("ROLE_ADMIN")).count();
        etudiantsLabel.setText(String.valueOf(etudiants));
        long profs = allUsers.stream()
                .filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_PROF")).count();
        profsLabel.setText(String.valueOf(profs));
        long banned = allUsers.stream().filter(u -> u.getIs_banned() == 1).count();
        bannedLabel.setText(String.valueOf(banned));
        animateCounter(totalUsersLabel, 0, allUsers.size());
    }

    // ── Filters ────────────────────────────────────────────────
    private void setupFilters() {
        roleFilter.getItems().setAll("Administrateur", "Professeur", "Étudiant");
        sexeFilter.getItems().setAll("Homme", "Femme");
        statusFilter.getItems().setAll("Actif", "Banni", "Non vérifié");
        roleFilter.valueProperty().addListener((o, ov, nv)   -> { currentPage = 1; applyFilters(); });
        sexeFilter.valueProperty().addListener((o, ov, nv)   -> { currentPage = 1; applyFilters(); });
        statusFilter.valueProperty().addListener((o, ov, nv) -> { currentPage = 1; applyFilters(); });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((o, ov, nv) -> { currentPage = 1; applyFilters(); });
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String role   = roleFilter.getValue();
        String sexe   = sexeFilter.getValue();
        String status = statusFilter.getValue();

        List<user> result = allUsers.stream()
                .filter(u -> search.isEmpty()
                        || (u.getNom()    != null && u.getNom().toLowerCase().contains(search))
                        || (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(search))
                        || (u.getEmail()  != null && u.getEmail().toLowerCase().contains(search)))
                .filter(u -> role   == null || matchRole(u.getRoles(), role))
                .filter(u -> sexe   == null || sexe.equalsIgnoreCase(u.getSexe()))
                .filter(u -> status == null || matchStatus(u, status))
                .collect(Collectors.toList());

        filtered.setAll(result);
        resultCountLabel.setText(result.size() + " résultat(s)");
        refreshPage();
    }

    private boolean matchRole(String roles, String filter) {
        if (roles == null) return false;
        return switch (filter) {
            case "Administrateur" -> roles.contains("ROLE_ADMIN");
            case "Professeur"     -> roles.contains("ROLE_PROF");
            case "Étudiant"       -> roles.contains("ROLE_USER") && !roles.contains("ROLE_ADMIN");
            default               -> true;
        };
    }

    private boolean matchStatus(user u, String status) {
        return switch (status) {
            case "Banni"       -> u.getIs_banned()   == 1;
            case "Actif"       -> u.getIs_banned()   == 0 && u.getIs_verified() == 1;
            case "Non vérifié" -> u.getIs_verified() == 0;
            default            -> true;
        };
    }

    @FXML private void handleResetFilters() {
        roleFilter.setValue(null);
        sexeFilter.setValue(null);
        statusFilter.setValue(null);
        searchField.clear();
        currentPage = 1;
        applyFilters();
    }

    // ── Pagination ─────────────────────────────────────────────
    private void refreshPage() {
        int total      = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage    = Math.min(currentPage, totalPages);
        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);
        userTable.setItems(FXCollections.observableArrayList(filtered.subList(from, to)));
        footerLabel.setText("Affichage de " + (total == 0 ? 0 : from + 1)
                + "–" + to + " sur " + total + " entrées");
        pageLabel.setText(String.valueOf(currentPage));
        prevBtn.setDisable(currentPage <= 1);
        nextBtn.setDisable(currentPage >= totalPages);
    }

    @FXML private void handlePrev() { if (currentPage > 1) { currentPage--; refreshPage(); } }
    @FXML private void handleNext() { currentPage++; refreshPage(); }

    // ── CRUD ───────────────────────────────────────────────────
    @FXML private void handleAdd()  { showUserDialog(null); }
    private void handleEdit(user u) { showUserDialog(u); }

    private void handleDelete(user u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer « " + u.getPrenom() + " " + u.getNom() + " » ?");
        confirm.setContentText("Cette action est irréversible.");
        styleAlert(confirm);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                userServices.delete(u.getId());
                loadData();
            }
        });
    }

    private void handleBan(user u) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Bannir l'utilisateur");
        dialog.setHeaderText("Raison du bannissement pour « " + u.getPrenom() + " " + u.getNom() + " »");
        dialog.setContentText("Raison :");
        dialog.getDialogPane().setStyle("-fx-background-color:#111318; -fx-border-color:#1e2130;");
        dialog.showAndWait().ifPresent(reason -> {
            userServices.banUser(u.getId(), reason);
            loadData();
        });
    }

    private void handleUnban(user u) {
        userServices.unbanUser(u.getId());
        loadData();
    }

    // ── Add / Edit Dialog ──────────────────────────────────────
    private void showUserDialog(user existing) {
        boolean isEdit = existing != null;
        Dialog<user> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier l'utilisateur" : "Nouvel utilisateur");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color:#111318; -fx-border-color:#1e2130; -fx-border-width:1;");
        pane.setPrefWidth(560);
        pane.getButtonTypes().addAll(
                new ButtonType(isEdit ? "Enregistrer" : "Créer", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );

        Button okBtn = (Button) pane.lookupButton(pane.getButtonTypes().get(0));
        okBtn.setStyle("-fx-background-color:#34d399; -fx-text-fill:#0d0f14; "
                + "-fx-font-weight:700; -fx-background-radius:8; -fx-padding:8 20;");
        Button cancelBtn = (Button) pane.lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle("-fx-background-color:#1e2130; -fx-text-fill:#6b7394; "
                + "-fx-background-radius:8; -fx-border-color:#272c3d; "
                + "-fx-border-width:1; -fx-border-radius:8; -fx-padding:8 20;");

        // ── Profile picture picker state ──────────────────────
        final String[] picFilename = { isEdit ? existing.getProfile_pic() : null };

        // ── Avatar preview ────────────────────────────────────
        StackPane avatarPreview = new StackPane();
        avatarPreview.setMinSize(72, 72); avatarPreview.setMaxSize(72, 72);

        Label initialsLbl = new Label(isEdit
                ? initials(existing.getPrenom(), existing.getNom()) : "?");
        initialsLbl.setStyle("-fx-background-color:#3b2fc9; -fx-text-fill:white; "
                + "-fx-background-radius:36; -fx-min-width:72; -fx-min-height:72; "
                + "-fx-max-width:72; -fx-max-height:72; "
                + "-fx-font-size:22px; -fx-font-weight:700; -fx-alignment:CENTER;");

        ImageView picPreview = new ImageView();
        picPreview.setFitWidth(72); picPreview.setFitHeight(72);
        picPreview.setPreserveRatio(true);
        Circle clip = new Circle(36, 36, 36);
        picPreview.setClip(clip);

        // Show existing pic if present
        if (isEdit && existing.getProfile_pic() != null && !existing.getProfile_pic().isBlank()) {
            File f = new File(UPLOAD_DIR + existing.getProfile_pic());
            if (f.exists()) {
                picPreview.setImage(new Image(f.toURI().toString()));
                picPreview.setVisible(true);
                initialsLbl.setVisible(false);
            } else {
                picPreview.setVisible(false);
            }
        } else {
            picPreview.setVisible(false);
        }

        Button picBtn = new Button(picFilename[0] != null ? "✎ Changer" : "＋ Ajouter photo");
        picBtn.setStyle("-fx-background-color:#1e2130; -fx-text-fill:#a5a0f0; "
                + "-fx-border-color:#3b2fc9; -fx-border-width:1; "
                + "-fx-border-radius:6; -fx-background-radius:6; "
                + "-fx-font-size:11px; -fx-cursor:hand; -fx-padding:4 10;");

        Label picNameLbl = new Label(picFilename[0] != null ? picFilename[0] : "Aucune image");
        picNameLbl.setStyle("-fx-font-size:10px; -fx-text-fill:#6b7394;");

        avatarPreview.getChildren().addAll(initialsLbl, picPreview);

        picBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir une photo de profil");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
            );
            File chosen = fc.showOpenDialog(pane.getScene().getWindow());
            if (chosen == null) return;

            // Copy file to uploads directory
            String savedName = copyToUploads(chosen);
            if (savedName != null) {
                picFilename[0] = savedName;
                picNameLbl.setText(savedName);
                picBtn.setText("✎ Changer");
                // Update preview
                try {
                    picPreview.setImage(new Image(chosen.toURI().toString()));
                    picPreview.setVisible(true);
                    initialsLbl.setVisible(false);
                } catch (Exception ex) { /* preview failed, no problem */ }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de copier l'image.");
            }
        });

        // Avatar section (left column)
        VBox avatarSection = new VBox(8, avatarPreview, picBtn, picNameLbl);
        avatarSection.setAlignment(Pos.CENTER);
        avatarSection.setPadding(new Insets(8, 16, 8, 0));

        // ── Form fields ───────────────────────────────────────
        TextField     nomField    = dialogField("ex: Ayari");
        TextField     prenomField = dialogField("ex: Mohamed");
        TextField     emailField  = dialogField("ex: m.ayari@esprit.tn");
        TextField     telField    = dialogField("ex: 0612345678");
        TextField     dateField   = dialogField("yyyy-MM-dd  ex: 2003-05-14");
        ComboBox<String> sexeCombo  = dialogCombo("Homme", "Femme");
        ComboBox<String> roleCombo  = dialogCombo("ROLE_USER", "ROLE_PROF", "ROLE_ADMIN");
        PasswordField passField   = new PasswordField();
        passField.setPromptText(isEdit ? "Laisser vide = inchangé" : "Mot de passe *");
        passField.setStyle(dialogFieldStyle()); passField.setPrefWidth(260);

        // ── Classe dropdown ───────────────────────────────────
        ComboBox<Classe> classeCombo = new ComboBox<>();
        classeCombo.setPrefWidth(260);
        classeCombo.setStyle(dialogFieldStyle());
        classeCombo.setPromptText("Sélectionner une classe...");
        classeCombo.getItems().add(null); // allow "none"
        classeCombo.getItems().addAll(classeList);

        // Display class name in dropdown
        classeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Classe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                setText(item == null ? "— Aucune classe —" : item.getNom() + "  (ID " + item.getId() + ")");
                setStyle("-fx-background-color:#161921; -fx-text-fill:#c8cfe8; -fx-font-size:13px;");
            }
        });
        classeCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Classe item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(dialogFieldStyle());
                if (empty || item == null) { setText("— Aucune classe —"); return; }
                setText(item.getNom() + "  (ID " + item.getId() + ")");
            }
        });

        // Pre-fill for edit
        if (isEdit) {
            nomField.setText(existing.getNom());
            prenomField.setText(existing.getPrenom());
            emailField.setText(existing.getEmail());
            telField.setText(existing.getNum_tel());
            dateField.setText(existing.getDate_de_naissance());
            sexeCombo.setValue(existing.getSexe());
            roleCombo.setValue(
                    existing.getRoles() != null
                            ? existing.getRoles().replaceAll("[\\[\\]\"]", "")
                            : "ROLE_USER"
            );
            // Pre-select classe
            if (existing.getClasse_id() != null) {
                classeList.stream()
                        .filter(c -> c.getId() == existing.getClasse_id())
                        .findFirst()
                        .ifPresent(classeCombo::setValue);
            }
        }

        // ── Grid layout ───────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        grid.setPadding(new Insets(20, 28, 16, 28));
        grid.setStyle("-fx-background-color:#111318;");

        // Left: avatar section
        grid.add(avatarSection, 0, 0, 1, 9);

        // Right: form fields
        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10);
        form.add(dialogLabel("Nom *"),            0, 0); form.add(nomField,    1, 0);
        form.add(dialogLabel("Prénom *"),         0, 1); form.add(prenomField, 1, 1);
        form.add(dialogLabel("Email *"),          0, 2); form.add(emailField,  1, 2);
        form.add(dialogLabel("Téléphone"),        0, 3); form.add(telField,    1, 3);
        form.add(dialogLabel("Date naissance *"), 0, 4); form.add(dateField,   1, 4);
        form.add(dialogLabel("Sexe *"),           0, 5); form.add(sexeCombo,   1, 5);
        form.add(dialogLabel("Rôle *"),           0, 6); form.add(roleCombo,   1, 6);
        form.add(dialogLabel("Classe"),           0, 7); form.add(classeCombo, 1, 7);
        form.add(dialogLabel("Mot de passe"),     0, 8); form.add(passField,   1, 8);
        form.setStyle("-fx-background-color:#111318;");

        grid.add(form, 1, 0);
        pane.setContent(grid);

        // ── Result converter ──────────────────────────────────
        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() != ButtonBar.ButtonData.OK_DONE) return null;

            String nom    = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email  = emailField.getText().trim();
            String date   = dateField.getText().trim();
            String sexe   = sexeCombo.getValue();
            String role   = roleCombo.getValue() != null
                    ? "[\"" + roleCombo.getValue() + "\"]" : "[\"ROLE_USER\"]";
            String tel    = telField.getText().trim();
            String pass   = passField.getText();

            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || date.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champs requis",
                        "Nom, Prénom, Email et Date de naissance sont obligatoires.");
                return null;
            }
            if (!isEdit && pass.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Mot de passe requis",
                        "Le mot de passe est obligatoire pour la création.");
                return null;
            }

            Classe selectedClasse = classeCombo.getValue();
            Integer classeId = (selectedClasse != null) ? selectedClasse.getId() : null;

            if (isEdit) {
                existing.setNom(nom);
                existing.setPrenom(prenom);
                existing.setEmail(email);
                existing.setNum_tel(tel);
                existing.setDate_de_naissance(date);
                existing.setSexe(sexe);
                existing.setRoles(role);
                existing.setClasse_id(classeId);
                existing.setProfile_pic(picFilename[0]);
                if (!pass.isEmpty()) existing.setPassword(pass);
                return existing;
            } else {
                return new user(
                        0, email, role, pass, 0,
                        nom, prenom, tel, date,
                        sexe != null ? sexe : "Homme",
                        picFilename[0],
                        null, 0, null, null, null,
                        classeId
                );
            }
        });

        dialog.showAndWait().ifPresent(u -> {
            if (u == null) return;
            if (isEdit) userServices.edit(u);
            else        userServices.add(u);
            loadData();
        });
    }

    // ── Profile pic: copy to uploads dir ──────────────────────
    private String copyToUploads(File source) {
        try {
            // Ensure directory exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            // Unique filename: timestamp + original name
            String ext      = source.getName().contains(".")
                    ? source.getName().substring(source.getName().lastIndexOf('.'))
                    : ".jpg";
            String filename = "profile_" + System.currentTimeMillis() + ext;
            Path   dest     = uploadPath.resolve(filename);
            Files.copy(source.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            System.err.println("Failed to copy profile pic: " + e.getMessage());
            return null;
        }
    }

    // ── Animations ─────────────────────────────────────────────
    private void animateEntrance() {
        userTable.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), userTable);
        ft.setFromValue(0); ft.setToValue(1);
        ft.setDelay(Duration.millis(200));
        ft.play();
    }

    private void animateCounter(Label label, int from, int to) {
        Timeline tl = new Timeline();
        for (int i = 0; i <= 20; i++) {
            final int val = from + (int) ((to - from) * (i / 20.0));
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(i * 30L),
                    e -> label.setText(String.valueOf(val))));
        }
        tl.play();
    }

    // ── Helpers ────────────────────────────────────────────────
    private String initials(String prenom, String nom) {
        String p = (prenom != null && !prenom.isEmpty()) ? String.valueOf(prenom.charAt(0)) : "?";
        String n = (nom    != null && !nom.isEmpty())    ? String.valueOf(nom.charAt(0))    : "?";
        return (p + n).toUpperCase();
    }

    private String[] avatarColor(String roles) {
        if (roles == null) return new String[]{"#3b82f615", "#60a5fa"};
        if (roles.contains("ROLE_ADMIN")) return new String[]{"#ff4d6d20", "#ff4d6d"};
        if (roles.contains("ROLE_PROF"))  return new String[]{"#a78bfa20", "#a78bfa"};
        return                            new String[]{"#34d39920", "#34d399"};
    }

    private String formatRole(String roles) {
        if (roles == null) return "Inconnu";
        if (roles.contains("ROLE_ADMIN")) return "Admin";
        if (roles.contains("ROLE_PROF"))  return "Professeur";
        return "Étudiant";
    }

    private String[] roleBadgeStyle(String role) {
        return switch (role) {
            case "Admin"      -> new String[]{"#ff4d6d15", "#ff4d6d40", "#ff4d6d"};
            case "Professeur" -> new String[]{"#a78bfa15", "#a78bfa40", "#a78bfa"};
            default           -> new String[]{"#34d39915", "#34d39940", "#34d399"};
        };
    }

    private TableCell<user, String> badgeCell(String bg, String border, String fg) {
        return new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setStyle(
                        "-fx-background-color:" + bg + "; -fx-border-color:" + border + "; "
                                + "-fx-border-width:1; -fx-border-radius:6; -fx-background-radius:6; "
                                + "-fx-text-fill:" + fg + "; "
                                + "-fx-font-size:11px; -fx-font-weight:700; -fx-padding:3 10;"
                );
                setGraphic(badge);
            }
        };
    }

    private Button actionBtn(String text, String styleClass) {
        Button b = new Button(text);
        b.getStyleClass().add(styleClass);
        return b;
    }

    private TextField dialogField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt); tf.setPrefWidth(260);
        tf.setStyle(dialogFieldStyle());
        return tf;
    }

    @SafeVarargs
    private <T> ComboBox<T> dialogCombo(T... items) {
        ComboBox<T> cb = new ComboBox<>();
        cb.getItems().addAll(items);
        cb.setStyle(dialogFieldStyle()); cb.setPrefWidth(260);
        return cb;
    }

    private String dialogFieldStyle() {
        return "-fx-background-color:#161921; -fx-border-color:#1e2130; "
                + "-fx-border-width:1; -fx-border-radius:8; -fx-background-radius:8; "
                + "-fx-text-fill:#c8cfe8; -fx-prompt-text-fill:#3a4060; "
                + "-fx-font-size:13px; -fx-padding:8 12;";
    }

    private Label dialogLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px; -fx-text-fill:#6b7394; "
                + "-fx-font-weight:600; -fx-min-width:140;");
        return l;
    }

    private void styleAlert(Alert a) {
        a.getDialogPane().setStyle(
                "-fx-background-color:#111318; -fx-border-color:#1e2130; -fx-border-width:1;");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        styleAlert(a); a.showAndWait();
    }
}
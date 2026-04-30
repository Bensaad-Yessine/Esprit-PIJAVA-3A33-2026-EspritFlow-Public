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
import piJava.services.StreakEngagementService;
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
    @FXML private Button    filterBtn;
    @FXML private Button    addUserBtn;
    @FXML private Label     currentUserLabel;

    // ── Stat Cards ─────────────────────────────────────────────
    @FXML private Label totalUsersLabel;
    @FXML private Label etudiantsLabel;
    @FXML private Label adminsLabel;
    @FXML private Label verifiesLabel;

    // ── Filters & Search ───────────────────────────────────────
    @FXML private VBox             filterBox;
    @FXML private TextField        searchField;
    @FXML private Label            resultCountLabel;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> sortFilter;
    @FXML private ComboBox<String> orderFilter;

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<user>            userTable;
    @FXML private TableColumn<user, user>    userCol;
    @FXML private TableColumn<user, String>  emailCol;
    @FXML private TableColumn<user, String>  dobCol;
    @FXML private TableColumn<user, String>  classeCol;
    @FXML private TableColumn<user, String>  telCol;
    @FXML private TableColumn<user, String>  sexeCol;
    @FXML private TableColumn<user, String>  roleCol;
    @FXML private TableColumn<user, String>  verifiedCol;
    @FXML private TableColumn<user, String>  bannedCol;
    @FXML private TableColumn<user, Void>    actionsCol;

    // ── Footer ─────────────────────────────────────────────────
    @FXML private Label  footerLabel;
    @FXML private Button prevBtn;
    @FXML private Label  pageLabel;
    @FXML private Button nextBtn;

    // ── State ──────────────────────────────────────────────────
    private final UserServices          userServices          = new UserServices();
    private final ClasseService         classeService         = new ClasseService();
    private final StreakEngagementService streakEngagementService = new StreakEngagementService();

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
        userCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue()));
        userCol.setCellFactory(col -> new TableCell<user, user>() {
            @Override protected void updateItem(user u, boolean empty) {
                super.updateItem(u, empty);
                if (empty || u == null) { setGraphic(null); return; }
                
                StackPane avatar = new StackPane();
                String pic = u.getProfile_pic();
                boolean usePic = false;
                if (pic != null && !pic.isBlank()) {
                    File imgFile = new File(UPLOAD_DIR + pic);
                    if (imgFile.exists()) {
                        try {
                            ImageView iv = new ImageView(new Image(imgFile.toURI().toString(), 36, 36, true, true));
                            iv.setClip(new Circle(18, 18, 18));
                            avatar.getChildren().add(iv);
                            usePic = true;
                        } catch (Exception ignored) {}
                    }
                }
                if (!usePic) {
                    Label initials = new Label(initials(u.getPrenom(), u.getNom()));
                    initials.getStyleClass().add("avatar-circle");
                    String[] colors = avatarColor(u.getRoles());
                    initials.setStyle("-fx-background-color:" + colors[0] + "; -fx-text-fill:" + colors[1] + ";");
                    avatar.getChildren().add(initials);
                }

                VBox text = new VBox(2);
                Label name = new Label(u.getPrenom() + " " + u.getNom());
                name.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-font-size: 13px;");
                Label emailSub = new Label(u.getEmail());
                emailSub.setStyle("-fx-text-fill: #7f8fa6; -fx-font-size: 11px;");
                text.getChildren().addAll(name, emailSub);

                if (u.getId() == 1) { // Indicate current user visually for demo
                    Label isYou = new Label("✔ C'est vous");
                    isYou.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: 600; -fx-font-size: 10px;");
                    text.getChildren().add(isYou);
                }

                HBox root = new HBox(12, avatar, text);
                root.setAlignment(Pos.CENTER_LEFT);
                setGraphic(root);
            }
        });

        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        emailCol.setCellFactory(col -> new TableCell<user, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label l = new Label(item);
                l.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                setGraphic(l);
            }
        });

        dobCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate_de_naissance()));
        dobCol.setCellFactory(col -> new TableCell<user, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Label l = new Label(item == null || item.isEmpty() ? "—" : item);
                l.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                setGraphic(l);
            }
        });

        classeCol.setCellValueFactory(d -> {
            String className = "—";
            if (d.getValue().getClasse_id() != null) {
                try {
                    Classe c = classeService.getById(d.getValue().getClasse_id());
                    if (c != null && c.getNom() != null) className = c.getNom();
                } catch(Exception ignored){}
            }
            return new SimpleStringProperty(className);
        });
        classeCol.setCellFactory(col -> badgeCell("#ff4d4d", "#ff4d4d", "#ffffff"));

        telCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNum_tel()));
        telCol.setCellFactory(col -> badgeCell("#8e44ad", "#8e44ad", "#ffffff"));

        sexeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSexe()));
        sexeCol.setCellFactory(col -> new TableCell<user, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label l = new Label(item);
                if ("Homme".equalsIgnoreCase(item)) {
                    l.setStyle("-fx-background-color:#0984e3; -fx-text-fill:#ffffff; -fx-padding:4 10; -fx-background-radius:6; -fx-font-weight:700; -fx-font-size:11px;");
                } else {
                    l.setStyle("-fx-text-fill:#ffffff; -fx-font-weight:600; -fx-font-size:12px;");
                }
                setGraphic(l);
            }
        });

        roleCol.setCellValueFactory(d -> new SimpleStringProperty(formatRole(d.getValue().getRoles())));
        roleCol.setCellFactory(col -> new TableCell<user, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String bg = "Admin".equals(item) ? "#9b59b6" : ("Professeur".equals(item) ? "#a78bfa" : "#00cec9");
                Label l = new Label("Admin".equals(item) ? "Administrateur" : item);
                l.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:#ffffff; -fx-padding:4 10; -fx-background-radius:6; -fx-font-weight:700; -fx-font-size:11px;");
                setGraphic(l);
            }
        });

        verifiedCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIs_verified() == 1 ? "Vérifié" : "Non vérifié"));
        verifiedCol.setCellFactory(col -> new TableCell<user, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String bg = "Vérifié".equals(item) ? "#00b894" : "#fdcb6e";
                String fg = "Vérifié".equals(item) ? "#ffffff" : "#000000";
                Label l = new Label(item);
                l.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg + "; -fx-padding:4 10; -fx-background-radius:6; -fx-font-weight:700; -fx-font-size:11px;");
                setGraphic(l);
            }
        });

        bannedCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIs_banned() == 1 ? "Banni" : "Actif"));
        bannedCol.setCellFactory(col -> new TableCell<user, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String bg = "Actif".equals(item) ? "#6c5ce7" : "#e74c3c";
                Label l = new Label(item);
                l.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:#ffffff; -fx-padding:4 10; -fx-background-radius:6; -fx-font-weight:700; -fx-font-size:11px;");
                setGraphic(l);
            }
        });

        actionsCol.setCellFactory(col -> new TableCell<user, Void>() {
            final Button viewBtn   = createSolidIconButton("🔍", "#8e44ad", "#ffffff");
            final Button editBtn   = createSolidIconButton("✏️", "#f1c40f", "#ffffff");
            final Button deleteBtn = createSolidIconButton("🗑", "#e74c3c", "#ffffff");
            final Button banBtn    = createSolidIconButton("Ø", "#ffffff", "#000000");

            final Button vousBtn   = new Button("Vous");
            final HBox box = new HBox(6);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                viewBtn.setOnAction(e   -> handleView(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e   -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                banBtn.setOnAction(e    -> {
                    user u = getTableView().getItems().get(getIndex());
                    if (u.getIs_banned() == 1) handleUnban(u); else handleBan(u);
                });
                vousBtn.setStyle("-fx-background-color:#ffffff; -fx-text-fill:#7f8fa6; -fx-font-weight:700; -fx-background-radius:6; -fx-padding:4 12;");
                vousBtn.setDisable(true);
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                user u = getTableView().getItems().get(getIndex());
                box.getChildren().clear();
                if (u.getId() == 1) {
                    box.getChildren().addAll(viewBtn, editBtn, vousBtn);
                } else {
                    box.getChildren().addAll(viewBtn, editBtn, banBtn, deleteBtn);
                }
                setGraphic(box);
            }
        });

        userTable.sortPolicyProperty().set(t -> {
            Comparator<user> comparator = (Comparator<user>) t.getComparator();
            if (comparator != null) {
                FXCollections.sort(filtered, comparator);
                refreshPage();
            }
            return true;
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
        long admins = allUsers.stream()
                .filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_ADMIN")).count();
        adminsLabel.setText(String.valueOf(admins));
        long verifies = allUsers.stream()
                .filter(u -> u.getIs_verified() == 1).count();
        verifiesLabel.setText(String.valueOf(verifies));
    }

    // ── Filters ────────────────────────────────────────────────
    private void setupFilters() {
        roleFilter.getItems().setAll("Tous les rôles", "Administrateur", "Professeur", "Étudiant");
        roleFilter.setValue("Tous les rôles");

        statusFilter.getItems().setAll("Tous", "Actif", "Banni", "Non vérifié");
        statusFilter.setValue("Tous");

        sortFilter.getItems().setAll("ID", "Nom", "Email", "Date de naissance");
        sortFilter.setValue("ID");

        orderFilter.getItems().setAll("A-Z", "Z-A");
        orderFilter.setValue("A-Z");

        // React immediately on input changes
        searchField.textProperty().addListener((o, ov, nv) -> handleApplyFilters());
        roleFilter.valueProperty().addListener((o, ov, nv) -> handleApplyFilters());
        statusFilter.valueProperty().addListener((o, ov, nv) -> handleApplyFilters());
        sortFilter.valueProperty().addListener((o, ov, nv) -> handleApplyFilters());
        orderFilter.valueProperty().addListener((o, ov, nv) -> handleApplyFilters());
    }

    @FXML private void toggleFilters() {
        filterBox.setVisible(!filterBox.isVisible());
        filterBox.setManaged(filterBox.isVisible());
    }

    @FXML private void handleApplyFilters() {
        currentPage = 1;
        applyFilters();
    }

    @FXML private void handleResetFilters() {
        searchField.clear();
        roleFilter.setValue("Tous les rôles");
        statusFilter.setValue("Tous");
        sortFilter.setValue("ID");
        orderFilter.setValue("A-Z");
        currentPage = 1;
        applyFilters();
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String role   = roleFilter.getValue();
        String status = statusFilter.getValue();

        List<user> result = allUsers.stream()
                .filter(u -> search.isEmpty()
                        || (u.getNom()    != null && u.getNom().toLowerCase().contains(search))
                        || (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(search))
                        || (u.getEmail()  != null && u.getEmail().toLowerCase().contains(search)))
                .filter(u -> role == null || "Tous les rôles".equals(role) || matchRole(u.getRoles(), role))
                .filter(u -> status == null || "Tous".equals(status) || matchStatus(u, status))
                .collect(Collectors.toList());

        // Sort
        String sortOpt = sortFilter.getValue();
        String ordOpt = orderFilter.getValue();
        if (sortOpt != null && ordOpt != null) {
            Comparator<user> c = switch (sortOpt) {
                case "Nom" -> Comparator.comparing(u -> (u.getPrenom() + " " + u.getNom()).toLowerCase());
                case "Email" -> Comparator.comparing(user::getEmail, Comparator.nullsLast(String::compareToIgnoreCase));
                case "Date de naissance" -> Comparator.comparing(user::getDate_de_naissance, Comparator.nullsLast(String::compareTo));
                default -> Comparator.comparing(user::getId);
            };
            if ("Z-A".equals(ordOpt)) c = c.reversed();
            result.sort(c);
        }

        filtered.setAll(result);
        resultCountLabel.setText(result.size() + " utilisateur(s) trouvé(s)");
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
    private void handleView(user u) { showUserInfoDialog(u); }
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
        
        DatePicker dateField = new DatePicker();
        dateField.setPromptText("Sélectionner une date");
        dateField.setPrefWidth(260);
        dateField.getEditor().setStyle("-fx-background-color:#161921; -fx-text-fill:#c8cfe8; -fx-prompt-text-fill:#3a4060; -fx-font-size:13px;");
        dateField.setStyle("-fx-background-color:#161921; -fx-border-color:#1e2130; -fx-border-width:1; -fx-border-radius:8; -fx-background-radius:8;");

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
        classeCombo.setCellFactory(lv -> new ListCell<Classe>() {
            @Override protected void updateItem(Classe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                setText(item == null ? "— Aucune classe —" : item.getNom() + "  (ID " + item.getId() + ")");
                setStyle("-fx-background-color:#161921; -fx-text-fill:#c8cfe8; -fx-font-size:13px;");
            }
        });
        classeCombo.setButtonCell(new ListCell<Classe>() {
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
            
            if (existing.getDate_de_naissance() != null && !existing.getDate_de_naissance().isEmpty()) {
                try {
                    dateField.setValue(java.time.LocalDate.parse(existing.getDate_de_naissance()));
                } catch (Exception ex) { /* in case of malformed date */ }
            }
            
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

        // ── Grid layout & Individual Error Labels ──────────────
        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        grid.setPadding(new Insets(20, 28, 16, 28));
        grid.setStyle("-fx-background-color:#111318;");

        // Left: avatar section
        grid.add(avatarSection, 0, 0, 1, 9);
        
        // Helper to create hidden error labels
        java.util.function.Supplier<Label> createErr = () -> {
            Label l = new Label();
            l.setStyle("-fx-text-fill:#ff4d6d; -fx-font-size:11px;");
            l.setVisible(false); l.setManaged(false);
            return l;
        };

        Label nomErr = createErr.get();
        Label prenomErr = createErr.get();
        Label emailErr = createErr.get();
        Label dateErr = createErr.get();
        Label passErr = createErr.get();

        VBox nomBox = new VBox(2, nomField, nomErr);
        VBox prenomBox = new VBox(2, prenomField, prenomErr);
        VBox emailBox = new VBox(2, emailField, emailErr);
        VBox telBox = new VBox(2, telField);
        VBox dateBox = new VBox(2, dateField, dateErr);
        VBox passBox = new VBox(2, passField, passErr);

        // Right: form fields
        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(8);
        form.add(dialogLabel("Nom *"),            0, 0); form.add(nomBox,      1, 0);
        form.add(dialogLabel("Prénom *"),         0, 1); form.add(prenomBox,   1, 1);
        form.add(dialogLabel("Email *"),          0, 2); form.add(emailBox,    1, 2);
        form.add(dialogLabel("Téléphone"),        0, 3); form.add(telBox,      1, 3);
        form.add(dialogLabel("Date *"),           0, 4); form.add(dateBox,     1, 4);
        form.add(dialogLabel("Sexe *"),           0, 5); form.add(sexeCombo,   1, 5);
        form.add(dialogLabel("Rôle *"),           0, 6); form.add(roleCombo,   1, 6);
        form.add(dialogLabel("Classe"),           0, 7); form.add(classeCombo, 1, 7);
        form.add(dialogLabel("Mot de passe"),     0, 8); form.add(passBox,     1, 8);
        form.setStyle("-fx-background-color:#111318;");

        // ── Validation logic (Contrôle de saisie live) ────────
        Runnable validate = () -> {
            boolean hasError = false;
            String n  = nomField.getText().trim();
            String p  = prenomField.getText().trim();
            String e  = emailField.getText().trim();
            String d  = dateField.getValue() != null ? dateField.getValue().toString() : "";
            String pw = passField.getText();

            String styleErr = dialogFieldStyle() + " -fx-border-color:#ff4d6d; -fx-effect:dropshadow(two-pass-box, #ff4d6d40, 6, 0,0,0);";
            String styleOk  = dialogFieldStyle() + " -fx-border-color:#34d399; -fx-effect:dropshadow(two-pass-box, #34d39920, 6, 0,0,0);";
            
            String dateStyleErr = "-fx-background-color:#161921; -fx-border-radius:8; -fx-background-radius:8; -fx-border-color:#ff4d6d; -fx-effect:dropshadow(two-pass-box, #ff4d6d40, 6, 0,0,0);";
            String dateStyleOk = "-fx-background-color:#161921; -fx-border-radius:8; -fx-background-radius:8; -fx-border-color:#34d399; -fx-effect:dropshadow(two-pass-box, #34d39920, 6, 0,0,0);";

            if (n.isEmpty() || !n.matches("[a-zA-ZÀ-ÿ\\s\\-]{2,}")) { nomField.setStyle(styleErr); nomErr.setText("Nom (lettres uniquement, min 2)"); nomErr.setVisible(true); nomErr.setManaged(true); hasError = true; }
            else { nomField.setStyle(styleOk); nomErr.setVisible(false); nomErr.setManaged(false); }

            if (p.isEmpty() || !p.matches("[a-zA-ZÀ-ÿ\\s\\-]{2,}")) { prenomField.setStyle(styleErr); prenomErr.setText("Prénom (lettres uniquement, min 2)"); prenomErr.setVisible(true); prenomErr.setManaged(true); hasError = true; }
            else { prenomField.setStyle(styleOk); prenomErr.setVisible(false); prenomErr.setManaged(false); }

            if (e.isEmpty() || !e.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) { emailField.setStyle(styleErr); emailErr.setText("Veuillez saisir un email valide"); emailErr.setVisible(true); emailErr.setManaged(true); hasError = true; }
            else { emailField.setStyle(styleOk); emailErr.setVisible(false); emailErr.setManaged(false); }

            if (d.isEmpty()) { dateField.setStyle(dateStyleErr); dateErr.setText("Sélectionnez votre date"); dateErr.setVisible(true); dateErr.setManaged(true); hasError = true; }
            else { dateField.setStyle(dateStyleOk); dateErr.setVisible(false); dateErr.setManaged(false); }

            if (!isEdit && pw.isEmpty()) { passField.setStyle(styleErr); passErr.setText("Mot de passe obligatoire"); passErr.setVisible(true); passErr.setManaged(true); hasError = true; }
            else if (!pw.isEmpty() && pw.length() < 6) { passField.setStyle(styleErr); passErr.setText("Trop court (6 car. minimum)"); passErr.setVisible(true); passErr.setManaged(true); hasError = true; }
            else { passField.setStyle(pw.isEmpty() ? dialogFieldStyle() : styleOk); passErr.setVisible(false); passErr.setManaged(false); }

            okBtn.setDisable(hasError);

            if (!hasError) {
                okBtn.setStyle("-fx-background-color:#34d399; -fx-text-fill:#0d0f14; -fx-font-weight:700; -fx-background-radius:8; -fx-padding:8 20; -fx-effect:dropshadow(two-pass-box, #34d39960, 8, 0,0,0);");
            } else {
                okBtn.setStyle("-fx-background-color:#1e2130; -fx-text-fill:#6b7394; -fx-font-weight:700; -fx-background-radius:8; -fx-padding:8 20;");
            }
        };

        nomField.textProperty().addListener((obs, old, nv) -> validate.run());
        prenomField.textProperty().addListener((obs, old, nv) -> validate.run());
        emailField.textProperty().addListener((obs, old, nv) -> validate.run());
        dateField.valueProperty().addListener((obs, old, nv) -> validate.run()); // for DatePicker
        passField.textProperty().addListener((obs, old, nv) -> validate.run());

        grid.add(form, 1, 0);
        pane.setContent(grid);
        
        validate.run(); // Lancement pour afficher les états dès l'ouverture

        // ── Result converter ──────────────────────────────────
        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() != ButtonBar.ButtonData.OK_DONE) return null;

            String nom    = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email  = emailField.getText().trim();
            String date   = dateField.getValue() != null ? dateField.getValue().toString() : "";
            String sexe   = sexeCombo.getValue();
            String role   = roleCombo.getValue() != null
                    ? "[\"" + roleCombo.getValue() + "\"]" : "[\"ROLE_USER\"]";
            String tel    = telField.getText().trim();
            String pass   = passField.getText();

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

    // ── View User Info Dialog ───────────────────────────────────
    private void showUserInfoDialog(user u) {
        Dialog<Void> infoDialog = new Dialog<>();
        infoDialog.setTitle("Détails Utilisateur : " + u.getPrenom() + " " + u.getNom());
        infoDialog.setHeaderText(null);

        DialogPane pane = infoDialog.getDialogPane();
        pane.setStyle("-fx-background-color:#111318; -fx-border-color:#1e2130; -fx-border-width:1;");
        pane.setPrefWidth(500);
        pane.getButtonTypes().add(ButtonType.CLOSE);

        Button closeBtn = (Button) pane.lookupButton(ButtonType.CLOSE);
        if (closeBtn != null) {
            closeBtn.setStyle("-fx-background-color:#1e2130; -fx-text-fill:#6b7394; "
                    + "-fx-background-radius:8; -fx-border-color:#272c3d; -fx-border-width:1; -fx-padding:8 20;");
        }

        // Profile Picture
        StackPane avatarBox = new StackPane();
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPadding(new Insets(10, 0, 20, 0));

        Label initialsLbl = new Label(initials(u.getPrenom(), u.getNom()));
        initialsLbl.setStyle("-fx-background-color:#3b2fc9; -fx-text-fill:white; "
                + "-fx-background-radius:40; -fx-min-width:80; -fx-min-height:80; "
                + "-fx-max-width:80; -fx-max-height:80; "
                + "-fx-font-size:26px; -fx-font-weight:700; -fx-alignment:CENTER;");

        ImageView picPreview = new ImageView();
        picPreview.setFitWidth(80); picPreview.setFitHeight(80);
        picPreview.setPreserveRatio(true);
        Circle clip = new Circle(40, 40, 40);
        picPreview.setClip(clip);

        boolean hasPic = false;
        if (u.getProfile_pic() != null && !u.getProfile_pic().isBlank()) {
            File f = new File(UPLOAD_DIR + u.getProfile_pic());
            if (f.exists()) {
                picPreview.setImage(new Image(f.toURI().toString(), false));
                hasPic = true;
            } else {
                try {
                    picPreview.setImage(new Image("file:///" + UPLOAD_DIR.replace("\\", "/") + u.getProfile_pic(), false));
                    hasPic = true;
                } catch (Exception ignored) {}
            }
        }
        picPreview.setVisible(hasPic);
        initialsLbl.setVisible(!hasPic);
        avatarBox.getChildren().addAll(initialsLbl, picPreview);

        // Details Grid
        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color:#161921; -fx-background-radius:8;");

        int row = 0;
        grid.add(infoLabel("ID :"), 0, row); grid.add(infoValue("#" + u.getId()), 1, row++);
        grid.add(infoLabel("Prénom & Nom :"), 0, row); grid.add(infoValue(u.getPrenom() + " " + u.getNom()), 1, row++);
        grid.add(infoLabel("Email :"), 0, row); grid.add(infoValue(u.getEmail()), 1, row++);
        grid.add(infoLabel("Téléphone :"), 0, row); grid.add(infoValue(u.getNum_tel() == null || u.getNum_tel().isEmpty() ? "Non renseigné" : u.getNum_tel()), 1, row++);
        grid.add(infoLabel("Date de naissance :"), 0, row); grid.add(infoValue(u.getDate_de_naissance() == null ? "Non renseignée" : u.getDate_de_naissance()), 1, row++);
        grid.add(infoLabel("Sexe :"), 0, row); grid.add(infoValue(u.getSexe() == null ? "Non renseigné" : u.getSexe()), 1, row++);

        String className = "—";
        if (u.getClasse_id() != null) {
            try {
                Classe c = classeService.getById(u.getClasse_id());
                if (c != null && c.getNom() != null) className = c.getNom();
            } catch (Exception ignored) {}
        }
        grid.add(infoLabel("Classe :"), 0, row); grid.add(infoValue(className), 1, row++);
        grid.add(infoLabel("Rôles :"), 0, row); grid.add(infoValue(formatRole(u.getRoles())), 1, row++);
        grid.add(infoLabel("Vérifié :"), 0, row); grid.add(infoValue(u.getIs_verified() == 1 ? "Oui ✅" : "Non ❌"), 1, row++);
        
        grid.add(infoLabel("Statut :"), 0, row);
        if (u.getIs_banned() == 1) {
            Label bannedLbl = infoValue("Banni 🚫 (" + u.getBan_reason() + ")");
            bannedLbl.setStyle("-fx-text-fill: #ff4d6d; -fx-font-size: 13px; -fx-font-weight: bold;");
            grid.add(bannedLbl, 1, row++);
        } else {
            Label activeLbl = infoValue("Actif ✅");
            activeLbl.setStyle("-fx-text-fill: #34d399; -fx-font-size: 13px; -fx-font-weight: bold;");
            grid.add(activeLbl, 1, row++);
        }
        grid.add(infoLabel("Date Inscription :"), 0, row); grid.add(infoValue(u.getCreated_at() == null ? "N/A" : u.getCreated_at()), 1, row++);

        VBox content = new VBox(avatarBox, grid);
        content.setPadding(new Insets(10));
        pane.setContent(content);

        infoDialog.showAndWait();
    }

    private Label infoLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #6b7394; -fx-font-size: 13px; -fx-font-weight: 700;");
        return l;
    }

    private Label infoValue(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #c8cfe8; -fx-font-size: 13px;");
        return l;
    }

    // ── Animations ─────────────────────────────────────────────
    private void animateEntrance() {
        if (userTable == null) return;
        userTable.setTranslateY(30);
        userTable.setOpacity(0);
        
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(500), userTable);
        tt.setToY(0);
        tt.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), userTable);
        ft.setFromValue(0);
        ft.setToValue(1);
        
        javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(tt, ft);
        pt.play();
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
        return new TableCell<user, String>() {
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

    private Button createSolidIconButton(String text, String bgHex, String fgHex) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + bgHex + "; -fx-text-fill:" + fgHex + "; "
                + "-fx-background-radius:6; -fx-font-size:14px; -fx-padding:4 8; -fx-cursor:hand;");
        b.setMinSize(30, 28);
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

    // ── Streak Re-Engagement Emails ───────────────────────────────────────────
    @FXML
    private void handleSendStreakEmails() {
        int inactiveDays = 1;
        var targets = streakEngagementService.findInactiveStreakUsers(inactiveDays);

        if (targets.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION,
                      "Aucun utilisateur ciblé",
                      "Tous les utilisateurs actifs se sont connectés récemment.\n" +
                      "Aucun email de re-engagement n'est nécessaire pour l'instant.");
            return;
        }

        // Build confirmation list
        StringBuilder sb = new StringBuilder();
        sb.append(targets.size()).append(" utilisateur(s) ciblé(s) :\n\n");
        int shown = 0;
        for (var u : targets) {
            if (shown >= 8) { sb.append("  … et ").append(targets.size() - shown).append(" autre(s)\n"); break; }
            sb.append("  🔥 ").append(u.getPrenom()).append(" ").append(u.getNom())
              .append(" (streak=").append(u.getCurrentStreak()).append(") — ").append(u.getEmail()).append("\n");
            shown++;
        }
        sb.append("\nEnvoyer les emails de re-engagement maintenant ?");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Envoyer les emails de streak");
        confirm.setHeaderText("🔥 Re-Engagement des utilisateurs inactifs");
        confirm.setContentText(sb.toString());
        styleAlert(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;

            // Run in background thread so UI stays responsive
            Thread emailThread = new Thread(() -> {
                String summary = streakEngagementService.sendEngagementEmails(inactiveDays);
                javafx.application.Platform.runLater(() ->
                    showAlert(Alert.AlertType.INFORMATION,
                              "Emails envoyés",
                              "Résultat de l'envoi :\n" + summary.replace(",", "\n"))
                );
            });
            emailThread.setDaemon(true);
            emailThread.setName("streak-email-sender");
            emailThread.start();
        });
    }
}

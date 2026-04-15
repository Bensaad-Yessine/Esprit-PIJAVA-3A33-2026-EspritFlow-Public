package piJava.Controllers.backoffice.Classe;

import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import piJava.entities.Classe;
import piJava.services.ClasseService;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ClasseContentController implements Initializable {

    // ── Header ─────────────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private Button    addClasseBtn;

    // ── Mini stats ─────────────────────────────────────────────
    @FXML private Label totalClassesLabel;
    @FXML private Label niveauxLabel;
    @FXML private Label filieresLabel;
    @FXML private Label anneeLabel;

    // ── Filters ────────────────────────────────────────────────
    @FXML private ComboBox<String> niveauFilter;
    @FXML private ComboBox<String> filiereFilter;
    @FXML private ComboBox<String> anneeFilter;
    @FXML private Button           resetFilterBtn;
    @FXML private Label            resultCountLabel;

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Classe>            classeTable;
    @FXML private TableColumn<Classe, String>  idCol;
    @FXML private TableColumn<Classe, String>  nomCol;
    @FXML private TableColumn<Classe, String>  niveauCol;
    @FXML private TableColumn<Classe, String>  filiereCol;
    @FXML private TableColumn<Classe, String>  anneeCol;
    @FXML private TableColumn<Classe, String>  descCol;
    @FXML private TableColumn<Classe, Void>    actionsCol;

    // ── Footer ─────────────────────────────────────────────────
    @FXML private Label  footerLabel;
    @FXML private Button prevBtn;
    @FXML private Label  pageLabel;
    @FXML private Button nextBtn;

    // ── State ──────────────────────────────────────────────────
    private final ClasseService classeService = new ClasseService();
    private ObservableList<Classe> allClasses   = FXCollections.observableArrayList();
    private ObservableList<Classe> filtered     = FXCollections.observableArrayList();

    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;

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
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label("#" + item);
                lbl.setStyle("-fx-font-family: 'Syne'; -fx-font-size: 11px; "
                           + "-fx-text-fill: #3a4060; -fx-font-weight: 700;");
                setGraphic(lbl);
            }
        });

        nomCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        nomCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("-fx-font-family: 'DM Sans'; -fx-font-size: 14px; "
                           + "-fx-font-weight: 600; -fx-text-fill: #eef0f8;");
                setGraphic(lbl);
            }
        });

        niveauCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNiveau()));
        niveauCol.setCellFactory(col -> badgeCell("#3b82f615", "#3b82f640", "#60a5fa"));

        filiereCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFiliere()));
        filiereCol.setCellFactory(col -> badgeCell("#00e5c815", "#00e5c840", "#00e5c8"));

        anneeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAnneeUniversitaire()));
        anneeCol.setCellFactory(col -> badgeCell("#a855f715", "#a855f740", "#c084fc"));

        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        descCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String truncated = item.length() > 40 ? item.substring(0, 40) + "…" : item;
                Label lbl = new Label(truncated);
                lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7394;");
                Tooltip.install(lbl, new Tooltip(item));
                setGraphic(lbl);
            }
        });

        actionsCol.setCellFactory(col -> new TableCell<>() {
            final Button editBtn   = styledBtn("✏  Modifier",  "btn-edit");
            final Button deleteBtn = styledBtn("🗑  Suppr.",   "btn-delete");
            final HBox   box       = new HBox(8, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER_LEFT);
                box.setPadding(new Insets(0, 8, 0, 8));

                editBtn.setOnAction(e -> {
                    Classe c = getTableView().getItems().get(getIndex());
                    handleEdit(c);
                });
                deleteBtn.setOnAction(e -> {
                    Classe c = getTableView().getItems().get(getIndex());
                    handleDelete(c);
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ── Data Loading ───────────────────────────────────────────
    private void loadData() {
        try {
            allClasses.setAll(classeService.getAllClasses());
            populateFilterOptions();
            applyFilters();
            updateStats();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                      "Impossible de charger les classes : " + e.getMessage());
        }
    }

    private void updateStats() {
        totalClassesLabel.setText(String.valueOf(allClasses.size()));

        long niveaux = allClasses.stream()
                .map(Classe::getNiveau).filter(Objects::nonNull).distinct().count();
        niveauxLabel.setText(String.valueOf(niveaux));

        long filieres = allClasses.stream()
                .map(Classe::getFiliere).filter(Objects::nonNull).distinct().count();
        filieresLabel.setText(String.valueOf(filieres));

        allClasses.stream()
                .map(Classe::getAnneeUniversitaire).filter(Objects::nonNull).findFirst()
                .ifPresent(anneeLabel::setText);

        animateCounter(totalClassesLabel, 0, allClasses.size());
    }

    // ── Filters ────────────────────────────────────────────────
    private void populateFilterOptions() {
        List<String> niveaux = allClasses.stream()
                .map(Classe::getNiveau).filter(Objects::nonNull).distinct().sorted()
                .collect(Collectors.toList());
        niveauFilter.getItems().setAll(niveaux);

        List<String> filieres = allClasses.stream()
                .map(Classe::getFiliere).filter(Objects::nonNull).distinct().sorted()
                .collect(Collectors.toList());
        filiereFilter.getItems().setAll(filieres);

        List<String> annees = allClasses.stream()
                .map(Classe::getAnneeUniversitaire).filter(Objects::nonNull).distinct().sorted()
                .collect(Collectors.toList());
        anneeFilter.getItems().setAll(annees);
    }

    private void setupFilters() {
        niveauFilter.valueProperty().addListener((o, ov, nv) -> { currentPage = 1; applyFilters(); });
        filiereFilter.valueProperty().addListener((o, ov, nv) -> { currentPage = 1; applyFilters(); });
        anneeFilter.valueProperty().addListener((o, ov, nv) -> { currentPage = 1; applyFilters(); });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((o, ov, nv) -> { currentPage = 1; applyFilters(); });
    }

    private void applyFilters() {
        String search  = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String niveau  = niveauFilter.getValue();
        String filiere = filiereFilter.getValue();
        String annee   = anneeFilter.getValue();

        List<Classe> result = allClasses.stream()
            .filter(c -> search.isEmpty()
                || (c.getNom() != null && c.getNom().toLowerCase().contains(search))
                || (c.getFiliere() != null && c.getFiliere().toLowerCase().contains(search))
                || (c.getNiveau() != null && c.getNiveau().toLowerCase().contains(search)))
            .filter(c -> niveau  == null || niveau.equals(c.getNiveau()))
            .filter(c -> filiere == null || filiere.equals(c.getFiliere()))
            .filter(c -> annee   == null || annee.equals(c.getAnneeUniversitaire()))
            .collect(Collectors.toList());

        filtered.setAll(result);
        resultCountLabel.setText(result.size() + " résultat(s)");
        refreshPage();
    }

    // ── Pagination ─────────────────────────────────────────────
    private void refreshPage() {
        int total = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage = Math.min(currentPage, totalPages);

        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);
        classeTable.setItems(FXCollections.observableArrayList(filtered.subList(from, to)));

        footerLabel.setText("Affichage de " + (total == 0 ? 0 : from + 1) + "–" + to + " sur " + total + " entrées");
        pageLabel.setText(String.valueOf(currentPage));
        prevBtn.setDisable(currentPage <= 1);
        nextBtn.setDisable(currentPage >= totalPages);
    }

    @FXML private void handlePrev() { if (currentPage > 1) { currentPage--; refreshPage(); } }
    @FXML private void handleNext() { currentPage++; refreshPage(); }

    // ── CRUD ───────────────────────────────────────────────────
    @FXML
    private void handleAdd() {
        showClasseDialog(null);
    }

    private void handleEdit(Classe classe) {
        showClasseDialog(classe);
    }

    private void handleDelete(Classe classe) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer « " + classe.getNom() + " » ?");
        confirm.setContentText("Cette action est irréversible.");
        styleAlert(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    classeService.delete(classe.getId());
                    loadData();
                    showToast("Classe supprimée avec succès");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML private void handleResetFilters() {
        niveauFilter.setValue(null);
        filiereFilter.setValue(null);
        anneeFilter.setValue(null);
        searchField.clear();
        currentPage = 1;
        applyFilters();
    }

    // ── Dialog for Add / Edit ──────────────────────────────────
    private void showClasseDialog(Classe existing) {
        boolean isEdit = existing != null;

        Dialog<Classe> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier la classe" : "Nouvelle classe");
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

        TextField nomField   = dialogField("ex: 3GL A");
        TextField nivField   = dialogField("ex: 3ème année");
        TextField filField   = dialogField("ex: Génie Logiciel");
        TextField anneeField = dialogField("ex: 2024–2025");
        TextArea  descArea   = new TextArea();
        descArea.setPromptText("Description de la classe...");
        descArea.setPrefRowCount(3);
        descArea.setStyle(dialogFieldStyle());
        descArea.setWrapText(true);

        if (isEdit) {
            nomField.setText(existing.getNom());
            nivField.setText(existing.getNiveau());
            filField.setText(existing.getFiliere());
            anneeField.setText(existing.getAnneeUniversitaire());
            descArea.setText(existing.getDescription());
        }

        grid.add(dialogLabel("Nom de la classe *"), 0, 0); grid.add(nomField,   1, 0);
        grid.add(dialogLabel("Niveau *"),            0, 1); grid.add(nivField,   1, 1);
        grid.add(dialogLabel("Filière"),             0, 2); grid.add(filField,   1, 2);
        grid.add(dialogLabel("Année universitaire"), 0, 3); grid.add(anneeField, 1, 3);
        grid.add(dialogLabel("Description"),         0, 4); grid.add(descArea,   1, 4);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Classe c = isEdit ? existing : new Classe();
                c.setNom(nomField.getText().trim());
                c.setNiveau(nivField.getText().trim());
                c.setFiliere(filField.getText().trim());
                c.setAnneeUniversitaire(anneeField.getText().trim());
                c.setDescription(descArea.getText().trim());
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            if (c.getNom().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champ requis", "Le nom de la classe est obligatoire.");
                return;
            }
            try {
                if (isEdit) {
                    classeService.edit(c);
                    showToast("Classe modifiée avec succès");
                } else {
                    classeService.add(c);
                    showToast("Classe créée avec succès");
                }
                loadData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        });
    }

    // ── Animations ─────────────────────────────────────────────
    private void animateEntrance() {
        classeTable.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), classeTable);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(200));
        ft.play();
    }

    private void animateCounter(Label label, int from, int to) {
        Timeline tl = new Timeline();
        int steps = 20;
        for (int i = 0; i <= steps; i++) {
            final int val = from + (int) ((to - from) * (i / (double) steps));
            KeyFrame kf = new KeyFrame(Duration.millis(i * 30L), e -> label.setText(String.valueOf(val)));
            tl.getKeyFrames().add(kf);
        }
        tl.play();
    }

    private void showToast(String message) {
        // Simple fade label as toast feedback
        Label toast = new Label("✓  " + message);
        toast.setStyle("-fx-background-color: #00e5c820; -fx-border-color: #00e5c850; "
                     + "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; "
                     + "-fx-text-fill: #00e5c8; -fx-font-size: 13px; -fx-font-weight: 600; "
                     + "-fx-padding: 10 20; -fx-translate-x: 0;");
        // In a real app, add this to a StackPane overlay; here we log it
        System.out.println("[TOAST] " + message);
    }

    // ── Helpers ────────────────────────────────────────────────
    private TableCell<Classe, String> badgeCell(String bg, String border, String fg) {
        return new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setStyle(
                    "-fx-background-color: " + bg + "; " +
                    "-fx-border-color: " + border + "; " +
                    "-fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; " +
                    "-fx-text-fill: " + fg + "; " +
                    "-fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 3 10;"
                );
                setGraphic(badge);
            }
        };
    }

    private Button styledBtn(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().add(styleClass);
        return btn;
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
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7394; "
                   + "-fx-font-weight: 600; -fx-min-width: 150;");
        return lbl;
    }

    private void styleAlert(Alert alert) {
        alert.getDialogPane().setStyle(
            "-fx-background-color: #111318; -fx-border-color: #1e2130; -fx-border-width: 1;"
        );
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        styleAlert(a);
        a.showAndWait();
    }
}

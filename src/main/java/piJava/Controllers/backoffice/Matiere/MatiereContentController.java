package piJava.Controllers.backoffice.Matiere;

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
import piJava.entities.Matiere;
import piJava.services.MatiereService;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class MatiereContentController implements Initializable {

    // ── Header ─────────────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private Button    addMatiereBtn;

    // ── Mini stats ─────────────────────────────────────────────
    @FXML private Label totalMatieresLabel;
    @FXML private Label avgCoeffLabel;
    @FXML private Label totalHeuresLabel;
    @FXML private Label avgComplexiteLabel;

    // ── Filters ────────────────────────────────────────────────
    @FXML private ComboBox<String> coeffFilter;
    @FXML private ComboBox<String> complexiteFilter;
    @FXML private Button           resetFilterBtn;
    @FXML private Label            resultCountLabel;

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Matiere>            matiereTable;
    @FXML private TableColumn<Matiere, String>  idCol;
    @FXML private TableColumn<Matiere, String>  nomCol;
    @FXML private TableColumn<Matiere, String>  coeffCol;
    @FXML private TableColumn<Matiere, String>  heuresCol;
    @FXML private TableColumn<Matiere, String>  complexiteCol;
    @FXML private TableColumn<Matiere, String>  descCol;
    @FXML private TableColumn<Matiere, Void>    actionsCol;

    // ── Wikipedia Area ──────────────────────────────────────────
    @FXML private VBox wikipediaBox;
    @FXML private Label wikiSubjectLabel;
    @FXML private Label wikiContentLabel;
    @FXML private Hyperlink wikiLinkLabel;

    // ── Footer ─────────────────────────────────────────────────
    @FXML private Label  footerLabel;
    @FXML private Button prevBtn;
    @FXML private Label  pageLabel;
    @FXML private Button nextBtn;

    // ── State ──────────────────────────────────────────────────
    private final MatiereService matiereService = new MatiereService();
    private ObservableList<Matiere> allMatieres = FXCollections.observableArrayList();
    private ObservableList<Matiere> filtered    = FXCollections.observableArrayList();

    private StackPane contentArea;

    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    // ───────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadData();
        setupSearch();
        setupFilters();
        animateEntrance();
        
        // Cacher la zone Wikipedia au début
        if (wikipediaBox != null) {
            wikipediaBox.setVisible(false);
            wikipediaBox.setManaged(false);
        }

        // Action lors du clic sur une ligne
        matiereTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showWikipediaInfo(newSelection);
            }
        });
    }

    private void showWikipediaInfo(Matiere m) {
        if (wikipediaBox == null) return;

        wikiSubjectLabel.setText(m.getNom());
        wikiContentLabel.setText("Chargement des informations depuis Wikipédia...");
        wikipediaBox.setVisible(true);
        wikipediaBox.setManaged(true);

        // Appel API asynchrone pour ne pas bloquer l'UI
        new Thread(() -> {
            String summary = piJava.api.WikipediaApi.getSummary(m.getNom());
            javafx.application.Platform.runLater(() -> {
                wikiContentLabel.setText(summary);
                wikiLinkLabel.setOnAction(e -> {
                    try {
                        String url = "https://fr.wikipedia.org/wiki/" + java.net.URLEncoder.encode(m.getNom().replace(" ", "_"), "UTF-8");
                        new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            });
        }).start();
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
        nomCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        nomCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("-fx-font-size:14px; -fx-font-weight:600; -fx-text-fill:#eef0f8;");
                setGraphic(lbl);
            }
        });

        // COEFFICIENT — violet badge
        coeffCol.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getCoefficient())));
        coeffCol.setCellFactory(col -> badgeCell("#a78bfa15", "#a78bfa40", "#a78bfa"));

        // HEURES
        heuresCol.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getChargehoraire() + "h"));
        heuresCol.setCellFactory(col -> badgeCell("#3b82f615", "#3b82f640", "#60a5fa"));

        // COMPLEXITÉ — color-coded by level
        complexiteCol.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getScorecomplexite())));
        complexiteCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                int score = Integer.parseInt(item);
                String[] style = complexiteStyle(score);
                Label badge = new Label("● " + item + "/10");
                badge.setStyle(
                    "-fx-background-color:" + style[0] + "; -fx-border-color:" + style[1] + "; "
                  + "-fx-border-width:1; -fx-border-radius:6; -fx-background-radius:6; "
                  + "-fx-text-fill:" + style[2] + "; "
                  + "-fx-font-size:11px; -fx-font-weight:700; -fx-padding:3 10;"
                );
                setGraphic(badge);
            }
        });

        // DESCRIPTION
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        descCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String truncated = item.length() > 45 ? item.substring(0, 45) + "…" : item;
                Label lbl = new Label(truncated);
                lbl.setStyle("-fx-font-size:12px; -fx-text-fill:#6b7394;");
                Tooltip.install(lbl, new Tooltip(item));
                setGraphic(lbl);
            }
        });

        // ACTIONS
        actionsCol.setCellFactory(col -> new TableCell<>() {
            final Button editBtn   = actionBtn("✏  Modifier", "btn-edit");
            final Button deleteBtn = actionBtn("🗑  Suppr.",  "btn-delete");
            final Button quizBtn   = actionBtn("📝 Quiz",  "btn-quiz");
            final HBox   box       = new HBox(8, editBtn, deleteBtn, quizBtn);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                box.setPadding(new Insets(0, 8, 0, 8));
                editBtn.setOnAction(e   -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                quizBtn.setOnAction(e   -> handleManageQuiz(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ── Data ───────────────────────────────────────────────────
    private void loadData() {
        try {
            allMatieres.setAll(matiereService.show());
            populateFilterOptions();
            applyFilters();
            updateStats();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void updateStats() {
        totalMatieresLabel.setText(String.valueOf(allMatieres.size()));

        double avgCoeff = allMatieres.stream()
                .mapToDouble(Matiere::getCoefficient).average().orElse(0);
        avgCoeffLabel.setText(String.format("%.1f", avgCoeff));

        int totalH = allMatieres.stream().mapToInt(Matiere::getChargehoraire).sum();
        totalHeuresLabel.setText(totalH + "h");

        double avgC = allMatieres.stream()
                .mapToInt(Matiere::getScorecomplexite).average().orElse(0);
        avgComplexiteLabel.setText(String.format("%.1f", avgC));

        animateCounter(totalMatieresLabel, 0, allMatieres.size());
    }

    // ── Filters ────────────────────────────────────────────────
    private void populateFilterOptions() {
        // Coefficient buckets
        coeffFilter.getItems().setAll("≤ 2", "2.5 – 3", "3.5 – 4", "> 4");

        // Complexity buckets
        complexiteFilter.getItems().setAll("Facile (1–3)", "Moyen (4–6)", "Difficile (7–8)", "Extrême (9–10)");
    }

    private void setupFilters() {
        coeffFilter.valueProperty().addListener((o, ov, nv)       -> { currentPage = 1; applyFilters(); });
        complexiteFilter.valueProperty().addListener((o, ov, nv)  -> { currentPage = 1; applyFilters(); });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((o, ov, nv) -> { currentPage = 1; applyFilters(); });
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String coeff  = coeffFilter.getValue();
        String compl  = complexiteFilter.getValue();

        List<Matiere> result = allMatieres.stream()
            .filter(m -> search.isEmpty()
                || (m.getNom()         != null && m.getNom().toLowerCase().contains(search))
                || (m.getDescription() != null && m.getDescription().toLowerCase().contains(search)))
            .filter(m -> coeff == null || matchCoeff(m.getCoefficient(), coeff))
            .filter(m -> compl == null || matchComplexite(m.getScorecomplexite(), compl))
            .collect(Collectors.toList());

        filtered.setAll(result);
        resultCountLabel.setText(result.size() + " résultat(s)");
        refreshPage();
    }

    private boolean matchCoeff(double c, String bucket) {
        return switch (bucket) {
            case "≤ 2"      -> c <= 2;
            case "2.5 – 3"  -> c >= 2.5 && c <= 3;
            case "3.5 – 4"  -> c >= 3.5 && c <= 4;
            case "> 4"       -> c > 4;
            default          -> true;
        };
    }

    private boolean matchComplexite(int s, String bucket) {
        return switch (bucket) {
            case "Facile (1–3)"    -> s >= 1  && s <= 3;
            case "Moyen (4–6)"     -> s >= 4  && s <= 6;
            case "Difficile (7–8)" -> s >= 7  && s <= 8;
            case "Extrême (9–10)"  -> s >= 9  && s <= 10;
            default                -> true;
        };
    }

    // ── Pagination ─────────────────────────────────────────────
    private void refreshPage() {
        int total      = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage    = Math.min(currentPage, totalPages);

        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);
        matiereTable.setItems(FXCollections.observableArrayList(filtered.subList(from, to)));

        footerLabel.setText("Affichage de " + (total == 0 ? 0 : from + 1) + "–" + to + " sur " + total + " entrées");
        pageLabel.setText(String.valueOf(currentPage));
        prevBtn.setDisable(currentPage <= 1);
        nextBtn.setDisable(currentPage >= totalPages);
    }

    @FXML private void handlePrev() { if (currentPage > 1) { currentPage--; refreshPage(); } }
    @FXML private void handleNext() { currentPage++; refreshPage(); }

    // ── CRUD ───────────────────────────────────────────────────
    @FXML private void handleAdd()              { showMatiereDialog(null); }
    private void handleEdit(Matiere m)          { showMatiereDialog(m); }

    @FXML private void handleResetFilters() {
        coeffFilter.setValue(null);
        complexiteFilter.setValue(null);
        searchField.clear();
        currentPage = 1;
        applyFilters();
    }

    private void handleDelete(Matiere m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer « " + m.getNom() + " » ?");
        confirm.setContentText("Cette action est irréversible.");
        styleAlert(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    matiereService.delete(m.getId());
                    loadData();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    private void handleManageQuiz(Matiere m) {
        if (contentArea == null) {
            System.err.println("contentArea is null in MatiereContentController.");
            return;
        }
        try {
            java.net.URL resource = getClass().getResource("/backoffice/quiz/QuizContent.fxml");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(resource);
            javafx.scene.layout.Region view = loader.load();

            piJava.Controllers.backoffice.quiz.QuizContentController controller = loader.getController();
            controller.setContentArea(contentArea);
            controller.initData(m);

            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Add / Edit Dialog ──────────────────────────────────────
    private void showMatiereDialog(Matiere existing) {
        boolean isEdit = existing != null;
        Dialog<Matiere> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier la matière" : "Nouvelle matière");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color:#111318; -fx-border-color:#1e2130; -fx-border-width:1;");
        pane.getButtonTypes().addAll(
            new ButtonType(isEdit ? "Enregistrer" : "Créer", ButtonBar.ButtonData.OK_DONE),
            ButtonType.CANCEL
        );

        Button okBtn = (Button) pane.lookupButton(pane.getButtonTypes().get(0));
        okBtn.setStyle("-fx-background-color:#a78bfa; -fx-text-fill:#0d0f14; "
                     + "-fx-font-weight:700; -fx-background-radius:8; -fx-padding:8 20;");
        Button cancelBtn = (Button) pane.lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle("-fx-background-color:#1e2130; -fx-text-fill:#6b7394; "
                         + "-fx-background-radius:8; -fx-border-color:#272c3d; "
                         + "-fx-border-width:1; -fx-border-radius:8; -fx-padding:8 20;");

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(14);
        grid.setPadding(new Insets(24, 28, 16, 28));
        grid.setStyle("-fx-background-color:#111318;");

        TextField nomField    = dialogField("ex: Algorithmique Avancée");
        TextField coeffField  = dialogField("ex: 3.5");
        TextField heuresField = dialogField("ex: 45");
        TextField scoreField  = dialogField("1 à 10");
        TextArea  descArea    = new TextArea();
        descArea.setPromptText("Description de la matière...");
        descArea.setPrefRowCount(3);
        descArea.setStyle(dialogFieldStyle());
        descArea.setWrapText(true);

        if (isEdit) {
            nomField.setText(existing.getNom());
            coeffField.setText(String.valueOf(existing.getCoefficient()));
            heuresField.setText(String.valueOf(existing.getChargehoraire()));
            scoreField.setText(String.valueOf(existing.getScorecomplexite()));
            descArea.setText(existing.getDescription());
        }

        grid.add(dialogLabel("Nom *"),              0, 0); grid.add(nomField,    1, 0);
        grid.add(dialogLabel("Coefficient *"),      0, 1); grid.add(coeffField,  1, 1);
        grid.add(dialogLabel("Charge horaire (h)"), 0, 2); grid.add(heuresField, 1, 2);
        grid.add(dialogLabel("Score complexité"),   0, 3); grid.add(scoreField,  1, 3);
        grid.add(dialogLabel("Description"),        0, 4); grid.add(descArea,    1, 4);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() != ButtonBar.ButtonData.OK_DONE) return null;
            try {
                Matiere mat = isEdit ? existing : new Matiere();
                mat.setNom(nomField.getText().trim());
                mat.setCoefficient(Double.parseDouble(coeffField.getText().trim()));
                mat.setChargehoraire(Integer.parseInt(heuresField.getText().trim()));
                mat.setScorecomplexite(Integer.parseInt(scoreField.getText().trim()));
                mat.setDescription(descArea.getText().trim());
                return mat;
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Valeur invalide",
                          "Coefficient et heures doivent être des nombres.");
                return null;
            }
        });

        dialog.showAndWait().ifPresent(mat -> {
            if (mat == null || mat.getNom().isEmpty()) return;
            try {
                if (isEdit) matiereService.edit(mat);
                else        matiereService.add(mat);
                loadData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        });
    }

    // ── Animations ─────────────────────────────────────────────
    private void animateEntrance() {
        matiereTable.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), matiereTable);
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
    /** Returns [bg, border, fg] for a complexity score */
    private String[] complexiteStyle(int score) {
        if (score <= 3) return new String[]{"#22c55e15", "#22c55e40", "#4ade80"};
        if (score <= 6) return new String[]{"#fbbf2415", "#fbbf2440", "#fbbf24"};
        if (score <= 8) return new String[]{"#f9731615", "#f9731640", "#fb923c"};
        return                 new String[]{"#ff4d6d15", "#ff4d6d40", "#ff4d6d"};
    }

    private TableCell<Matiere, String> badgeCell(String bg, String border, String fg) {
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
        tf.setPromptText(prompt);
        tf.setPrefWidth(260);
        tf.setStyle(dialogFieldStyle());
        return tf;
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
                 + "-fx-font-weight:600; -fx-min-width:150;");
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

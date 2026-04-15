package piJava.Controllers.backoffice.Seance;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import piJava.entities.Classe;
import piJava.entities.Matiere;
import piJava.entities.Salle;
import piJava.entities.Seance;
import piJava.services.ClasseService;
import piJava.services.MatiereService;
import piJava.services.SalleService;
import piJava.services.SeanceService;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class SeanceContentController implements Initializable {

    @FXML private Label lblTotalCount;
    @FXML private Label lblCoursCount;
    @FXML private Label lblPresentielCount;
    @FXML private Label lblSemaineCount;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboJour;
    @FXML private ComboBox<String> comboType;
    @FXML private ComboBox<String> comboMode;
    @FXML private ComboBox<String> comboTri;

    @FXML private TableView<Seance> seanceTable;
    @FXML private TableColumn<Seance, Integer> colId;
    @FXML private TableColumn<Seance, String> colMatiere;
    @FXML private TableColumn<Seance, String> colClasse;
    @FXML private TableColumn<Seance, String> colSalle;
    @FXML private TableColumn<Seance, String> colJour;
    @FXML private TableColumn<Seance, String> colHoraires;
    @FXML private TableColumn<Seance, String> colMode;
    @FXML private TableColumn<Seance, Void> colActions;

    private SeanceService seanceService;
    private MatiereService matiereService;
    private ClasseService classeService;
    private SalleService salleService;

    private ObservableList<Seance> masterData = FXCollections.observableArrayList();
    private List<Matiere> matieres = new ArrayList<>();
    private List<Classe> classes = new ArrayList<>();
    private List<Salle> salles = new ArrayList<>();

    private Map<Integer, String> matiereMap = new HashMap<>();
    private Map<Integer, String> classeMap = new HashMap<>();
    private Map<Integer, String> salleMap = new HashMap<>();

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        seanceService = new SeanceService();
        matiereService = new MatiereService();
        classeService = new ClasseService();
        salleService = new SalleService();

        loadMappings();
        setupColumns();
        setupFilters();
        loadData();
    }

    private void loadMappings() {
        try {
            matieres = matiereService.show();
            for (Matiere m : matieres) matiereMap.put(m.getId(), m.getNom());

            classes = classeService.getAllClasses();
            for (Classe c : classes) classeMap.put(c.getId(), c.getNom());

            salles = salleService.getAllSalles();
            for (Salle s : salles) salleMap.put(s.getId(), s.getName());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setStyle("-fx-alignment: center-left; -fx-text-fill: white;");

        colMatiere.setCellValueFactory(cell -> new SimpleStringProperty(matiereMap.getOrDefault(cell.getValue().getMatiereId(), "Unknown")));
        colMatiere.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        colClasse.setCellValueFactory(cell -> new SimpleStringProperty(classeMap.getOrDefault(cell.getValue().getClasseId(), "Unknown")));
        colClasse.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge-classe");
                    setGraphic(badge);
                }
            }
        });

        colSalle.setCellValueFactory(cell -> new SimpleStringProperty(salleMap.getOrDefault(cell.getValue().getSalleId(), "Unknown")));
        colSalle.setStyle("-fx-text-fill: white;");

        colJour.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getJour()));
        colJour.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge-jour");
                    setGraphic(badge);
                }
            }
        });

        colHoraires.setCellValueFactory(cell -> {
            Seance s = cell.getValue();
            if(s.getHeureDebut() != null && s.getHeureFin() != null) {
                String d = s.getHeureDebut().toLocalDateTime().format(timeFormatter);
                String f = s.getHeureFin().toLocalDateTime().format(timeFormatter);
                return new SimpleStringProperty(d + " - " + f);
            }
            return new SimpleStringProperty("--:-- - --:--");
        });
        colHoraires.setStyle("-fx-text-fill: white;");

        colMode.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMode()));
        colMode.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    if (item.toLowerCase().contains("présentiel")) {
                        badge.getStyleClass().add("badge-mode-presentiel");
                    } else if (item.toLowerCase().contains("ligne")) {
                        badge.getStyleClass().add("badge-mode-enligne");
                    } else {
                        badge.getStyleClass().add("badge-mode-hybride");
                    }
                    setGraphic(badge);
                }
            }
        });

        colActions.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER);

                    Button btnView = new Button("👁");
                    btnView.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5; -fx-min-width: 32px; -fx-min-height: 30px;");
                    btnView.setOnAction(e -> {
                        Seance s = getTableView().getItems().get(getIndex());
                        handleView(s);
                    });

                    Button btnQr = new Button("🔳");
                    btnQr.setStyle("-fx-background-color: #06b6d4; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5; -fx-min-width: 32px; -fx-min-height: 30px;");
                    btnQr.setOnAction(e -> {
                        showAlert(Alert.AlertType.INFORMATION, "Information", "Scanner QR Code n'est pas encore disponible.");
                    });

                    Button btnEdit = new Button("✎");
                    btnEdit.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5; -fx-min-width: 32px; -fx-min-height: 30px;");
                    btnEdit.setOnAction(e -> {
                        Seance s = getTableView().getItems().get(getIndex());
                        handleEdit(s);
                    });

                    Button btnDelete = new Button("🗑");
                    btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5; -fx-min-width: 32px; -fx-min-height: 30px;");
                    btnDelete.setOnAction(e -> {
                        Seance s = getTableView().getItems().get(getIndex());
                        handleDelete(s);
                    });

                    box.getChildren().addAll(btnView, btnQr, btnEdit, btnDelete);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupFilters() {
        comboJour.setItems(FXCollections.observableArrayList("Tous les jours", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"));
        comboType.setItems(FXCollections.observableArrayList("Tous les types", "Cours", "TD", "TP"));
        comboMode.setItems(FXCollections.observableArrayList("Tous les modes", "Présentiel", "En ligne", "Hybride"));
        comboTri.setItems(FXCollections.observableArrayList("ID (Croissant)", "ID (Décroissant)"));
        comboTri.setValue("ID (Croissant)");

        searchField.textProperty().addListener((obs, old, nv) -> filterData());
        comboJour.valueProperty().addListener((obs, old, nv) -> filterData());
        comboType.valueProperty().addListener((obs, old, nv) -> filterData());
        comboMode.valueProperty().addListener((obs, old, nv) -> filterData());
        comboTri.valueProperty().addListener((obs, old, nv) -> filterData());
    }

    private void loadData() {
        try {
            masterData.setAll(seanceService.getAllSeances());
            updateDashboardCards();
            filterData();

            FadeTransition ft = new FadeTransition(Duration.millis(1000), seanceTable);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDashboardCards() {
        lblTotalCount.setText(String.valueOf(masterData.size()));
        
        long countCours = masterData.stream().filter(s -> s.getTypeSeance() != null && s.getTypeSeance().equalsIgnoreCase("Cours")).count();
        lblCoursCount.setText(String.valueOf(countCours));

        long countPres = masterData.stream().filter(s -> s.getMode() != null && s.getMode().equalsIgnoreCase("Présentiel")).count();
        lblPresentielCount.setText(String.valueOf(countPres));

        long semaine = masterData.stream().filter(s -> {
            if(s.getHeureDebut() == null) return false;
            LocalDate d = s.getHeureDebut().toLocalDateTime().toLocalDate();
            LocalDate now = LocalDate.now();
            return !d.isBefore(now) && !d.isAfter(now.plusDays(7));
        }).count();
        lblSemaineCount.setText(String.valueOf(semaine));
    }

    private void filterData() {
        List<Seance> filtered = masterData.stream().filter(s -> {
            boolean matchSearch = true;
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String search = searchField.getText().toLowerCase();
                String mat = matiereMap.getOrDefault(s.getMatiereId(), "").toLowerCase();
                String sal = salleMap.getOrDefault(s.getSalleId(), "").toLowerCase();
                matchSearch = mat.contains(search) || sal.contains(search);
            }

            boolean matchJour = true;
            if (comboJour.getValue() != null && !comboJour.getValue().startsWith("Tous")) {
                matchJour = comboJour.getValue().equalsIgnoreCase(s.getJour());
            }

            boolean matchType = true;
            if (comboType.getValue() != null && !comboType.getValue().startsWith("Tous")) {
                matchType = comboType.getValue().equalsIgnoreCase(s.getTypeSeance());
            }

            boolean matchMode = true;
            if (comboMode.getValue() != null && !comboMode.getValue().startsWith("Tous")) {
                matchMode = comboMode.getValue().equalsIgnoreCase(s.getMode());
            }

            return matchSearch && matchJour && matchType && matchMode;
        }).collect(Collectors.toList());

        String tri = comboTri.getValue();
        if (tri != null) {
            if (tri.equals("ID (Croissant)")) {
                filtered.sort(Comparator.comparingInt(Seance::getId));
            } else if (tri.equals("ID (Décroissant)")) {
                filtered.sort((s1, s2) -> Integer.compare(s2.getId(), s1.getId()));
            }
        }

        seanceTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        comboJour.setValue(null);
        comboType.setValue(null);
        comboMode.setValue(null);
        comboTri.setValue("ID (Croissant)");
    }

    @FXML
    private void handleAdd() {
        showSeanceDialog(null);
    }

    private void handleEdit(Seance seance) {
        showSeanceDialog(seance);
    }

    private void showSeanceDialog(Seance existing) {
        boolean isEdit = existing != null;

        Dialog<Seance> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier la séance" : "Créer une nouvelle séance");
        dialog.setHeaderText(null);
        dialog.setResizable(true);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #111318; -fx-border-color: #1e2130; -fx-border-width: 1;");
        pane.getButtonTypes().addAll(
                new ButtonType(isEdit ? "Enregistrer" : "Créer la séance", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );

        Button okBtn = (Button) pane.lookupButton(pane.getButtonTypes().get(0));
        okBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 8 20;");
        Button cancelBtn = (Button) pane.lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle("-fx-background-color: #1e2130; -fx-text-fill: #6b7394; -fx-background-radius: 8; -fx-border-color: #272c3d; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 8 20;");

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));

        // --- Informations de base ---
        GridPane grid1 = new GridPane();
        grid1.setHgap(15); grid1.setVgap(10);
        
        ComboBox<Matiere> matiereCombo = new ComboBox<>();
        matiereCombo.setItems(FXCollections.observableArrayList(matieres));
        matiereCombo.setCellFactory(cf -> new ListCell<>(){
            @Override protected void updateItem(Matiere item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        matiereCombo.setButtonCell(matiereCombo.getCellFactory().call(null));
        matiereCombo.setStyle(dialogFieldStyle());

        ComboBox<Classe> classeCombo = new ComboBox<>();
        classeCombo.setItems(FXCollections.observableArrayList(classes));
        classeCombo.setCellFactory(cf -> new ListCell<>(){
            @Override protected void updateItem(Classe item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        classeCombo.setButtonCell(classeCombo.getCellFactory().call(null));
        classeCombo.setStyle(dialogFieldStyle());

        grid1.add(dialogLabel("Matière *"), 0, 0); grid1.add(matiereCombo, 0, 1);
        grid1.add(dialogLabel("Classe *"), 1, 0); grid1.add(classeCombo, 1, 1);

        // --- Type et mode ---
        GridPane grid2 = new GridPane();
        grid2.setHgap(15); grid2.setVgap(10);

        TextField txtType = new TextField();
        txtType.setPromptText("Ex: Cours, TD, TP");
        txtType.setStyle(dialogFieldStyle());

        TextField txtMode = new TextField();
        txtMode.setPromptText("Ex: Présentiel, En ligne, Hybride");
        txtMode.setStyle(dialogFieldStyle());

        grid2.add(dialogLabel("Type de séance *"), 0, 0); grid2.add(txtType, 0, 1);
        grid2.add(dialogLabel("Mode *"), 1, 0); grid2.add(txtMode, 1, 1);

        // --- Horaire ---
        GridPane grid3 = new GridPane();
        grid3.setHgap(15); grid3.setVgap(10);

        DatePicker datePicker = new DatePicker();
        datePicker.setStyle("-fx-control-inner-background: #161921; -fx-text-fill: white;");
        
        ComboBox<String> horaireCombo = new ComboBox<>();
        horaireCombo.setItems(FXCollections.observableArrayList("09:00 - 10:30", "10:45 - 12:15", "13:30 - 15:00", "15:15 - 16:45"));
        horaireCombo.setStyle(dialogFieldStyle());

        grid3.add(dialogLabel("Date de la séance *"), 0, 0); grid3.add(datePicker, 0, 1);
        grid3.add(dialogLabel("Horaires *"), 1, 0); grid3.add(horaireCombo, 1, 1);

        // --- Localisation ---
        VBox aiBox = new VBox(10);
        Button btnIa = new Button("✨ Optimiser avec l'IA");
        btnIa.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        
        ComboBox<Salle> salleCombo = new ComboBox<>();
        salleCombo.setItems(FXCollections.observableArrayList(salles));
        salleCombo.setCellFactory(cf -> new ListCell<>(){
            @Override protected void updateItem(Salle item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        salleCombo.setButtonCell(salleCombo.getCellFactory().call(null));
        salleCombo.setStyle(dialogFieldStyle());
        salleCombo.setVisible(false);

        btnIa.setOnAction(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Information", "L'optimisation par IA n'est pas encore disponible.\nVeuillez choisir une salle manuellement.");
            salleCombo.setVisible(true);
        });

        aiBox.getChildren().addAll(btnIa, new Label("Ou sélectionner manuellement :"), salleCombo);

        layout.getChildren().addAll(
            new Label("Informations de base"), grid1,
            new Label("Type et mode"), grid2,
            new Label("Horaire"), grid3,
            new Label("Localisation"), aiBox
        );

        // Fill existing
        if (isEdit) {
            matieres.stream().filter(m -> m.getId() == existing.getMatiereId()).findFirst().ifPresent(matiereCombo::setValue);
            classes.stream().filter(c -> c.getId() == existing.getClasseId()).findFirst().ifPresent(classeCombo::setValue);
            salles.stream().filter(s -> s.getId() == existing.getSalleId()).findFirst().ifPresent(salleCombo::setValue);
            
            salleCombo.setVisible(true);
            txtType.setText(existing.getTypeSeance());
            txtMode.setText(existing.getMode());

            if(existing.getHeureDebut() != null) {
                LocalDateTime start = existing.getHeureDebut().toLocalDateTime();
                datePicker.setValue(start.toLocalDate());
                String slot = start.format(timeFormatter) + " - " + existing.getHeureFin().toLocalDateTime().format(timeFormatter);
                horaireCombo.setValue(slot);
            }
        }

        pane.setContent(layout);

        // Validation Control
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            StringBuilder errorMsg = new StringBuilder();

            if (matiereCombo.getValue() == null) errorMsg.append("- La matière est obligatoire.\n");
            if (classeCombo.getValue() == null) errorMsg.append("- La classe est obligatoire.\n");
            
            String t = txtType.getText().trim().toLowerCase();
            if (!t.equals("td") && !t.equals("tp") && !t.equals("cours")) {
                errorMsg.append("- Le type doit être TD, TP ou Cours.\n");
            }

            String m = txtMode.getText().trim().toLowerCase();
            if (!m.equals("présentiel") && !m.equals("en ligne") && !m.equals("hybride") && !m.equals("presentiel")) {
                errorMsg.append("- Le mode doit être Présentiel, En ligne ou Hybride.\n");
            }

            if (datePicker.getValue() == null) {
                errorMsg.append("- La date est obligatoire.\n");
            } else {
                LocalDate date = datePicker.getValue();
                if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    errorMsg.append("- Les cours ne peuvent pas avoir lieu un dimanche.\n");
                }
                if (date.isBefore(LocalDate.now())) {
                    errorMsg.append("- La date doit être dans le futur.\n");
                }
            }

            if (horaireCombo.getValue() == null) errorMsg.append("- Veuillez sélectionner un horaire.\n");
            
            if (salleCombo.getValue() == null) errorMsg.append("- La salle est obligatoire.\n");

            if (errorMsg.length() > 0) {
                event.consume();
                showAlert(Alert.AlertType.WARNING, "Erreur de Saisie", errorMsg.toString());
            }
        });

        // Conversion
        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Seance s = isEdit ? existing : new Seance();
                s.setMatiereId(matiereCombo.getValue().getId());
                s.setClasseId(classeCombo.getValue().getId());
                s.setSalleId(salleCombo.getValue().getId());
                s.setTypeSeance(capitalize(txtType.getText().trim()));
                s.setMode(capitalize(txtMode.getText().trim()));

                LocalDate date = datePicker.getValue();
                String[] times = horaireCombo.getValue().split(" - ");
                LocalTime tStart = LocalTime.parse(times[0]);
                LocalTime tEnd = LocalTime.parse(times[1]);

                s.setHeureDebut(Timestamp.valueOf(LocalDateTime.of(date, tStart)));
                s.setHeureFin(Timestamp.valueOf(LocalDateTime.of(date, tEnd)));

                // Génération automatique du jour
                String jour = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE);
                s.setJour(capitalize(jour));

                return s;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(s -> {
            try {
                if (isEdit) {
                    seanceService.edit(s);
                } else {
                    seanceService.add(s);
                }
                loadData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur BD", e.getMessage());
            }
        });
    }

    private String capitalize(String text) {
        if(text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void handleView(Seance seance) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la Séance");
        dialog.setHeaderText(null);
        dialog.setResizable(true);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #111318; -fx-border-color: #1e2130; -fx-border-width: 1;");

        // Boutons
        ButtonType btnRetour = new ButtonType("Retour à la liste", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnPrint = new ButtonType("🖨 Imprimer", ButtonBar.ButtonData.OTHER);
        ButtonType btnQr = new ButtonType("QR Présence", ButtonBar.ButtonData.OTHER);
        ButtonType btnPresences = new ButtonType("📊 Présences", ButtonBar.ButtonData.OTHER);
        ButtonType btnIa = new ButtonType("✨ Optimiser IA", ButtonBar.ButtonData.OTHER);
        ButtonType btnModifier = new ButtonType("Modifier", ButtonBar.ButtonData.OTHER);
        ButtonType btnSupprimer = new ButtonType("Supprimer", ButtonBar.ButtonData.OTHER);

        pane.getButtonTypes().addAll(btnRetour, btnPrint, btnQr, btnPresences, btnIa, btnModifier, btnSupprimer);

        // Styling Buttons
        Button bRet = (Button) pane.lookupButton(btnRetour);
        bRet.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #374151; -fx-border-radius: 5;");
        
        Button bPrint = (Button) pane.lookupButton(btnPrint);
        bPrint.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #374151; -fx-border-radius: 5;");
        bPrint.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            e.consume();
            showAlert(Alert.AlertType.INFORMATION, "Information", "L'impression n'est pas encore disponible.");
        });

        Button bQr = (Button) pane.lookupButton(btnQr);
        bQr.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        bQr.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            e.consume();
            showAlert(Alert.AlertType.INFORMATION, "Information", "Génération de QR Code n'est pas encore disponible.");
        });

        Button bPres = (Button) pane.lookupButton(btnPresences);
        bPres.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        bPres.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            e.consume();
            showAlert(Alert.AlertType.INFORMATION, "Information", "La liste des présences n'est pas encore disponible.");
        });

        Button bIa = (Button) pane.lookupButton(btnIa);
        bIa.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        bIa.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            e.consume();
            showAlert(Alert.AlertType.INFORMATION, "Information", "L'optimisation par IA n'est pas encore disponible.");
        });

        Button bModif = (Button) pane.lookupButton(btnModifier);
        bModif.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        bModif.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            e.consume();
            dialog.close();
            handleEdit(seance);
        });

        Button bSuppr = (Button) pane.lookupButton(btnSupprimer);
        bSuppr.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        bSuppr.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            e.consume();
            dialog.close();
            handleDelete(seance);
        });

        // Layout de la vue
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #111318;");

        Label title = new Label("Séance #" + seance.getId());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subtitle = new Label("Détails complets de la séance programmée");
        subtitle.setStyle("-fx-text-fill: #9ca3af;");

        // Stats boxes (Jour, Type, Mode)
        HBox topCards = new HBox(15);
        VBox cJour = createDetailCard("JOUR", seance.getJour(), "#06b6d4");
        VBox cType = createDetailCard("TYPE", seance.getTypeSeance(), "#c084fc");
        VBox cMode = createDetailCard("MODE", seance.getMode(), "#10b981");
        topCards.getChildren().addAll(cJour, cType, cMode);

        // Infos related (Matiere, Classe, Salle)
        HBox infoCards = new HBox(15);
        String matName = matiereMap.getOrDefault(seance.getMatiereId(), "Non définie");
        String cName = classeMap.getOrDefault(seance.getClasseId(), "Non définie");
        String sName = salleMap.getOrDefault(seance.getSalleId(), "Non définie");

        VBox cMat = createDetailBox("📖 Matière", "NOM\n" + matName);
        VBox cCla = createDetailBox("🎓 Classe", "NOM\n" + cName);
        VBox cSal = createDetailBox("🏢 Salle", "NOM\n" + sName);
        infoCards.getChildren().addAll(cMat, cCla, cSal);

        // Horaires
        VBox horaireBox = new VBox(10);
        horaireBox.setStyle("-fx-background-color: #1a1e2b; -fx-padding: 20; -fx-background-radius: 10;");
        Label hTitle = new Label("⏱ Horaires");
        hTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        String startStr = seance.getHeureDebut() != null ? seance.getHeureDebut().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Inconnu";
        String endStr = seance.getHeureFin() != null ? seance.getHeureFin().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Inconnu";

        Label lblStart = new Label("Début: " + startStr); lblStart.setStyle("-fx-text-fill: #9ca3af;");
        Label lblEnd = new Label("Fin: " + endStr); lblEnd.setStyle("-fx-text-fill: #9ca3af;");
        horaireBox.getChildren().addAll(hTitle, lblStart, lblEnd);

        layout.getChildren().addAll(title, subtitle, topCards, infoCards, horaireBox);
        pane.setContent(layout);

        dialog.showAndWait();
    }

    private VBox createDetailCard(String title, String value, String color) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: #1a1e2b; -fx-padding: 15; -fx-background-radius: 10; -fx-min-width: 150;");
        Label lblT = new Label(title);
        lblT.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label lblV = new Label(value != null ? value.toUpperCase() : "");
        lblV.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 15;");
        box.getChildren().addAll(lblT, lblV);
        return box;
    }

    private VBox createDetailBox(String title, String content) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: #1a1e2b; -fx-padding: 15; -fx-background-radius: 10; -fx-min-width: 180;");
        Label lblT = new Label(title);
        lblT.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label lblC = new Label(content);
        lblC.setStyle("-fx-text-fill: #9ca3af;");
        box.getChildren().addAll(lblT, lblC);
        return box;
    }

    private void handleDelete(Seance seance) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText("Supprimer la séance # " + seance.getId() + " ?");
        styleAlert(confirm);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    seanceService.delete(seance.getId());
                    loadData();
                } catch (SQLException e) {
                    if (e.getMessage().contains("foreign key constraint") || e.getMessage().contains("a parent row")) {
                        showAlert(Alert.AlertType.ERROR, "Suppression Impossible", "Vous ne pouvez pas supprimer cette séance car elle est liée à d'autres données.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "La suppression a échoué : " + e.getMessage());
                    }
                }
            }
        });
    }

    private String dialogFieldStyle() {
        return "-fx-background-color: #161921; -fx-border-color: #1e2130; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: #c8cfe8; -fx-font-size: 13px; -fx-padding: 8 12;";
    }

    private Label dialogLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7394; -fx-font-weight: 600; -fx-min-width: 150;");
        return lbl;
    }

    private void styleAlert(Alert alert) {
        alert.getDialogPane().setStyle("-fx-background-color: #111318; -fx-border-color: #1e2130; -fx-border-width: 1;");
        Label content = (Label) alert.getDialogPane().lookup(".content.label");
        if(content != null) content.setStyle("-fx-text-fill: white;");
        Label header = (Label) alert.getDialogPane().lookup(".header-panel .label");
        if(header != null) header.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
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

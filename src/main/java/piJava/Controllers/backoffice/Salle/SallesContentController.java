package piJava.Controllers.backoffice.Salle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import piJava.entities.Salle;
import piJava.services.SalleService;

import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.util.Duration;

public class SallesContentController implements Initializable {

    @FXML private Label lblTotalCount;
    @FXML private Label lblBlocACount;
    @FXML private Label lblBlocBCount;
    @FXML private Label lblAutresCount;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboBloc;
    @FXML private TextField txtCapaciteMax;
    @FXML private ComboBox<String> comboTri;

    @FXML private TableView<Salle> salleTable;
    @FXML private TableColumn<Salle, Integer> colId;
    @FXML private TableColumn<Salle, String> colSalle;
    @FXML private TableColumn<Salle, String> colBloc;
    @FXML private TableColumn<Salle, String> colEtage;
    @FXML private TableColumn<Salle, String> colCapacite;
    @FXML private TableColumn<Salle, Void> colActions;

    private SalleService salleService;
    private ObservableList<Salle> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        salleService = new SalleService();

        setupColumns();
        setupFilters();
        loadData();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setStyle("-fx-alignment: center-left; -fx-text-fill: white;");

        // Custom Salle column with Icon
        colSalle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colSalle.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Salle salle = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);
                    Label icon = new Label("🏢");
                    icon.getStyleClass().add("salle-icon");
                    VBox texts = new VBox(2);
                    Label nameLbl = new Label(item);
                    nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    Label subLbl = new Label("ID: " + salle.getId());
                    subLbl.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px;");
                    texts.getChildren().addAll(nameLbl, subLbl);
                    box.getChildren().addAll(icon, texts);
                    setGraphic(box);
                }
            }
        });

        // Bloc column
        colBloc.setCellValueFactory(cellData -> new SimpleStringProperty("Bloc " + cellData.getValue().getBlock()));
        colBloc.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge-block");
                    if (item.contains("A")) badge.setStyle("-fx-background-color: #ec4899;");
                    else if(item.contains("B")) badge.setStyle("-fx-background-color: #06b6d4;");
                    else if(item.contains("D")) badge.setStyle("-fx-background-color: #10b981;");
                    setGraphic(badge);
                }
            }
        });

        // Etage column
        colEtage.setCellValueFactory(cellData -> new SimpleStringProperty("Étage " + cellData.getValue().getEtage()));
        colEtage.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge-etage");
                    setGraphic(badge);
                }
            }
        });

        // Capacite column
        colCapacite.setCellValueFactory(cellData -> new SimpleStringProperty("👥 " + cellData.getValue().getCapacite()));
        colCapacite.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge-capacite");
                    setGraphic(badge);
                }
            }
        });

        // Actions column
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
                    btnView.getStyleClass().addAll("action-btn", "btn-view");
                    btnView.setOnAction(e -> {
                        Salle s = getTableView().getItems().get(getIndex());
                        handleView(s);
                    });

                    Button btnEdit = new Button("✎");
                    btnEdit.getStyleClass().addAll("action-btn", "btn-edit");
                    btnEdit.setOnAction(e -> {
                        Salle s = getTableView().getItems().get(getIndex());
                        handleEdit(s);
                    });

                    Button btnDelete = new Button("🗑");
                    btnDelete.getStyleClass().addAll("action-btn", "btn-delete");
                    btnDelete.setOnAction(e -> {
                        Salle s = getTableView().getItems().get(getIndex());
                        handleDelete(s);
                    });

                    box.getChildren().addAll(btnView, btnEdit, btnDelete);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupFilters() {
        comboBloc.setItems(FXCollections.observableArrayList("-- Tous les blocs --", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"));
        comboTri.setItems(FXCollections.observableArrayList("ID (croissant)", "ID (décroissant)", "Capacité max"));
        comboTri.setValue("ID (croissant)");

        searchField.textProperty().addListener((obs, old, nv) -> filterData());
        comboBloc.valueProperty().addListener((obs, old, nv) -> filterData());
        txtCapaciteMax.textProperty().addListener((obs, old, nv) -> filterData());
        comboTri.valueProperty().addListener((obs, old, nv) -> filterData());
    }

    private void loadData() {
        try {
            masterData.setAll(salleService.getAllSalles());
            updateDashboardCards();
            filterData();
            
            FadeTransition ft = new FadeTransition(Duration.millis(1000), salleTable);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDashboardCards() {
        lblTotalCount.setText(String.valueOf(masterData.size()));
        
        long countA = masterData.stream().filter(s -> "A".equalsIgnoreCase(s.getBlock())).count();
        lblBlocACount.setText(String.valueOf(countA));

        long countB = masterData.stream().filter(s -> "B".equalsIgnoreCase(s.getBlock())).count();
        lblBlocBCount.setText(String.valueOf(countB));

        long countOther = masterData.size() - countA - countB;
        lblAutresCount.setText(String.valueOf(countOther));
    }

    private void filterData() {
        // 1. Filtrage
        List<Salle> filtered = masterData.stream().filter(s -> {
            boolean matchSearch = true;
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                matchSearch = s.getName().toLowerCase().contains(searchField.getText().toLowerCase());
            }

            boolean matchBloc = true;
            if (comboBloc.getValue() != null && !comboBloc.getValue().startsWith("--")) {
                String b = comboBloc.getValue().replace("Bloc ", "");
                matchBloc = b.equalsIgnoreCase(s.getBlock());
            }

            boolean matchCapacite = true;
            if (txtCapaciteMax.getText() != null && !txtCapaciteMax.getText().isEmpty()) {
                try {
                    int max = Integer.parseInt(txtCapaciteMax.getText().trim());
                    matchCapacite = s.getCapacite() <= max;
                } catch (NumberFormatException ignored) {}
            }

            return matchSearch && matchBloc && matchCapacite;
        }).collect(Collectors.toList());

        // 2. Tri Dynamique (AJAX)
        String tri = comboTri.getValue();
        if (tri != null) {
            switch (tri) {
                case "ID (croissant)":
                    filtered.sort(Comparator.comparingInt(Salle::getId));
                    break;
                case "ID (décroissant)":
                    filtered.sort((s1, s2) -> Integer.compare(s2.getId(), s1.getId()));
                    break;
                case "Capacité max":
                    filtered.sort((s1, s2) -> Integer.compare(s2.getCapacite(), s1.getCapacite()));
                    break;
            }
        }

        salleTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleAdd() {
        showSalleDialog(null);
    }

    private void handleEdit(Salle salle) {
        showSalleDialog(salle);
    }

    private void showSalleDialog(Salle existing) {
        boolean isEdit = existing != null;

        Dialog<Salle> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier la salle" : "Nouvelle Salle");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #111318; -fx-border-color: #1e2130; -fx-border-width: 1;");
        pane.getButtonTypes().addAll(
                new ButtonType(isEdit ? "Enregistrer" : "Créer", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );

        Button okBtn = (Button) pane.lookupButton(pane.getButtonTypes().get(0));
        okBtn.setStyle("-fx-background-color: #00e5c8; -fx-text-fill: #0d0f14; -fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 8 20;");
        Button cancelBtn = (Button) pane.lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle("-fx-background-color: #1e2130; -fx-text-fill: #6b7394; -fx-background-radius: 8; -fx-border-color: #272c3d; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 8 20;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(24, 28, 16, 28));
        grid.setStyle("-fx-background-color: #111318;");

        // Champs
        ComboBox<String> blocCombo = new ComboBox<>();
        blocCombo.setItems(FXCollections.observableArrayList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"));
        blocCombo.setPromptText("Sélectionnez le bloc");
        blocCombo.setStyle(dialogFieldStyle());

        TextField numField = new TextField();
        numField.setPromptText("Ex: 1 à 10");
        numField.setStyle(dialogFieldStyle());

        ComboBox<String> etageCombo = new ComboBox<>();
        etageCombo.setItems(FXCollections.observableArrayList("Rez-de-chaussée (0)", "1er étage (1)", "2ème étage (2)", "3ème étage (3)", "4ème étage (4)"));
        etageCombo.setPromptText("L'étage (0 à 4)");
        etageCombo.setStyle(dialogFieldStyle());

        TextField capField = new TextField();
        capField.setPromptText("De 1 à 30");
        capField.setStyle(dialogFieldStyle());

        if (isEdit) {
            blocCombo.setValue(existing.getBlock());
            numField.setText(String.valueOf(existing.getNumber()));
            capField.setText(String.valueOf(existing.getCapacite()));
            
            String et = "Rez-de-chaussée (0)";
            if(existing.getEtage() == 1) et = "1er étage (1)";
            if(existing.getEtage() == 2) et = "2ème étage (2)";
            if(existing.getEtage() == 3) et = "3ème étage (3)";
            if(existing.getEtage() == 4) et = "4ème étage (4)";
            etageCombo.setValue(et);
        }

        grid.add(dialogLabel("Bloc *"), 0, 0); grid.add(blocCombo, 1, 0);
        grid.add(dialogLabel("Numéro de salle *"), 0, 1); grid.add(numField, 1, 1);
        grid.add(dialogLabel("Étage *"), 0, 2); grid.add(etageCombo, 1, 2);
        grid.add(dialogLabel("Capacité *"), 0, 3); grid.add(capField, 1, 3);

        pane.setContent(grid);

        // Validation Control
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            boolean hasError = false;
            StringBuilder errorMsg = new StringBuilder();

            if (blocCombo.getValue() == null) {
                hasError = true;
                errorMsg.append("- Le Bloc est obligatoire.\n");
            }
            if (etageCombo.getValue() == null) {
                hasError = true;
                errorMsg.append("- L'étage est obligatoire.\n");
            }

            try {
                int num = Integer.parseInt(numField.getText().trim());
                if (num < 1 || num > 10) {
                    hasError = true;
                    errorMsg.append("- Le numéro de salle doit être entre 1 et 10.\n");
                }
            } catch (NumberFormatException e) {
                hasError = true;
                errorMsg.append("- Le numéro de salle doit être un nombre valide.\n");
            }

            try {
                int cap = Integer.parseInt(capField.getText().trim());
                if (cap < 1 || cap > 30) {
                    hasError = true;
                    errorMsg.append("- La capacité doit être entre 1 et 30.\n");
                }
            } catch (NumberFormatException e) {
                hasError = true;
                errorMsg.append("- La capacité doit être un nombre valide.\n");
            }

            if (hasError) {
                event.consume(); // Prevent dialog from closing
                showAlert(Alert.AlertType.WARNING, "Erreur de Saisie", errorMsg.toString());
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Salle s = isEdit ? existing : new Salle();
                s.setBlock(blocCombo.getValue());
                s.setNumber(Integer.parseInt(numField.getText().trim()));
                s.setName(s.getBlock() + String.format("%02d", s.getNumber()));
                s.setCapacite(Integer.parseInt(capField.getText().trim()));
                
                int etage = 0;
                String etStr = etageCombo.getValue();
                if(etStr.contains("1")) etage = 1;
                if(etStr.contains("2")) etage = 2;
                if(etStr.contains("3")) etage = 3;
                if(etStr.contains("4")) etage = 4;
                s.setEtage(etage);
                
                return s;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(s -> {
            try {
                if (isEdit) {
                    salleService.edit(s);
                } else {
                    salleService.add(s);
                }
                loadData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", e.getMessage());
            }
        });
    }

    private void handleView(Salle salle) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la Salle");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #111318; -fx-border-color: #1e2130; -fx-border-width: 1;");
        
        ButtonType btnModifier = new ButtonType("Modifier", ButtonBar.ButtonData.OTHER);
        ButtonType btnSupprimer = new ButtonType("Supprimer", ButtonBar.ButtonData.OTHER);
        ButtonType btnRetour = new ButtonType("Retour à la liste", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        pane.getButtonTypes().addAll(btnRetour, btnModifier, btnSupprimer);

        // Styling Buttons
        Button btnM = (Button) pane.lookupButton(btnModifier);
        btnM.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        btnM.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            e.consume();
            dialog.close();
            Platform.runLater(() -> handleEdit(salle));
        });

        Button btnS = (Button) pane.lookupButton(btnSupprimer);
        btnS.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        btnS.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            e.consume();
            dialog.close();
            Platform.runLater(() -> handleDelete(salle));
        });

        Button btnR = (Button) pane.lookupButton(btnRetour);
        btnR.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #374151; -fx-border-radius: 5;");

        // Layout
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #111318;");

        Label title = new Label("Salle " + salle.getName());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: #1a1e2b; -fx-padding: 20; -fx-background-radius: 10;");

        grid.add(dialogLabel("ID"), 0, 0);
        Label vId = new Label(String.valueOf(salle.getId())); vId.setStyle("-fx-text-fill: white;"); grid.add(vId, 1, 0);

        grid.add(dialogLabel("Code complet"), 0, 1);
        Label vCode = new Label("Salle " + salle.getName()); vCode.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); grid.add(vCode, 1, 1);

        grid.add(dialogLabel("Étage"), 0, 2);
        Label vEtage = new Label("Étage " + salle.getEtage()); vEtage.setStyle("-fx-text-fill: #10b981; -fx-border-color: #10b981; -fx-border-radius: 5; -fx-padding: 2 6;"); grid.add(vEtage, 1, 2);

        grid.add(dialogLabel("Capacité"), 0, 3);
        Label vCap = new Label("👥 " + salle.getCapacite() + " personnes"); vCap.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5;"); grid.add(vCap, 1, 3);

        layout.getChildren().addAll(title, grid);
        pane.setContent(layout);

        dialog.showAndWait();
    }

    private void handleDelete(Salle salle) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer la salle « " + salle.getName() + " » ?");
        confirm.setContentText("Cette action est irréversible.");
        styleAlert(confirm);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    salleService.delete(salle.getId());
                    loadData();
                } catch (SQLException e) {
                    if (e.getMessage().contains("foreign key constraint") || e.getMessage().contains("a parent row")) {
                        showAlert(Alert.AlertType.ERROR, "Suppression Impossible", "Vous ne pouvez pas supprimer cette salle car elle est actuellement affectée à une ou plusieurs séances. Veuillez d'abord modifier ou supprimer les séances correspondantes.");
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

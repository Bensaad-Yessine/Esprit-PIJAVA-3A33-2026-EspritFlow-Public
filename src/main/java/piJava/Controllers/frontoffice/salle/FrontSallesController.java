package piJava.Controllers.frontoffice.salle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import piJava.entities.Salle;
import piJava.entities.user;
import piJava.services.SalleService;
import piJava.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class FrontSallesController implements Initializable {

    @FXML private Label lblSessionEmail;
    @FXML private Label lblTotalSalles;
    @FXML private Label lblTotalBlocs;
    @FXML private Label lblTotalEtages;

    @FXML private ComboBox<String> comboBloc;
    @FXML private ComboBox<String> comboEtage;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboTri;
    @FXML private Label lblCountResults;

    @FXML private FlowPane sallesFlowPane;

    private SalleService salleService;
    private List<Salle> masterSalles = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        salleService = new SalleService();

        // Session load
        user u = SessionManager.getInstance().getCurrentUser();
        if(u != null) {
            lblSessionEmail.setText(u.getEmail());
        } else {
            lblSessionEmail.setText("Invite");
        }

        setupFilters();
        loadSalles();
    }

    private void setupFilters() {
        comboTri.setItems(FXCollections.observableArrayList("Nom ↑", "Nom ↓", "Capacité max", "Capacité min"));
        comboTri.setValue("Nom ↑");

        // Listeners for auto search
        comboBloc.valueProperty().addListener((obs, old, nv) -> filterSalles());
        comboEtage.valueProperty().addListener((obs, old, nv) -> filterSalles());
        searchField.textProperty().addListener((obs, old, nv) -> filterSalles());
        comboTri.valueProperty().addListener((obs, old, nv) -> filterSalles());
    }

    private void loadSalles() {
        try {
            masterSalles = salleService.getAllSalles();
            updateStats();
            populateFilters();
            filterSalles();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Impossible de charger les salles : " + e.getMessage());
        }
    }

    private void updateStats() {
        lblTotalSalles.setText(String.valueOf(masterSalles.size()));

        long blocsCount = masterSalles.stream().map(Salle::getBlock).filter(Objects::nonNull).distinct().count();
        lblTotalBlocs.setText(String.valueOf(blocsCount));

        long etageCount = masterSalles.stream().map(Salle::getEtage).distinct().count();
        lblTotalEtages.setText(String.valueOf(etageCount));
    }

    private void populateFilters() {
        List<String> blocs = masterSalles.stream()
                .map(Salle::getBlock)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        blocs.add(0, "Tous les blocs");
        comboBloc.setItems(FXCollections.observableArrayList(blocs));
        comboBloc.setValue("Tous les blocs");

        List<String> etages = masterSalles.stream()
                .map(s -> "Étage " + s.getEtage())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        etages.add(0, "Tous les étages");
        comboEtage.setItems(FXCollections.observableArrayList(etages));
        comboEtage.setValue("Tous les étages");
    }

    @FXML
    private void resetFilters() {
        comboBloc.setValue("Tous les blocs");
        comboEtage.setValue("Tous les étages");
        searchField.clear();
        comboTri.setValue("Nom ↑");
    }

    private void filterSalles() {
        if(masterSalles.isEmpty()) return;

        List<Salle> filtered = masterSalles.stream().filter(s -> {
            boolean matchBloc = true;
            if (comboBloc.getValue() != null && !comboBloc.getValue().equals("Tous les blocs")) {
                matchBloc = comboBloc.getValue().equals(s.getBlock());
            }

            boolean matchEtage = true;
            if (comboEtage.getValue() != null && !comboEtage.getValue().equals("Tous les étages")) {
                String et = "Étage " + s.getEtage();
                matchEtage = comboEtage.getValue().equals(et);
            }

            boolean matchSearch = true;
            if (searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
                matchSearch = s.getName().toLowerCase().contains(searchField.getText().trim().toLowerCase());
            }

            return matchBloc && matchEtage && matchSearch;
        }).collect(Collectors.toList());

        // Sort
        String tri = comboTri.getValue();
        if (tri != null) {
            switch (tri) {
                case "Nom ↑":
                    filtered.sort(Comparator.comparing(Salle::getName)); break;
                case "Nom ↓":
                    filtered.sort((s1,s2) -> s2.getName().compareTo(s1.getName())); break;
                case "Capacité max":
                    filtered.sort((s1,s2) -> Integer.compare(s2.getCapacite(), s1.getCapacite())); break;
                case "Capacité min":
                    filtered.sort(Comparator.comparingInt(Salle::getCapacite)); break;
            }
        }

        renderCards(filtered);
        lblCountResults.setText(filtered.size() + " salles trouvées");
    }

    private void renderCards(List<Salle> sallesData) {
        sallesFlowPane.getChildren().clear();

        for (Salle s : sallesData) {
            sallesFlowPane.getChildren().add(createSalleCard(s));
        }
    }

    private VBox createSalleCard(Salle salle) {
        VBox card = new VBox(25);
        card.getStyleClass().add("salle-card");
        card.setMinWidth(320);
        card.setPrefWidth(320);

        // Header (Icon + Titles)
        HBox top = new HBox(15);
        top.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconPane = new StackPane(new Label("🚪"));
        iconPane.getStyleClass().add("salle-icon-container");
        String bgColor = getBlocColor(salle.getBlock());
        iconPane.setStyle("-fx-background-color: " + bgColor + ";");
        iconPane.setMinSize(64, 64);
        iconPane.setMaxSize(64, 64);
        
        VBox titleBox = new VBox(2);
        Label lblName = new Label(salle.getName());
        lblName.getStyleClass().add("salle-card-title");
        Label lblSub = new Label("Bloc " + salle.getBlock() + " - Étage " + salle.getEtage());
        lblSub.getStyleClass().add("salle-card-subtitle");
        titleBox.getChildren().addAll(lblName, lblSub);
        
        top.getChildren().addAll(iconPane, titleBox);

        // Grid details
        GridPane grid = new GridPane();
        grid.setHgap(40);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        VBox bBloc = new VBox(8); bBloc.setAlignment(Pos.CENTER);
        Label lBlocTitle = new Label("BLOC"); lBlocTitle.getStyleClass().add("badge-label");
        Label lBlocValue = new Label(salle.getBlock());
        lBlocValue.getStyleClass().add("badge-bloc");
        lBlocValue.setStyle("-fx-background-color: " + bgColor + ";");
        bBloc.getChildren().addAll(lBlocTitle, lBlocValue);

        VBox bEtage = new VBox(8); bEtage.setAlignment(Pos.CENTER);
        Label lEtT = new Label("ÉTAGE"); lEtT.getStyleClass().add("badge-label");
        String etageStr = salle.getEtage() == 0 ? "RDC" : String.valueOf(salle.getEtage());
        Label lEtV = new Label(etageStr); lEtV.getStyleClass().add("badge-etage");
        bEtage.getChildren().addAll(lEtT, lEtV);

        VBox bCap = new VBox(8); bCap.setAlignment(Pos.CENTER);
        Label lCpT = new Label("CAPACITÉ"); lCpT.getStyleClass().add("badge-label");
        Label lCpV = new Label(salle.getCapacite() + " pers."); lCpV.getStyleClass().add("badge-capacite");
        bCap.getChildren().addAll(lCpT, lCpV);

        VBox bStat = new VBox(8); bStat.setAlignment(Pos.CENTER);
        Label lStT = new Label("STATUT"); lStT.getStyleClass().add("badge-label");
        Label lStV = new Label("✔ Active"); lStV.getStyleClass().add("badge-statut");
        bStat.getChildren().addAll(lStT, lStV);

        grid.add(bBloc, 0, 0); grid.add(bEtage, 1, 0);
        grid.add(bCap, 0, 1); grid.add(bStat, 1, 1);

        // Action Box
        HBox actionBox = new HBox();
        actionBox.setAlignment(Pos.CENTER);
        Button btnMap = new Button("🗺 Réserver sur la map");
        btnMap.getStyleClass().add("btn-card-action");
        btnMap.setOnAction(e -> handleMapClick());
        actionBox.getChildren().add(btnMap);
        
        card.getChildren().addAll(top, grid, actionBox);
        return card;
    }

    private String getBlocColor(String bloc) {
        if(bloc == null) return "#9ca3af"; // gras
        return switch (bloc.toUpperCase()) {
            case "A" -> "#f43f5e"; // rose-rouge
            case "B" -> "#f472b6"; // pink
            case "C" -> "#06b6d4"; // cyan
            case "D" -> "#10b981"; // emeraude
            case "E" -> "#f59e0b"; // ambre
            case "F" -> "#8b5cf6"; // violet
            default -> "#f97316";  // orange
        };
    }

    @FXML
    private void handleMapClick() {
        showAlert(Alert.AlertType.INFORMATION, "Fonctionnalité en cours", "L'accès à la carte et la réservation sur la carte interactive (Map) ne sont pas encore disponibles.");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}

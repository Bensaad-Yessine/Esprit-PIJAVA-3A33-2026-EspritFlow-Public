package piJava.Controllers.backoffice.suivibienetre;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import piJava.Controllers.backoffice.SidebarController;
import piJava.Controllers.backoffice.objectifsante.AfficherObjectifsController;
import piJava.entities.SuiviBienEtre;
import piJava.services.SuiviBienEtreService;

import java.sql.Date;
import java.sql.SQLException;

public class AfficherSuivisController {

    @FXML
    private TableView<SuiviBienEtre> tableSuivis;

    @FXML
    private TableColumn<SuiviBienEtre, Date> colDateSaisie;

    @FXML
    private TableColumn<SuiviBienEtre, String> colHumeur;

    @FXML
    private TableColumn<SuiviBienEtre, Integer> colQualiteSommeil;

    @FXML
    private TableColumn<SuiviBienEtre, Integer> colNiveauEnergie;

    @FXML
    private TableColumn<SuiviBienEtre, Integer> colNiveauStress;

    @FXML
    private TableColumn<SuiviBienEtre, Integer> colQualiteAlimentation;

    @FXML
    private TableColumn<SuiviBienEtre, Double> colScore;

    @FXML
    private TableColumn<SuiviBienEtre, Void> colActions;

    @FXML
    private ComboBox<String> cbTri;

    private int objectifId;
    private SidebarController sidebarController;
    private StackPane contentArea;

    public void setSidebarController(SidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        colDateSaisie.setCellValueFactory(new PropertyValueFactory<>("dateSaisie"));
        colHumeur.setCellValueFactory(new PropertyValueFactory<>("humeur"));
        colQualiteSommeil.setCellValueFactory(new PropertyValueFactory<>("qualiteSommeil"));
        colNiveauEnergie.setCellValueFactory(new PropertyValueFactory<>("niveauEnergie"));
        colNiveauStress.setCellValueFactory(new PropertyValueFactory<>("niveauStress"));
        colQualiteAlimentation.setCellValueFactory(new PropertyValueFactory<>("qualiteAlimentation"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));

        appliquerStylesColonnes();
        ajouterColonneActions();

        cbTri.getItems().addAll("Par défaut", "Date", "Score", "Humeur");
        cbTri.setValue("Par défaut");
        cbTri.valueProperty().addListener((obs, oldVal, newVal) -> appliquerTri());
    }

    private void appliquerStylesColonnes() {
        colDateSaisie.setCellFactory(column -> new TableCell<SuiviBienEtre, Date>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                badge.setText(item.toString());
                badge.getStyleClass().setAll("date-badge-dark");

                setGraphic(badge);
                setText(null);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        });

        colHumeur.setCellFactory(column -> new TableCell<SuiviBienEtre, String>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                badge.setText(item);
                badge.getStyleClass().setAll("table-badge");

                switch (item.toUpperCase()) {
                    case "EXCELLENT" -> badge.getStyleClass().add("badge-dark-blue");
                    case "BIEN" -> badge.getStyleClass().add("badge-steel");
                    case "MOYEN" -> badge.getStyleClass().add("badge-midnight");
                    case "MAUVAIS" -> badge.getStyleClass().add("badge-priority-high");
                    default -> badge.getStyleClass().add("badge-neutral-dark");
                }

                setGraphic(badge);
                setText(null);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        });

        colQualiteSommeil.setCellFactory(column -> createNumericBadgeCell());
        colNiveauEnergie.setCellFactory(column -> createNumericBadgeCell());
        colNiveauStress.setCellFactory(column -> createNumericBadgeCell());
        colQualiteAlimentation.setCellFactory(column -> createNumericBadgeCell());

        colScore.setCellFactory(column -> new TableCell<SuiviBienEtre, Double>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                badge.setText(String.format("%.2f", item));
                badge.getStyleClass().setAll("table-badge");

                if (item >= 80) {
                    badge.getStyleClass().add("badge-status-done");
                } else if (item >= 50) {
                    badge.getStyleClass().add("badge-status-progress");
                } else {
                    badge.getStyleClass().add("badge-priority-high");
                }

                setGraphic(badge);
                setText(null);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        });
    }

    private TableCell<SuiviBienEtre, Integer> createNumericBadgeCell() {
        return new TableCell<SuiviBienEtre, Integer>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                badge.setText(String.valueOf(item));
                badge.getStyleClass().setAll("mini-badge-dark");

                setGraphic(badge);
                setText(null);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        };
    }

    private void ajouterColonneActions() {
        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnModifier = new Button("✎");
            private final Button btnSupprimer = new Button("✖");
            private final HBox container = new HBox(8, btnModifier, btnSupprimer);

            {
                btnModifier.getStyleClass().add("primary-button");
                btnSupprimer.getStyleClass().add("danger-button");

                btnModifier.setPrefWidth(45);
                btnSupprimer.setPrefWidth(45);

                btnModifier.setStyle("-fx-font-size: 16px; -fx-padding: 4;");
                btnSupprimer.setStyle("-fx-font-size: 16px; -fx-padding: 4;");

                btnModifier.setOnAction(event -> {
                    SuiviBienEtre suivi = getTableView().getItems().get(getIndex());
                    ouvrirModifierSuiviDepuisLigne(suivi);
                });

                btnSupprimer.setOnAction(event -> {
                    SuiviBienEtre suivi = getTableView().getItems().get(getIndex());
                    supprimerSuiviDepuisLigne(suivi);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    public void setObjectifId(int objectifId) {
        this.objectifId = objectifId;
        chargerSuivisParObjectif();
    }

    public void chargerSuivisParObjectif() {
        SuiviBienEtreService service = new SuiviBienEtreService();

        try {
            ObservableList<SuiviBienEtre> suivis = FXCollections.observableArrayList();
            suivis.addAll(service.recupererParObjectif(objectifId));
            tableSuivis.setItems(suivis);
        } catch (SQLException e) {
            System.out.println("Erreur lors du chargement des suivis : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void appliquerTri() {
        SuiviBienEtreService service = new SuiviBienEtreService();

        try {
            String triSelection = cbTri.getValue();
            String tri = null;

            if ("Date".equals(triSelection)) {
                tri = "date";
            } else if ("Score".equals(triSelection)) {
                tri = "score";
            } else if ("Humeur".equals(triSelection)) {
                tri = "humeur";
            }

            ObservableList<SuiviBienEtre> suivis = FXCollections.observableArrayList();
            suivis.addAll(service.recupererParObjectifAvecTri(objectifId, tri));
            tableSuivis.setItems(suivis);

        } catch (SQLException e) {
            System.out.println("Erreur lors du tri des suivis : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void reinitialiserTri() {
        cbTri.setValue("Par défaut");
        chargerSuivisParObjectif();
    }

    @FXML
    public void ouvrirAjouterSuivi() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/backoffice/suivibienetre/AjouterSuivi.fxml"));
            Parent root = loader.load();

            AjouterSuiviController controller = loader.getController();
            controller.setAfficherSuivisController(this);
            controller.setObjectifId(objectifId);

            Stage stage = new Stage();
            stage.setTitle("Nouveau suivi");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.showAndWait();

            chargerSuivisParObjectif();

        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture de la fenêtre d'ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirModifierSuivi() {
        try {
            SuiviBienEtre suiviSelectionne = tableSuivis.getSelectionModel().getSelectedItem();

            if (suiviSelectionne == null) {
                System.out.println("Aucun suivi sélectionné pour modification.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/backoffice/suivibienetre/ModifierSuivi.fxml"));
            Parent root = loader.load();

            ModifierSuiviController controller = loader.getController();
            controller.setSuivi(suiviSelectionne);
            controller.setAfficherSuivisController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier suivi");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.showAndWait();

            chargerSuivisParObjectif();

        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture de la fenêtre de modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ouvrirModifierSuiviDepuisLigne(SuiviBienEtre suiviSelectionne) {
        try {
            if (suiviSelectionne == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/backoffice/suivibienetre/ModifierSuivi.fxml"));
            Parent root = loader.load();

            ModifierSuiviController controller = loader.getController();
            controller.setSuivi(suiviSelectionne);
            controller.setAfficherSuivisController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier suivi");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.showAndWait();

            chargerSuivisParObjectif();

        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture de la fenêtre de modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void supprimerSuivi() {
        try {
            SuiviBienEtre suiviSelectionne = tableSuivis.getSelectionModel().getSelectedItem();

            if (suiviSelectionne == null) {
                System.out.println("Aucun suivi sélectionné.");
                return;
            }

            SuiviBienEtreService service = new SuiviBienEtreService();
            service.supprimer(suiviSelectionne.getId());
            chargerSuivisParObjectif();

        } catch (Exception e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void supprimerSuiviDepuisLigne(SuiviBienEtre suiviSelectionne) {
        try {
            if (suiviSelectionne == null) {
                return;
            }

            SuiviBienEtreService service = new SuiviBienEtreService();
            service.supprimer(suiviSelectionne.getId());
            chargerSuivisParObjectif();

        } catch (Exception e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirObjectifs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/backoffice/objectifsante/AfficherObjectifs.fxml"));
            Parent root = loader.load();

            AfficherObjectifsController controller = loader.getController();
            controller.setSidebarController(sidebarController);
            controller.setContentArea(contentArea);

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }

        } catch (Exception e) {
            System.out.println("Erreur lors du retour vers les objectifs : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void rechargerPageSuivis(int objectifId, SidebarController sidebarController) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/backoffice/suivibienetre/AfficherSuivis.fxml"));
            Parent root = loader.load();

            AfficherSuivisController controller = loader.getController();
            controller.setObjectifId(objectifId);
            controller.setSidebarController(sidebarController);
            controller.setContentArea(contentArea);

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }

        } catch (Exception e) {
            System.out.println("Erreur lors du rechargement des suivis : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void actualiserSuivis() {
        chargerSuivisParObjectif();
    }
}
package piJava.Controllers.backoffice.objectifsante;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import piJava.Controllers.backoffice.SidebarController;
import piJava.Controllers.backoffice.suivibienetre.AfficherSuivisController;
import piJava.entities.ObjectifSante;
import piJava.services.ObjectifSanteService;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.sql.SQLException;

public class AfficherArchivesObjectifsController {

    @FXML private TableView<ObjectifSante> tableArchives;
    @FXML private TableColumn<ObjectifSante, Integer> colId;
    @FXML private TableColumn<ObjectifSante, Integer> colUserId;
    @FXML private TableColumn<ObjectifSante, String> colUserNom;
    @FXML private TableColumn<ObjectifSante, String> colUserPrenom;
    @FXML private TableColumn<ObjectifSante, String> colTitre;
    @FXML private TableColumn<ObjectifSante, String> colType;
    @FXML private TableColumn<ObjectifSante, Integer> colValeurCible;
    @FXML private TableColumn<ObjectifSante, java.sql.Date> colDateDebut;
    @FXML private TableColumn<ObjectifSante, java.sql.Date> colDateFin;
    @FXML private TableColumn<ObjectifSante, String> colPriorite;
    @FXML private TableColumn<ObjectifSante, String> colStatut;
    @FXML private TableColumn<ObjectifSante, java.sql.Timestamp> colArchivedAt;
    @FXML private TableColumn<ObjectifSante, Void> colActions;
    @FXML private Label lblNbArchives;

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
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUserNom.setCellValueFactory(new PropertyValueFactory<>("userNom"));
        colUserPrenom.setCellValueFactory(new PropertyValueFactory<>("userPrenom"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colValeurCible.setCellValueFactory(new PropertyValueFactory<>("valeurCible"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colArchivedAt.setCellValueFactory(new PropertyValueFactory<>("archivedAt"));

        appliquerBadges();
        appliquerStylesColonnes();
        ajouterColonneActions();

        chargerArchives();
    }

    private void appliquerBadges() {
        colType.setCellFactory(column -> new TableCell<>() {
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
                    case "SOMMEIL" -> badge.getStyleClass().add("badge-dark-blue");
                    case "SPORT" -> badge.getStyleClass().add("badge-midnight");
                    case "ALIMENTATION" -> badge.getStyleClass().add("badge-steel");
                    default -> badge.getStyleClass().add("badge-neutral-dark");
                }

                setGraphic(badge);
                setText(null);
            }
        });

        colPriorite.setCellFactory(column -> new TableCell<>() {
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
                    case "HAUTE" -> badge.getStyleClass().add("badge-priority-high");
                    case "MOYENNE" -> badge.getStyleClass().add("badge-priority-medium");
                    case "BASSE" -> badge.getStyleClass().add("badge-priority-low");
                    default -> badge.getStyleClass().add("badge-neutral-dark");
                }

                setGraphic(badge);
                setText(null);
            }
        });

        colStatut.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                badge.setText(item.replace("_", " "));
                badge.getStyleClass().setAll("table-badge");

                switch (item.toUpperCase()) {
                    case "ATTEINT" -> badge.getStyleClass().add("badge-status-done");
                    case "ABANDONNE" -> badge.getStyleClass().add("badge-status-cancel");
                    default -> badge.getStyleClass().add("badge-neutral-dark");
                }

                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void appliquerStylesColonnes() {
        colId.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                badge.setText("#" + item);
                badge.getStyleClass().setAll("mini-badge-dark");
                setGraphic(badge);
                setText(null);
            }
        });

        colUserId.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                badge.setText("U-" + item);
                badge.getStyleClass().setAll("mini-badge-steel");
                setGraphic(badge);
                setText(null);
            }
        });

        colTitre.setCellFactory(column -> new TableCell<>() {
            private final Label label = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                label.setText(item);
                label.getStyleClass().setAll("cell-title-strong");
                setGraphic(label);
                setText(null);
            }
        });

        colUserNom.setCellFactory(column -> new TableCell<>() {
            private final Label label = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                label.setText(item);
                label.getStyleClass().setAll("cell-user-name");
                setGraphic(label);
                setText(null);
            }
        });

        colUserPrenom.setCellFactory(column -> new TableCell<>() {
            private final Label label = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                label.setText(item);
                label.getStyleClass().setAll("cell-user-firstname");
                setGraphic(label);
                setText(null);
            }
        });

        colValeurCible.setCellFactory(column -> new TableCell<>() {
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
            }
        });

        colDateDebut.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(java.sql.Date item, boolean empty) {
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
            }
        });

        colDateFin.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(java.sql.Date item, boolean empty) {
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
            }
        });

        colArchivedAt.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(java.sql.Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                badge.setText(item.toLocalDateTime().toString().replace("T", " "));
                badge.getStyleClass().setAll("mini-badge-steel");
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void ajouterColonneActions() {
        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnVoirSuivis = new Button("👁");
            private final Button btnModifier = new Button("✎");
            private final Button btnSupprimer = new Button("✖");
            private final HBox container = new HBox(8, btnVoirSuivis, btnModifier, btnSupprimer);

            {
                btnVoirSuivis.getStyleClass().add("secondary-button");
                btnModifier.getStyleClass().add("primary-button");
                btnSupprimer.getStyleClass().add("danger-button");

                btnVoirSuivis.setPrefWidth(45);
                btnModifier.setPrefWidth(45);
                btnSupprimer.setPrefWidth(45);

                btnVoirSuivis.setStyle("-fx-font-size: 16px; -fx-padding: 4;");
                btnModifier.setStyle("-fx-font-size: 16px; -fx-padding: 4;");
                btnSupprimer.setStyle("-fx-font-size: 16px; -fx-padding: 4;");

                btnVoirSuivis.setOnAction(event -> {
                    ObjectifSante objectif = getTableView().getItems().get(getIndex());
                    ouvrirAfficherSuivisDepuisLigne(objectif);
                });

                btnModifier.setOnAction(event -> {
                    ObjectifSante objectif = getTableView().getItems().get(getIndex());
                    ouvrirModifierObjectifDepuisLigne(objectif);
                });

                btnSupprimer.setOnAction(event -> {
                    ObjectifSante objectif = getTableView().getItems().get(getIndex());
                    supprimerObjectifDepuisLigne(objectif);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void ouvrirAfficherSuivisDepuisLigne(ObjectifSante objectifSelectionne) {
        try {
            if (objectifSelectionne == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/backoffice/suivibienetre/AfficherSuivis.fxml"));
            Parent root = loader.load();

            AfficherSuivisController controller = loader.getController();
            controller.setObjectifId(objectifSelectionne.getId());
            controller.setSidebarController(sidebarController);
            controller.setContentArea(contentArea);

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture des suivis : " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void afficherMessage(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void ouvrirModifierObjectifDepuisLigne(ObjectifSante objectifSelectionne) {
        try {
            if (objectifSelectionne == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/backoffice/objectifsante/ModifierObjectif.fxml"));
            Parent root = loader.load();

            ModifierObjectifController controller = loader.getController();
            controller.setObjectif(objectifSelectionne);
            controller.setAfficherObjectifsController(null);

            Stage stage = new Stage();
            stage.setTitle("Modifier objectif archivé");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            chargerArchives();

        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture de la modification archive : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void supprimerObjectifDepuisLigne(ObjectifSante objectifSelectionne) {
        try {
            if (objectifSelectionne == null) {
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer la suppression");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous vraiment supprimer cet objectif archivé ?");

            ButtonType resultat = confirm.showAndWait().orElse(ButtonType.CANCEL);

            if (resultat != ButtonType.OK) {
                return;
            }

            ObjectifSanteService service = new ObjectifSanteService();
            service.supprimer(objectifSelectionne.getId());
            chargerArchives();

            afficherMessage("Succès", "L'objectif archivé a été supprimé avec succès.");

        } catch (Exception e) {
            System.out.println("Erreur lors de la suppression archive : " + e.getMessage());
            e.printStackTrace();
            afficherMessage("Erreur", "Erreur lors de la suppression de l'objectif archivé.");
        }
    }
    @FXML
    public void retourObjectifsActifs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/backoffice/objectifsante/AfficherObjectifs.fxml"));
            Parent root = loader.load();

            AfficherObjectifsController controller = loader.getController();
            controller.setSidebarController(sidebarController);
            controller.setContentArea(contentArea);
            controller.chargerObjectifs();

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }

        } catch (Exception e) {
            System.out.println("Erreur lors du retour aux objectifs actifs : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void chargerArchives() {
        ObjectifSanteService service = new ObjectifSanteService();

        try {
            ObservableList<ObjectifSante> objectifs = FXCollections.observableArrayList();
            objectifs.addAll(service.recupererArchivesBack());
            tableArchives.setItems(objectifs);

            if (lblNbArchives != null) {
                lblNbArchives.setText(objectifs.size() + " archive(s)");
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors du chargement des archives : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
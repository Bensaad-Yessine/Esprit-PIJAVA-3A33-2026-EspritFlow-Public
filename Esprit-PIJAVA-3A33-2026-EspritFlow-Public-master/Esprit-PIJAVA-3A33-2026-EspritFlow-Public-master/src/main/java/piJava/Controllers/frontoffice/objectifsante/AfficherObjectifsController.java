package piJava.Controllers.frontoffice.objectifsante;

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
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.Controllers.frontoffice.suivibienetre.AfficherSuivisController;
import piJava.entities.ObjectifSante;
import piJava.entities.user;
import piJava.services.ObjectifSanteService;
import piJava.utils.SessionManager;

import java.sql.SQLException;

public class AfficherObjectifsController {

    @FXML
    private TableView<ObjectifSante> tableObjectifs;

    @FXML
    private TableColumn<ObjectifSante, String> colTitre;

    @FXML
    private TableColumn<ObjectifSante, String> colType;

    @FXML
    private TableColumn<ObjectifSante, Integer> colValeurCible;

    @FXML
    private TableColumn<ObjectifSante, java.sql.Date> colDateDebut;

    @FXML
    private TableColumn<ObjectifSante, java.sql.Date> colDateFin;

    @FXML
    private TableColumn<ObjectifSante, String> colPriorite;

    @FXML
    private TableColumn<ObjectifSante, String> colStatut;

    @FXML
    private TableColumn<ObjectifSante, Void> colActions;

    @FXML
    private TextField txtRecherche;

    @FXML
    private ComboBox<String> cbCategorie;

    @FXML
    private ComboBox<String> cbTri;

    private FrontSidebarController sidebarController;
    private StackPane contentArea;

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colValeurCible.setCellValueFactory(new PropertyValueFactory<>("valeurCible"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        appliquerBadges();
        appliquerStylesColonnes();
        ajouterColonneActions();

        cbCategorie.getItems().addAll("Toutes", "SOMMEIL", "SPORT", "ALIMENTATION");
        cbCategorie.setValue("Toutes");

        cbTri.getItems().addAll("Par défaut", "Priorité", "Date début");
        cbTri.setValue("Par défaut");

        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> appliquerRechercheEtTri());
        cbCategorie.valueProperty().addListener((obs, oldVal, newVal) -> appliquerRechercheEtTri());
        cbTri.valueProperty().addListener((obs, oldVal, newVal) -> appliquerRechercheEtTri());

        chargerObjectifs();
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
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
                    case "EN_COURS" -> badge.getStyleClass().add("badge-status-progress");
                    case "ATTEINT" -> badge.getStyleClass().add("badge-status-done");
                    case "ABANDONNE" -> badge.getStyleClass().add("badge-status-cancel");
                    default -> badge.getStyleClass().add("badge-neutral-dark");
                }

                setGraphic(badge);
                setText(null);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        });
    }

    private void appliquerStylesColonnes() {
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
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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

                btnVoirSuivis.setMinWidth(45);
                btnModifier.setMinWidth(45);
                btnSupprimer.setMinWidth(45);

                btnVoirSuivis.setPrefWidth(45);
                btnModifier.setPrefWidth(45);
                btnSupprimer.setPrefWidth(45);

                btnVoirSuivis.setMaxWidth(45);
                btnModifier.setMaxWidth(45);
                btnSupprimer.setMaxWidth(45);

                btnVoirSuivis.setStyle("-fx-font-size: 16px; -fx-padding: 4;");
                btnModifier.setStyle("-fx-font-size: 16px; -fx-padding: 4;");
                btnSupprimer.setStyle("-fx-font-size: 16px; -fx-padding: 4;");

                container.setPrefWidth(160);
                container.setMinWidth(160);

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

    @FXML
    public void ouvrirAjouterObjectif() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/objectifsante/AjouterObjectif.fxml"));
            Parent root = loader.load();

            AjouterObjectifController controller = loader.getController();
            controller.setAfficherObjectifsController(this);

            Stage stage = new Stage();
            stage.setTitle("Nouvel objectif");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.showAndWait();

            chargerObjectifs();

        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture de la fenêtre d'ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void supprimerObjectif() {
        try {
            ObjectifSante objectifSelectionne = tableObjectifs.getSelectionModel().getSelectedItem();

            if (objectifSelectionne == null) {
                System.out.println("Aucun objectif sélectionné.");
                return;
            }

            user currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                System.out.println("Aucun utilisateur connecté.");
                return;
            }

            ObjectifSanteService service = new ObjectifSanteService();
            boolean success = service.supprimerParUser(objectifSelectionne.getId(), currentUser.getId());

            if (success) {
                chargerObjectifs();
            } else {
                System.out.println("Suppression refusée.");
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirModifierObjectif() {
        try {
            ObjectifSante objectifSelectionne = tableObjectifs.getSelectionModel().getSelectedItem();

            if (objectifSelectionne == null) {
                System.out.println("Aucun objectif sélectionné pour modification.");
                return;
            }

            user currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                System.out.println("Aucun utilisateur connecté.");
                return;
            }

            ObjectifSanteService service = new ObjectifSanteService();
            ObjectifSante objectifSecure = service.recupererParIdEtUser(objectifSelectionne.getId(), currentUser.getId());

            if (objectifSecure == null) {
                System.out.println("Accès refusé à cet objectif.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/objectifsante/ModifierObjectif.fxml"));
            Parent root = loader.load();

            ModifierObjectifController controller = loader.getController();
            controller.setObjectif(objectifSecure);
            controller.setAfficherObjectifsController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier objectif");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.showAndWait();

            chargerObjectifs();

        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture de la fenêtre de modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirAfficherSuivis() {
        try {
            ObjectifSante objectifSelectionne = tableObjectifs.getSelectionModel().getSelectedItem();

            if (objectifSelectionne == null) {
                System.out.println("Aucun objectif sélectionné.");
                return;
            }

            user currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                System.out.println("Aucun utilisateur connecté.");
                return;
            }

            ObjectifSanteService service = new ObjectifSanteService();
            ObjectifSante objectifSecure = service.recupererParIdEtUser(objectifSelectionne.getId(), currentUser.getId());

            if (objectifSecure == null) {
                System.out.println("Accès refusé à cet objectif.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/suivibienetre/AfficherSuivis.fxml"));
            Parent root = loader.load();

            AfficherSuivisController controller = loader.getController();
            controller.setObjectifId(objectifSecure.getId());
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

    private void ouvrirAfficherSuivisDepuisLigne(ObjectifSante objectifSelectionne) {
        try {
            if (objectifSelectionne == null) {
                return;
            }

            user currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                System.out.println("Aucun utilisateur connecté.");
                return;
            }

            ObjectifSanteService service = new ObjectifSanteService();
            ObjectifSante objectifSecure = service.recupererParIdEtUser(objectifSelectionne.getId(), currentUser.getId());

            if (objectifSecure == null) {
                System.out.println("Accès refusé à cet objectif.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/suivibienetre/AfficherSuivis.fxml"));
            Parent root = loader.load();

            AfficherSuivisController controller = loader.getController();
            controller.setObjectifId(objectifSecure.getId());
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

    private void ouvrirModifierObjectifDepuisLigne(ObjectifSante objectifSelectionne) {
        try {
            if (objectifSelectionne == null) {
                return;
            }

            user currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                System.out.println("Aucun utilisateur connecté.");
                return;
            }

            ObjectifSanteService service = new ObjectifSanteService();
            ObjectifSante objectifSecure = service.recupererParIdEtUser(objectifSelectionne.getId(), currentUser.getId());

            if (objectifSecure == null) {
                System.out.println("Accès refusé à cet objectif.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/objectifsante/ModifierObjectif.fxml"));
            Parent root = loader.load();

            ModifierObjectifController controller = loader.getController();
            controller.setObjectif(objectifSecure);
            controller.setAfficherObjectifsController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier objectif");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.showAndWait();

            chargerObjectifs();

        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture de la fenêtre de modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void supprimerObjectifDepuisLigne(ObjectifSante objectifSelectionne) {
        try {
            if (objectifSelectionne == null) {
                return;
            }

            user currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                System.out.println("Aucun utilisateur connecté.");
                return;
            }

            ObjectifSanteService service = new ObjectifSanteService();
            boolean success = service.supprimerParUser(objectifSelectionne.getId(), currentUser.getId());

            if (success) {
                chargerObjectifs();
            } else {
                System.out.println("Suppression refusée.");
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void appliquerRechercheEtTri() {
        ObjectifSanteService service = new ObjectifSanteService();

        try {
            user currentUser = SessionManager.getInstance().getCurrentUser();

            if (currentUser == null) {
                tableObjectifs.setItems(FXCollections.observableArrayList());
                return;
            }

            String recherche = txtRecherche.getText();
            String categorie = cbCategorie.getValue();
            String triSelection = cbTri.getValue();

            String tri = null;
            if ("Priorité".equals(triSelection)) {
                tri = "priorite";
            } else if ("Date début".equals(triSelection)) {
                tri = "date_debut";
            }

            ObservableList<ObjectifSante> objectifs = FXCollections.observableArrayList();
            objectifs.addAll(service.rechercherEtTrierFront(currentUser.getId(), recherche, categorie, tri));
            tableObjectifs.setItems(objectifs);

        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche / tri des objectifs FRONT : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void reinitialiserFiltres() {
        txtRecherche.clear();
        cbCategorie.setValue("Toutes");
        cbTri.setValue("Par défaut");
        chargerObjectifs();
    }

    public void chargerObjectifs() {
        ObjectifSanteService service = new ObjectifSanteService();

        try {
            user currentUser = SessionManager.getInstance().getCurrentUser();

            if (currentUser == null) {
                tableObjectifs.setItems(FXCollections.observableArrayList());
                return;
            }

            ObservableList<ObjectifSante> objectifs = FXCollections.observableArrayList();
            objectifs.addAll(service.recupererParUser(currentUser.getId()));
            tableObjectifs.setItems(objectifs);

        } catch (SQLException e) {
            System.out.println("Erreur lors du chargement des objectifs : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
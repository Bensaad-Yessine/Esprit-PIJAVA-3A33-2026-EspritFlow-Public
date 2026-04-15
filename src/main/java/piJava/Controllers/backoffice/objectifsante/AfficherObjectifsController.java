package piJava.Controllers.backoffice.objectifsante;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import piJava.Controllers.backoffice.SidebarController;
import piJava.Controllers.backoffice.suivibienetre.AfficherSuivisController;
import piJava.entities.ObjectifSante;
import piJava.services.ObjectifSanteService;

import java.sql.SQLException;

public class AfficherObjectifsController {

    @FXML
    private TableView<ObjectifSante> tableObjectifs;

    @FXML
    private TableColumn<ObjectifSante, Integer> colId;

    @FXML
    private TableColumn<ObjectifSante, Integer> colUserId;

    @FXML
    private TableColumn<ObjectifSante, String> colUserNom;

    @FXML
    private TableColumn<ObjectifSante, String> colUserPrenom;

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
    private TextField txtRecherche;

    @FXML
    private ComboBox<String> cbTri;

    @FXML
    private ComboBox<String> cbCategorie;

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
        System.out.println("AfficherObjectifsController chargé avec succès !");

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

        appliquerBadges();
        appliquerStylesColonnes();

        cbTri.getItems().addAll(
                "Par défaut",
                "Date début",
                "Priorité"
        );
        cbTri.setValue("Par défaut");

        cbCategorie.getItems().addAll(
                "Toutes",
                "SOMMEIL",
                "SPORT",
                "ALIMENTATION"
        );
        cbCategorie.setValue("Toutes");

        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> appliquerRechercheEtTri());
        cbTri.valueProperty().addListener((obs, oldVal, newVal) -> appliquerRechercheEtTri());
        cbCategorie.valueProperty().addListener((obs, oldVal, newVal) -> appliquerRechercheEtTri());

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
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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

    @FXML
    public void ouvrirAjouterObjectif() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/backoffice/objectifsante/AjouterObjectif.fxml"));
            Parent root = loader.load();

            AjouterObjectifController controller = loader.getController();
            controller.setSidebarController(sidebarController);
            controller.setAfficherObjectifsController(this);
            controller.setContentArea(contentArea);

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture de la page d'ajout : " + e.getMessage());
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

            ObjectifSanteService service = new ObjectifSanteService();
            service.supprimer(objectifSelectionne.getId());

            System.out.println("Objectif supprimé avec succès depuis JavaFX !");
            chargerObjectifs();

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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/backoffice/objectifsante/ModifierObjectif.fxml"));
            Parent root = loader.load();

            ModifierObjectifController controller = loader.getController();
            controller.setObjectif(objectifSelectionne);
            controller.setSidebarController(sidebarController);
            controller.setAfficherObjectifsController(this);

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture de la page de modification : " + e.getMessage());
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

    @FXML
    public void appliquerRechercheEtTri() {
        ObjectifSanteService service = new ObjectifSanteService();

        try {
            String recherche = txtRecherche.getText();
            String categorie = cbCategorie.getValue();
            String triSelection = cbTri.getValue();

            String tri = null;
            if ("Date début".equals(triSelection)) {
                tri = "date_debut";
            } else if ("Priorité".equals(triSelection)) {
                tri = "priorite";
            }

            ObservableList<ObjectifSante> objectifs = FXCollections.observableArrayList();
            objectifs.addAll(service.rechercherEtTrierBack(recherche, categorie, tri));
            tableObjectifs.setItems(objectifs);

        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche / tri des objectifs : " + e.getMessage());
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
            ObservableList<ObjectifSante> objectifs = FXCollections.observableArrayList();
            objectifs.addAll(service.recuperer());
            tableObjectifs.setItems(objectifs);
        } catch (SQLException e) {
            System.out.println("Erreur lors du chargement des objectifs : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
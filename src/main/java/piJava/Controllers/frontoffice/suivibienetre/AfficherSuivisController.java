package piJava.Controllers.frontoffice.suivibienetre;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.Controllers.frontoffice.objectifsante.AfficherObjectifsController;
import piJava.entities.SuiviBienEtre;
import piJava.entities.user;
import piJava.services.SuiviBienEtreService;
import piJava.utils.SessionManager;

import java.sql.Date;
import java.sql.SQLException;

public class AfficherSuivisController {

    @FXML
    private TableView<SuiviBienEtre> tableSuivis;

    @FXML
    private TableColumn<SuiviBienEtre, Integer> colId;

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
    private TableColumn<SuiviBienEtre, Integer> colObjectifId;

    private int objectifId;
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
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDateSaisie.setCellValueFactory(new PropertyValueFactory<>("dateSaisie"));
        colHumeur.setCellValueFactory(new PropertyValueFactory<>("humeur"));
        colQualiteSommeil.setCellValueFactory(new PropertyValueFactory<>("qualiteSommeil"));
        colNiveauEnergie.setCellValueFactory(new PropertyValueFactory<>("niveauEnergie"));
        colNiveauStress.setCellValueFactory(new PropertyValueFactory<>("niveauStress"));
        colQualiteAlimentation.setCellValueFactory(new PropertyValueFactory<>("qualiteAlimentation"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colObjectifId.setCellValueFactory(new PropertyValueFactory<>("objectifId"));

        appliquerStylesColonnes();
    }

    private void appliquerStylesColonnes() {
        colId.setCellFactory(column -> new TableCell<SuiviBienEtre, Integer>() {
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

        colObjectifId.setCellFactory(column -> new TableCell<SuiviBienEtre, Integer>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                badge.setText("OBJ-" + item);
                badge.getStyleClass().setAll("mini-badge-steel");

                setGraphic(badge);
                setText(null);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        });

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

    public void setObjectifId(int objectifId) {
        this.objectifId = objectifId;
        chargerSuivisParObjectif();
    }

    public void chargerSuivisParObjectif() {
        SuiviBienEtreService service = new SuiviBienEtreService();

        try {
            user currentUser = SessionManager.getInstance().getCurrentUser();

            if (currentUser == null) {
                System.out.println("Aucun utilisateur connecté.");
                tableSuivis.setItems(FXCollections.observableArrayList());
                return;
            }

            ObservableList<SuiviBienEtre> suivis = FXCollections.observableArrayList();
            suivis.addAll(service.recupererParObjectifEtUser(objectifId, currentUser.getId()));
            tableSuivis.setItems(suivis);

        } catch (SQLException e) {
            System.out.println("Erreur lors du chargement des suivis : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirAjouterSuivi() {
        try {
            user currentUser = SessionManager.getInstance().getCurrentUser();

            if (currentUser == null) {
                System.out.println("Aucun utilisateur connecté.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/suivibienetre/AjouterSuivi.fxml"));
            Parent root = loader.load();

            AjouterSuiviController controller = loader.getController();
            controller.setAfficherSuivisController(this);
            controller.setObjectifId(objectifId);
            controller.setSidebarController(sidebarController);
            controller.setContentArea(contentArea);

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            } else {
                System.out.println("contentArea est null.");
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture de la page d'ajout : " + e.getMessage());
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

            user currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                System.out.println("Aucun utilisateur connecté.");
                return;
            }

            SuiviBienEtreService service = new SuiviBienEtreService();
            SuiviBienEtre suiviSecure = service.recupererParIdEtUser(suiviSelectionne.getId(), currentUser.getId());

            if (suiviSecure == null) {
                System.out.println("Accès refusé à ce suivi.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/suivibienetre/ModifierSuivi.fxml"));
            Parent root = loader.load();

            ModifierSuiviController controller = loader.getController();
            controller.setSuivi(suiviSecure);
            controller.setAfficherSuivisController(this);
            controller.setSidebarController(sidebarController);
            controller.setContentArea(contentArea);

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            } else {
                System.out.println("contentArea est null.");
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture de la page de modification : " + e.getMessage());
            e.printStackTrace();
        }
    }



    @FXML
    public void ouvrirObjectifs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/objectifsante/AfficherObjectifs.fxml"));
            Parent root = loader.load();

            AfficherObjectifsController controller = loader.getController();
            controller.setSidebarController(sidebarController);
            controller.setContentArea(contentArea);

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            } else {
                System.out.println("contentArea est null.");
            }

        } catch (Exception e) {
            System.out.println("Erreur lors du retour vers les objectifs : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void rechargerPageSuivis(int objectifId, FrontSidebarController sidebarController) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/suivibienetre/AfficherSuivis.fxml"));
            Parent root = loader.load();

            AfficherSuivisController controller = loader.getController();
            controller.setObjectifId(objectifId);
            controller.setSidebarController(sidebarController);
            controller.setContentArea(contentArea);

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            } else {
                System.out.println("contentArea est null.");
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
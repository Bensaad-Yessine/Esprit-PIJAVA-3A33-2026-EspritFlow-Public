package piJava.Controllers.frontoffice.objectifsante;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.ObjectifSante;
import piJava.entities.SuiviBienEtre;
import piJava.entities.user;
import piJava.services.ObjectifSanteService;
import piJava.services.SuiviBienEtreService;
import piJava.services.api.PdfArchiveObjectifService;
import piJava.utils.SessionManager;

import java.awt.Desktop;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class AfficherArchivesObjectifsController {

    @FXML
    private TableView<ObjectifSante> tableArchives;

    @FXML
    private TableColumn<ObjectifSante, String> colTitre;

    @FXML
    private TableColumn<ObjectifSante, String> colType;

    @FXML
    private TableColumn<ObjectifSante, Integer> colCible;

    @FXML
    private TableColumn<ObjectifSante, java.sql.Date> colDebut;

    @FXML
    private TableColumn<ObjectifSante, java.sql.Date> colFin;

    @FXML
    private TableColumn<ObjectifSante, String> colPriorite;

    @FXML
    private TableColumn<ObjectifSante, String> colStatutFinal;

    @FXML
    private TableColumn<ObjectifSante, java.sql.Timestamp> colArchivedAt;

    @FXML
    private TableColumn<ObjectifSante, Void> colPdf;

    @FXML
    private TableColumn<ObjectifSante, Void> colRetourAction;

    @FXML
    private TableColumn<ObjectifSante, Void> colSupprimer;

    @FXML
    private Label lblNbArchives;

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
        colCible.setCellValueFactory(new PropertyValueFactory<>("valeurCible"));
        colDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colStatutFinal.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colArchivedAt.setCellValueFactory(new PropertyValueFactory<>("archivedAt"));

        tableArchives.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        appliquerStylesColonnes();
        ajouterColonnePdf();
        ajouterColonneRetour();
        ajouterColonneSupprimer();

        chargerArchives();
    }

    @FXML
    public void retour() {
        retourObjectifsActifs();
    }

    @FXML
    public void retourObjectifsActifs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/objectifsante/AfficherObjectifs.fxml"));
            Parent root = loader.load();

            AfficherObjectifsController controller = loader.getController();
            controller.setSidebarController(sidebarController);
            controller.setContentArea(contentArea);
            controller.chargerObjectifs();

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }

        } catch (Exception e) {
            e.printStackTrace();
            afficherMessage("Erreur", "Impossible de retourner à la liste des objectifs.");
        }
    }

    public void chargerArchives() {
        ObjectifSanteService service = new ObjectifSanteService();

        try {
            user currentUser = SessionManager.getInstance().getCurrentUser();

            if (currentUser == null) {
                tableArchives.setItems(FXCollections.observableArrayList());
                lblNbArchives.setText("0 archive(s)");
                return;
            }

            ObservableList<ObjectifSante> objectifs = FXCollections.observableArrayList();
            objectifs.addAll(service.recupererArchivesParUser(currentUser.getId()));
            tableArchives.setItems(objectifs);
            lblNbArchives.setText(objectifs.size() + " archive(s)");

        } catch (SQLException e) {
            e.printStackTrace();
            afficherMessage("Erreur", "Erreur lors du chargement des objectifs archivés.");
        }
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
            }
        });

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
                badge.getStyleClass().setAll("badge-chip", "badge-type");
                setGraphic(badge);
                setText(null);
            }
        });

        colCible.setCellFactory(column -> new TableCell<>() {
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

        colDebut.setCellFactory(column -> new TableCell<>() {
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
                badge.getStyleClass().setAll("badge-chip", "badge-date");
                setGraphic(badge);
                setText(null);
            }
        });

        colFin.setCellFactory(column -> new TableCell<>() {
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
                badge.getStyleClass().setAll("badge-chip", "badge-date");
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

                String style = "badge-priorite-moyenne";
                if ("BASSE".equalsIgnoreCase(item)) {
                    style = "badge-priorite-basse";
                } else if ("HAUTE".equalsIgnoreCase(item)) {
                    style = "badge-priorite-haute";
                }

                badge.setText(item);
                badge.getStyleClass().setAll("badge-chip", style);
                setGraphic(badge);
                setText(null);
            }
        });

        colStatutFinal.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                String style = "badge-statut-abandonne";
                if ("ATTEINT".equalsIgnoreCase(item)) {
                    style = "badge-statut-atteint";
                }

                badge.setText(item.replace("_", " "));
                badge.getStyleClass().setAll("badge-chip", style);
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

                java.time.format.DateTimeFormatter formatter =
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                badge.setText(item.toLocalDateTime().format(formatter));
                badge.getStyleClass().setAll("badge-chip", "badge-date");
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void ajouterColonnePdf() {
        colPdf.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("📄");

            {
                btn.getStyleClass().addAll("table-action-btn", "pdf-btn");
                btn.setTooltip(new Tooltip("Ouvrir le PDF"));

                btn.setOnAction(e -> {
                    ObjectifSante obj = getTableView().getItems().get(getIndex());
                    ouvrirPdfArchive(obj);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
                setText(null);
            }
        });
    }

    private void ajouterColonneRetour() {
        colRetourAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("←");

            {
                btn.getStyleClass().addAll("table-action-btn", "retour-btn");
                btn.setTooltip(new Tooltip("Retour à la liste active"));
                btn.setOnAction(e -> retourObjectifsActifs());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
                setText(null);
            }
        });
    }

    private void ajouterColonneSupprimer() {
        colSupprimer.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✖");

            {
                btn.getStyleClass().addAll("table-action-btn", "delete-btn");
                btn.setTooltip(new Tooltip("Supprimer l'objectif archivé"));

                btn.setOnAction(e -> {
                    ObjectifSante obj = getTableView().getItems().get(getIndex());
                    supprimerArchive(obj);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
                setText(null);
            }
        });
    }

    private void supprimerArchive(ObjectifSante objectif) {
        try {
            if (objectif == null) {
                afficherMessage("Suppression", "Aucun objectif sélectionné.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Supprimer l'archive");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous vraiment supprimer cet objectif archivé ?");

            ButtonType resultat = confirm.showAndWait().orElse(ButtonType.CANCEL);
            if (resultat != ButtonType.OK) {
                return;
            }

            ObjectifSanteService service = new ObjectifSanteService();
            service.supprimer(objectif.getId());

            chargerArchives();
            afficherMessage("Succès", "L'objectif archivé a été supprimé avec succès.");

        } catch (Exception e) {
            e.printStackTrace();
            afficherMessage("Erreur", "Impossible de supprimer l'objectif archivé.");
        }
    }

    private void ouvrirPdfArchive(ObjectifSante objectif) {
        try {
            if (objectif == null) {
                afficherMessage("PDF", "Aucun objectif sélectionné.");
                return;
            }

            SuiviBienEtreService suiviService = new SuiviBienEtreService();
            PdfArchiveObjectifService pdfService = new PdfArchiveObjectifService();

            List<SuiviBienEtre> suivis = suiviService.recupererParObjectif(objectif.getId());
            File fichierPdf = pdfService.genererPdf(objectif, suivis);

            if (fichierPdf == null || !fichierPdf.exists()) {
                afficherMessage("PDF", "Le PDF n'a pas pu être généré.");
                return;
            }

            ouvrirFichierPdf(fichierPdf);

        } catch (Exception e) {
            e.printStackTrace();
            afficherMessage("Erreur", "Impossible de générer ou d'ouvrir le PDF.");
        }
    }

    private void ouvrirFichierPdf(File fichierPdf) {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", "", "\"" + fichierPdf.getAbsolutePath() + "\"")
                        .redirectErrorStream(true)
                        .start();
                return;
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(fichierPdf);
                return;
            }

            afficherMessage("PDF généré", "Le PDF a été généré ici :\n" + fichierPdf.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            afficherMessage(
                    "PDF généré mais non ouvert",
                    "Le PDF existe bien, mais il n'a pas pu être ouvert automatiquement.\n\nChemin du fichier :\n"
                            + fichierPdf.getAbsolutePath()
            );
        }
    }

    private void afficherMessage(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
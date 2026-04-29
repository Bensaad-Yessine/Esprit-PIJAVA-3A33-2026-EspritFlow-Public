package piJava.Controllers.frontoffice.objectifsante;

/*
    ===============================
    IMPORTS JAVAFX
    ===============================
*/
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

/*
    ===============================
    IMPORTS PROJET
    ===============================
*/
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.ExerciceSport;
import piJava.entities.ObjectifSante;
import piJava.entities.Recette;
import piJava.entities.SuiviBienEtre;
import piJava.services.SuiviBienEtreService;
import piJava.services.api.CoachingPriveService;
import piJava.services.api.CoachingResponse;
import piJava.services.api.SpoonacularClient;
import piJava.services.api.WorkoutApiExerciseClient;

/*
    ===============================
    IMPORTS JAVA
    ===============================
*/
import java.awt.Desktop;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailObjectifController {

    /*
        ===============================
        PARTIE 1 : ELEMENTS FXML - DETAILS OBJECTIF
        ===============================
    */

    @FXML
    private Label lblTitre;

    @FXML
    private Label lblType;

    @FXML
    private Label lblValeurCible;

    @FXML
    private Label lblDateDebut;

    @FXML
    private Label lblDateFin;

    @FXML
    private Label lblPriorite;

    @FXML
    private Label lblStatut;

    /*
        ===============================
        PARTIE 2 : ELEMENTS FXML - RESUME SUIVIS
        ===============================
    */

    @FXML
    private Label lblNombreSuivis;

    @FXML
    private Label lblScoreMoyen;

    @FXML
    private ListView<String> listDerniersSuivis;

    /*
        ===============================
        PARTIE 3 : ELEMENTS FXML - COACHING INTELLIGENT
        ===============================
    */

    @FXML
    private Label lblCoachingTitre;

    @FXML
    private Label lblCoachingNiveau;

    @FXML
    private Label lblPointFort;

    @FXML
    private Label lblPointFaible;

    @FXML
    private Label lblTendance;

    @FXML
    private Label lblMessageMotivation;

    @FXML
    private Label lblResumeAnalyse;

    @FXML
    private VBox boxConseils;

    @FXML
    private VBox boxConseilsFaibles;

    @FXML
    private Label lblMessageConfiance;

    /*
        ===============================
        PARTIE 4 : ELEMENTS FXML - GRAPHIQUES
        ===============================
    */

    @FXML
    private LineChart<String, Number> lineChartScore;

    @FXML
    private BarChart<String, Number> barChartIndicateurs;

    @FXML
    private PieChart pieChartHumeurs;

    @FXML
    private Label lblStatScoreDetail;

    @FXML
    private Label lblStatIndicateursDetail;

    @FXML
    private Label lblStatHumeurDetail;

    /*
        ===============================
        PARTIE 5 : ELEMENTS FXML - API SPOONACULAR RECETTES
        ===============================
    */

    @FXML
    private VBox boxRecettes;

    @FXML
    private Label lblRecetteMessage;

    @FXML
    private Button btnProposerRecette;

  /*
    ===============================
    PARTIE 6 : ELEMENTS FXML - WORKOUT API EXERCICES
    ===============================
*/

    @FXML
    private VBox boxExercices;

    @FXML
    private Label lblExerciceMessage;

    @FXML
    private Button btnProposerExercices;

    /*
        ===============================
        PARTIE 7 : VARIABLES GLOBALES
        ===============================
    */

    private FrontSidebarController sidebarController;
    private StackPane contentArea;
    private ObjectifSante objectif;

    /*
        ===============================
        PARTIE 8 : SETTERS POUR LA NAVIGATION
        ===============================
    */

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void setObjectif(ObjectifSante objectif) {
        this.objectif = objectif;
        chargerDetails();
    }

    /*
        ===============================
        PARTIE 9 : RETOUR VERS LA LISTE DES OBJECTIFS
        ===============================
    */

    @FXML
    private void retourListeObjectifs() {
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
            System.out.println("Erreur lors du retour vers la liste des objectifs : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
        ===============================
        PARTIE 10 : CHARGEMENT PRINCIPAL DES DETAILS
        ===============================
    */

    private void chargerDetails() {
        if (objectif == null) {
            return;
        }

        /*
            Remplissage des informations de l'objectif
        */
        lblTitre.setText(safe(objectif.getTitre()));
        lblType.setText(safe(objectif.getType()));
        lblValeurCible.setText(String.valueOf(objectif.getValeurCible()));
        lblDateDebut.setText(objectif.getDateDebut() != null ? objectif.getDateDebut().toString() : "-");
        lblDateFin.setText(objectif.getDateFin() != null ? objectif.getDateFin().toString() : "-");
        lblPriorite.setText(safe(objectif.getPriorite()));
        lblStatut.setText(safe(objectif.getStatut()));

        /*
            Gestion de visibilité selon le type d'objectif
        */
        boolean estAlimentation = "ALIMENTATION".equalsIgnoreCase(safe(objectif.getType()));
        boolean estSport = "SPORT".equalsIgnoreCase(safe(objectif.getType()));

        /*
            Bloc recettes visible seulement pour ALIMENTATION
        */
        if (btnProposerRecette != null) {
            btnProposerRecette.setVisible(estAlimentation);
            btnProposerRecette.setManaged(estAlimentation);
        }

        if (lblRecetteMessage != null) {
            if (estAlimentation) {
                lblRecetteMessage.setText("Cliquez sur le bouton pour proposer une recette adaptée à votre objectif alimentation.");
            } else {
                lblRecetteMessage.setText("Les recettes sont disponibles uniquement pour les objectifs de type ALIMENTATION.");
            }
        }

        if (boxRecettes != null && !estAlimentation) {
            boxRecettes.getChildren().clear();
        }

        /*
            Bloc exercices visible seulement pour SPORT
        */
        if (btnProposerExercices != null) {
            btnProposerExercices.setVisible(estSport);
            btnProposerExercices.setManaged(estSport);
        }

        if (lblExerciceMessage != null) {
            if (estSport) {
                lblExerciceMessage.setText("Cliquez sur le bouton pour proposer des exercices adaptés à votre objectif sport.");
            } else {
                lblExerciceMessage.setText("Les exercices sont disponibles uniquement pour les objectifs de type SPORT.");
            }
        }

        if (boxExercices != null && !estSport) {
            boxExercices.getChildren().clear();
        }

        try {
            /*
                Récupération des suivis de l'objectif
            */
            SuiviBienEtreService suiviService = new SuiviBienEtreService();
            List<SuiviBienEtre> suivis = suiviService.recupererParObjectif(objectif.getId());

            /*
                Affichage du nombre de suivis et du score moyen
            */
            lblNombreSuivis.setText(String.valueOf(suivis.size()));

            double scoreMoyen = calculerScoreMoyen(suivis);
            lblScoreMoyen.setText(String.format("%.2f / 100", scoreMoyen));

            /*
                Affichage des 3 derniers suivis
            */
            listDerniersSuivis.setItems(FXCollections.observableArrayList(
                    suivis.stream()
                            .sorted((a, b) -> b.getDateSaisie().compareTo(a.getDateSaisie()))
                            .limit(3)
                            .map(s -> s.getDateSaisie()
                                    + " | Humeur: " + safe(s.getHumeur())
                                    + " | Score: " + String.format("%.0f", s.getScore()) + "/100")
                            .toList()
            ));

            /*
                Chargement des graphiques
            */
            chargerGraphiqueScore(suivis);
            chargerGraphiqueIndicateurs(suivis);
            chargerGraphiqueHumeurs(suivis);

            /*
                Chargement du coaching intelligent
            */
            CoachingPriveService coachingService = new CoachingPriveService();
            CoachingResponse response = coachingService.genererCoaching(objectif, suivis);

            lblCoachingTitre.setText(safe(response.getTitre()));
            lblCoachingNiveau.setText(safe(response.getNiveau()));
            lblPointFort.setText(safe(response.getPointFort()));
            lblPointFaible.setText(safe(response.getPointFaible()));
            lblTendance.setText(safe(response.getTendance()));
            lblMessageMotivation.setText(safe(response.getMessageMotivation()));
            lblResumeAnalyse.setText(safe(response.getResumeAnalyse()));
            lblMessageConfiance.setText(safe(response.getMessageConfiance()));

            /*
                Affichage des conseils sous forme de cards
            */
            afficherConseilsEnCards(
                    boxConseils,
                    response.getConseilsTypeObjectif(),
                    "Conseil objectif",
                    "objectif-card"
            );

            afficherConseilsEnCards(
                    boxConseilsFaibles,
                    response.getConseilsNiveauxFaibles(),
                    "Niveau faible",
                    "faible-card"
            );

        } catch (Exception e) {
            e.printStackTrace();

            /*
                Valeurs par défaut en cas d'erreur
            */
            lblNombreSuivis.setText("0");
            lblScoreMoyen.setText("0 / 100");
            lblCoachingTitre.setText("Coaching indisponible");
            lblCoachingNiveau.setText("-");
            lblPointFort.setText("-");
            lblPointFaible.setText("-");
            lblTendance.setText("-");
            lblMessageMotivation.setText("Impossible de charger le coaching.");
            lblResumeAnalyse.setText("Une erreur s'est produite pendant le chargement.");
            lblMessageConfiance.setText("Le message de confiance est indisponible.");
            lblStatScoreDetail.setText("Aucune donnée.");
            lblStatIndicateursDetail.setText("Aucune donnée.");
            lblStatHumeurDetail.setText("Aucune donnée.");
            listDerniersSuivis.setItems(FXCollections.observableArrayList());

            if (lineChartScore != null) {
                lineChartScore.getData().clear();
            }

            if (barChartIndicateurs != null) {
                barChartIndicateurs.getData().clear();
            }

            if (pieChartHumeurs != null) {
                pieChartHumeurs.getData().clear();
            }

            if (boxConseils != null) {
                boxConseils.getChildren().clear();
            }

            if (boxConseilsFaibles != null) {
                boxConseilsFaibles.getChildren().clear();
            }

            if (boxRecettes != null) {
                boxRecettes.getChildren().clear();
            }

            if (boxExercices != null) {
                boxExercices.getChildren().clear();
            }
        }
    }

    /*
        ===============================
        PARTIE 11 : CALCUL SCORE MOYEN
        ===============================
    */

    private double calculerScoreMoyen(List<SuiviBienEtre> suivis) {
        if (suivis == null || suivis.isEmpty()) {
            return 0;
        }

        double somme = 0;

        for (SuiviBienEtre s : suivis) {
            somme += s.getScore();
        }

        return somme / suivis.size();
    }

    /*
        ===============================
        PARTIE 12 : GRAPHIQUE SCORE
        ===============================
    */

    private void chargerGraphiqueScore(List<SuiviBienEtre> suivis) {
        if (lineChartScore == null) {
            return;
        }

        lineChartScore.getData().clear();

        if (suivis == null || suivis.isEmpty()) {
            lblStatScoreDetail.setText("Aucun suivi pour afficher l’évolution.");
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Score");

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double dernier = 0;

        List<SuiviBienEtre> tries = suivis.stream()
                .sorted((a, b) -> a.getDateSaisie().compareTo(b.getDateSaisie()))
                .toList();

        for (SuiviBienEtre suivi : tries) {
            String date = suivi.getDateSaisie().toString();
            double score = suivi.getScore();

            min = Math.min(min, score);
            max = Math.max(max, score);
            dernier = score;

            XYChart.Data<String, Number> data = new XYChart.Data<>(date, score);
            data.setNode(creerPointAvecValeur(score));
            series.getData().add(data);
        }

        lineChartScore.getData().add(series);

        lblStatScoreDetail.setText(
                "Min: " + String.format("%.0f", min) + "/100   |   " +
                        "Max: " + String.format("%.0f", max) + "/100   |   " +
                        "Dernier: " + String.format("%.0f", dernier) + "/100"
        );

        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();

                if (node != null) {
                    node.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-color: transparent;");

                    Tooltip.install(node, new Tooltip(
                            "Date : " + data.getXValue() + "\nScore : " +
                                    String.format("%.0f", data.getYValue().doubleValue()) + "/100"
                    ));
                }
            }
        });
    }

    private VBox creerPointAvecValeur(double score) {
        VBox container = new VBox(2);
        container.setAlignment(Pos.TOP_CENTER);
        container.setMouseTransparent(false);
        container.getStyleClass().add("custom-point-container");
        container.setTranslateY(9);

        Label point = new Label();
        point.getStyleClass().add("custom-chart-point");
        point.setMinSize(10, 10);
        point.setPrefSize(10, 10);
        point.setMaxSize(10, 10);
        point.setMouseTransparent(true);

        Label valueLabel = new Label(String.format("%.0f", score));
        valueLabel.getStyleClass().add("chart-value-label");
        valueLabel.setMouseTransparent(true);

        container.getChildren().addAll(point, valueLabel);

        return container;
    }

    /*
        ===============================
        PARTIE 13 : GRAPHIQUE INDICATEURS
        ===============================
    */

    private void chargerGraphiqueIndicateurs(List<SuiviBienEtre> suivis) {
        if (barChartIndicateurs == null) {
            return;
        }

        barChartIndicateurs.getData().clear();

        if (suivis == null || suivis.isEmpty()) {
            lblStatIndicateursDetail.setText("Aucun suivi pour calculer les moyennes.");
            return;
        }

        double sommeSommeil = 0;
        double sommeEnergie = 0;
        double sommeStress = 0;
        double sommeAlimentation = 0;

        for (SuiviBienEtre suivi : suivis) {
            sommeSommeil += suivi.getQualiteSommeil();
            sommeEnergie += suivi.getNiveauEnergie();
            sommeStress += suivi.getNiveauStress();
            sommeAlimentation += suivi.getQualiteAlimentation();
        }

        int total = suivis.size();

        double moyenneSommeil = sommeSommeil / total;
        double moyenneEnergie = sommeEnergie / total;
        double moyenneStress = sommeStress / total;
        double moyenneAlimentation = sommeAlimentation / total;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Moyennes");

        series.getData().add(new XYChart.Data<>("Sommeil", moyenneSommeil));
        series.getData().add(new XYChart.Data<>("Énergie", moyenneEnergie));
        series.getData().add(new XYChart.Data<>("Stress", moyenneStress));
        series.getData().add(new XYChart.Data<>("Alimentation", moyenneAlimentation));

        barChartIndicateurs.getData().add(series);

        lblStatIndicateursDetail.setText(
                "Sommeil: " + String.format("%.1f", moyenneSommeil) + "/10   |   " +
                        "Énergie: " + String.format("%.1f", moyenneEnergie) + "/10   |   " +
                        "Stress: " + String.format("%.1f", moyenneStress) + "/10   |   " +
                        "Alimentation: " + String.format("%.1f", moyenneAlimentation) + "/10"
        );

        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();

                if (node == null) {
                    continue;
                }

                Tooltip.install(node, new Tooltip(
                        data.getXValue() + " : " +
                                String.format("%.1f", data.getYValue().doubleValue()) + "/10"
                ));

                if (node instanceof StackPane stackPane) {
                    Label valueLabel = new Label(String.format("%.1f", data.getYValue().doubleValue()));
                    valueLabel.getStyleClass().add("chart-value-label");
                    valueLabel.setMouseTransparent(true);

                    stackPane.setAlignment(Pos.TOP_CENTER);
                    stackPane.getChildren().add(valueLabel);

                    valueLabel.setTranslateY(-18);
                }
            }
        });
    }

    /*
        ===============================
        PARTIE 14 : GRAPHIQUE HUMEUR
        ===============================
    */

    private void chargerGraphiqueHumeurs(List<SuiviBienEtre> suivis) {
        if (pieChartHumeurs == null) {
            return;
        }

        pieChartHumeurs.getData().clear();

        if (suivis == null || suivis.isEmpty()) {
            lblStatHumeurDetail.setText("Aucun suivi pour analyser les humeurs.");
            return;
        }

        Map<String, Integer> humeurCount = new HashMap<>();

        for (SuiviBienEtre suivi : suivis) {
            String humeur = safe(suivi.getHumeur());
            humeurCount.put(humeur, humeurCount.getOrDefault(humeur, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : humeurCount.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
            pieChartHumeurs.getData().add(data);
        }

        String humeurDominante = humeurCount.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");

        lblStatHumeurDetail.setText(
                "Humeur dominante : " + humeurDominante + "   |   " +
                        "Types d’humeurs : " + humeurCount.size()
        );

        Platform.runLater(() -> {
            for (PieChart.Data data : pieChartHumeurs.getData()) {
                Node node = data.getNode();

                if (node != null) {
                    Tooltip.install(node, new Tooltip(
                            data.getName() + "\nOccurrences : " + String.format("%.0f", data.getPieValue())
                    ));
                }
            }
        });
    }

    /*
        ===============================
        PARTIE 15 : AFFICHAGE DES CONSEILS COACHING EN CARDS
        ===============================
    */

    private void afficherConseilsEnCards(VBox container, List<String> conseils, String prefixeTitre, String styleCard) {
        if (container == null) {
            return;
        }

        container.getChildren().clear();

        if (conseils == null || conseils.isEmpty()) {
            return;
        }

        for (int i = 0; i < conseils.size(); i++) {
            VBox card = new VBox(8);
            card.getStyleClass().add("conseil-card");
            card.getStyleClass().add(styleCard);

            Label titre = new Label(prefixeTitre + " " + (i + 1));
            titre.getStyleClass().add("conseil-card-title");

            Label texte = new Label(safe(conseils.get(i)));
            texte.setWrapText(true);
            texte.getStyleClass().add("conseil-card-text");

            card.getChildren().addAll(titre, texte);
            container.getChildren().add(card);
        }
    }

    /*
        ===============================
        PARTIE 16 : API SPOONACULAR - PROPOSER RECETTE
        ===============================
    */

    @FXML
    private void proposerRecetteSaine() {
        if (objectif == null) {
            lblRecetteMessage.setText("Aucun objectif sélectionné.");
            return;
        }

        if (!"ALIMENTATION".equalsIgnoreCase(safe(objectif.getType()))) {
            lblRecetteMessage.setText("Les recettes sont proposées uniquement pour les objectifs de type ALIMENTATION.");
            return;
        }

        try {
            SuiviBienEtreService suiviService = new SuiviBienEtreService();
            SuiviBienEtre dernierSuivi = suiviService.getDernierSuiviByObjectifId(objectif.getId());

            String query = choisirMotCleRecette(dernierSuivi);

            lblRecetteMessage.setText("Recherche de recettes adaptées : " + query);

            SpoonacularClient spoonacularClient = new SpoonacularClient();
            List<Recette> recettes = spoonacularClient.chercherRecettes(query);

            afficherRecettes(recettes, query);

        } catch (Exception e) {
            e.printStackTrace();
            lblRecetteMessage.setText("Impossible de charger les recettes. Vérifiez votre clé API ou votre connexion.");

            if (boxRecettes != null) {
                boxRecettes.getChildren().clear();
            }
        }
    }

    private String choisirMotCleRecette(SuiviBienEtre suivi) {
        if (suivi == null) {
            return "healthy";
        }

        if (suivi.getNiveauEnergie() < 5) {
            return "protein";
        }

        if (suivi.getNiveauStress() > 7) {
            return "salad";
        }

        if (suivi.getQualiteSommeil() < 5) {
            return "soup";
        }

        if (suivi.getQualiteAlimentation() < 5) {
            return "healthy";
        }

        return "chicken";
    }

    private void afficherRecettes(List<Recette> recettes, String query) {
        if (boxRecettes == null) {
            return;
        }

        boxRecettes.getChildren().clear();

        if (recettes == null || recettes.isEmpty()) {
            lblRecetteMessage.setText("Aucune recette trouvée pour : " + query);
            return;
        }

        lblRecetteMessage.setText(genererMessageProfilRecette(query));

        for (Recette recette : recettes) {
            VBox card = new VBox(8);
            card.getStyleClass().add("conseil-card");
            card.getStyleClass().add("objectif-card");

            Label titre = new Label("🍽 " + safe(recette.getTitre()));
            titre.getStyleClass().add("conseil-card-title");
            titre.setWrapText(true);

            String caloriesText = recette.getCalories() > 0
                    ? String.format("%.0f kcal", recette.getCalories())
                    : "Non disponible";

            Label infos = new Label(
                    "Temps : " + recette.getReadyInMinutes() + " min"
                            + "   |   Calories : " + caloriesText
            );
            infos.getStyleClass().add("conseil-card-text");
            infos.setWrapText(true);

            Label conseil = new Label(genererConseilRecette());
            conseil.getStyleClass().add("conseil-card-text");
            conseil.setWrapText(true);

            Hyperlink lien = new Hyperlink("Voir la recette");
            lien.setOnAction(event -> ouvrirLien(recette.getSourceUrl(), lblRecetteMessage));

            card.getChildren().addAll(titre, infos, conseil, lien);
            boxRecettes.getChildren().add(card);
        }
    }

    private String genererMessageProfilRecette(String query) {
        return switch (query) {
            case "protein" -> "Recettes riches en protéines proposées selon votre niveau d’énergie.";
            case "healthy" -> "Recettes saines proposées selon votre objectif alimentation.";
            case "salad" -> "Recettes simples et légères proposées selon votre niveau de stress.";
            case "soup" -> "Repas légers proposés selon votre qualité de sommeil.";
            case "chicken" -> "Recettes équilibrées proposées selon votre profil.";
            default -> "Recettes proposées selon votre profil santé.";
        };
    }

    private String genererConseilRecette() {
        return "Conseil : cette recette peut aider votre objectif alimentation en proposant une idée de repas plus équilibrée.";
    }

    /*
    ===============================
    PARTIE 17 : WORKOUT API - PROPOSER EXERCICES SPORT
    ===============================
*/

    @FXML
    private void proposerExercicesSport() {
        if (objectif == null) {
            lblExerciceMessage.setText("Aucun objectif sélectionné.");
            return;
        }

    /*
        L'API exercices est utilisée seulement pour les objectifs de type SPORT
    */
        if (!"SPORT".equalsIgnoreCase(safe(objectif.getType()))) {
            lblExerciceMessage.setText("Les exercices sont proposés uniquement pour les objectifs de type SPORT.");
            return;
        }

        try {
        /*
            Récupérer le dernier suivi lié à l'objectif
        */
            SuiviBienEtreService suiviService = new SuiviBienEtreService();
            SuiviBienEtre dernierSuivi = suiviService.getDernierSuiviByObjectifId(objectif.getId());

        /*
            Choisir automatiquement les critères selon le dernier suivi
        */
            String[] criteres = choisirCriteresWorkoutApi(dernierSuivi);
            String bodyPart = criteres[0];
            String equipment = criteres[1];

            lblExerciceMessage.setText("Recherche d’exercices adaptés : " + bodyPart + " - " + equipment);

        /*
            Appel de l'API Workout API
        */
            WorkoutApiExerciseClient workoutApiClient = new WorkoutApiExerciseClient();
            List<ExerciceSport> exercices = workoutApiClient.chercherExercices(bodyPart, equipment);

        /*
            Affichage dans JavaFX
        */
            afficherExercices(exercices, bodyPart, equipment);

        } catch (Exception e) {
            e.printStackTrace();

            lblExerciceMessage.setText("Erreur Workout API : " + e.getMessage());

            if (boxExercices != null) {
                boxExercices.getChildren().clear();
            }
        }
    }

    /*
        Choix des critères selon le dernier suivi bien-être.
        bodyPart = zone du corps
        equipment = équipement
    */
    private String[] choisirCriteresWorkoutApi(SuiviBienEtre suivi) {
        if (suivi == null) {
            return new String[]{"cardio", "body weight"};
        }

    /*
        Stress élevé : exercices doux
    */
        if (suivi.getNiveauStress() > 7) {
            return new String[]{"back", "body weight"};
        }

    /*
        Énergie faible : exercices simples
    */
        if (suivi.getNiveauEnergie() < 5) {
            return new String[]{"cardio", "body weight"};
        }

    /*
        Score faible : exercices faciles
    */
        if (suivi.getScore() < 50) {
            return new String[]{"cardio", "body weight"};
        }

    /*
        Énergie élevée : renforcement
    */
        if (suivi.getNiveauEnergie() >= 7) {
            return new String[]{"upper arms", "body weight"};
        }

    /*
        Cas normal
    */
        return new String[]{"cardio", "body weight"};
    }

    /*
        Afficher les exercices retournés par l'API sous forme de cartes professionnelles
    */
    private void afficherExercices(List<ExerciceSport> exercices, String bodyPart, String equipment) {
        if (boxExercices == null) {
            return;
        }

        boxExercices.getChildren().clear();

        if (exercices == null || exercices.isEmpty()) {
            lblExerciceMessage.setText("Aucun exercice trouvé pour : " + bodyPart + " - " + equipment);
            return;
        }

        lblExerciceMessage.setText(genererMessageProfilWorkoutApi(bodyPart));

        int limite = Math.min(exercices.size(), 3);

        for (int i = 0; i < limite; i++) {
            ExerciceSport exercice = exercices.get(i);

        /*
            Carte principale de l'exercice
        */
            VBox card = new VBox(14);
            card.getStyleClass().add("exercise-card");

        /*
            Titre de l'exercice
        */
            Label titre = new Label("🏋 " + safe(exercice.getName()));
            titre.getStyleClass().add("exercise-title");
            titre.setWrapText(true);

        /*
            Badges : Zone / Muscle / Équipement
        */
            HBox badgesBox = new HBox(10);
            badgesBox.getStyleClass().add("exercise-badges-box");

            Label badgeZone = creerBadge("Zone", traduireZoneCorps(exercice.getType()));
            Label badgeMuscle = creerBadge("Muscle", traduireMuscle(exercice.getMuscle()));
            Label badgeEquipement = creerBadge("Équipement", traduireEquipement(exercice.getEquipment()));

            badgesBox.getChildren().addAll(badgeZone, badgeMuscle, badgeEquipement);

        /*
            Bloc instructions
        */
            Label instructionsTitre = new Label("Instructions");
            instructionsTitre.getStyleClass().add("exercise-section-title");

            Label instructionsTexte = new Label(
                    limiterTexte(safe(exercice.getInstructions()), 260)
            );
            instructionsTexte.getStyleClass().add("exercise-section-text");
            instructionsTexte.setWrapText(true);

            VBox instructionsBox = new VBox(6, instructionsTitre, instructionsTexte);
            instructionsBox.getStyleClass().add("exercise-section-box");

        /*
            Bloc conseil personnalisé
        */
            Label conseilTitre = new Label("Conseil");
            conseilTitre.getStyleClass().add("exercise-section-title");

            Label conseilTexte = new Label(genererConseilWorkoutApi(bodyPart));
            conseilTexte.getStyleClass().add("exercise-advice-text");
            conseilTexte.setWrapText(true);

            VBox conseilBox = new VBox(6, conseilTitre, conseilTexte);
            conseilBox.getStyleClass().add("exercise-advice-box");

        /*
            Lien vers une démonstration si l'API retourne une image ou un gif
        */
            if (exercice.getGifUrl() != null && !exercice.getGifUrl().isBlank()) {
                Hyperlink lienGif = new Hyperlink("Voir la démonstration");
                lienGif.getStyleClass().add("exercise-link");
                lienGif.setOnAction(event -> ouvrirLien(exercice.getGifUrl(), lblExerciceMessage));

                card.getChildren().addAll(titre, badgesBox, instructionsBox, conseilBox, lienGif);
            } else {
                card.getChildren().addAll(titre, badgesBox, instructionsBox, conseilBox);
            }

            boxExercices.getChildren().add(card);
        }
    }

    /*
        Création d'un badge professionnel
    */
    private Label creerBadge(String label, String valeur) {
        Label badge = new Label(label + " : " + valeur);
        badge.getStyleClass().add("exercise-badge");
        badge.setWrapText(true);
        return badge;
    }

    /*
        Message affiché selon le profil sportif
    */
    private String genererMessageProfilWorkoutApi(String bodyPart) {
        if ("back".equalsIgnoreCase(bodyPart)) {
            return "Exercices doux proposés selon votre niveau de stress.";
        }

        if ("upper arms".equalsIgnoreCase(bodyPart)) {
            return "Exercices de renforcement proposés selon votre bon niveau d’énergie.";
        }

        if ("cardio".equalsIgnoreCase(bodyPart)) {
            return "Exercices simples proposés car votre niveau d’énergie est faible.";
        }

        return "Exercices proposés selon votre objectif sport et votre dernier suivi.";
    }

    /*
        Conseil personnalisé affiché dans chaque carte
    */
    private String genererConseilWorkoutApi(String bodyPart) {
        if ("back".equalsIgnoreCase(bodyPart)) {
            return "Cet exercice peut aider à relâcher les tensions et à réduire le stress.";
        }

        if ("upper arms".equalsIgnoreCase(bodyPart)) {
            return "Cet exercice peut aider à renforcer progressivement les bras.";
        }

        if ("cardio".equalsIgnoreCase(bodyPart)) {
            return "Cet exercice est adapté pour améliorer progressivement votre endurance.";
        }

        return "Cet exercice peut vous aider à progresser dans votre objectif sportif.";
    }

    /*
        Traduction des zones du corps retournées par l'API
    */
    private String traduireZoneCorps(String bodyPart) {
        if (bodyPart == null) {
            return "-";
        }

        return switch (bodyPart.toLowerCase()) {
            case "cardio" -> "Cardio";
            case "back" -> "Dos";
            case "chest" -> "Poitrine";
            case "upper arms" -> "Bras";
            case "lower arms" -> "Avant-bras";
            case "upper legs" -> "Jambes";
            case "lower legs" -> "Mollets";
            case "waist" -> "Abdominaux";
            case "shoulders" -> "Épaules";
            default -> bodyPart;
        };
    }

    /*
        Traduction des muscles retournés par l'API
    */
    private String traduireMuscle(String muscle) {
        if (muscle == null) {
            return "-";
        }

        return switch (muscle.toLowerCase()) {
            case "upper arms" -> "Bras";
            case "lower arms" -> "Avant-bras";
            case "upper legs" -> "Jambes";
            case "lower legs" -> "Mollets";
            case "back" -> "Dos";
            case "chest" -> "Poitrine";
            case "shoulders" -> "Épaules";
            case "waist" -> "Abdominaux";
            case "cardio" -> "Cardio";
            case "neck" -> "Cou";
            case "abdominals" -> "Abdominaux";
            case "biceps" -> "Biceps";
            case "triceps" -> "Triceps";
            case "quadriceps" -> "Quadriceps";
            case "hamstrings" -> "Ischio-jambiers";
            case "glutes" -> "Fessiers";
            case "calves" -> "Mollets";
            default -> muscle;
        };
    }

    /*
        Traduction de l'équipement retourné par l'API
    */
    private String traduireEquipement(String equipment) {
        if (equipment == null) {
            return "-";
        }

        return switch (equipment.toLowerCase()) {
            case "body weight" -> "Poids du corps";
            case "dumbbell" -> "Haltères";
            case "barbell" -> "Barre";
            case "cable" -> "Machine câble";
            case "assisted" -> "Assisté";
            case "band" -> "Élastique";
            case "none" -> "Aucun";
            default -> equipment;
        };
    }
    /*
        ===============================
        PARTIE 18 : OUTILS GENERAUX
        ===============================
    */

    private void ouvrirLien(String url, Label labelMessage) {
        try {
            if (url == null || url.isBlank()) {
                if (labelMessage != null) {
                    labelMessage.setText("Lien indisponible.");
                }
                return;
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                if (labelMessage != null) {
                    labelMessage.setText("Ouverture du lien non supportée sur ce système.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

            if (labelMessage != null) {
                labelMessage.setText("Impossible d’ouvrir le lien.");
            }
        }
    }

    private String limiterTexte(String texte, int max) {
        if (texte == null || texte.isBlank()) {
            return "-";
        }

        if (texte.length() <= max) {
            return texte;
        }

        return texte.substring(0, max) + "...";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
package piJava.Controllers.frontoffice.emploi;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import piJava.entities.Classe;
import piJava.entities.Matiere;
import piJava.entities.Salle;
import piJava.entities.Seance;
import piJava.entities.user;
import piJava.services.ClasseService;
import piJava.services.MatiereService;
import piJava.services.SalleService;
import piJava.services.SeanceService;
import piJava.utils.SessionManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class EmploiContentController implements Initializable {

    @FXML private VBox mainContent;
    @FXML private Label lblClasseBadge;
    @FXML private Label lblInfos;
    @FXML private Label statTotalSeances;
    @FXML private Label statMatieres;
    @FXML private Label statSalles;
    @FXML private Label statHeures;
    @FXML private HBox chargeSemaineContainer;
    @FXML private Label lblMaxSeances;
    @FXML private Label lblTotalHeuresStat;
    @FXML private BarChart<String, Number> barChartStats;
    @FXML private VBox calendarContainer;
    @FXML private Label lblSemaineRange;
    @FXML private Label lblSemaineBadge;
    @FXML private GridPane timetableGrid;

    private SeanceService seanceService;
    private MatiereService matiereService;
    private ClasseService classeService;
    private SalleService salleService;

    private List<Seance> seances = new ArrayList<>();
    private List<Seance> allClassSeances = new ArrayList<>();
    private LocalDate currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    private Map<Integer, String> matiereMap = new HashMap<>();
    private Map<Integer, String> salleMap = new HashMap<>();
    
    private final String[] j = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
    private final String[] jFull = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    private final String[] jCouleurs = {"#ef4444", "#f59e0b", "#10b981", "#3b82f6", "#8b5cf6", "#ec4899"}; // Couleurs screenshot

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        seanceService = new SeanceService();
        matiereService = new MatiereService();
        classeService = new ClasseService();
        salleService = new SalleService();

        loadMappings();
        loadDataForUser();
    }

    private void loadMappings() {
        try {
            for (Matiere m : matiereService.show()) matiereMap.put(m.getId(), m.getNom());
            for (Salle s : salleService.getAllSalles()) salleMap.put(s.getId(), s.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDataForUser() {
        try {
            List<Seance> toutes = seanceService.getAllSeances();
            user sessionUser = SessionManager.getInstance().getCurrentUser();
            
            // Mode test: si admin ou pas de classe_id on prend la première classe disponible
            Integer targetClassId = (sessionUser != null && sessionUser.getClasse_id() != null && sessionUser.getClasse_id() > 0) ? sessionUser.getClasse_id() : -1;
            
            if (targetClassId == -1 && !toutes.isEmpty()) {
                targetClassId = toutes.get(0).getClasseId(); // Fallback juste pour tester
            }

            final int cid = targetClassId;
            allClassSeances = toutes.stream().filter(s -> s.getClasseId() == cid).toList();

            // Set Header Badges
            Classe c = classeService.getAllClasses().stream().filter(cl -> cl.getId() == cid).findFirst().orElse(null);
            if (c != null) {
                lblClasseBadge.setText(c.getNom().toUpperCase());
            } else {
                lblClasseBadge.setText("SANS CLASSE");
            }
            
            refreshViewForCurrentWeek();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handlePrevWeek() {
        currentWeekStart = currentWeekStart.minusDays(7);
        refreshViewForCurrentWeek();
    }
    
    @FXML private void handleNextWeek() {
        currentWeekStart = currentWeekStart.plusDays(7);
        refreshViewForCurrentWeek();
    }
    
    @FXML private void handleToday() {
        currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        refreshViewForCurrentWeek();
    }

    private void refreshViewForCurrentWeek() {
        LocalDate currentWeekEnd = currentWeekStart.plusDays(6); // Sunday
        
        seances = allClassSeances.stream().filter(s -> {
            if (s.getHeureDebut() == null) return false;
            LocalDate sd = s.getHeureDebut().toLocalDateTime().toLocalDate();
            return !sd.isBefore(currentWeekStart) && !sd.isAfter(currentWeekEnd);
        }).toList();

        lblInfos.setText(seances.size() + " séances programmées (Semaine ciblée)");

        LocalDate todayMon = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (currentWeekStart.isEqual(todayMon)) {
            if(lblSemaineBadge != null) {
                lblSemaineBadge.setText("Cette semaine");
                lblSemaineBadge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 12px;");
            }
        } else if (currentWeekStart.isEqual(todayMon.plusDays(7))) {
            if(lblSemaineBadge != null) {
                lblSemaineBadge.setText("Semaine prochaine");
                lblSemaineBadge.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 12px;");
            }
        } else if (currentWeekStart.isEqual(todayMon.minusDays(7))) {
            if(lblSemaineBadge != null) {
                lblSemaineBadge.setText("Semaine passée");
                lblSemaineBadge.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 12px;");
            }
        } else {
            if(lblSemaineBadge != null) {
                lblSemaineBadge.setText("Autre semaine");
                lblSemaineBadge.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 12px;");
            }
        }

        setupDateRange();
        drawTimetableGrid();
        updateKPIs();
        updateChargeJour();
        updateBarChart();
    }

    private void updateKPIs() {
        statTotalSeances.setText(String.valueOf(seances.size()));
        long matCount = seances.stream().map(Seance::getMatiereId).distinct().count();
        statMatieres.setText(String.valueOf(matCount));
        long salCount = seances.stream().map(Seance::getSalleId).distinct().count();
        statSalles.setText(String.valueOf(salCount));

        double totalHours = 0;
        for (Seance s : seances) {
             if(s.getHeureDebut()!=null && s.getHeureFin()!=null){
                 long diffMs = s.getHeureFin().getTime() - s.getHeureDebut().getTime();
                 totalHours += (diffMs / (1000.0 * 60 * 60));
             }
        }
        statHeures.setText((totalHours == (long) totalHours ? String.format("%d", (long)totalHours) : String.format("%.1f", totalHours)));
    }

    private void updateChargeJour() {
        chargeSemaineContainer.getChildren().clear();
        int maxSeances = 0;
        double totalHoursWeek = 0;

        for (int i = 0; i < jFull.length; i++) {
             String jourName = jFull[i];
             List<Seance> sj = seances.stream().filter(s -> s.getJour().equalsIgnoreCase(jourName)).toList();
             
             int count = sj.size();
             if (count > maxSeances) maxSeances = count;

             double hours = 0;
             for (Seance s : sj) {
                 if(s.getHeureDebut()!=null && s.getHeureFin()!=null){
                     hours += (s.getHeureFin().getTime() - s.getHeureDebut().getTime()) / (1000.0 * 60 * 60);
                 }
             }
             totalHoursWeek += hours;

             VBox box = new VBox(5);
             box.getStyleClass().add("charge-day-box");
             if (count > 0) box.getStyleClass().add("active");
             else box.getStyleClass().add("empty");

             Label l1 = new Label(j[i]); l1.getStyleClass().add("charge-day-label");
             Label l2 = new Label(String.valueOf(count)); l2.getStyleClass().add("charge-day-val");
             Label l3 = new Label(hours > 0 ? (hours == (long)hours ? (long)hours+"h" : String.format("%.1fh", hours)) : "0h");
             l3.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");

             box.getChildren().addAll(l1, l2, l3);
             chargeSemaineContainer.getChildren().add(box);
        }

        lblMaxSeances.setText(String.valueOf(maxSeances));
        lblTotalHeuresStat.setText(totalHoursWeek == (long)totalHoursWeek ? (long)totalHoursWeek+"h" : String.format("%.1fh", totalHoursWeek));
    }

    private void updateBarChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < jFull.length; i++) {
             final String fullDayName = jFull[i];
             final String shortDayName = j[i];
             int count = (int) seances.stream()
                     .filter(s -> s.getJour().equalsIgnoreCase(fullDayName))
                     .count();
             XYChart.Data<String, Number> data = new XYChart.Data<>(shortDayName, count);
             series.getData().add(data);
        }
        barChartStats.getData().clear();
        barChartStats.getData().add(series);
        
        // Appliquer les couleurs
        int index = 0;
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: " + jCouleurs[index] + ";");
            index++;
        }
    }

    private void setupDateRange() {
        LocalDate saturday = currentWeekStart.plusDays(5);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblSemaineRange.setText("Semaine du " + currentWeekStart.format(dtf) + " au " + saturday.format(dtf));
    }

    private void drawTimetableGrid() {
        timetableGrid.getChildren().clear();
        timetableGrid.getColumnConstraints().clear();
        timetableGrid.getRowConstraints().clear();

        // On définit 7 colonnes : Heure + 6 Jours
        ColumnConstraints colHeure = new ColumnConstraints(60); // Petite colonne Heure
        timetableGrid.getColumnConstraints().add(colHeure);
        for(int i = 0; i < 6; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setMinWidth(100); 
            timetableGrid.getColumnConstraints().add(cc);
        }

        // On définit les lignes (0 = header, 1 à 21 = de 8h à 18h par pas de 30min)
        RowConstraints headerRow = new RowConstraints(40);
        timetableGrid.getRowConstraints().add(headerRow);
        for(int i = 1; i <= 20; i++) {
            RowConstraints rc = new RowConstraints(30); // Chaque bloc de 30min fait 30px de haut
            timetableGrid.getRowConstraints().add(rc);
        }

        // Dessiner le Header
        Label lblH = new Label("Heure"); lblH.setStyle("-fx-text-fill: #94a3b8;");
        timetableGrid.add(lblH, 0, 0);
        GridPane.setHalignment(lblH, HPos.CENTER);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM");

        for(int i = 0; i < 6; i++) {
            VBox headerCell = new VBox(2);
            headerCell.setAlignment(Pos.CENTER);
            headerCell.getStyleClass().add("grid-header-cell");
            headerCell.setStyle("-fx-border-color: " + jCouleurs[i] + " transparent #e2e8f0 transparent; -fx-border-width: 3 0 1 0;");
            
            Label lJour = new Label(jFull[i]); 
            lJour.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
            Label lDate = new Label(currentWeekStart.plusDays(i).format(df)); 
            lDate.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
            
            headerCell.getChildren().addAll(lJour, lDate);
            timetableGrid.add(headerCell, i + 1, 0);
        }

        // Dessiner le background quadrillage (lignes grises fines)
        for(int r = 1; r <= 20; r++) {
            // Heure label (seulement aux heures pleines)
            if(r % 2 != 0) {
                int h = 8 + (r/2);
                Label hLbl = new Label(String.format("%02d:00", h));
                hLbl.getStyleClass().add("hour-label");
                timetableGrid.add(hLbl, 0, r);
                GridPane.setValignment(hLbl, VPos.TOP); // Aligner au top de la ligne
                GridPane.setHalignment(hLbl, HPos.CENTER);
            }
            // Cellules de fond
            for(int c = 1; c <= 6; c++) {
                Pane p = new Pane();
                p.getStyleClass().add("grid-cell");
                timetableGrid.add(p, c, r);
            }
        }

        // Placer les Séances
        for (Seance s : seances) {
            if(s.getHeureDebut() != null && s.getHeureFin() != null) {
                int col = 0;
                String jourC = s.getJour().toUpperCase();
                if(jourC.contains("LUNDI")) col = 1;
                else if(jourC.contains("MARDI")) col = 2;
                else if(jourC.contains("MERCREDI")) col = 3;
                else if(jourC.contains("JEUDI")) col = 4;
                else if(jourC.contains("VENDREDI")) col = 5;
                else if(jourC.contains("SAMEDI")) col = 6;

                if (col > 0) {
                    LocalDateTime start = s.getHeureDebut().toLocalDateTime();
                    LocalDateTime end = s.getHeureFin().toLocalDateTime();
                    
                    int startH = start.getHour();
                    int startM = start.getMinute();
                    int endH = end.getHour();
                    int endM = end.getMinute();

                    int rowStart = (startH - 8) * 2 + (startM == 30 ? 2 : 1) + (startM == 15 ? 1 : 0); // approx
                    int rowEnd = (endH - 8) * 2 + (endM == 30 ? 2 : 1) + (endM >= 45 ? 1 : 0);
                    int span = rowEnd - rowStart;
                    if(span <= 0) span = 1;

                    String color = jCouleurs[col-1]; // Couleur du jour pour le bloc

                    VBox bloc = new VBox(2);
                    bloc.getStyleClass().add("seance-block");
                    
                    // Couleur différente pour les séances de révision
                    boolean isRevision = "Révision".equalsIgnoreCase(s.getTypeSeance());
                    if (isRevision) {
                        bloc.setStyle("-fx-background-color: #8b5cf6;"); // Violet pour les révisions
                    } else {
                        bloc.setStyle("-fx-background-color: " + color + ";");
                    }
                    
                    // Titre : "Séance de révision" pour les révisions, sinon le nom de la matière
                    String titleText;
                    if (isRevision) {
                        titleText = "Séance de révision";
                    } else {
                        titleText = matiereMap.getOrDefault(s.getMatiereId(), "Inconnu");
                    }
                    Label mName = new Label(titleText);
                    mName.getStyleClass().add("seance-title");
                    
                    String strTime = String.format("%02d:%02d - %02d:%02d", startH, startM, endH, endM);
                    Label lTime = new Label("🕒 " + strTime);
                    lTime.getStyleClass().add("seance-time");
                    
                    Label sNam = new Label("🏢 " + salleMap.getOrDefault(s.getSalleId(), "S.Inc"));
                    sNam.getStyleClass().add("seance-time");

                    bloc.getChildren().addAll(mName, lTime, sNam);
                    
                    // Marges internes pour que ça ne touche pas les lignes
                    GridPane.setMargin(bloc, new Insets(2, 4, 2, 4));

                    timetableGrid.add(bloc, col, rowStart, 1, span);
                }
            }
        }
    }

    @FXML
    private void exportToPdf() {
        try {
            // Prendre capture uniquement du calendrier
            WritableImage snapshot = calendarContainer.snapshot(new SnapshotParameters(), null);
            BufferedImage bImage = SwingFXUtils.fromFXImage(snapshot, null);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bImage, "png", baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();

            Image pdfImg = Image.getInstance(imageInByte);

            // Ajustement dimensions (Mode Paysage si l'image est large)
            Document doc = new Document(new com.itextpdf.text.Rectangle(pdfImg.getWidth() + 40, pdfImg.getHeight() + 40));
            
            String userHome = System.getProperty("user.home");
            String savePath = userHome + File.separator + "Downloads" + File.separator + "Emploi_Du_Temps.pdf";
            
            PdfWriter.getInstance(doc, new FileOutputStream(savePath));
            doc.open();
            
            pdfImg.setAbsolutePosition(20, 20); // 20px de marge
            doc.add(pdfImg);
            doc.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("PDF généré avec succès !");
            alert.setContentText("Le fichier a été enregistré dans : \n" + savePath);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Erreur");
            a.setHeaderText("La génération PDF a échoué.");
            a.setContentText(e.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void handleScanQR(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/frontoffice/emploi/ScanQRCode.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Scanner QR Code de Présence");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Erreur");
            a.setHeaderText(null);
            a.setContentText("Impossible d'ouvrir le scanner QR.");
            a.showAndWait();
        }
    }
}

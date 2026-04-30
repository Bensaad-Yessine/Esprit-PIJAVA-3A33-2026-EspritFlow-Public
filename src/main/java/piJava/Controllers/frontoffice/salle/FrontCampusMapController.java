package piJava.Controllers.frontoffice.salle;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import piJava.entities.Salle;
import piJava.entities.Seance;
import piJava.entities.user;
import piJava.services.SalleService;
import piJava.services.SeanceService;
import piJava.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FrontCampusMapController implements Initializable {

    @FXML private WebView webView;
    @FXML private VBox sidePanel;
    @FXML private VBox roomsContainer;
    @FXML private Label lblBlockTitle;
    @FXML private Label lblActionSuccess;

    private WebEngine webEngine;
    private SalleService salleService = new SalleService();
    private SeanceService seanceService = new SeanceService();

    // Standard Esprit time slots
    private final String[][] SLOTS = {
        {"09:00", "10:30"},
        {"10:45", "12:15"},
        {"13:30", "15:00"},
        {"15:15", "16:45"}
    };

    // STRONG REFERENCE required to prevent Garbage Collection of the JavaConnector
    private JavaConnector myJavaConnector = new JavaConnector();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sidePanel.setVisible(false);
        sidePanel.setManaged(false);
        lblActionSuccess.setVisible(false);

        webEngine = webView.getEngine();
        
        // Load the HTML file
        URL mapUrl = getClass().getResource("/frontoffice/salle/map.html");
        if (mapUrl != null) {
            webEngine.load(mapUrl.toExternalForm());
        } else {
            System.err.println("Map HTML not found.");
        }

        // Expose Java object to JavaScript once the page is loaded
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", myJavaConnector);
            }
        });
    }

    public class JavaConnector {
        public void onBlockClicked(String blockName) {
            Platform.runLater(() -> handleBlockSelection(blockName));
        }
    }

    private void handleBlockSelection(String blockName) {
        lblBlockTitle.setText(blockName);
        sidePanel.setVisible(true);
        sidePanel.setManaged(true);
        lblActionSuccess.setVisible(false);
        
        // Parse block letters from the name (e.g., "Bloc A / B / C" -> ["A", "B", "C"])
        String[] targets = blockName.replace("Bloc", "").replace(" ", "").split("/");
        
        try {
            List<Salle> allSalles = salleService.getAllSalles();
            List<Salle> matchingSalles = new ArrayList<>();
            
            for (Salle s : allSalles) {
                for (String t : targets) {
                    if (s.getBlock().equalsIgnoreCase(t)) {
                        matchingSalles.add(s);
                        break;
                    }
                }
            }
            
            displayRooms(matchingSalles);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayRooms(List<Salle> salles) {
        roomsContainer.getChildren().clear();

        if (salles.isEmpty()) {
            roomsContainer.getChildren().add(new Label("Aucune salle trouvée dans ce bloc."));
            return;
        }

        LocalDate today = LocalDate.now();
        List<LocalDate> weekDays = new ArrayList<>();
        // Give 6 days starting from today (or Monday to Saturday)
        for(int i=0; i<6; i++) {
            weekDays.add(today.plusDays(i));
        }

        for (Salle salle : salles) {
            VBox salleBox = new VBox(10);
            salleBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            
            Label lSalle = new Label("Salle " + salle.getBlock() + salle.getNumber());
            lSalle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            
            Label lCap = new Label("Capacité: " + salle.getCapacite() + " | Étage: " + salle.getEtage());
            lCap.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

            salleBox.getChildren().addAll(lSalle, lCap);

            // Fetch seances for this room
            List<Seance> seancesForSalle = new ArrayList<>();
            try {
                // Assuming we get all and filter (for simplicity/prototype)
                seancesForSalle = seanceService.getAllSeances().stream().filter(s -> s.getSalleId() == salle.getId()).toList();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            for (LocalDate date : weekDays) {
                if (date.getDayOfWeek() == DayOfWeek.SUNDAY) continue; // Skip sunday
                
                Label lDate = new Label(date.format(DateTimeFormatter.ofPattern("EEEE dd/MM")));
                lDate.setStyle("-fx-font-weight: bold; -fx-text-fill: #059669; -fx-padding: 10 0 5 0;");
                salleBox.getChildren().add(lDate);
                
                for (String[] slot : SLOTS) {
                    LocalDateTime startDT = LocalDateTime.of(date, LocalTime.parse(slot[0]));
                    LocalDateTime endDT = LocalDateTime.of(date, LocalTime.parse(slot[1]));
                    
                    if (startDT.isBefore(LocalDateTime.now())) continue; // Passé
                    
                    boolean isFree = true;
                    for (Seance s : seancesForSalle) {
                        if (s.getHeureDebut() != null && s.getHeureFin() != null) {
                            LocalDateTime sStart = s.getHeureDebut().toLocalDateTime();
                            LocalDateTime sEnd = s.getHeureFin().toLocalDateTime();
                            
                            // Check overlap
                            if (startDT.isBefore(sEnd) && endDT.isAfter(sStart)) {
                                isFree = false;
                                break;
                            }
                        }
                    }
                    
                    HBox slotBox = new HBox(10);
                    slotBox.setAlignment(Pos.CENTER_LEFT);
                    slotBox.setStyle("-fx-background-color: " + (isFree ? "#ecfdf5" : "#fee2e2") + "; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: " + (isFree ? "#a7f3d0" : "#fca5a5") + "; -fx-border-radius: 8;");
                    
                    VBox timeInfos = new VBox(2);
                    Label lTime = new Label(slot[0] + " - " + slot[1]);
                    lTime.setStyle("-fx-font-weight: bold;");
                    Label lStat = new Label(isFree ? "✔ Disponible" : "✖ Occupé");
                    lStat.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (isFree ? "#059669" : "#ef4444") + "; -fx-font-weight: bold;");
                    
                    timeInfos.getChildren().addAll(lTime, lStat);
                    
                    slotBox.getChildren().add(timeInfos);
                    
                    // Add spacer
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    slotBox.getChildren().add(spacer);
                    
                    if (isFree) {
                        Button btnReserve = new Button("Réserver");
                        btnReserve.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
                        btnReserve.setOnAction(e -> bookSlot(salle, startDT, endDT));
                        slotBox.getChildren().add(btnReserve);
                    } else {
                        Label lblOcc = new Label("Indisponible");
                        lblOcc.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-font-weight: bold;");
                        slotBox.getChildren().add(lblOcc);
                    }
                    
                    salleBox.getChildren().add(slotBox);
                }
            }
            
            roomsContainer.getChildren().add(salleBox);
        }
    }

    private void bookSlot(Salle salle, LocalDateTime start, LocalDateTime end) {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert("Erreur", "Vous devez être connecté pour réserver.");
            return;
        }

        try {
            Seance nouvelleRevision = new Seance();
            // Defaults for a revision
            String[] frenchDays = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
            int dayIdx = start.getDayOfWeek().getValue() - 1;
            nouvelleRevision.setJour(frenchDays[dayIdx]);
            
            nouvelleRevision.setTypeSeance("Révision"); // Type: Révision (pas "révisé")
            nouvelleRevision.setMode("Présentiel");
            nouvelleRevision.setHeureDebut(Timestamp.valueOf(start));
            nouvelleRevision.setHeureFin(Timestamp.valueOf(end));
            nouvelleRevision.setSalleId(salle.getId());
            nouvelleRevision.setClasseId(currentUser.getClasse_id() != null ? currentUser.getClasse_id() : 1);
            // using a dummy matiereId = 1 or the first available, or creating one "Revision"
            nouvelleRevision.setMatiereId(1); 
            
            seanceService.add(nouvelleRevision);
            
            lblActionSuccess.setText("✔ Séance de révision ajoutée — " + salle.getBlock() + salle.getNumber() + ", " + start.format(DateTimeFormatter.ofPattern("EEEE HH:mm")));
            lblActionSuccess.setVisible(true);
            
            // Refresh
            handleBlockSelection(lblBlockTitle.getText());
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de la réservation.");
        }
    }

    @FXML
    private void hideSidePanel() {
        sidePanel.setVisible(false);
        sidePanel.setManaged(false);
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

package piJava.Controllers.backoffice.Seance;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import piJava.entities.Attendance;
import piJava.entities.Seance;
import piJava.entities.user;
import piJava.services.AttendanceService;
import piJava.services.ClasseService;
import piJava.services.MatiereService;
import piJava.services.SalleService;
import piJava.services.UserServices;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class SeanceDashboardController {

    @FXML private Label lblTitle;
    @FXML private Label lblDetails;
    @FXML private Label lblTotalEtudiants;
    @FXML private Label lblTotalPresents;
    @FXML private Label lblTotalRetards;
    @FXML private Label lblTotalAbsents;
    @FXML private ProgressBar progressPresence;
    @FXML private TableView<StudentAttendance> tablePresences;
    @FXML private TableColumn<StudentAttendance, String> colEtudiant;
    @FXML private TableColumn<StudentAttendance, String> colStatut;
    @FXML private TableColumn<StudentAttendance, String> colScanneA;
    @FXML private Button btnRetour;

    private Seance currentSeance;
    private AttendanceService attendanceService = new AttendanceService();
    private UserServices userService = new UserServices();
    private MatiereService matiereService = new MatiereService();
    private ClasseService classeService = new ClasseService();
    private SalleService salleService = new SalleService();

    private ObservableList<StudentAttendance> attendanceList = FXCollections.observableArrayList();

    public void initData(Seance seance) {
        this.currentSeance = seance;
        setupUI();
        loadData();
    }

    private void setupUI() {
        try {
            String matiere = matiereService.show().stream().filter(m -> m.getId() == currentSeance.getMatiereId()).map(m -> m.getNom()).findFirst().orElse("Matière");
            String classe = classeService.getAllClasses().stream().filter(c -> c.getId() == currentSeance.getClasseId()).map(c -> c.getNom()).findFirst().orElse("Classe");
            String salle = salleService.getAllSalles().stream().filter(s -> s.getId() == currentSeance.getSalleId()).map(s -> s.getName()).findFirst().orElse("Salle");
            
            lblTitle.setText("👥 Présences - " + matiere);
            
            String dateStr = "";
            String startStr = "";
            String endStr = "";
            if (currentSeance.getHeureDebut() != null) {
                dateStr = currentSeance.getHeureDebut().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                startStr = currentSeance.getHeureDebut().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                endStr = currentSeance.getHeureFin().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            }
            
            lblDetails.setText(classe + " | Salle: " + salle + " | " + dateStr + " " + startStr + " → " + endStr);
            
            colEtudiant.setCellValueFactory(new PropertyValueFactory<>("studentName"));
            colStatut.setCellValueFactory(new PropertyValueFactory<>("status"));
            
            colStatut.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        Label lbl = new Label(item);
                        lbl.setStyle("-fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");
                        if (item.equals("PRESENT")) {
                            lbl.setStyle(lbl.getStyle() + "-fx-background-color: #d1fae5; -fx-text-fill: #059669;");
                        } else if (item.equals("RETARD")) {
                            lbl.setStyle(lbl.getStyle() + "-fx-background-color: #fef3c7; -fx-text-fill: #d97706;");
                        } else {
                            lbl.setStyle(lbl.getStyle() + "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");
                        }
                        setGraphic(lbl);
                    }
                }
            });
            
            colScanneA.setCellValueFactory(new PropertyValueFactory<>("scannedAtStr"));
            
            tablePresences.setItems(attendanceList);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            attendanceList.clear();
            
            // Get all students of the class
            List<user> students = userService.getUsersByClasse(currentSeance.getClasseId());
            
            // Get all attendances for the seance
            List<Attendance> attendances = attendanceService.getBySeanceId(currentSeance.getId());
            
            int presentCount = 0;
            int retardCount = 0;
            int absentCount = 0;
            
            for (Attendance att : attendances) {
                user student = students.stream().filter(s -> s.getId() == att.getUserId()).findFirst().orElse(null);
                if (student == null) continue;
                
                String status = att.getStatus();
                String scannedAtStr = "-";
                
                if (att.getScannedAt() != null) {
                    scannedAtStr = att.getScannedAt().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                }
                
                if (status.equals("PRESENT")) presentCount++;
                else if (status.equals("RETARD")) retardCount++;
                else absentCount++;
                
                attendanceList.add(new StudentAttendance(
                    student.getNom() + " " + student.getPrenom(),
                    status,
                    scannedAtStr
                ));
            }
            
            lblTotalEtudiants.setText(String.valueOf(attendanceList.size()));
            lblTotalPresents.setText(String.valueOf(presentCount));
            lblTotalRetards.setText(String.valueOf(retardCount));
            lblTotalAbsents.setText(String.valueOf(absentCount));
            
            if (students.size() > 0) {
                double rate = (double) (presentCount + retardCount) / students.size();
                progressPresence.setProgress(rate);
                
                if (rate < 0.5) progressPresence.setStyle("-fx-accent: #ef4444;");
                else if (rate < 0.8) progressPresence.setStyle("-fx-accent: #f59e0b;");
                else progressPresence.setStyle("-fx-accent: #10b981;");
            } else {
                progressPresence.setProgress(0);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetour() {
        Stage stage = (Stage) btnRetour.getScene().getWindow();
        stage.close();
    }

    public static class StudentAttendance {
        private String studentName;
        private String status;
        private String scannedAtStr;

        public StudentAttendance(String studentName, String status, String scannedAtStr) {
            this.studentName = studentName;
            this.status = status;
            this.scannedAtStr = scannedAtStr;
        }

        public String getStudentName() { return studentName; }
        public String getStatus() { return status; }
        public String getScannedAtStr() { return scannedAtStr; }
    }
}

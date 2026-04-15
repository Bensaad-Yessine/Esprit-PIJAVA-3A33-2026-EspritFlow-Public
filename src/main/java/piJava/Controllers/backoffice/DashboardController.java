package piJava.Controllers.backoffice;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.util.ResourceBundle;

import piJava.entities.user;
import piJava.entities.Classe;
import piJava.services.UserServices;
import piJava.services.ClasseService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;

public class DashboardController implements Initializable {

    // Stats
    @FXML private Label totalStudentsLbl;
    @FXML private Label activeClassesLbl;
    @FXML private Label teachersLbl;
    @FXML private Label bannedUsersLbl;

    // Chart
    @FXML private BarChart<String, Number> enrollmentChart;

    // Table
    @FXML private TableView<user> studentsTable;
    @FXML private TableColumn<user, String> nameCol;
    @FXML private TableColumn<user, String> gradeCol;
    @FXML private TableColumn<user, String> courseCol;
    @FXML private TableColumn<user, String> attendanceCol;
    @FXML private TableColumn<user, String> statusCol;

    // Only buttons that exist in dashboard-content.fxml
    @FXML private Button newStudentBtn;
    @FXML private Button viewAllBtn;
    
    private final UserServices userServices = new UserServices();
    private final ClasseService classeService = new ClasseService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupHoverEffects();
        loadRealData();
    }

    private void loadRealData() {
        try {
            List<user> users = userServices.show();
            
            // Stats
            long etudiants = users.stream().filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_USER") && !u.getRoles().contains("ROLE_ADMIN")).count();
            long profs = users.stream().filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_PROF")).count();
            long bannis = users.stream().filter(u -> u.getIs_banned() == 1).count();
            long classes = classeService.show().size();
            
            if (totalStudentsLbl != null) totalStudentsLbl.setText(String.valueOf(etudiants));
            if (teachersLbl != null) teachersLbl.setText(String.valueOf(profs));
            if (bannedUsersLbl != null) bannedUsersLbl.setText(String.valueOf(bannis));
            if (activeClassesLbl != null) activeClassesLbl.setText(String.valueOf(classes));
            
            // Setup Table
            setupTable(users);
            
            // Setup Chart
            setupEnrollmentChart(users);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupTable(List<user> users) {
        if (studentsTable == null) return;
        
        nameCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getId() + " - " + d.getValue().getPrenom() + " " + d.getValue().getNom()));
        gradeCol.setCellValueFactory(d -> {
            String r = d.getValue().getRoles();
            if (r == null) return new SimpleStringProperty("Étudiant");
            if (r.contains("ROLE_ADMIN")) return new SimpleStringProperty("Admin");
            if (r.contains("ROLE_PROF")) return new SimpleStringProperty("Professeur");
            return new SimpleStringProperty("Étudiant");
        });
        courseCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        attendanceCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNum_tel()));
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIs_banned() == 1 ? "Banni 🚫" : (d.getValue().getIs_verified() == 1 ? "Vérifié ✅" : "Non vérifié ❌")));
        
        // Get 10 recent
        List<user> recent = users.stream()
                .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
                .limit(10)
                .collect(Collectors.toList());
        studentsTable.setItems(FXCollections.observableArrayList(recent));
    }

    private void setupEnrollmentChart(List<user> users) {
        if (enrollmentChart == null) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscriptions Récentes");

        // Group by creation month from created_at
        Map<String, Long> byMonth = users.stream()
                .filter(u -> u.getCreated_at() != null && u.getCreated_at().length() >= 7)
                .collect(Collectors.groupingBy(
                        u -> u.getCreated_at().substring(0, 7), // "YYYY-MM"
                        Collectors.counting()
                ));
        
        byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));

        enrollmentChart.getData().clear();
        enrollmentChart.getData().add(series);
        enrollmentChart.setAnimated(true);
        enrollmentChart.setLegendVisible(false);
    }

    private void setupHoverEffects() {
        if (newStudentBtn != null) {
            newStudentBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, e ->
                newStudentBtn.setStyle(
                    "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 13px; " +
                    "-fx-font-weight: 600; -fx-background-radius: 8; -fx-cursor: hand; " +
                    "-fx-padding: 10 20 10 20;"));
            newStudentBtn.addEventHandler(MouseEvent.MOUSE_EXITED, e ->
                newStudentBtn.setStyle(
                    "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 13px; " +
                    "-fx-font-weight: 600; -fx-background-radius: 8; -fx-cursor: hand; " +
                    "-fx-padding: 10 20 10 20;"));
        }

        if (viewAllBtn != null) {
            viewAllBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, e ->
                viewAllBtn.setStyle(
                    "-fx-background-color: #2c1a1a; -fx-text-fill: #e74c3c; -fx-font-size: 13px; " +
                    "-fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 8 16 8 16; " +
                    "-fx-background-radius: 8;"));
            viewAllBtn.addEventHandler(MouseEvent.MOUSE_EXITED, e ->
                viewAllBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 13px; " +
                    "-fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 8 16 8 16; " +
                    "-fx-background-radius: 8;"));
        }
    }
}

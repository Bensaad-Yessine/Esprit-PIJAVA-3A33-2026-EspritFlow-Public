package piJava.Controllers.backoffice;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // Chart
    @FXML private BarChart<String, Number> enrollmentChart;

    // Table
    @FXML private TableView<?> studentsTable;
    @FXML private TableColumn<?, ?> nameCol;
    @FXML private TableColumn<?, ?> gradeCol;
    @FXML private TableColumn<?, ?> courseCol;
    @FXML private TableColumn<?, ?> attendanceCol;
    @FXML private TableColumn<?, ?> statusCol;

    // Only buttons that exist in dashboard-content.fxml
    @FXML private Button newStudentBtn;
    @FXML private Button viewAllBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEnrollmentChart();
        setupHoverEffects();
    }

    private void setupEnrollmentChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscriptions 2024");

        series.getData().add(new XYChart.Data<>("Sept", 65));
        series.getData().add(new XYChart.Data<>("Oct",  78));
        series.getData().add(new XYChart.Data<>("Nov",  82));
        series.getData().add(new XYChart.Data<>("Déc",  70));
        series.getData().add(new XYChart.Data<>("Jan",  85));
        series.getData().add(new XYChart.Data<>("Fév",  92));
        series.getData().add(new XYChart.Data<>("Mar",  88));
        series.getData().add(new XYChart.Data<>("Avr",  94));

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

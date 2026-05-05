package piJava.Controllers.backoffice.Classe;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import piJava.entities.Classe;
import piJava.entities.WorkloadStats;
import piJava.services.ClasseService;

import java.net.URL;
import java.sql.SQLException;

public class WorkloadDetailsController {

    @FXML private Label lblClasseName;
    @FXML private Label lblChargeBrute;
    @FXML private Label lblChargePonderee;
    @FXML private Label lblIndicePression;
    @FXML private Label lblStatus;
    @FXML private Label lblSuggestion;
    @FXML private Circle statusCircle;
    @FXML private Label statusIconLabel;

    private Classe currentClasse;
    private StackPane contentArea;
    private final ClasseService classeService = new ClasseService();

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void initData(Classe classe) {
        this.currentClasse = classe;
        lblClasseName.setText(classe.getNom());
        loadStats();
    }

    private void loadStats() {
        try {
            WorkloadStats stats = classeService.getWorkloadStats(currentClasse.getId());
            
            lblChargeBrute.setText(String.format("%.1fh", stats.getChargeBruteTotale()));
            lblChargePonderee.setText(String.format("%.1f", stats.getChargePonderee()));
            lblIndicePression.setText(String.format("%.1f", stats.getIndicePression()));
            lblSuggestion.setText(stats.getSuggestion());
            
            switch (stats.getAlerteNiveau()) {
                case "VERT":
                    statusCircle.setFill(Color.web("#27ae6015"));
                    statusCircle.setStroke(Color.web("#27ae60"));
                    statusIconLabel.setText("✅");
                    lblStatus.setText("ÉQUILIBRÉE");
                    lblStatus.setTextFill(Color.web("#27ae60"));
                    break;
                case "ORANGE":
                    statusCircle.setFill(Color.web("#f39c1215"));
                    statusCircle.setStroke(Color.web("#f39c12"));
                    statusIconLabel.setText("⚠");
                    lblStatus.setText("À SURVEILLER");
                    lblStatus.setTextFill(Color.web("#f39c12"));
                    break;
                case "ROUGE":
                    statusCircle.setFill(Color.web("#e74c3c15"));
                    statusCircle.setStroke(Color.web("#e74c3c"));
                    statusIconLabel.setText("🚨");
                    lblStatus.setText("SURCHARGE");
                    lblStatus.setTextFill(Color.web("#e74c3c"));
                    break;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        if (contentArea == null) return;
        try {
            URL resource = getClass().getResource("/backoffice/Classe/ClassesContent.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            Region view = loader.load();
            
            ClasseContentController controller = loader.getController();
            controller.setContentArea(contentArea);
            
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

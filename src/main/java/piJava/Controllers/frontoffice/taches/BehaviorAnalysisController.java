package piJava.Controllers.frontoffice.taches;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import piJava.entities.StudentIntelligenceProfile;
import piJava.services.api.BehaviorAnalysisService;

public class BehaviorAnalysisController {

    @FXML
    private TextArea summaryArea;

    @FXML
    private TextArea adviceArea;

    @FXML
    private Label loadingLabel;

    private final BehaviorAnalysisService service = new BehaviorAnalysisService();

    private int userId;

    public void loadDataForUser(int userId) {
        this.userId = userId;

        loadingLabel.setText("Analyse en cours...");

        // run analysis
        new Thread(() -> {
            try {
                StudentIntelligenceProfile profile =
                        service.getOrComputeProfile(userId);

                javafx.application.Platform.runLater(() -> {
                    loadingLabel.setText("");

                    summaryArea.setText(
                            profile.getWeeklyProductivitySummary()
                    );

                    adviceArea.setText(
                            profile.getBehavioralAdvice()
                    );
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loadingLabel.setText("Erreur lors de l'analyse");
                });
            }
        }).start();
    }

    public void handleClose() {
        Stage stage = (Stage) summaryArea.getScene().getWindow();
        stage.close();
    }
}
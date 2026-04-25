package piJava.Controllers.frontoffice.classe;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import piJava.entities.Classe;
import piJava.entities.ClasseStats;
import piJava.entities.user;
import piJava.services.ClasseService;
import piJava.utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

public class ClasseContentController implements Initializable {

    @FXML private Label lblClassName;
    @FXML private Label lblClassLevel;
    @FXML private Label lblClassYear;
    @FXML private Label lblClassDescription;
    @FXML private Label lblStatsMatieres;
    @FXML private Label lblStatsCoefficient;
    @FXML private Label lblStatsCharge;
    @FXML private Label lblStatsComplexite;

    private final ClasseService classeService = new ClasseService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCurrentUserClass();
    }

    private void loadCurrentUserClass() {
        SessionManager session = SessionManager.getInstance();
        user currentUser = session.getCurrentUser();

        if (currentUser == null) {
            showEmptyState("Aucun utilisateur connecté.");
            return;
        }

        Integer classId = currentUser.getClasse_id();
        if (classId == null) {
            showEmptyState("Aucune classe associée à votre compte.");
            return;
        }

        try {
            Classe classe = classeService.getById(classId);
            if (classe == null) {
                showEmptyState("Classe introuvable pour l’identifiant " + classId + ".");
                return;
            }

            lblClassName.setText(nvl(classe.getNom(), "—"));
            lblClassLevel.setText(nvl(classe.getNiveau(), "—"));
            lblClassYear.setText(nvl(classe.getAnneeUniversitaire(), "—"));
            lblClassDescription.setText(nvl(classe.getDescription(), "Aucune description disponible."));

            ClasseStats stats = classeService.getStatistiquesClasse(classe.getId());
            if (stats != null) {
                lblStatsMatieres.setText(stats.getNombreMatieres() + " matière(s)");
                lblStatsCoefficient.setText(String.format("%.1f", stats.getTotalCoefficient()));
                lblStatsCharge.setText(stats.getTotalChargeHoraire() + " h");
                lblStatsComplexite.setText(String.format("%.1f/10", stats.getMoyenneComplexite()));
            } else {
                lblStatsMatieres.setText("0 matière");
                lblStatsCoefficient.setText("0.0");
                lblStatsCharge.setText("0 h");
                lblStatsComplexite.setText("0.0/10");
            }
        } catch (Exception e) {
            showEmptyState("Erreur lors du chargement de la classe : " + e.getMessage());
        }
    }

    @FXML
    public void handleRefresh() {
        loadCurrentUserClass();
    }

    private void showEmptyState(String message) {
        lblClassName.setText("—");
        lblClassLevel.setText("—");
        lblClassYear.setText("—");
        lblClassDescription.setText(message);
        lblStatsMatieres.setText("—");
        lblStatsCoefficient.setText("—");
        lblStatsCharge.setText("—");
        lblStatsComplexite.setText("—");
    }

    private static String nvl(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}




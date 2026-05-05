package piJava.Controllers.frontoffice.quiz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import piJava.entities.*;
import piJava.services.QuizAttemptService;
import piJava.services.QuizService;
import piJava.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class QuizAttemptController implements Initializable {

    @FXML private Label lblQuizTitle;
    @FXML private Label lblQuizDesc;
    @FXML private Label lblMatiereName;
    @FXML private VBox questionsContainer;
    @FXML private Button btnSubmit;
    @FXML private Button btnRetry;
    @FXML private Label lblResult;

    private Matiere currentMatiere;
    private Quiz currentQuiz;
    private List<QuizQuestion> questions;
    private final Map<Integer, ToggleGroup> answerGroups = new HashMap<>();
    private final Map<Integer, List<QuizAnswer>> questionAnswers = new HashMap<>();

    private final QuizService quizService = new QuizService();
    private final QuizAttemptService attemptService = new QuizAttemptService();
    private StackPane contentArea;

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void initData(Matiere matiere) {
        this.currentMatiere = matiere;
        lblMatiereName.setText(matiere.getNom() != null ? matiere.getNom() : "Matière");
        loadQuiz();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblResult.setVisible(false);
    }

    private void loadQuiz() {
        try {
            currentQuiz = quizService.getQuizByMatiereId(currentMatiere.getId());
            if (currentQuiz == null) {
                showEmptyState("Aucun quiz disponible pour cette matière.");
                return;
            }

            lblQuizTitle.setText(currentQuiz.getTitle());
            lblQuizDesc.setText(currentQuiz.getDescription() != null ? currentQuiz.getDescription() : "");

            questions = quizService.getQuestionsByQuizId(currentQuiz.getId());
            if (questions.isEmpty()) {
                showEmptyState("Ce quiz ne contient aucune question.");
                return;
            }

            questionsContainer.getChildren().clear();
            for (int i = 0; i < questions.size(); i++) {
                QuizQuestion q = questions.get(i);
                VBox qBox = createQuestionBox(q, i + 1);
                questionsContainer.getChildren().add(qBox);
            }
            btnSubmit.setDisable(false);

        } catch (SQLException e) {
            showEmptyState("Erreur lors du chargement du quiz : " + e.getMessage());
        }
    }

    private VBox createQuestionBox(QuizQuestion question, int index) throws SQLException {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 10;");

        Label lblQText = new Label("Q" + index + ". " + question.getQuestionText());
        lblQText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1f2937;");
        lblQText.setWrapText(true);
        box.getChildren().add(lblQText);

        List<QuizAnswer> answers = quizService.getAnswersByQuestionId(question.getId());
        questionAnswers.put(question.getId(), answers);

        ToggleGroup group = new ToggleGroup();
        answerGroups.put(question.getId(), group);

        for (QuizAnswer ans : answers) {
            RadioButton rb = new RadioButton(ans.getAnswerText());
            rb.setUserData(ans);
            rb.setToggleGroup(group);
            rb.setStyle("-fx-font-size: 13px; -fx-text-fill: #4b5563;");
            box.getChildren().add(rb);
        }

        return box;
    }

    private void showEmptyState(String msg) {
        lblQuizTitle.setText("Indisponible");
        lblQuizDesc.setText(msg);
        questionsContainer.getChildren().clear();
        btnSubmit.setDisable(true);
    }

    @FXML
    private void handleSubmit() {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            lblResult.setText("Vous devez être connecté pour passer ce quiz.");
            lblResult.setStyle("-fx-text-fill: red;");
            lblResult.setVisible(true);
            return;
        }

        // Validate all questions are answered
        for (QuizQuestion q : questions) {
            ToggleGroup group = answerGroups.get(q.getId());
            if (group.getSelectedToggle() == null) {
                lblResult.setText("Veuillez répondre à toutes les questions avant de valider.");
                lblResult.setStyle("-fx-text-fill: #b91c1c; -fx-background-color: #fee2e2; -fx-padding: 10; -fx-background-radius: 5;");
                lblResult.setVisible(true);
                return;
            }
        }

        int score = 0;
        int total = questions.size();
        Map<Integer, Integer> selectedAnswers = new HashMap<>();

        for (QuizQuestion q : questions) {
            ToggleGroup group = answerGroups.get(q.getId());
            RadioButton selected = (RadioButton) group.getSelectedToggle();
            QuizAnswer ans = (QuizAnswer) selected.getUserData();
            selectedAnswers.put(q.getId(), ans.getId());
            if (ans.isCorrect()) {
                score++;
            }
        }

        try {
            QuizAttempt attempt = new QuizAttempt(0, currentQuiz.getId(), currentMatiere.getId(), currentUser.getId(), score, total);
            int attemptId = attemptService.createAttempt(attempt);

            for (Map.Entry<Integer, Integer> entry : selectedAnswers.entrySet()) {
                int qId = entry.getKey();
                int aId = entry.getValue();
                // Check if correct to save
                boolean isCorrect = questionAnswers.get(qId).stream().filter(a -> a.getId() == aId).findFirst().map(QuizAnswer::isCorrect).orElse(false);
                attemptService.addAttemptAnswer(attemptId, qId, aId, isCorrect);
            }

            lblResult.setText("Quiz terminé ! Votre score est de " + score + " / " + total);
            lblResult.setStyle("-fx-text-fill: #166534; -fx-background-color: #dcfce7; -fx-padding: 15; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 14px;");
            lblResult.setVisible(true);

            btnSubmit.setDisable(true);
            btnSubmit.setVisible(false);
            btnSubmit.setManaged(false);
            
            btnRetry.setVisible(true);
            btnRetry.setManaged(true);

            for (ToggleGroup g : answerGroups.values()) {
                g.getToggles().forEach(t -> ((RadioButton)t).setDisable(true));
            }

        } catch (SQLException e) {
            lblResult.setText("Erreur lors de l'enregistrement de la tentative : " + e.getMessage());
            lblResult.setStyle("-fx-text-fill: #b91c1c; -fx-background-color: #fee2e2; -fx-padding: 10; -fx-background-radius: 5;");
            lblResult.setVisible(true);
        }
    }

    @FXML
    private void handleRetry() {
        lblResult.setVisible(false);
        btnSubmit.setDisable(false);
        btnSubmit.setVisible(true);
        btnSubmit.setManaged(true);
        
        btnRetry.setVisible(false);
        btnRetry.setManaged(false);

        for (ToggleGroup g : answerGroups.values()) {
            g.selectToggle(null);
            g.getToggles().forEach(t -> ((RadioButton)t).setDisable(false));
        }
        
        // Scroll to top
        questionsContainer.getParent().layout();
        if (questionsContainer.getParent() instanceof ScrollPane sp) {
            sp.setVvalue(0);
        }
    }

    @FXML
    private void handleBack() {
        if (contentArea == null) {
            System.err.println("contentArea is null in QuizAttemptController.");
            return;
        }
        try {
            URL resource = getClass().getResource("/frontoffice/matieres/matieres-content.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            Region view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

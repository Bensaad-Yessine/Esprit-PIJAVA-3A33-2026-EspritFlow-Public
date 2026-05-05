package piJava.Controllers.backoffice.quiz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import piJava.entities.Matiere;
import piJava.entities.Quiz;
import piJava.entities.QuizAnswer;
import piJava.entities.QuizQuestion;
import piJava.services.QuizService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class QuizContentController implements Initializable {

    @FXML private Label lblMatiereName;
    @FXML private Label lblStatus;

    // --- CREATE QUIZ BOX ---
    @FXML private VBox createQuizBox;
    @FXML private TextField txtQuizTitle;
    @FXML private TextArea txtQuizDesc;

    // --- MANAGE QUIZ BOX ---
    @FXML private VBox manageQuizBox;
    @FXML private Label lblQuizTitle;
    @FXML private Label lblQuizDesc;

    // --- QUESTIONS TABLE ---
    @FXML private TableView<QuizQuestion> questionTable;
    @FXML private TableColumn<QuizQuestion, String> qPosCol;
    @FXML private TableColumn<QuizQuestion, String> qTextCol;
    @FXML private TableColumn<QuizQuestion, Void> qActionCol;
    @FXML private Label questionCountLabel;

    // --- ADD QUESTION ---
    @FXML private TextField txtQuestion;
    @FXML private ComboBox<String> cbCorrect;
    @FXML private TextField txtAnswer1;
    @FXML private TextField txtAnswer2;
    @FXML private TextField txtAnswer3;
    @FXML private TextField txtAnswer4;

    private Matiere currentMatiere;
    private Quiz currentQuiz;
    private final QuizService quizService = new QuizService();
    private final ObservableList<QuizQuestion> questionItems = FXCollections.observableArrayList();

    private StackPane contentArea;

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbCorrect.setItems(FXCollections.observableArrayList("1", "2", "3", "4"));
        setupQuestionTable();
    }

    private void setupQuestionTable() {
        qPosCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getPosition())));
        qTextCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getQuestionText()));

        // We can add delete question functionality if needed, but for now we keep it empty or simple
        qActionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new Label("...")); // placeholder
                }
            }
        });

        questionTable.setItems(questionItems);
    }

    public void initData(Matiere matiere) {
        this.currentMatiere = matiere;
        if (lblMatiereName != null) {
            lblMatiereName.setText(matiere.getNom());
        }
        loadQuizData();
    }

    private void loadQuizData() {
        if (currentMatiere == null) return;
        try {
            currentQuiz = quizService.getQuizByMatiereId(currentMatiere.getId());
            if (currentQuiz == null) {
                // Aucun quiz n'existe
                createQuizBox.setVisible(true);
                createQuizBox.setManaged(true);
                manageQuizBox.setVisible(false);
                manageQuizBox.setManaged(false);
            } else {
                // Un quiz existe
                createQuizBox.setVisible(false);
                createQuizBox.setManaged(false);
                manageQuizBox.setVisible(true);
                manageQuizBox.setManaged(true);

                lblQuizTitle.setText(currentQuiz.getTitle());
                lblQuizDesc.setText(currentQuiz.getDescription() != null ? currentQuiz.getDescription() : "");
                
                // Charger les questions
                List<QuizQuestion> qs = quizService.getQuestionsByQuizId(currentQuiz.getId());
                questionItems.setAll(qs);
                if (questionCountLabel != null) {
                    questionCountLabel.setText(qs.size() + " questions");
                }
            }
        } catch (SQLException e) {
            setStatus("Erreur chargement quiz: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateQuiz() {
        if (currentMatiere == null) {
            setStatus("Aucune matière sélectionnée.");
            return;
        }
        String title = txtQuizTitle.getText() == null ? "" : txtQuizTitle.getText().trim();
        if (title.isBlank()) {
            setStatus("Le titre du quiz est obligatoire.");
            return;
        }
        try {
            Quiz quiz = new Quiz(0, currentMatiere.getId(), title, txtQuizDesc.getText());
            quizService.createQuiz(quiz);
            setStatus("Quiz créé avec succès.");
            loadQuizData(); // Recharge l'interface vers le mode "Gérer"
        } catch (SQLException e) {
            setStatus("Erreur création quiz: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteQuiz() {
        if (currentQuiz == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer ce quiz et toutes ses questions ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Suppression du quiz");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    quizService.deleteQuiz(currentQuiz.getId());
                    currentQuiz = null;
                    setStatus("Quiz supprimé.");
                    loadQuizData(); // Recharge l'interface vers le mode "Créer"
                } catch (SQLException e) {
                    setStatus("Erreur suppression: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleAddQuestion() {
        if (currentQuiz == null) {
            setStatus("Aucun quiz actif.");
            return;
        }
        String qText = txtQuestion.getText() == null ? "" : txtQuestion.getText().trim();
        if (qText.isBlank()) {
            setStatus("La question est obligatoire.");
            return;
        }
        String[] answers = {
                valueOrEmpty(txtAnswer1),
                valueOrEmpty(txtAnswer2),
                valueOrEmpty(txtAnswer3),
                valueOrEmpty(txtAnswer4)
        };
        for (String a : answers) {
            if (a.isBlank()) {
                setStatus("Toutes les 4 réponses sont obligatoires.");
                return;
            }
        }
        String correct = cbCorrect.getValue();
        if (correct == null) {
            setStatus("Veuillez choisir la bonne réponse.");
            return;
        }

        try {
            int position = questionItems.size() + 1;
            QuizQuestion question = new QuizQuestion(0, currentQuiz.getId(), qText, position);
            question = quizService.addQuestion(question);

            int correctIndex = Integer.parseInt(correct) - 1;
            for (int i = 0; i < answers.length; i++) {
                QuizAnswer ans = new QuizAnswer(0, question.getId(), answers[i], i == correctIndex);
                quizService.addAnswer(ans);
            }

            // Mettre à jour l'interface
            questionItems.add(question);
            if (questionCountLabel != null) {
                questionCountLabel.setText(questionItems.size() + " questions");
            }
            txtQuestion.clear();
            txtAnswer1.clear();
            txtAnswer2.clear();
            txtAnswer3.clear();
            txtAnswer4.clear();
            cbCorrect.setValue(null);
            setStatus("Question ajoutée avec succès.");
        } catch (SQLException e) {
            setStatus("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private String valueOrEmpty(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void setStatus(String msg) {
        lblStatus.setText(msg == null ? "" : msg);
    }

    @FXML
    private void handleBack() {
        if (contentArea == null) return;
        try {
            URL resource = getClass().getResource("/backoffice/Matiere/MatiereContent.fxml");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(resource);
            javafx.scene.layout.Region view = loader.load();
            
            piJava.Controllers.backoffice.Matiere.MatiereContentController c = loader.getController();
            c.setContentArea(contentArea);
            
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package piJava.entities;

public class QuizQuestion {
    private int id;
    private int quizId;
    private String questionText;
    private int position;

    public QuizQuestion() {
    }

    public QuizQuestion(int id, int quizId, String questionText, int position) {
        this.id = id;
        this.quizId = quizId;
        this.questionText = questionText;
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return questionText;
    }
}

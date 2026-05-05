package piJava.entities;

public class QuizAnswer {
    private int id;
    private int questionId;
    private String answerText;
    private boolean correct;

    public QuizAnswer() {
    }

    public QuizAnswer(int id, int questionId, String answerText, boolean correct) {
        this.id = id;
        this.questionId = questionId;
        this.answerText = answerText;
        this.correct = correct;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}


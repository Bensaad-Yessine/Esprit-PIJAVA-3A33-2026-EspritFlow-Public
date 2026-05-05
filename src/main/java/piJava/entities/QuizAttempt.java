package piJava.entities;

public class QuizAttempt {
    private int id;
    private int quizId;
    private int matiereId;
    private int userId;
    private int score;
    private int totalQuestions;

    public QuizAttempt() {
    }

    public QuizAttempt(int id, int quizId, int matiereId, int userId, int score, int totalQuestions) {
        this.id = id;
        this.quizId = quizId;
        this.matiereId = matiereId;
        this.userId = userId;
        this.score = score;
        this.totalQuestions = totalQuestions;
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

    public int getMatiereId() {
        return matiereId;
    }

    public void setMatiereId(int matiereId) {
        this.matiereId = matiereId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
}


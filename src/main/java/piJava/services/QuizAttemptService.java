package piJava.services;

import piJava.entities.QuizAttempt;
import piJava.utils.MyDataBase;

import java.sql.*;

public class QuizAttemptService {

    private Connection cnx;

    public QuizAttemptService() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    public int createAttempt(QuizAttempt attempt) throws SQLException {
        String query = "INSERT INTO quiz_attempt (quiz_id, matiere_id, user_id, score, total_questions) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, attempt.getQuizId());
            pstmt.setInt(2, attempt.getMatiereId());
            pstmt.setInt(3, attempt.getUserId());
            pstmt.setInt(4, attempt.getScore());
            pstmt.setInt(5, attempt.getTotalQuestions());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public void addAttemptAnswer(int attemptId, int questionId, int answerId, boolean isCorrect) throws SQLException {
        String query = "INSERT INTO quiz_attempt_answer (attempt_id, question_id, answer_id, is_correct) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, attemptId);
            pstmt.setInt(2, questionId);
            pstmt.setInt(3, answerId);
            pstmt.setBoolean(4, isCorrect);
            pstmt.executeUpdate();
        }
    }
}

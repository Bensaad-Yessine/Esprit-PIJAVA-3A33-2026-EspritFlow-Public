package piJava.services;

import piJava.entities.Quiz;
import piJava.entities.QuizAnswer;
import piJava.entities.QuizQuestion;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizService {

    private Connection cnx;

    public QuizService() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    public Quiz getQuizByMatiereId(int matiereId) throws SQLException {
        String query = "SELECT * FROM quiz WHERE matiere_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, matiereId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Quiz(
                        rs.getInt("id"),
                        rs.getInt("matiere_id"),
                        rs.getString("title"),
                        rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

    public List<Quiz> getQuizzesByMatiereId(int matiereId) throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();
        String query = "SELECT * FROM quiz WHERE matiere_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, matiereId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    quizzes.add(new Quiz(
                        rs.getInt("id"),
                        rs.getInt("matiere_id"),
                        rs.getString("title"),
                        rs.getString("description")
                    ));
                }
            }
        }
        return quizzes;
    }

    public List<QuizQuestion> getQuestionsByQuizId(int quizId) throws SQLException {
        List<QuizQuestion> questions = new ArrayList<>();
        String query = "SELECT * FROM quiz_question WHERE quiz_id = ? ORDER BY position";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, quizId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(new QuizQuestion(
                        rs.getInt("id"),
                        rs.getInt("quiz_id"),
                        rs.getString("question_text"),
                        rs.getInt("position")
                    ));
                }
            }
        }
        return questions;
    }

    public List<QuizAnswer> getAnswersByQuestionId(int questionId) throws SQLException {
        List<QuizAnswer> answers = new ArrayList<>();
        String query = "SELECT * FROM quiz_answer WHERE question_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    answers.add(new QuizAnswer(
                        rs.getInt("id"),
                        rs.getInt("question_id"),
                        rs.getString("answer_text"),
                        rs.getBoolean("is_correct")
                    ));
                }
            }
        }
        return answers;
    }

    // Methods needed by QuizContentController
    public Quiz createQuiz(Quiz quiz) throws SQLException {
        String query = "INSERT INTO quiz (matiere_id, title, description) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, quiz.getMatiereId());
            pstmt.setString(2, quiz.getTitle());
            pstmt.setString(3, quiz.getDescription());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    quiz.setId(rs.getInt(1));
                }
            }
        }
        return quiz;
    }

    public QuizQuestion addQuestion(QuizQuestion question) throws SQLException {
        String query = "INSERT INTO quiz_question (quiz_id, question_text, position) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, question.getQuizId());
            pstmt.setString(2, question.getQuestionText());
            pstmt.setInt(3, question.getPosition());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    question.setId(rs.getInt(1));
                }
            }
        }
        return question;
    }

    public void addAnswer(QuizAnswer answer) throws SQLException {
        String query = "INSERT INTO quiz_answer (question_id, answer_text, is_correct) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, answer.getQuestionId());
            pstmt.setString(2, answer.getAnswerText());
            pstmt.setBoolean(3, answer.isCorrect());
            pstmt.executeUpdate();
        }
    }

    public void deleteQuiz(int quizId) throws SQLException {
        String query = "DELETE FROM quiz WHERE id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, quizId);
            pstmt.executeUpdate();
        }
    }
}

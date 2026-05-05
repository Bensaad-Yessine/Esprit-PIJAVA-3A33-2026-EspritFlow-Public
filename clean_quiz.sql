
USE pidev;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS quiz_attempt_answer;
DROP TABLE IF EXISTS quiz_attempt_detail;
DROP TABLE IF EXISTS quiz_attempt;
DROP TABLE IF EXISTS quiz_answer;
DROP TABLE IF EXISTS quiz_question;
DROP TABLE IF EXISTS quiz;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE quiz (
    id INT AUTO_INCREMENT PRIMARY KEY,
    matiere_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    CONSTRAINT fk_quiz_matiere FOREIGN KEY (matiere_id) REFERENCES matiere_classe(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE quiz_question (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    question_text TEXT NOT NULL,
    position INT DEFAULT 1,
    CONSTRAINT fk_q_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE quiz_answer (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    answer_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_a_q FOREIGN KEY (question_id) REFERENCES quiz_question(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE quiz_attempt (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    matiere_id INT NOT NULL,
    user_id INT NOT NULL,
    score INT NOT NULL,
    total_questions INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_att_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE,
    CONSTRAINT fk_att_mat FOREIGN KEY (matiere_id) REFERENCES matiere_classe(id) ON DELETE CASCADE,
    CONSTRAINT fk_att_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE quiz_attempt_answer (
    id INT AUTO_INCREMENT PRIMARY KEY,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    answer_id INT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_aa_att FOREIGN KEY (attempt_id) REFERENCES quiz_attempt(id) ON DELETE CASCADE,
    CONSTRAINT fk_aa_q FOREIGN KEY (question_id) REFERENCES quiz_question(id) ON DELETE CASCADE,
    CONSTRAINT fk_aa_a FOREIGN KEY (answer_id) REFERENCES quiz_answer(id) ON DELETE CASCADE
) ENGINE=InnoDB;


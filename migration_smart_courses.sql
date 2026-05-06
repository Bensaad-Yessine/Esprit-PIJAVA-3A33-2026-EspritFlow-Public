-- ════════════════════════════════════════════════════════════════════════════════
-- SMART ASSISTANT FOR STUDENTS - Database Migration
-- Tables pour gérer les cours, les streaks, et les recommandations
-- ════════════════════════════════════════════════════════════════════════════════

-- ────────────────────────────────────────────────────────────────────────────────
-- 1. ALTER TABLE user - Ajouter les champs de streak
-- ────────────────────────────────────────────────────────────────────────────────
ALTER TABLE user ADD COLUMN IF NOT EXISTS current_streak INT DEFAULT 0;
ALTER TABLE user ADD COLUMN IF NOT EXISTS longest_streak INT DEFAULT 0;
ALTER TABLE user ADD COLUMN IF NOT EXISTS last_streak_date DATE DEFAULT NULL;
ALTER TABLE user ADD COLUMN IF NOT EXISTS total_courses_completed INT DEFAULT 0;
ALTER TABLE user ADD COLUMN IF NOT EXISTS learning_points INT DEFAULT 0;

-- ────────────────────────────────────────────────────────────────────────────────
-- 2. CREATE TABLE course_cache - Cache local des cours depuis l'API
-- ────────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS course_cache (
    id INT AUTO_INCREMENT PRIMARY KEY,
    api_id VARCHAR(255) UNIQUE NOT NULL,
    title VARCHAR(500) NOT NULL,
    description LONGTEXT,
    category VARCHAR(255),
    coupon_code VARCHAR(100),
    expiration_date DATE,
    course_url VARCHAR(1024),
    instructor VARCHAR(255),
    rating DECIMAL(3, 2) DEFAULT 0,
    students_enrolled INT DEFAULT 0,
    thumbnail_url VARCHAR(500),
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX (category),
    INDEX (title)
);

-- ────────────────────────────────────────────────────────────────────────────────
-- 3. CREATE TABLE user_course - Suivi de la progression utilisateur
-- ────────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_course (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    api_course_id VARCHAR(255),
    is_completed BOOLEAN DEFAULT FALSE,
    completion_date DATETIME DEFAULT NULL,
    progress_percentage INT DEFAULT 0,
    enrolled_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_accessed DATETIME DEFAULT NULL,
    notes LONGTEXT,
    UNIQUE KEY unique_user_course (user_id, course_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course_cache(id) ON DELETE CASCADE,
    INDEX (user_id),
    INDEX (is_completed),
    INDEX (completion_date)
);

-- ────────────────────────────────────────────────────────────────────────────────
-- 4. CREATE TABLE user_interest - Suivi des catégories intéressant l'utilisateur
-- ────────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_interest (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category VARCHAR(255) NOT NULL,
    interest_score INT DEFAULT 1,
    last_engagement DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_category (user_id, category),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX (user_id),
    INDEX (interest_score)
);

-- ────────────────────────────────────────────────────────────────────────────────
-- 5. CREATE TABLE achievement - Badges et réalisations utilisateur
-- ────────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS achievement (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    achievement_type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    badge_icon VARCHAR(1024),
    earned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    metadata JSON,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX (user_id),
    INDEX (achievement_type)
);

-- ────────────────────────────────────────────────────────────────────────────────
-- 6. CREATE TABLE api_request_log - Anti-spam et rate-limiting
-- ────────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS api_request_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    request_type VARCHAR(100),
    endpoint VARCHAR(500),
    request_count INT DEFAULT 1,
    last_request TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    request_date DATE AS (DATE(last_request)) STORED,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_request (user_id, request_type, request_date),
    INDEX (user_id),
    INDEX (last_request)
);

-- ════════════════════════════════════════════════════════════════════════════════
-- FIN DES MIGRATIONS
-- ════════════════════════════════════════════════════════════════════════════════


/*
  Knowva DDL - MySQL 8 / InnoDB / utf8mb4
  Source: Notion DB 명세 v2.4 / 관리자 댓글 삭제 주체

  MySQL Workbench connection 설정
  1. MySQL Connections 화면에서 + 버튼 클릭
  2. Connection Name: elearning
  3. Username: local .env의 DB_USERNAME
  4. Password: local .env의 DB_PASSWORD
  5. Default Schema: 비움
  6. Test Connection 후 OK

  ** 테스트 실패할 시 ai에게 ddl 실행시키고 다시 위 항목 진행하면 됩니다.**
  ** 이후 schema탭에 elearning이 생성된 것을 확인할 수 있습니다.**
  ** select문으로 테스트하고 싶다면 use elearning; 후 select * from subjects; 등으로 해보시면 됩니다.**

  실행 기준
  - Workbench connection의 Default Schema는 비워둔다.
  - 이 script는 elearning schema를 생성한 뒤 USE elearning을 수행한다.
  - 전체 실행 시 elearning schema 안의 기존 Knowva table을 모두 DROP TABLE IF EXISTS 후 다시 생성한다.
  - 기존 table/data를 보존하는 migration script가 아니다. schema 최신화 시 반드시 백업 여부를 먼저 확인한다.
  - sample data는 이 script가 오류 없이 끝난 뒤 docs/ddl/Knowva_sample_data.sql을 별도로 전체 실행한다.
  - MySQL 8 기준으로 별도 CREATE SEQUENCE 객체는 만들지 않는다.
    각 table의 BIGINT UNSIGNED PK에 AUTO_INCREMENT를 지정해 sequence 역할을 처리한다.
  - FK 기본 정책은 DB 명세 기준 ON DELETE RESTRICT, ON UPDATE CASCADE다.
  - AI 시험 정답/오답 판정은 AI 점수가 아니라 testcase 실행 결과로 저장한다.
  - 인증 수단별 독립 계정 정책: users.email은 연락처/표시 정보이며 계정 병합 기준으로 사용하지 않는다.
    이메일 로그인은 user_credentials.login_email, 소셜 로그인은 social_accounts(provider, provider_user_id)로 식별한다.
*/

SET NAMES utf8mb4;
SET time_zone = '+09:00';

CREATE DATABASE IF NOT EXISTS elearning
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE elearning;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS subject_content_status_backups;
DROP TABLE IF EXISTS content_recommendations;
DROP TABLE IF EXISTS admin_operation_logs;
DROP TABLE IF EXISTS notices;
DROP TABLE IF EXISTS payment_refunds;
DROP TABLE IF EXISTS premium_grants;
DROP TABLE IF EXISTS dummy_payments;
DROP TABLE IF EXISTS payment_products;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS post_scraps;
DROP TABLE IF EXISTS post_likes;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS post_attachments;
DROP TABLE IF EXISTS community_posts;
DROP TABLE IF EXISTS ai_request_logs;
DROP TABLE IF EXISTS ai_analysis_reports;
DROP TABLE IF EXISTS exam_answers;
DROP TABLE IF EXISTS ai_exam_problems;
DROP TABLE IF EXISTS user_subject_enrollments;
DROP TABLE IF EXISTS user_level_unlocks;
DROP TABLE IF EXISTS exam_sessions;
DROP TABLE IF EXISTS ranking_scores;
DROP TABLE IF EXISTS score_events;
DROP TABLE IF EXISTS wrong_answers;
DROP TABLE IF EXISTS practice_submissions;
DROP TABLE IF EXISTS practice_set_items;
DROP TABLE IF EXISTS problem_choices;
DROP TABLE IF EXISTS practice_problems;
DROP TABLE IF EXISTS attendance_records;
DROP TABLE IF EXISTS practice_set_attempts;
DROP TABLE IF EXISTS learning_progress;
DROP TABLE IF EXISTS level_test_answers;
DROP TABLE IF EXISTS level_test_attempts;
DROP TABLE IF EXISTS level_test_choices;
DROP TABLE IF EXISTS level_test_questions;
DROP TABLE IF EXISTS user_lesson_progress;
DROP TABLE IF EXISTS lesson_bookmarks;
DROP TABLE IF EXISTS lessons;
DROP TABLE IF EXISTS user_learning_profiles;
DROP TABLE IF EXISTS user_settings;
DROP TABLE IF EXISTS social_accounts;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS user_credentials;
DROP TABLE IF EXISTS curriculum_nodes;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
  user_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  role VARCHAR(30) NOT NULL DEFAULT 'ROLE_USER',
  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  profile_image_url VARCHAR(500) NULL,
  last_login_at DATETIME NULL,
  withdrawn_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_users_nickname (nickname),
  KEY idx_users_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE subjects (
  subject_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  subject_code VARCHAR(30) NOT NULL,
  subject_name VARCHAR(100) NOT NULL,
  description VARCHAR(500) NULL,
  sort_order INT UNSIGNED NOT NULL DEFAULT 0,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (subject_id),
  UNIQUE KEY uk_subjects_code (subject_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE curriculum_nodes (
  node_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  subject_id BIGINT UNSIGNED NOT NULL,
  parent_node_id BIGINT UNSIGNED NULL,
  level_code VARCHAR(30) NOT NULL,
  node_type VARCHAR(30) NOT NULL DEFAULT 'PLANET',
  planet_no TINYINT UNSIGNED NULL,
  title VARCHAR(150) NOT NULL,
  description VARCHAR(500) NULL,
  sort_order INT UNSIGNED NOT NULL DEFAULT 0,
  gate_condition VARCHAR(255) NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (node_id),
  KEY idx_nodes_subject_level (subject_id, level_code),
  KEY idx_nodes_parent (parent_node_id),
  KEY idx_nodes_subject_planet (subject_id, level_code, planet_no),
  CONSTRAINT fk_curriculum_nodes_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_curriculum_nodes_parent FOREIGN KEY (parent_node_id) REFERENCES curriculum_nodes (node_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE payment_products (
  product_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  product_code VARCHAR(30) NOT NULL,
  product_name VARCHAR(100) NOT NULL,
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (product_id),
  UNIQUE KEY uk_payment_products_code (product_code),
  KEY idx_payment_products_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE password_reset_tokens (
  token_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  token_hash CHAR(64) NOT NULL,
  expires_at DATETIME NOT NULL,
  used_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (token_id),
  UNIQUE KEY uk_password_reset_tokens_hash (token_hash),
  KEY idx_password_reset_tokens_user (user_id),
  KEY idx_password_reset_tokens_expires (expires_at),
  CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_credentials (
  credential_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  login_email VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  email_verified_at DATETIME NOT NULL,
  password_updated_at DATETIME NULL,
  failed_login_count INT UNSIGNED NOT NULL DEFAULT 0,
  locked_until DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (credential_id),
  UNIQUE KEY uk_user_credentials_user_id (user_id),
  UNIQUE KEY uk_user_credentials_login_email (login_email),
  CONSTRAINT fk_user_credentials_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE social_accounts (
  social_account_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  provider VARCHAR(30) NOT NULL,
  provider_user_id VARCHAR(191) NOT NULL,
  provider_email VARCHAR(255) NULL,
  provider_email_verified TINYINT(1) NOT NULL DEFAULT 0,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  connected_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  disconnected_at DATETIME NULL,
  PRIMARY KEY (social_account_id),
  UNIQUE KEY uk_social_provider_user (provider, provider_user_id),
  UNIQUE KEY uk_social_user_provider (user_id, provider),
  CONSTRAINT fk_social_accounts_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_settings (
  setting_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  theme VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
  notification_enabled TINYINT(1) NOT NULL DEFAULT 1,
  accessibility_mode VARCHAR(30) NULL,
  reduced_motion_enabled TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (setting_id),
  UNIQUE KEY uk_user_settings_user_id (user_id),
  CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_learning_profiles (
  profile_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  primary_subject_id BIGINT UNSIGNED NULL,
  learning_goal VARCHAR(500) NULL,
  current_level_code VARCHAR(30) NOT NULL DEFAULT 'BRONZE',
  total_score INT UNSIGNED NOT NULL DEFAULT 0,
  grade_code VARCHAR(30) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (profile_id),
  UNIQUE KEY uk_learning_profiles_user_id (user_id),
  KEY idx_learning_profiles_subject_level (primary_subject_id, current_level_code),
  CONSTRAINT fk_learning_profiles_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_learning_profiles_subject FOREIGN KEY (primary_subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_subject_enrollments (
  enrollment_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  start_mode VARCHAR(30) NOT NULL DEFAULT 'BASIC',
  enrolled_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (enrollment_id),
  UNIQUE KEY uk_user_subject_enrollment (user_id, subject_id),
  KEY idx_enrollment_user_status (user_id, status),
  CONSTRAINT fk_user_subject_enrollments_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_user_subject_enrollments_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE lessons (
  lesson_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  node_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(150) NOT NULL,
  summary VARCHAR(500) NULL,
  content MEDIUMTEXT NOT NULL,
  example_code TEXT NULL,
  sort_order INT UNSIGNED NOT NULL DEFAULT 0,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  required_for_completion TINYINT(1) NOT NULL DEFAULT 1,
  created_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (lesson_id),
  KEY idx_lessons_node (node_id),
  KEY idx_lessons_node_sort (node_id, is_active, required_for_completion, sort_order, lesson_id),
  KEY idx_lessons_active (is_active),
  KEY idx_lessons_created_by (created_by),
  CONSTRAINT fk_lessons_node FOREIGN KEY (node_id) REFERENCES curriculum_nodes (node_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_lessons_created_by FOREIGN KEY (created_by) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE lesson_bookmarks (
  bookmark_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  lesson_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (bookmark_id),
  UNIQUE KEY uk_lesson_bookmark_user_lesson (user_id, lesson_id),
  KEY idx_lesson_bookmarks_user (user_id),
  KEY idx_lesson_bookmarks_lesson (lesson_id),
  CONSTRAINT fk_lesson_bookmarks_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_lesson_bookmarks_lesson FOREIGN KEY (lesson_id) REFERENCES lessons (lesson_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_lesson_progress (
  lesson_progress_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  lesson_id BIGINT UNSIGNED NOT NULL,
  theory_completed TINYINT(1) NOT NULL DEFAULT 0,
  practice_passed TINYINT(1) NOT NULL DEFAULT 0,
  progress_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  completed_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (lesson_progress_id),
  UNIQUE KEY uk_user_lesson_progress (user_id, lesson_id),
  KEY idx_user_lesson_progress_lesson (lesson_id),
  KEY idx_user_lesson_progress_user_completed (user_id, completed_at),
  CONSTRAINT fk_user_lesson_progress_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_user_lesson_progress_lesson FOREIGN KEY (lesson_id) REFERENCES lessons (lesson_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE level_test_questions (
  question_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  subject_id BIGINT UNSIGNED NOT NULL,
  question_no TINYINT UNSIGNED NOT NULL,
  question_text TEXT NOT NULL,
  question_type VARCHAR(30) NOT NULL DEFAULT 'MULTIPLE_CHOICE',
  explanation TEXT NULL,
  difficulty_code VARCHAR(30) NOT NULL DEFAULT 'BRONZE',
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (question_id),
  KEY idx_level_questions_subject_active (subject_id, is_active),
  CONSTRAINT fk_level_test_questions_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE level_test_choices (
  choice_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  question_id BIGINT UNSIGNED NOT NULL,
  choice_label VARCHAR(10) NOT NULL,
  choice_text TEXT NOT NULL,
  is_correct TINYINT(1) NOT NULL DEFAULT 0,
  sort_order INT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (choice_id),
  UNIQUE KEY uk_level_choice_question_label (question_id, choice_label),
  KEY idx_level_choices_question (question_id),
  CONSTRAINT fk_level_test_choices_question FOREIGN KEY (question_id) REFERENCES level_test_questions (question_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE level_test_attempts (
  attempt_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,
  total_count TINYINT UNSIGNED NOT NULL DEFAULT 8,
  correct_count TINYINT UNSIGNED NOT NULL DEFAULT 0,
  result_level_code VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
  submitted_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (attempt_id),
  KEY idx_level_attempt_user_subject (user_id, subject_id),
  KEY idx_level_attempt_submitted_at (submitted_at),
  CONSTRAINT fk_level_test_attempts_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_level_test_attempts_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE level_test_answers (
  answer_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  attempt_id BIGINT UNSIGNED NOT NULL,
  question_id BIGINT UNSIGNED NOT NULL,
  choice_id BIGINT UNSIGNED NULL,
  submitted_answer VARCHAR(500) NULL,
  is_correct TINYINT(1) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (answer_id),
  UNIQUE KEY uk_level_answer_attempt_question (attempt_id, question_id),
  KEY idx_level_answers_question (question_id),
  KEY idx_level_answers_choice (choice_id),
  CONSTRAINT fk_level_test_answers_attempt FOREIGN KEY (attempt_id) REFERENCES level_test_attempts (attempt_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_level_test_answers_question FOREIGN KEY (question_id) REFERENCES level_test_questions (question_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_level_test_answers_choice FOREIGN KEY (choice_id) REFERENCES level_test_choices (choice_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE learning_progress (
  progress_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,
  node_id BIGINT UNSIGNED NOT NULL,
  lesson_completed TINYINT(1) NOT NULL DEFAULT 0,
  practice_passed TINYINT(1) NOT NULL DEFAULT 0,
  progress_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  completed_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (progress_id),
  UNIQUE KEY uk_learning_progress_user_node (user_id, subject_id, node_id),
  KEY idx_learning_progress_subject_node (subject_id, node_id),
  CONSTRAINT fk_learning_progress_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_learning_progress_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_learning_progress_node FOREIGN KEY (node_id) REFERENCES curriculum_nodes (node_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE practice_set_attempts (
  set_attempt_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,
  node_id BIGINT UNSIGNED NOT NULL,
  lesson_id BIGINT UNSIGNED NULL,
  total_count TINYINT UNSIGNED NOT NULL DEFAULT 10,
  correct_count TINYINT UNSIGNED NOT NULL DEFAULT 0,
  status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
  passed TINYINT(1) NOT NULL DEFAULT 0,
  completed_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (set_attempt_id),
  KEY idx_practice_set_attempt_user_node (user_id, node_id),
  KEY idx_practice_set_attempt_lesson (user_id, lesson_id),
  KEY idx_practice_set_attempt_subject (subject_id),
  KEY idx_practice_set_attempt_completed_at (completed_at),
  CONSTRAINT fk_practice_set_attempts_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_practice_set_attempts_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_practice_set_attempts_node FOREIGN KEY (node_id) REFERENCES curriculum_nodes (node_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_practice_set_attempts_lesson FOREIGN KEY (lesson_id) REFERENCES lessons (lesson_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE attendance_records (
  attendance_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  attendance_date DATE NOT NULL,
  streak_count INT UNSIGNED NOT NULL DEFAULT 1,
  qualified_set_attempt_id BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (attendance_id),
  UNIQUE KEY uk_attendance_user_date (user_id, attendance_date),
  UNIQUE KEY uk_attendance_qualified_attempt (qualified_set_attempt_id),
  KEY idx_attendance_date (attendance_date),
  CONSTRAINT fk_attendance_records_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_attendance_records_qualified_attempt FOREIGN KEY (qualified_set_attempt_id) REFERENCES practice_set_attempts (set_attempt_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE practice_problems (
  problem_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  subject_id BIGINT UNSIGNED NOT NULL,
  node_id BIGINT UNSIGNED NOT NULL,
  lesson_id BIGINT UNSIGNED NULL,
  problem_type VARCHAR(30) NOT NULL DEFAULT 'MULTIPLE_CHOICE',
  question TEXT NOT NULL,
  answer_text TEXT NULL,
  explanation TEXT NULL,
  difficulty_code VARCHAR(30) NOT NULL DEFAULT 'BRONZE',
  created_by BIGINT UNSIGNED NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (problem_id),
  KEY idx_practice_problems_subject_node (subject_id, node_id),
  KEY idx_practice_problems_lesson (lesson_id, is_active),
  KEY idx_practice_problems_created_by (created_by),
  KEY idx_practice_problems_active (is_active),
  CONSTRAINT fk_practice_problems_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_practice_problems_node FOREIGN KEY (node_id) REFERENCES curriculum_nodes (node_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_practice_problems_lesson FOREIGN KEY (lesson_id) REFERENCES lessons (lesson_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_practice_problems_created_by FOREIGN KEY (created_by) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE subject_content_status_backups (
  backup_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  subject_id BIGINT UNSIGNED NOT NULL,
  content_type VARCHAR(30) NOT NULL,
  content_id BIGINT UNSIGNED NOT NULL,
  was_active TINYINT(1) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (backup_id),
  UNIQUE KEY uk_subject_content_backup (subject_id, content_type, content_id),
  KEY idx_subject_content_backup_subject (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE problem_choices (
  choice_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  problem_id BIGINT UNSIGNED NOT NULL,
  choice_label VARCHAR(10) NOT NULL,
  choice_text TEXT NOT NULL,
  is_correct TINYINT(1) NOT NULL DEFAULT 0,
  sort_order INT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (choice_id),
  UNIQUE KEY uk_problem_choice_problem_label (problem_id, choice_label),
  KEY idx_problem_choices_problem (problem_id),
  CONSTRAINT fk_problem_choices_problem FOREIGN KEY (problem_id) REFERENCES practice_problems (problem_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE practice_set_items (
  set_item_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  set_attempt_id BIGINT UNSIGNED NOT NULL,
  problem_id BIGINT UNSIGNED NOT NULL,
  sort_order TINYINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (set_item_id),
  UNIQUE KEY uk_practice_set_item_order (set_attempt_id, sort_order),
  UNIQUE KEY uk_practice_set_item_problem (set_attempt_id, problem_id),
  KEY idx_practice_set_items_problem (problem_id),
  CONSTRAINT fk_practice_set_items_attempt FOREIGN KEY (set_attempt_id) REFERENCES practice_set_attempts (set_attempt_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_practice_set_items_problem FOREIGN KEY (problem_id) REFERENCES practice_problems (problem_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE practice_submissions (
  submission_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  set_attempt_id BIGINT UNSIGNED NOT NULL,
  set_item_id BIGINT UNSIGNED NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  problem_id BIGINT UNSIGNED NOT NULL,
  submission_context VARCHAR(30) NOT NULL DEFAULT 'PRACTICE_SET',
  submitted_answer TEXT NULL,
  is_correct TINYINT(1) NOT NULL DEFAULT 0,
  is_skipped TINYINT(1) NOT NULL DEFAULT 0,
  solved_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (submission_id),
  UNIQUE KEY uk_practice_submission_set_problem (set_attempt_id, problem_id, submission_context),
  KEY idx_practice_submissions_set_item (set_item_id),
  KEY idx_practice_submissions_user (user_id),
  KEY idx_practice_submissions_problem (problem_id),
  KEY idx_practice_submissions_solved_at (solved_at),
  CONSTRAINT fk_practice_submissions_set_attempt FOREIGN KEY (set_attempt_id) REFERENCES practice_set_attempts (set_attempt_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_practice_submissions_set_item FOREIGN KEY (set_item_id) REFERENCES practice_set_items (set_item_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_practice_submissions_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_practice_submissions_problem FOREIGN KEY (problem_id) REFERENCES practice_problems (problem_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE wrong_answers (
  wrong_answer_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  set_attempt_id BIGINT UNSIGNED NOT NULL,
  problem_id BIGINT UNSIGNED NOT NULL,
  last_submission_id BIGINT UNSIGNED NULL,
  wrong_count INT UNSIGNED NOT NULL DEFAULT 1,
  review_status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
  retry_bonus_awarded TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (wrong_answer_id),
  UNIQUE KEY uk_wrong_answers_user_problem (user_id, problem_id),
  KEY idx_wrong_answers_set_attempt (set_attempt_id),
  KEY idx_wrong_answers_problem (problem_id),
  KEY idx_wrong_answers_last_submission (last_submission_id),
  KEY idx_wrong_answers_review_status (review_status),
  CONSTRAINT fk_wrong_answers_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_wrong_answers_set_attempt FOREIGN KEY (set_attempt_id) REFERENCES practice_set_attempts (set_attempt_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_wrong_answers_problem FOREIGN KEY (problem_id) REFERENCES practice_problems (problem_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_wrong_answers_last_submission FOREIGN KEY (last_submission_id) REFERENCES practice_submissions (submission_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE score_events (
  score_event_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,
  source_type VARCHAR(30) NOT NULL,
  source_id BIGINT UNSIGNED NOT NULL,
  score_delta INT NOT NULL,
  reason_code VARCHAR(30) NOT NULL,
  idempotency_key VARCHAR(191) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (score_event_id),
  UNIQUE KEY uk_score_events_idempotency_key (idempotency_key),
  KEY idx_score_events_user_subject (user_id, subject_id),
  KEY idx_score_events_source (source_type, source_id),
  CONSTRAINT fk_score_events_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_score_events_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ranking_scores (
  ranking_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED NULL,
  scope_type VARCHAR(30) NOT NULL,
  scope_key VARCHAR(100) NOT NULL,
  period_type VARCHAR(30) NOT NULL,
  period_key VARCHAR(30) NOT NULL,
  score INT UNSIGNED NOT NULL DEFAULT 0,
  rank_no INT UNSIGNED NULL,
  calculated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (ranking_id),
  UNIQUE KEY uk_ranking_scores_scope_user (scope_type, scope_key, period_type, period_key, user_id),
  KEY idx_ranking_scores_user (user_id),
  KEY idx_ranking_scores_subject (subject_id),
  KEY idx_ranking_scores_rank (scope_type, scope_key, period_type, period_key, rank_no),
  CONSTRAINT fk_ranking_scores_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_ranking_scores_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE exam_sessions (
  exam_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,
  level_code VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
  active_session_key VARCHAR(128) GENERATED ALWAYS AS (
    CASE
      WHEN status IN ('CREATED', 'READY') THEN CONCAT(user_id, ':', subject_id, ':', level_code)
      ELSE NULL
    END
  ) VIRTUAL,
  result_status VARCHAR(30) NULL,
  total_problem_count TINYINT UNSIGNED NOT NULL DEFAULT 3,
  correct_count TINYINT UNSIGNED NOT NULL DEFAULT 0,
  retry_count TINYINT UNSIGNED NOT NULL DEFAULT 0,
  started_at DATETIME NULL,
  submitted_at DATETIME NULL,
  graded_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (exam_id),
  UNIQUE KEY uk_exam_sessions_active (active_session_key),
  KEY idx_exam_sessions_user_subject_level (user_id, subject_id, level_code),
  KEY idx_exam_sessions_status (status),
  KEY idx_exam_sessions_result_status (result_status),
  CONSTRAINT fk_exam_sessions_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_exam_sessions_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_level_unlocks (
  unlock_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,
  level_code VARCHAR(30) NOT NULL,
  unlock_source VARCHAR(30) NOT NULL,
  unlocked_by_exam_id BIGINT UNSIGNED NULL,
  unlocked_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (unlock_id),
  UNIQUE KEY uk_user_level_unlock (user_id, subject_id, level_code),
  KEY idx_unlock_user_subject (user_id, subject_id),
  KEY idx_unlock_exam (unlocked_by_exam_id),
  CONSTRAINT fk_user_level_unlocks_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_user_level_unlocks_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_user_level_unlocks_exam FOREIGN KEY (unlocked_by_exam_id) REFERENCES exam_sessions (exam_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ai_exam_problems (
  ai_problem_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  exam_id BIGINT UNSIGNED NOT NULL,
  problem_no TINYINT UNSIGNED NOT NULL,
  prompt TEXT NOT NULL,
  test_case_spec JSON NULL,
  ai_raw_response JSON NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'GENERATED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (ai_problem_id),
  UNIQUE KEY uk_ai_exam_problem_exam_no (exam_id, problem_no),
  KEY idx_ai_exam_problems_status (status),
  CONSTRAINT fk_ai_exam_problems_exam FOREIGN KEY (exam_id) REFERENCES exam_sessions (exam_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE exam_answers (
  answer_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  exam_id BIGINT UNSIGNED NOT NULL,
  ai_problem_id BIGINT UNSIGNED NOT NULL,
  answer_text MEDIUMTEXT NOT NULL,
  passed_case_count INT UNSIGNED NOT NULL DEFAULT 0,
  is_correct TINYINT(1) NOT NULL DEFAULT 0,
  ai_review TEXT NULL,
  test_case_result JSON NULL,
  submitted_at DATETIME NULL,
  graded_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (answer_id),
  UNIQUE KEY uk_exam_answers_exam_problem (exam_id, ai_problem_id),
  KEY idx_exam_answers_ai_problem (ai_problem_id),
  CONSTRAINT fk_exam_answers_exam FOREIGN KEY (exam_id) REFERENCES exam_sessions (exam_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_exam_answers_ai_problem FOREIGN KEY (ai_problem_id) REFERENCES ai_exam_problems (ai_problem_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ai_analysis_reports (
  report_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  exam_id BIGINT UNSIGNED NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  free_summary TEXT NULL,
  premium_detail JSON NULL,
  analysis_error_code VARCHAR(100) NULL,
  retry_count TINYINT UNSIGNED NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (report_id),
  UNIQUE KEY uk_ai_analysis_reports_user_exam (user_id, exam_id),
  KEY idx_ai_analysis_reports_user (user_id),
  KEY idx_ai_analysis_reports_exam (exam_id),
  KEY idx_ai_analysis_reports_status (status),
  KEY idx_ai_analysis_reports_refund_eligibility (user_id, status),
  CONSTRAINT fk_ai_analysis_reports_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_ai_analysis_reports_exam FOREIGN KEY (exam_id) REFERENCES exam_sessions (exam_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ai_request_logs (
  ai_request_log_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  target_type VARCHAR(30) NOT NULL,
  target_id BIGINT UNSIGNED NOT NULL,
  request_type VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL,
  retry_no TINYINT UNSIGNED NOT NULL DEFAULT 0,
  request_payload JSON NULL,
  response_payload JSON NULL,
  error_code VARCHAR(100) NULL,
  error_message VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (ai_request_log_id),
  KEY idx_ai_request_logs_target (target_type, target_id),
  KEY idx_ai_request_logs_status (status),
  KEY idx_ai_request_logs_request_type (request_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE community_posts (
  post_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  writer_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,
  board_type VARCHAR(30) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  view_count INT UNSIGNED NOT NULL DEFAULT 0,
  like_count INT UNSIGNED NOT NULL DEFAULT 0,
  comment_count INT UNSIGNED NOT NULL DEFAULT 0,
  scrap_count INT UNSIGNED NOT NULL DEFAULT 0,
  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  deleted_at DATETIME NULL,
  PRIMARY KEY (post_id),
  KEY idx_community_posts_writer (writer_id),
  KEY idx_community_posts_subject_board_status (subject_id, board_type, status),
  KEY idx_community_posts_created_at (created_at),
  CONSTRAINT fk_community_posts_writer FOREIGN KEY (writer_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_community_posts_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE post_attachments (
  attachment_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id BIGINT UNSIGNED NOT NULL,
  uploader_id BIGINT UNSIGNED NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  stored_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(500) NOT NULL,
  file_size BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (attachment_id),
  UNIQUE KEY uk_post_attachments_stored_name (stored_name),
  KEY idx_post_attachments_post (post_id),
  KEY idx_post_attachments_uploader (uploader_id),
  CONSTRAINT fk_post_attachments_post FOREIGN KEY (post_id) REFERENCES community_posts (post_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_post_attachments_uploader FOREIGN KEY (uploader_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE comments (
  comment_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id BIGINT UNSIGNED NOT NULL,
  parent_comment_id BIGINT UNSIGNED NULL,
  writer_id BIGINT UNSIGNED NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  deleted_at DATETIME NULL,
  deleted_by_admin_id BIGINT NULL COMMENT '관리자 삭제 처리자 ID',
  PRIMARY KEY (comment_id),
  KEY idx_comments_post (post_id),
  KEY idx_comments_parent (parent_comment_id),
  KEY idx_comments_writer (writer_id),
  CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES community_posts (post_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments (comment_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_comments_writer FOREIGN KEY (writer_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE post_likes (
  like_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (like_id),
  UNIQUE KEY uk_post_likes_post_user (post_id, user_id),
  KEY idx_post_likes_user (user_id),
  CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES community_posts (post_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE post_scraps (
  scrap_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (scrap_id),
  UNIQUE KEY uk_post_scraps_post_user (post_id, user_id),
  KEY idx_post_scraps_user (user_id),
  CONSTRAINT fk_post_scraps_post FOREIGN KEY (post_id) REFERENCES community_posts (post_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_post_scraps_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reports (
  report_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  target_type VARCHAR(30) NOT NULL,
  target_id BIGINT UNSIGNED NOT NULL,
  reporter_id BIGINT UNSIGNED NOT NULL,
  reason_code VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
  handled_by BIGINT UNSIGNED NULL,
  handled_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (report_id),
  KEY idx_reports_target (target_type, target_id),
  KEY idx_reports_reporter (reporter_id),
  KEY idx_reports_handled_by (handled_by),
  KEY idx_reports_status (status),
  CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_reports_handled_by FOREIGN KEY (handled_by) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE dummy_payments (
  payment_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(100) NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  product_id BIGINT UNSIGNED NOT NULL,
  payment_method VARCHAR(30) NOT NULL DEFAULT 'DUMMY',
  payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  pg_provider VARCHAR(30) NULL,
  pg_transaction_id VARCHAR(200) NULL,
  amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  paid_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (payment_id),
  UNIQUE KEY uk_dummy_payments_order_no (order_no),
  UNIQUE KEY uk_dummy_payments_pg_transaction (pg_provider, pg_transaction_id),
  KEY idx_dummy_payments_user (user_id),
  KEY idx_dummy_payments_product (product_id),
  KEY idx_dummy_payments_status (payment_status),
  CONSTRAINT fk_dummy_payments_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_dummy_payments_product FOREIGN KEY (product_id) REFERENCES payment_products (product_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE premium_grants (
  grant_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  payment_id BIGINT UNSIGNED NULL,
  grant_type VARCHAR(30) NOT NULL DEFAULT 'LIFETIME',
  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  granted_at DATETIME NOT NULL,
  expires_at DATETIME NULL,
  revoked_at DATETIME NULL,
  revoke_reason VARCHAR(200) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (grant_id),
  UNIQUE KEY uk_premium_grants_payment_id (payment_id),
  KEY idx_premium_grants_user_status (user_id, status),
  CONSTRAINT fk_premium_grants_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_premium_grants_payment FOREIGN KEY (payment_id) REFERENCES dummy_payments (payment_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE payment_refunds (
  refund_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  payment_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  refund_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  refund_amount DECIMAL(10,2) NOT NULL,
  refund_reason VARCHAR(200) NOT NULL,
  pg_provider VARCHAR(30) NOT NULL,
  pg_refund_transaction_id VARCHAR(200) NULL,
  requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME NULL,
  failed_at DATETIME NULL,
  failure_code VARCHAR(100) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (refund_id),
  UNIQUE KEY uk_payment_refunds_payment (payment_id),
  KEY idx_payment_refunds_user_status (user_id, refund_status),
  KEY idx_payment_refunds_provider_transaction (pg_provider, pg_refund_transaction_id),
  CONSTRAINT fk_payment_refunds_payment FOREIGN KEY (payment_id) REFERENCES dummy_payments (payment_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_payment_refunds_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE notices (
  notice_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  writer_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(200) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
  published_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (notice_id),
  KEY idx_notices_writer (writer_id),
  KEY idx_notices_status_published (status, published_at),
  CONSTRAINT fk_notices_writer FOREIGN KEY (writer_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE admin_operation_logs (
  log_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  admin_id BIGINT UNSIGNED NOT NULL,
  action_type VARCHAR(50) NOT NULL,
  target_type VARCHAR(30) NOT NULL,
  target_id BIGINT UNSIGNED NULL,
  target_name VARCHAR(255) NULL,
  change_detail VARCHAR(500) NULL,
  result_status VARCHAR(30) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (log_id),
  KEY idx_admin_operation_logs_admin (admin_id),
  KEY idx_admin_operation_logs_target (target_type, target_id),
  KEY idx_admin_operation_logs_action_type (action_type),
  KEY idx_admin_operation_logs_created_at (created_at),
  CONSTRAINT fk_admin_operation_logs_admin FOREIGN KEY (admin_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE content_recommendations (
  content_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  subject_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(200) NOT NULL,
  url VARCHAR(500) NOT NULL,
  content_type VARCHAR(30) NOT NULL,
  recommendation_slot VARCHAR(30) NOT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (content_id),
  KEY idx_content_recommendations_subject_slot (subject_id, recommendation_slot),
  KEY idx_content_recommendations_active (is_active),
  CONSTRAINT fk_content_recommendations_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Seed data: DB 명세 확정 과목 seed
INSERT INTO subjects (subject_code, subject_name, description, sort_order, is_active)
VALUES
  ('JAVA', 'Java', 'Java programming', 1, 1),
  ('PYTHON', 'Python', 'Python programming', 2, 1),
  ('WEB', 'Web', 'HTML/CSS/JavaScript', 3, 1),
  ('SQL', 'SQL', 'SQL database learning', 4, 1)
ON DUPLICATE KEY UPDATE
  subject_name = VALUES(subject_name),
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  is_active = VALUES(is_active),
  updated_at = CURRENT_TIMESTAMP;

-- Seed data: MVP dummy Premium product
INSERT INTO payment_products (product_code, product_name, price, is_active)
VALUES ('PREMIUM_LIFETIME', 'Premium Lifetime', 9900.00, 1)
ON DUPLICATE KEY UPDATE
  product_name = VALUES(product_name),
  price = VALUES(price),
  is_active = VALUES(is_active),
  updated_at = CURRENT_TIMESTAMP;

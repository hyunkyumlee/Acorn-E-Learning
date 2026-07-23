/*
  Knowva demo setup data v2.5.7 - MySQL 8 / InnoDB / utf8mb4
  File: docs/ddl/Knowva_demo_setup_data.sql
  Compatibility: Knowva_DDL.sql / Notion DB 명세 v2.4
  Source: 레슨 단위 학습 구조 / Premium 환불 / 비밀번호 재설정 / 관리자 댓글 삭제 주체
  Execute after docs/ddl/Knowva_DDL.sql.

  Revision history
  - v2.5.7 (2026-07-23): 시연 계정(user_id=6)의 Silver 진도에 맞춰 레슨 문제 풀이 3회(30문항), 오답 2건, 채점 완료 AI 코딩테스트 2회와 분석 리포트를 추가. 분석 대시보드가 실제 풀이 이력을 표시한다.
  - v2.5.6 (2026-07-22): ranking_seed_data.sql의 20개 랭킹 데모 계정·학습 활동·WEEKLY/MONTHLY 집계 데이터를 본 셋업에 통합. 별도 랭킹 seed 실행은 필요 없음.
  - v2.5.5 (2026-07-21): 일반·Premium 계정은 Knowva_sample_data.sql 원본 진행·시험·결제 데이터를 보존하도록 final reconciliation 정리 범위에서 제외.
  - v2.5.4 (2026-07-21): seed 실행 세션에서만 sql_safe_updates를 일시 해제하고 COMMIT 뒤 원래 값으로 복구해 optimizer의 full scan 선택에도 전체 실행을 보장.
  - v2.5.3 (2026-07-21): UNSIGNED primary key의 항상 참인 `>= 0` 조건을 `> 0`으로 보정해 Workbench safe update mode 인식을 보장.
  - v2.5.2 (2026-07-21): Workbench safe update mode에서도 실행되도록 fixture 정리 UPDATE/DELETE의 WHERE에 primary key 조건을 추가.
  - v2.5.1 (2026-07-21): 시연 사용자 행을 유지한 뒤 계정 설정을 upsert하도록 fixture 사용자 삭제 순서를 보정.
  - v2.5 (2026-07-21): 시연 계정 2개(admin, 사용자)로 정리하고 사용자 계정의 Java Silver 마지막 행성 9번 레슨까지 해금 상태를 반영.

  Execution contract
  - This is sample seed data, not a schema migration.
  - Run the full Knowva_DDL.sql first when resetting a local development DB.
  - Ranking demo data is included: do not run ranking_seed_data.sql separately after this file.
  - A schema preflight runs before the first INSERT. If it fails, update the schema first.
  - All seed INSERT/UPDATE statements run in one transaction to prevent partial application.

  Demo login accounts
  - admin account (ROLE_ADMIN)
  - general learner account (ROLE_USER, Java BRONZE)
  - premium learner account (ROLE_USER, all subjects GOLD / Premium)
  - demo user account (ROLE_USER, Java SILVER)
  - Login credentials are managed only by this setup file's final account fixture.
  - OAuth sample accounts are deleted before COMMIT; local test accounts are rebuilt in the final account fixture.

  Password policy
  - BCrypt(10), generated with Spring Security Crypto 7.0.5.
  - Do not copy plain passwords into documentation or change history.
*/

SET NAMES utf8mb4;
SET time_zone = '+09:00';

USE elearning;

-- Workbench safe update mode can reject a mutation when the optimizer chooses
-- a full scan despite a primary key predicate. Disable it only for this seed
-- session and restore the caller's value after COMMIT.
SET @knowva_seed_sql_safe_updates = @@SESSION.sql_safe_updates;
SET SESSION sql_safe_updates = 0;

DROP PROCEDURE IF EXISTS assert_knowva_seed_schema;
DELIMITER $$
CREATE PROCEDURE assert_knowva_seed_schema()
BEGIN
  DECLARE required_column_count INT DEFAULT 0;
  DECLARE pending_status_default_count INT DEFAULT 0;
  DECLARE refund_eligibility_index_count INT DEFAULT 0;


  SELECT COUNT(*)
    INTO required_column_count
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND (table_name, column_name) IN (
      ('community_posts', 'view_count'),
      ('user_credentials', 'login_email'),
      ('user_credentials', 'email_verified_at'),
      ('password_reset_tokens', 'token_id'),
      ('social_accounts', 'provider_email_verified'),
      ('lessons', 'node_id'),
      ('practice_set_attempts', 'lesson_id'),
      ('practice_set_items', 'set_item_id'),
      ('subject_content_status_backups', 'backup_id'),
      ('dummy_payments', 'pg_provider'),
      ('dummy_payments', 'pg_transaction_id'),
      ('payment_refunds', 'refund_id'),
      ('premium_grants', 'revoked_at'),
      ('premium_grants', 'revoke_reason'),
      ('user_subject_enrollments', 'enrollment_id'),
      ('user_subject_enrollments', 'status'),
      ('user_subject_enrollments', 'start_mode'),
      ('admin_operation_logs', 'target_name'),
      ('admin_operation_logs', 'change_detail'),
      ('comments', 'deleted_by_admin_id')
    );

  SELECT COUNT(*)
    INTO pending_status_default_count
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dummy_payments'
    AND column_name = 'payment_status'
    AND column_default = 'PENDING';

  SELECT COUNT(*)
    INTO refund_eligibility_index_count
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'ai_analysis_reports'
    AND index_name = 'idx_ai_analysis_reports_refund_eligibility';

  IF required_column_count <> 20
      OR pending_status_default_count <> 1
      OR refund_eligibility_index_count <> 2 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Knowva sample data requires the current Knowva_DDL.sql. Run Knowva_DDL.sql first.';
  END IF;
END$$
DELIMITER ;

CALL assert_knowva_seed_schema();
DROP PROCEDURE assert_knowva_seed_schema;

START TRANSACTION;

SET @sample_password_hash = '$2a$10$rXWwp4H7mIiDJv.c2H9moOtZ3m/8cBMGOsMuaX0G7vW/A1W3tPCxy';

DELETE FROM subject_content_status_backups WHERE backup_id > 0;

DELETE FROM wrong_answers WHERE set_attempt_id IN (100101, 200501) AND wrong_answer_id > 0;
DELETE FROM practice_submissions WHERE set_attempt_id IN (100101, 200501) AND submission_id > 0;
DELETE FROM practice_set_items WHERE set_attempt_id IN (100101, 200501) AND set_item_id > 0;
DELETE FROM practice_set_attempts WHERE set_attempt_id IN (100101, 200501) AND set_attempt_id > 0;

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

INSERT INTO payment_products (product_code, product_name, price, is_active)
VALUES ('PREMIUM_LIFETIME', 'Premium Lifetime', 9900.00, 1)
ON DUPLICATE KEY UPDATE
  product_name = VALUES(product_name),
  price = VALUES(price),
  is_active = VALUES(is_active),
  updated_at = CURRENT_TIMESTAMP;

SELECT subject_id INTO @java_subject_id FROM subjects WHERE subject_code = 'JAVA';
SELECT subject_id INTO @python_subject_id FROM subjects WHERE subject_code = 'PYTHON';
SELECT subject_id INTO @web_subject_id FROM subjects WHERE subject_code = 'WEB';
SELECT subject_id INTO @sql_subject_id FROM subjects WHERE subject_code = 'SQL';
SELECT product_id INTO @premium_product_id FROM payment_products WHERE product_code = 'PREMIUM_LIFETIME';

INSERT INTO users (
  user_id, email, nickname, role, status, profile_image_url, last_login_at, withdrawn_at, created_at, updated_at
)
VALUES
  (1, 'admin@knowva.local', '관리자', 'ROLE_ADMIN', 'ACTIVE', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'learner@knowva.local', '누비학습자', 'ROLE_USER', 'ACTIVE', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'premium@knowva.local', '프리미엄학습자', 'ROLE_USER', 'ACTIVE', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 'learner@knowva.local', '구글학습자', 'ROLE_USER', 'ACTIVE', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 'learner@knowva.local', '깃허브학습자', 'ROLE_USER', 'ACTIVE', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  email = VALUES(email),
  nickname = VALUES(nickname),
  role = VALUES(role),
  status = VALUES(status),
  profile_image_url = VALUES(profile_image_url),
  last_login_at = VALUES(last_login_at),
  withdrawn_at = VALUES(withdrawn_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_credentials (
  credential_id, user_id, login_email, password_hash, email_verified_at, password_updated_at, failed_login_count, locked_until, created_at, updated_at
)
VALUES
  (1, 1, 'admin@knowva.local', @sample_password_hash, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 2, 'learner@knowva.local', @sample_password_hash, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 3, 'premium@knowva.local', @sample_password_hash, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  login_email = VALUES(login_email),
  password_hash = VALUES(password_hash),
  email_verified_at = VALUES(email_verified_at),
  password_updated_at = VALUES(password_updated_at),
  failed_login_count = VALUES(failed_login_count),
  locked_until = VALUES(locked_until),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO social_accounts (
  social_account_id, user_id, provider, provider_user_id, provider_email, provider_email_verified, is_active, connected_at, disconnected_at
)
VALUES
  (1, 4, 'google', 'sample-google-learner-001', 'learner@knowva.local', 1, 1, CURRENT_TIMESTAMP, NULL),
  (2, 5, 'github', 'sample-github-learner-001', 'learner@knowva.local', 1, 1, CURRENT_TIMESTAMP, NULL)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  provider = VALUES(provider),
  provider_user_id = VALUES(provider_user_id),
  provider_email = VALUES(provider_email),
  provider_email_verified = VALUES(provider_email_verified),
  is_active = VALUES(is_active),
  connected_at = VALUES(connected_at),
  disconnected_at = VALUES(disconnected_at);

INSERT INTO user_settings (
  setting_id, user_id, theme, notification_enabled, accessibility_mode, reduced_motion_enabled, created_at, updated_at
)
VALUES
  (1, 1, 'SYSTEM', 1, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 2, 'LIGHT', 1, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 3, 'DARK', 1, 'HIGH_CONTRAST', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 4, 'SYSTEM', 1, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 5, 'SYSTEM', 1, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  theme = VALUES(theme),
  notification_enabled = VALUES(notification_enabled),
  accessibility_mode = VALUES(accessibility_mode),
  reduced_motion_enabled = VALUES(reduced_motion_enabled),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_learning_profiles (
  profile_id, user_id, primary_subject_id, learning_goal, current_level_code, total_score, grade_code, created_at, updated_at
)
VALUES
  (1, 1, @java_subject_id, '관리자 검수용 계정입니다.', 'GOLD', 0, 'ADVANCED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 2, @java_subject_id, 'Java 기초 문법을 꾸준히 학습합니다.', 'BRONZE', 230, 'BEGINNER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 3, @java_subject_id, 'AI 코딩테스트와 Premium 분석을 확인합니다.', 'GOLD', 1870, 'ADVANCED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 4, @java_subject_id, 'Google OAuth 가입 계정입니다. 이메일 계정과 같은 이메일이어도 별도 계정입니다.', 'BRONZE', 0, 'BEGINNER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 5, @java_subject_id, 'GitHub OAuth 가입 계정입니다. 이메일 계정과 같은 이메일이어도 별도 계정입니다.', 'BRONZE', 0, 'BEGINNER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  primary_subject_id = VALUES(primary_subject_id),
  learning_goal = VALUES(learning_goal),
  current_level_code = VALUES(current_level_code),
  total_score = VALUES(total_score),
  grade_code = VALUES(grade_code),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO curriculum_nodes (
  node_id, subject_id, parent_node_id, level_code, node_type, planet_no, title, description, sort_order, gate_condition, is_active, created_at, updated_at
)
VALUES
  (1, @java_subject_id, NULL, 'BRONZE', 'PLANET', 1, 'Java 변수와 자료형 행성', '변수, 자료형, 출력문을 학습합니다.', 1, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, @java_subject_id, NULL, 'BRONZE', 'PLANET', 2, 'Java 조건문 행성', 'if, else, switch 흐름을 학습합니다.', 2, 'planet 1 complete', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, @java_subject_id, NULL, 'BRONZE', 'PLANET', 3, 'Java 반복문 행성', 'for, while 반복 흐름을 학습합니다.', 3, 'planet 2 complete', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, @java_subject_id, NULL, 'BRONZE', 'PLANET', 4, 'Java 배열 행성', '배열 선언과 순회를 학습합니다.', 4, 'planet 3 complete', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, @java_subject_id, NULL, 'BRONZE', 'PLANET', 5, 'Java 메서드 행성', '메서드 분리와 반환값을 학습합니다.', 5, 'planet 4 complete', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, @java_subject_id, NULL, 'BRONZE', 'GATE', NULL, 'Bronze AI 코딩테스트 Gate', '5개 planet 완료 후 응시하는 AI 코딩테스트입니다.', 6, '5 planets complete', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  subject_id = VALUES(subject_id),
  parent_node_id = VALUES(parent_node_id),
  level_code = VALUES(level_code),
  node_type = VALUES(node_type),
  planet_no = VALUES(planet_no),
  title = VALUES(title),
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  gate_condition = VALUES(gate_condition),
  is_active = VALUES(is_active),
  updated_at = CURRENT_TIMESTAMP;

/*
  Learning seed coverage
  - Existing Java Bronze rows above are kept as the hand-written base example.
  - The generated rows below fill every active subject and every roadmap level
    so subject/level switching never lands on an empty curriculum.
  - Node id ranges: JAVA 1-26, PYTHON 101-126, WEB 201-226, SQL 301-326.
*/
INSERT INTO curriculum_nodes (
  node_id, subject_id, parent_node_id, level_code, node_type, planet_no, title, description, sort_order, gate_condition, is_active, created_at, updated_at
)
SELECT
  seed_subject.node_base + seed_level.node_offset + seed_step.sort_order AS node_id,
  seed_subject.subject_id,
  NULL AS parent_node_id,
  seed_level.level_code,
  IF(seed_step.sort_order = 6, 'GATE', 'PLANET') AS node_type,
  IF(seed_step.sort_order = 6, NULL, seed_step.sort_order) AS planet_no,
  IF(
    seed_step.sort_order = 6,
    CONCAT(seed_subject.subject_name, ' ', seed_level.level_label, ' AI 코딩테스트 Gate'),
    CONCAT(seed_subject.subject_name, ' ', seed_level.level_label, ' ', seed_step.topic, ' 행성')
  ) AS title,
  IF(
    seed_step.sort_order = 6,
    '5개 planet 완료 후 응시하는 AI 코딩테스트입니다.',
    CONCAT(seed_step.description, ' 학습합니다.')
  ) AS description,
  seed_step.sort_order,
  CASE
    WHEN seed_step.sort_order = 1 THEN NULL
    WHEN seed_step.sort_order = 6 THEN '5 planets complete'
    ELSE CONCAT('planet ', seed_step.sort_order - 1, ' complete')
  END AS gate_condition,
  1 AS is_active,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM (
  SELECT @java_subject_id AS subject_id, 'JAVA' AS subject_code, 'Java' AS subject_name, 0 AS node_base
  UNION ALL SELECT @python_subject_id, 'PYTHON', 'Python', 100
  UNION ALL SELECT @web_subject_id, 'WEB', 'Web', 200
  UNION ALL SELECT @sql_subject_id, 'SQL', 'SQL', 300
) seed_subject
CROSS JOIN (
  SELECT 'BRONZE' AS level_code, 'Bronze' AS level_label, 0 AS node_offset
  UNION ALL SELECT 'SILVER', 'Silver', 10
  UNION ALL SELECT 'GOLD', 'Gold', 20
) seed_level
JOIN (
  SELECT 'BRONZE' AS level_code, 1 AS sort_order, '기초 문법' AS topic, '기초 문법과 실행 흐름을' AS description
  UNION ALL SELECT 'BRONZE', 2, '조건 분기', '조건에 따라 다른 처리를 하는 방법을'
  UNION ALL SELECT 'BRONZE', 3, '반복 처리', '반복문과 반복 조건을'
  UNION ALL SELECT 'BRONZE', 4, '자료 구조', '기초 자료 구조와 값 접근을'
  UNION ALL SELECT 'BRONZE', 5, '함수와 모듈', '기능을 함수와 모듈로 나누는 방법을'
  UNION ALL SELECT 'BRONZE', 6, 'Gate', 'Bronze Gate를'
  UNION ALL SELECT 'SILVER', 1, '자료구조 활용', '자료구조를 목적에 맞게 활용하는 방법을'
  UNION ALL SELECT 'SILVER', 2, '예외와 검증', '예외 상황과 입력 검증 흐름을'
  UNION ALL SELECT 'SILVER', 3, '입출력 처리', '입력과 출력 데이터를 다루는 방법을'
  UNION ALL SELECT 'SILVER', 4, '모듈화 설계', '역할별로 코드를 나누는 설계를'
  UNION ALL SELECT 'SILVER', 5, '테스트와 디버깅', '작은 단위로 검증하고 오류를 찾는 방법을'
  UNION ALL SELECT 'SILVER', 6, 'Gate', 'Silver Gate를'
  UNION ALL SELECT 'GOLD', 1, '성능 최적화', '시간과 메모리를 고려한 구현을'
  UNION ALL SELECT 'GOLD', 2, '설계 패턴', '반복되는 설계 문제를 해결하는 패턴을'
  UNION ALL SELECT 'GOLD', 3, '동시성과 비동기', '동시 실행과 비동기 처리 흐름을'
  UNION ALL SELECT 'GOLD', 4, '보안과 안정성', '입력 신뢰성과 안전한 처리 기준을'
  UNION ALL SELECT 'GOLD', 5, '실전 프로젝트', '실전 기능을 작은 단위로 완성하는 방법을'
  UNION ALL SELECT 'GOLD', 6, 'Gate', 'Gold Gate를'
) seed_step
  ON seed_step.level_code = seed_level.level_code
WHERE NOT (seed_subject.subject_code = 'JAVA' AND seed_level.level_code = 'BRONZE')
ON DUPLICATE KEY UPDATE
  subject_id = VALUES(subject_id),
  parent_node_id = VALUES(parent_node_id),
  level_code = VALUES(level_code),
  node_type = VALUES(node_type),
  planet_no = VALUES(planet_no),
  title = VALUES(title),
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  gate_condition = VALUES(gate_condition),
  is_active = VALUES(is_active),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO lessons (
  lesson_id, node_id, title, summary, content, example_code, sort_order, is_active, required_for_completion, created_by, created_at, updated_at
)
SELECT
  (node.node_id * 100) + lesson_step.lesson_no AS lesson_id,
  node.node_id,
  CONCAT(REPLACE(node.title, ' 행성', ''), ' ', LPAD(lesson_step.lesson_no, 2, '0'), ' - ', lesson_step.topic) AS title,
  CONCAT(node.description, ' ', lesson_step.topic, ' 레슨입니다.') AS summary,
  CONCAT(node.title, '의 ', lesson_step.topic, ' 개념을 짧게 학습하고 레슨별 10문제로 확인합니다.') AS content,
  CASE subject.subject_code
    WHEN 'JAVA' THEN 'int value = 10;\nSystem.out.println(value);'
    WHEN 'PYTHON' THEN 'value = 10\nprint(value)'
    WHEN 'WEB' THEN '<button class="primary">Start</button>'
    WHEN 'SQL' THEN 'SELECT title\nFROM lessons\nWHERE is_active = 1;'
    ELSE 'System.out.println("Knowva");'
  END AS example_code,
  lesson_step.lesson_no AS sort_order,
  1 AS is_active,
  1 AS required_for_completion,
  1 AS created_by,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM curriculum_nodes node
JOIN subjects subject ON subject.subject_id = node.subject_id
CROSS JOIN (
  SELECT 1 AS lesson_no, '핵심 개념' AS topic
  UNION ALL SELECT 2, '기본 문법'
  UNION ALL SELECT 3, '실행 흐름'
  UNION ALL SELECT 4, '예제 분석'
  UNION ALL SELECT 5, '조건과 분기'
  UNION ALL SELECT 6, '반복과 누적'
  UNION ALL SELECT 7, '자료 다루기'
  UNION ALL SELECT 8, '함수화'
  UNION ALL SELECT 9, '오류 점검'
  UNION ALL SELECT 10, '종합 연습'
) lesson_step
WHERE node.node_type = 'PLANET'
  AND node.is_active = 1
ON DUPLICATE KEY UPDATE
  node_id = VALUES(node_id),
  title = VALUES(title),
  summary = VALUES(summary),
  content = VALUES(content),
  example_code = VALUES(example_code),
  sort_order = VALUES(sort_order),
  is_active = VALUES(is_active),
  required_for_completion = VALUES(required_for_completion),
  created_by = VALUES(created_by),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO lesson_bookmarks (bookmark_id, user_id, lesson_id, created_at)
VALUES
  (1, 2, 101, CURRENT_TIMESTAMP),
  (2, 3, 303, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  lesson_id = VALUES(lesson_id);

INSERT INTO level_test_questions (
  question_id, subject_id, question_no, question_text, question_type, explanation, difficulty_code, is_active, created_at, updated_at
)
VALUES
  (1, @java_subject_id, 1, 'Java에서 정수를 저장하는 대표 기본형은 무엇인가요?', 'MULTIPLE_CHOICE', 'int는 정수 값을 저장하는 기본형입니다.', 'BRONZE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, @java_subject_id, 2, 'System.out.println의 역할은 무엇인가요?', 'MULTIPLE_CHOICE', '콘솔에 값을 출력하고 줄바꿈합니다.', 'BRONZE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, @java_subject_id, 3, 'if 문의 조건식 결과 타입은 무엇인가요?', 'MULTIPLE_CHOICE', '조건식은 boolean 결과를 가져야 합니다.', 'BRONZE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, @java_subject_id, 4, 'for 문에서 반복 횟수를 제어하는 구성요소가 아닌 것은 무엇인가요?', 'MULTIPLE_CHOICE', 'for 문은 초기식, 조건식, 증감식을 사용합니다.', 'SILVER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, @java_subject_id, 5, '배열의 첫 번째 index는 무엇인가요?', 'MULTIPLE_CHOICE', 'Java 배열 index는 0부터 시작합니다.', 'SILVER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, @java_subject_id, 6, '메서드가 값을 돌려줄 때 사용하는 keyword는 무엇인가요?', 'MULTIPLE_CHOICE', 'return은 메서드 결과를 반환합니다.', 'SILVER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (7, @java_subject_id, 7, '문자열 비교에 권장되는 메서드는 무엇인가요?', 'MULTIPLE_CHOICE', 'String 값 비교는 equals를 사용합니다.', 'GOLD', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (8, @java_subject_id, 8, '객체 생성에 사용하는 keyword는 무엇인가요?', 'MULTIPLE_CHOICE', 'new는 객체를 생성합니다.', 'GOLD', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  subject_id = VALUES(subject_id),
  question_no = VALUES(question_no),
  question_text = VALUES(question_text),
  question_type = VALUES(question_type),
  explanation = VALUES(explanation),
  difficulty_code = VALUES(difficulty_code),
  is_active = VALUES(is_active),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO level_test_choices (choice_id, question_id, choice_label, choice_text, is_correct, sort_order)
VALUES
  (1, 1, 'A', 'int', 1, 1), (2, 1, 'B', 'String', 0, 2), (3, 1, 'C', 'boolean', 0, 3), (4, 1, 'D', 'class', 0, 4),
  (5, 2, 'A', '파일 삭제', 0, 1), (6, 2, 'B', '콘솔 출력', 1, 2), (7, 2, 'C', '네트워크 연결', 0, 3), (8, 2, 'D', 'DB 저장', 0, 4),
  (9, 3, 'A', 'String', 0, 1), (10, 3, 'B', 'int', 0, 2), (11, 3, 'C', 'boolean', 1, 3), (12, 3, 'D', 'double', 0, 4),
  (13, 4, 'A', '초기식', 0, 1), (14, 4, 'B', '조건식', 0, 2), (15, 4, 'C', '증감식', 0, 3), (16, 4, 'D', '패키지 선언', 1, 4),
  (17, 5, 'A', '0', 1, 1), (18, 5, 'B', '1', 0, 2), (19, 5, 'C', '-1', 0, 3), (20, 5, 'D', '배열 길이', 0, 4),
  (21, 6, 'A', 'break', 0, 1), (22, 6, 'B', 'continue', 0, 2), (23, 6, 'C', 'return', 1, 3), (24, 6, 'D', 'static', 0, 4),
  (25, 7, 'A', '==', 0, 1), (26, 7, 'B', 'equals', 1, 2), (27, 7, 'C', 'compareToOnly', 0, 3), (28, 7, 'D', 'length', 0, 4),
  (29, 8, 'A', 'class', 0, 1), (30, 8, 'B', 'void', 0, 2), (31, 8, 'C', 'new', 1, 3), (32, 8, 'D', 'final', 0, 4)
ON DUPLICATE KEY UPDATE
  question_id = VALUES(question_id),
  choice_label = VALUES(choice_label),
  choice_text = VALUES(choice_text),
  is_correct = VALUES(is_correct),
  sort_order = VALUES(sort_order);

INSERT INTO level_test_attempts (
  attempt_id, user_id, subject_id, total_count, correct_count, result_level_code, status, submitted_at, created_at
)
VALUES
  (1, 2, @java_subject_id, 8, 3, 'SILVER', 'SUBMITTED', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY)),
  (2, 3, @java_subject_id, 8, 6, 'GOLD', 'SUBMITTED', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY))
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  total_count = VALUES(total_count),
  correct_count = VALUES(correct_count),
  result_level_code = VALUES(result_level_code),
  status = VALUES(status),
  submitted_at = VALUES(submitted_at);

INSERT INTO level_test_answers (answer_id, attempt_id, question_id, choice_id, submitted_answer, is_correct, created_at)
VALUES
  (1, 1, 1, 1, 'int', 1, CURRENT_TIMESTAMP),
  (2, 1, 2, 6, '콘솔 출력', 1, CURRENT_TIMESTAMP),
  (3, 1, 3, 9, 'String', 0, CURRENT_TIMESTAMP),
  (4, 1, 4, 16, '패키지 선언', 1, CURRENT_TIMESTAMP),
  (5, 2, 1, 1, 'int', 1, CURRENT_TIMESTAMP),
  (6, 2, 2, 6, '콘솔 출력', 1, CURRENT_TIMESTAMP),
  (7, 2, 3, 11, 'boolean', 1, CURRENT_TIMESTAMP),
  (8, 2, 4, 16, '패키지 선언', 1, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  attempt_id = VALUES(attempt_id),
  question_id = VALUES(question_id),
  choice_id = VALUES(choice_id),
  submitted_answer = VALUES(submitted_answer),
  is_correct = VALUES(is_correct);

INSERT INTO user_level_unlocks (
  unlock_id, user_id, subject_id, level_code, unlock_source, unlocked_by_exam_id, unlocked_at, created_at
)
VALUES
  (1, 2, @java_subject_id, 'BRONZE', 'ONBOARDING', NULL, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY)),
  (2, 3, @java_subject_id, 'BRONZE', 'ONBOARDING', NULL, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY))
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  level_code = VALUES(level_code),
  unlock_source = VALUES(unlock_source),
  unlocked_by_exam_id = VALUES(unlocked_by_exam_id),
  unlocked_at = VALUES(unlocked_at);

INSERT INTO user_subject_enrollments (
  user_id, subject_id, status, start_mode, enrolled_at
)
SELECT
  profile.user_id,
  profile.primary_subject_id,
  'ACTIVE',
  'BASIC',
  CURRENT_TIMESTAMP
FROM user_learning_profiles profile
WHERE profile.primary_subject_id IS NOT NULL
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_level_unlocks (
  unlock_id, user_id, subject_id, level_code, unlock_source, unlocked_by_exam_id, unlocked_at, created_at
)
SELECT
  1000 + seed_subject.node_base + seed_level.unlock_order AS unlock_id,
  3 AS user_id,
  seed_subject.subject_id,
  seed_level.level_code,
  IF(seed_level.level_code = 'BRONZE', 'ONBOARDING', 'ADMIN_ADJUST') AS unlock_source,
  NULL AS unlocked_by_exam_id,
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL (4 - seed_level.unlock_order) DAY) AS unlocked_at,
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL (4 - seed_level.unlock_order) DAY) AS created_at
FROM (
  SELECT @java_subject_id AS subject_id, 0 AS node_base
  UNION ALL SELECT @python_subject_id, 100
  UNION ALL SELECT @web_subject_id, 200
  UNION ALL SELECT @sql_subject_id, 300
) seed_subject
CROSS JOIN (
  SELECT 'BRONZE' AS level_code, 1 AS unlock_order
  UNION ALL SELECT 'SILVER', 2
  UNION ALL SELECT 'GOLD', 3
) seed_level
WHERE 1 = 1
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  level_code = VALUES(level_code),
  unlock_source = VALUES(unlock_source),
  unlocked_by_exam_id = VALUES(unlocked_by_exam_id),
  unlocked_at = VALUES(unlocked_at);

INSERT INTO user_subject_enrollments (
  user_id, subject_id, status, start_mode, enrolled_at
)
SELECT DISTINCT
  level_unlock.user_id,
  level_unlock.subject_id,
  'ACTIVE',
  'BASIC',
  CURRENT_TIMESTAMP
FROM user_level_unlocks level_unlock
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO learning_progress (
  progress_id, user_id, subject_id, node_id, lesson_completed, practice_passed, progress_rate, completed_at, created_at, updated_at
)
VALUES
  (1, 2, @java_subject_id, 1, 1, 1, 100.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), CURRENT_TIMESTAMP),
  (2, 2, @java_subject_id, 2, 1, 0, 50.00, NULL, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (3, 3, @java_subject_id, 1, 1, 1, 100.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), CURRENT_TIMESTAMP),
  (4, 3, @java_subject_id, 2, 1, 1, 100.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), CURRENT_TIMESTAMP),
  (5, 3, @java_subject_id, 3, 1, 1, 100.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (6, 3, @java_subject_id, 4, 1, 1, 100.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (7, 3, @java_subject_id, 5, 1, 1, 100.00, CURRENT_TIMESTAMP, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  node_id = VALUES(node_id),
  lesson_completed = VALUES(lesson_completed),
  practice_passed = VALUES(practice_passed),
  progress_rate = VALUES(progress_rate),
  completed_at = VALUES(completed_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO learning_progress (
  progress_id, user_id, subject_id, node_id, lesson_completed, practice_passed, progress_rate, completed_at, created_at, updated_at
)
SELECT
  10000 + node.node_id AS progress_id,
  3 AS user_id,
  node.subject_id,
  node.node_id,
  1 AS lesson_completed,
  1 AS practice_passed,
  100.00 AS progress_rate,
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) AS completed_at,
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) AS created_at,
  CURRENT_TIMESTAMP AS updated_at
FROM curriculum_nodes node
WHERE node.node_type = 'PLANET'
  AND node.is_active = 1
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  node_id = VALUES(node_id),
  lesson_completed = VALUES(lesson_completed),
  practice_passed = VALUES(practice_passed),
  progress_rate = VALUES(progress_rate),
  completed_at = VALUES(completed_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_lesson_progress (
  lesson_progress_id, user_id, lesson_id, theory_completed, practice_passed, progress_rate, completed_at, created_at, updated_at
)
SELECT
  seed_lesson_progress.lesson_progress_id,
  seed_lesson_progress.user_id,
  seed_lesson_progress.lesson_id,
  seed_lesson_progress.theory_completed,
  seed_lesson_progress.practice_passed,
  seed_lesson_progress.progress_rate,
  seed_lesson_progress.completed_at,
  seed_lesson_progress.created_at,
  seed_lesson_progress.updated_at
FROM (
  SELECT
    100000 + lesson.lesson_id AS lesson_progress_id,
    2 AS user_id,
    lesson.lesson_id,
    1 AS theory_completed,
    1 AS practice_passed,
    100.00 AS progress_rate,
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) AS completed_at,
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) AS created_at,
    CURRENT_TIMESTAMP AS updated_at
  FROM lessons lesson
  WHERE lesson.node_id = 1
    AND lesson.required_for_completion = 1
  UNION ALL
  SELECT
    110000 + lesson.lesson_id AS lesson_progress_id,
    2 AS user_id,
    lesson.lesson_id,
    1 AS theory_completed,
    0 AS practice_passed,
    50.00 AS progress_rate,
    NULL AS completed_at,
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) AS created_at,
    CURRENT_TIMESTAMP AS updated_at
  FROM lessons lesson
  WHERE lesson.node_id = 2
    AND lesson.sort_order <= 10
    AND lesson.required_for_completion = 1
  UNION ALL
  SELECT
    200000 + lesson.lesson_id AS lesson_progress_id,
    3 AS user_id,
    lesson.lesson_id,
    1 AS theory_completed,
    1 AS practice_passed,
    100.00 AS progress_rate,
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) AS completed_at,
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) AS created_at,
    CURRENT_TIMESTAMP AS updated_at
  FROM lessons lesson
  JOIN curriculum_nodes node ON node.node_id = lesson.node_id
  WHERE node.node_type = 'PLANET'
    AND node.is_active = 1
    AND lesson.required_for_completion = 1
) seed_lesson_progress
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  lesson_id = VALUES(lesson_id),
  theory_completed = VALUES(theory_completed),
  practice_passed = VALUES(practice_passed),
  progress_rate = VALUES(progress_rate),
  completed_at = VALUES(completed_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO practice_set_attempts (
  set_attempt_id, user_id, subject_id, node_id, lesson_id, total_count, correct_count, status, passed, completed_at, created_at, updated_at
)
VALUES
  (1, 2, @java_subject_id, 1, 101, 10, 7, 'COMPLETED', 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (2, 3, @java_subject_id, 5, 501, 10, 9, 'COMPLETED', 1, CURRENT_TIMESTAMP, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  node_id = VALUES(node_id),
  lesson_id = VALUES(lesson_id),
  total_count = VALUES(total_count),
  correct_count = VALUES(correct_count),
  status = VALUES(status),
  passed = VALUES(passed),
  completed_at = VALUES(completed_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO practice_set_attempts (
  set_attempt_id, user_id, subject_id, node_id, lesson_id, total_count, correct_count, status, passed, completed_at, created_at, updated_at
)
SELECT
  seed_attempt.set_attempt_id,
  seed_attempt.user_id,
  seed_attempt.subject_id,
  seed_attempt.node_id,
  seed_attempt.lesson_id,
  seed_attempt.total_count,
  seed_attempt.correct_count,
  seed_attempt.status,
  seed_attempt.passed,
  seed_attempt.completed_at,
  seed_attempt.created_at,
  seed_attempt.updated_at
FROM (
  SELECT
    200000 + lesson.lesson_id AS set_attempt_id,
    3 AS user_id,
    node.subject_id,
    node.node_id,
    lesson.lesson_id,
    10 AS total_count,
    10 AS correct_count,
    'COMPLETED' AS status,
    1 AS passed,
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) AS completed_at,
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) AS created_at,
    CURRENT_TIMESTAMP AS updated_at
  FROM curriculum_nodes node
  JOIN lessons lesson ON lesson.node_id = node.node_id
  WHERE node.node_type = 'PLANET'
    AND node.is_active = 1
    AND lesson.required_for_completion = 1
    AND lesson.lesson_id <> 501
  UNION ALL
  SELECT
    100000 + lesson.lesson_id AS set_attempt_id,
    2 AS user_id,
    node.subject_id,
    node.node_id,
    lesson.lesson_id,
    10 AS total_count,
    7 AS correct_count,
    'COMPLETED' AS status,
    1 AS passed,
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) AS completed_at,
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) AS created_at,
    CURRENT_TIMESTAMP AS updated_at
  FROM curriculum_nodes node
  JOIN lessons lesson ON lesson.node_id = node.node_id
  WHERE node.node_id = 1
    AND lesson.required_for_completion = 1
    AND lesson.lesson_id <> 101
) seed_attempt
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  node_id = VALUES(node_id),
  lesson_id = VALUES(lesson_id),
  total_count = VALUES(total_count),
  correct_count = VALUES(correct_count),
  status = VALUES(status),
  passed = VALUES(passed),
  completed_at = VALUES(completed_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO attendance_records (
  attendance_id, user_id, attendance_date, streak_count, qualified_set_attempt_id, created_at, updated_at
)
VALUES
  (1, 2, DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), 1, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (2, 2, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 2, NULL, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), CURRENT_TIMESTAMP),
  (3, 3, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 1, NULL, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), CURRENT_TIMESTAMP),
  (4, 3, CURRENT_DATE, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  attendance_date = VALUES(attendance_date),
  streak_count = VALUES(streak_count),
  qualified_set_attempt_id = VALUES(qualified_set_attempt_id),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO practice_problems (
  problem_id, subject_id, node_id, lesson_id, problem_type, question, answer_text, explanation, difficulty_code, created_by, is_active, created_at, updated_at
)
SELECT
  (lesson.lesson_id * 100) + problem_step.problem_no AS problem_id,
  node.subject_id,
  node.node_id,
  lesson.lesson_id,
  problem_step.problem_type,
  CONCAT(lesson.title, ' 문제 ', problem_step.problem_no, '. ', problem_step.prompt) AS question,
  problem_step.answer_text,
  CONCAT('해설: ', lesson.title, '의 ', problem_step.prompt, ' 기준 정답은 ', problem_step.answer_text, '입니다.') AS explanation,
  node.level_code AS difficulty_code,
  1 AS created_by,
  1 AS is_active,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM lessons lesson
JOIN curriculum_nodes node ON node.node_id = lesson.node_id
CROSS JOIN (
  SELECT 1 AS problem_no, 'MULTIPLE_CHOICE' AS problem_type, '핵심 개념으로 가장 알맞은 것을 고르세요.' AS prompt, '핵심 개념' AS answer_text
  UNION ALL SELECT 2, 'MULTIPLE_CHOICE', '기본 문법으로 맞는 것을 고르세요.', '기본 문법'
  UNION ALL SELECT 3, 'MULTIPLE_CHOICE', '실행 흐름으로 맞는 것을 고르세요.', '실행 흐름'
  UNION ALL SELECT 4, 'MULTIPLE_CHOICE', '예제의 결과로 알맞은 것을 고르세요.', '예제 결과'
  UNION ALL SELECT 5, 'FILL_BLANK', '빈칸에 들어갈 핵심 표현을 입력하세요.', '핵심 표현'
  UNION ALL SELECT 6, 'FILL_BLANK', '조건 또는 반복 흐름의 핵심 단어를 입력하세요.', '흐름 제어'
  UNION ALL SELECT 7, 'FILL_BLANK', '자료를 다룰 때 필요한 표현을 입력하세요.', '자료 접근'
  UNION ALL SELECT 8, 'CODE_SHORT', '짧은 코드 조각을 작성하세요.', 'return value;'
  UNION ALL SELECT 9, 'CODE_SHORT', '오류를 줄이는 검증 코드를 작성하세요.', 'if (value == null) return;'
  UNION ALL SELECT 10, 'CODE_SHORT', '레슨 내용을 종합한 코드를 작성하세요.', 'System.out.println(value);'
) problem_step
WHERE node.node_type = 'PLANET'
  AND node.is_active = 1
  AND lesson.is_active = 1
ON DUPLICATE KEY UPDATE
  subject_id = VALUES(subject_id),
  node_id = VALUES(node_id),
  lesson_id = VALUES(lesson_id),
  problem_type = VALUES(problem_type),
  question = VALUES(question),
  answer_text = VALUES(answer_text),
  explanation = VALUES(explanation),
  difficulty_code = VALUES(difficulty_code),
  created_by = VALUES(created_by),
  is_active = VALUES(is_active),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO problem_choices (choice_id, problem_id, choice_label, choice_text, is_correct, sort_order)
SELECT
  (problem.problem_id * 10) + seed_choice.choice_no AS choice_id,
  problem.problem_id,
  seed_choice.choice_label,
  CASE seed_choice.choice_label
    WHEN 'A' THEN problem.answer_text
    WHEN 'B' THEN CONCAT(problem.difficulty_code, ' 오답 보기')
    WHEN 'C' THEN '문제와 무관한 선택지'
    ELSE '정답 없음'
  END AS choice_text,
  IF(seed_choice.choice_label = 'A', 1, 0) AS is_correct,
  seed_choice.choice_no AS sort_order
FROM practice_problems problem
CROSS JOIN (
  SELECT 1 AS choice_no, 'A' AS choice_label
  UNION ALL SELECT 2, 'B'
  UNION ALL SELECT 3, 'C'
  UNION ALL SELECT 4, 'D'
) seed_choice
WHERE problem.problem_type = 'MULTIPLE_CHOICE'
ON DUPLICATE KEY UPDATE
  problem_id = VALUES(problem_id),
  choice_label = VALUES(choice_label),
  choice_text = VALUES(choice_text),
  is_correct = VALUES(is_correct),
  sort_order = VALUES(sort_order);

INSERT INTO practice_set_items (set_item_id, set_attempt_id, problem_id, sort_order, created_at)
SELECT
  (attempt.set_attempt_id * 100) + problem_step.problem_no AS set_item_id,
  attempt.set_attempt_id,
  (attempt.lesson_id * 100) + problem_step.problem_no AS problem_id,
  problem_step.problem_no AS sort_order,
  CURRENT_TIMESTAMP AS created_at
FROM practice_set_attempts attempt
CROSS JOIN (
  SELECT 1 AS problem_no
  UNION ALL SELECT 2
  UNION ALL SELECT 3
  UNION ALL SELECT 4
  UNION ALL SELECT 5
  UNION ALL SELECT 6
  UNION ALL SELECT 7
  UNION ALL SELECT 8
  UNION ALL SELECT 9
  UNION ALL SELECT 10
) problem_step
WHERE attempt.lesson_id IS NOT NULL
ON DUPLICATE KEY UPDATE
  set_attempt_id = VALUES(set_attempt_id),
  problem_id = VALUES(problem_id),
  sort_order = VALUES(sort_order);

INSERT INTO practice_submissions (
  submission_id, set_attempt_id, set_item_id, user_id, problem_id, submission_context, submitted_answer, is_correct, is_skipped, solved_at, created_at
)
VALUES
  (1, 1, 101, 2, 10101, 'PRACTICE_SET', '핵심 개념', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (2, 1, 102, 2, 10102, 'PRACTICE_SET', '기본 문법', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (3, 1, 103, 2, 10103, 'PRACTICE_SET', '틀린 흐름', 0, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (4, 1, 104, 2, 10104, 'PRACTICE_SET', '틀린 예제', 0, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (5, 1, 105, 2, 10105, 'PRACTICE_SET', '핵심 표현', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (6, 1, 106, 2, 10106, 'PRACTICE_SET', '흐름 제어', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (7, 1, 107, 2, 10107, 'PRACTICE_SET', '자료 접근', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (8, 1, 108, 2, 10108, 'PRACTICE_SET', 'return value;', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (9, 1, 109, 2, 10109, 'PRACTICE_SET', 'if (value == null) return;', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (10, 1, 110, 2, 10110, 'PRACTICE_SET', 'System.out.println(wrong);', 0, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  set_attempt_id = VALUES(set_attempt_id),
  set_item_id = VALUES(set_item_id),
  user_id = VALUES(user_id),
  problem_id = VALUES(problem_id),
  submission_context = VALUES(submission_context),
  submitted_answer = VALUES(submitted_answer),
  is_correct = VALUES(is_correct),
  is_skipped = VALUES(is_skipped),
  solved_at = VALUES(solved_at);

INSERT INTO practice_submissions (
  submission_id, set_attempt_id, set_item_id, user_id, problem_id, submission_context, submitted_answer, is_correct, is_skipped, solved_at, created_at
)
SELECT
  (attempt.set_attempt_id * 100) + item.sort_order AS submission_id,
  attempt.set_attempt_id,
  item.set_item_id,
  attempt.user_id,
  item.problem_id,
  'PRACTICE_SET',
  CASE
    WHEN item.sort_order <= attempt.correct_count THEN problem.answer_text
    ELSE CONCAT('__seed_incorrect_', item.sort_order)
  END AS submitted_answer,
  IF(item.sort_order <= attempt.correct_count, 1, 0) AS is_correct,
  0 AS is_skipped,
  COALESCE(attempt.completed_at, attempt.created_at) AS solved_at,
  attempt.created_at
FROM practice_set_attempts attempt
JOIN practice_set_items item ON item.set_attempt_id = attempt.set_attempt_id
JOIN practice_problems problem ON problem.problem_id = item.problem_id
LEFT JOIN practice_submissions existing
  ON existing.set_attempt_id = attempt.set_attempt_id
  AND existing.problem_id = item.problem_id
  AND existing.submission_context = 'PRACTICE_SET'
WHERE attempt.status = 'COMPLETED'
  AND existing.submission_id IS NULL
  AND (
    (attempt.user_id = 2 AND (attempt.set_attempt_id = 1 OR attempt.set_attempt_id = 100000 + attempt.lesson_id))
    OR (attempt.user_id = 3 AND (attempt.set_attempt_id = 2 OR attempt.set_attempt_id = 200000 + attempt.lesson_id))
  )
ON DUPLICATE KEY UPDATE
  set_attempt_id = VALUES(set_attempt_id),
  set_item_id = VALUES(set_item_id),
  user_id = VALUES(user_id),
  problem_id = VALUES(problem_id),
  submission_context = VALUES(submission_context),
  submitted_answer = VALUES(submitted_answer),
  is_correct = VALUES(is_correct),
  is_skipped = VALUES(is_skipped),
  solved_at = VALUES(solved_at);

INSERT INTO wrong_answers (
  wrong_answer_id, user_id, set_attempt_id, problem_id, last_submission_id, wrong_count, review_status, retry_bonus_awarded, created_at, updated_at
)
VALUES
  (1, 2, 1, 10103, 3, 1, 'OPEN', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 2, 1, 10110, 10, 1, 'OPEN', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  set_attempt_id = VALUES(set_attempt_id),
  problem_id = VALUES(problem_id),
  last_submission_id = VALUES(last_submission_id),
  wrong_count = VALUES(wrong_count),
  review_status = VALUES(review_status),
  retry_bonus_awarded = VALUES(retry_bonus_awarded),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO score_events (
  score_event_id, user_id, subject_id, source_type, source_id, score_delta, reason_code, idempotency_key, created_at
)
VALUES
  (1, 2, @java_subject_id, 'PRACTICE_SET', 1, 70, 'PRACTICE_PASS', 'seed:learner:practice:1', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY)),
  (2, 3, @java_subject_id, 'PRACTICE_SET', 2, 90, 'PRACTICE_PASS', 'seed:premium:practice:2', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)),
  (3, 3, @java_subject_id, 'AI_EXAM', 1, 200, 'AI_EXAM_PASS', 'seed:premium:exam:1', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  source_type = VALUES(source_type),
  source_id = VALUES(source_id),
  score_delta = VALUES(score_delta),
  reason_code = VALUES(reason_code),
  idempotency_key = VALUES(idempotency_key);

INSERT INTO ranking_scores (
  ranking_id, user_id, subject_id, scope_type, scope_key, period_type, period_key, score, rank_no, calculated_at, created_at, updated_at
)
VALUES
  (1, 3, @java_subject_id, 'SUBJECT', 'JAVA', 'WEEKLY', DATE_FORMAT(CURRENT_DATE, '%x-W%v'), 1870, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 2, @java_subject_id, 'SUBJECT', 'JAVA', 'WEEKLY', DATE_FORMAT(CURRENT_DATE, '%x-W%v'), 230, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 1, NULL, 'GLOBAL', 'ALL', 'WEEKLY', DATE_FORMAT(CURRENT_DATE, '%x-W%v'), 0, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  scope_type = VALUES(scope_type),
  scope_key = VALUES(scope_key),
  period_type = VALUES(period_type),
  period_key = VALUES(period_key),
  score = VALUES(score),
  rank_no = VALUES(rank_no),
  calculated_at = CURRENT_TIMESTAMP,
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO exam_sessions (
  exam_id, user_id, subject_id, level_code, status, result_status, total_problem_count, correct_count, retry_count, started_at, submitted_at, graded_at, created_at, updated_at
)
VALUES
  (1, 3, @java_subject_id, 'BRONZE', 'GRADED', 'PASSED', 3, 2, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 90 MINUTE), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 80 MINUTE), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  level_code = VALUES(level_code),
  status = VALUES(status),
  result_status = VALUES(result_status),
  total_problem_count = VALUES(total_problem_count),
  correct_count = VALUES(correct_count),
  retry_count = VALUES(retry_count),
  started_at = VALUES(started_at),
  submitted_at = VALUES(submitted_at),
  graded_at = VALUES(graded_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_level_unlocks (
  unlock_id, user_id, subject_id, level_code, unlock_source, unlocked_by_exam_id, unlocked_at, created_at
)
VALUES
  (3, 3, @java_subject_id, 'SILVER', 'AI_EXAM', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  level_code = VALUES(level_code),
  unlock_source = VALUES(unlock_source),
  unlocked_by_exam_id = VALUES(unlocked_by_exam_id),
  unlocked_at = VALUES(unlocked_at);

INSERT INTO ai_exam_problems (
  ai_problem_id, exam_id, problem_no, prompt, test_case_spec, ai_raw_response, status, created_at, updated_at
)
VALUES
  (1, 1, 1, '두 정수의 합을 반환하는 add 메서드를 작성하세요.', '{"cases":[{"input":"2 3","expected":"5"},{"input":"10 -4","expected":"6"}]}', '{"model":"sample","usage":"seed"}', 'GENERATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 1, 2, '문자열 배열에서 가장 긴 문자열을 반환하는 메서드를 작성하세요.', '{"cases":[{"input":"java sql python","expected":"python"}]}', '{"model":"sample","usage":"seed"}', 'GENERATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 1, 3, '정수 배열에서 짝수의 개수를 반환하는 메서드를 작성하세요.', '{"cases":[{"input":"1 2 4 7","expected":"2"}]}', '{"model":"sample","usage":"seed"}', 'GENERATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  exam_id = VALUES(exam_id),
  problem_no = VALUES(problem_no),
  prompt = VALUES(prompt),
  test_case_spec = VALUES(test_case_spec),
  ai_raw_response = VALUES(ai_raw_response),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO exam_answers (
  answer_id, exam_id, ai_problem_id, answer_text, passed_case_count, is_correct, ai_review, test_case_result, submitted_at, graded_at, created_at, updated_at
)
VALUES
  (1, 1, 1, 'int add(int a, int b) { return a + b; }', 2, 1, '테스트케이스를 모두 통과했습니다. 메서드 이름과 반환 타입이 요구사항과 일치합니다.', '{"passed":2,"total":2}', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 90 MINUTE), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 80 MINUTE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 1, 2, 'String longest(String[] values) { return values[0]; }', 0, 0, '첫 번째 값만 반환하고 있어 전체 배열 비교가 필요합니다.', '{"passed":0,"total":1}', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 90 MINUTE), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 80 MINUTE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 1, 3, 'int countEven(int[] arr) { int count = 0; for (int n : arr) { if (n % 2 == 0) count++; } return count; }', 1, 1, '반복문과 조건식이 올바릅니다.', '{"passed":1,"total":1}', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 90 MINUTE), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 80 MINUTE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  exam_id = VALUES(exam_id),
  ai_problem_id = VALUES(ai_problem_id),
  answer_text = VALUES(answer_text),
  passed_case_count = VALUES(passed_case_count),
  is_correct = VALUES(is_correct),
  ai_review = VALUES(ai_review),
  test_case_result = VALUES(test_case_result),
  submitted_at = VALUES(submitted_at),
  graded_at = VALUES(graded_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO ai_analysis_reports (
  report_id, user_id, exam_id, status, free_summary, premium_detail, analysis_error_code, retry_count, created_at, updated_at
)
VALUES
  (1, 3, 1, 'SUCCESS', '조건문과 반복문은 안정적이며, 배열/문자열 탐색 문제에서 보완이 필요합니다.', '{"strengths":["기본 연산","반복문"],"weaknesses":["배열 탐색"],"nextActions":["문자열 배열 순회 문제를 3개 더 풀기"]}', NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  exam_id = VALUES(exam_id),
  status = VALUES(status),
  free_summary = VALUES(free_summary),
  premium_detail = VALUES(premium_detail),
  analysis_error_code = VALUES(analysis_error_code),
  retry_count = VALUES(retry_count),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO ai_request_logs (
  ai_request_log_id, target_type, target_id, request_type, status, retry_no, request_payload, response_payload, error_code, error_message, created_at, updated_at
)
VALUES
  (1, 'EXAM_SESSION', 1, 'PROBLEM_GENERATION', 'SUCCESS', 0, '{"level":"BRONZE","subject":"JAVA"}', '{"problemCount":3}', NULL, NULL, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR), CURRENT_TIMESTAMP),
  (2, 'EXAM_ANSWER', 1, 'CODE_REVIEW', 'SUCCESS', 0, '{"answerId":1}', '{"review":"passed"}', NULL, NULL, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 80 MINUTE), CURRENT_TIMESTAMP),
  (3, 'ANALYSIS_REPORT', 1, 'ANALYSIS', 'SUCCESS', 0, '{"examId":1}', '{"status":"SUCCESS"}', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  target_type = VALUES(target_type),
  target_id = VALUES(target_id),
  request_type = VALUES(request_type),
  status = VALUES(status),
  retry_no = VALUES(retry_no),
  request_payload = VALUES(request_payload),
  response_payload = VALUES(response_payload),
  error_code = VALUES(error_code),
  error_message = VALUES(error_message),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO community_posts (
  post_id, writer_id, subject_id, board_type, title, content, view_count, like_count, comment_count, scrap_count, status, created_at, updated_at, deleted_at
)
VALUES
  (1, 2, @java_subject_id, 'QUESTION', '조건문에서 else if를 언제 쓰나요?', '점수 구간별로 다른 메시지를 보여주고 싶습니다.', 28, 4, 2, 1, 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), CURRENT_TIMESTAMP, NULL),
  (2, 3, @java_subject_id, 'STUDY_LOG', 'Bronze 5개 행성 완료 기록', '오늘 Java Bronze 행성을 모두 완료하고 AI 시험까지 통과했습니다.', 21, 3, 1, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (3, 1, @java_subject_id, 'FREE', '샘플 데이터 안내', '로컬 개발용 샘플 게시글입니다. 관리자 화면 검수에 사용합니다.', 12, 3, 0, 0, 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 14 DAY), CURRENT_TIMESTAMP, NULL)
ON DUPLICATE KEY UPDATE
  writer_id = VALUES(writer_id),
  subject_id = VALUES(subject_id),
  board_type = VALUES(board_type),
  title = VALUES(title),
  content = VALUES(content),
  view_count = VALUES(view_count),
  like_count = VALUES(like_count),
  comment_count = VALUES(comment_count),
  scrap_count = VALUES(scrap_count),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP,
  deleted_at = VALUES(deleted_at);

INSERT INTO post_attachments (
  attachment_id, post_id, uploader_id, original_name, stored_name, file_path, file_size, created_at
)
VALUES
  (1, 1, 2, 'condition-example.txt', 'seed-condition-example.txt', './uploads/sample/seed-condition-example.txt', 128, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  post_id = VALUES(post_id),
  uploader_id = VALUES(uploader_id),
  original_name = VALUES(original_name),
  stored_name = VALUES(stored_name),
  file_path = VALUES(file_path),
  file_size = VALUES(file_size);

INSERT INTO comments (
  comment_id, post_id, parent_comment_id, writer_id, content, status, created_at, updated_at, deleted_at, deleted_by_admin_id
)
VALUES
  (1, 1, NULL, 3, '조건이 여러 구간으로 나뉘면 else if를 쓰면 됩니다.', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, NULL),
  (2, 1, 1, 2, '예시 감사합니다. 점수 구간으로 적용해보겠습니다.', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, NULL),
  (3, 2, NULL, 2, '축하합니다. 저도 Bronze부터 따라가겠습니다.', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, NULL),
  (4, 3, NULL, 3, '운영 정책 검수용 관리자 삭제 댓글입니다.', 'DELETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1)
ON DUPLICATE KEY UPDATE
  post_id = VALUES(post_id),
  parent_comment_id = VALUES(parent_comment_id),
  writer_id = VALUES(writer_id),
  content = VALUES(content),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP,
  deleted_at = VALUES(deleted_at),
  deleted_by_admin_id = VALUES(deleted_by_admin_id);

INSERT INTO post_likes (like_id, post_id, user_id, created_at)
VALUES
  (1, 1, 3, CURRENT_TIMESTAMP),
  (2, 2, 2, CURRENT_TIMESTAMP),
  (3, 1, 1, CURRENT_TIMESTAMP),
  (4, 1, 4, CURRENT_TIMESTAMP),
  (5, 1, 5, CURRENT_TIMESTAMP),
  (6, 2, 1, CURRENT_TIMESTAMP),
  (7, 2, 4, CURRENT_TIMESTAMP),
  (8, 3, 2, CURRENT_TIMESTAMP),
  (9, 3, 3, CURRENT_TIMESTAMP),
  (10, 3, 4, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  post_id = VALUES(post_id),
  user_id = VALUES(user_id);

INSERT INTO post_scraps (scrap_id, post_id, user_id, created_at)
VALUES
  (1, 1, 2, CURRENT_TIMESTAMP),
  (2, 2, 5, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  post_id = VALUES(post_id),
  user_id = VALUES(user_id);

-- Community expansion: Python/WEB/SQL 게시글 81건과 집계 카운트에 대응하는 정규화 상호작용 데이터
-- 고정 ID 범위(사용자 6~53, 게시글 1001~1081)를 사용해 반복 실행해도 같은 seed 상태를 유지한다.
INSERT INTO users (
  user_id, email, nickname, role, status, profile_image_url, last_login_at, withdrawn_at, created_at, updated_at
)
WITH RECURSIVE community_seed_numbers (n) AS (
  SELECT 1
  UNION ALL
  SELECT n + 1
  FROM community_seed_numbers
  WHERE n < 48
)
SELECT
  5 + n,
  CONCAT('community-seed-', LPAD(n, 2, '0'), '@example.invalid'),
  CONCAT('커뮤니티샘플', LPAD(n, 2, '0')),
  'ROLE_USER',
  'ACTIVE',
  NULL,
  NULL,
  NULL,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM community_seed_numbers
ON DUPLICATE KEY UPDATE
  email = VALUES(email),
  nickname = VALUES(nickname),
  role = VALUES(role),
  status = VALUES(status),
  profile_image_url = VALUES(profile_image_url),
  last_login_at = VALUES(last_login_at),
  withdrawn_at = VALUES(withdrawn_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO community_posts (
  post_id, writer_id, subject_id, board_type, title, content,
  view_count, like_count, comment_count, scrap_count, status,
  created_at, updated_at, deleted_at
)
VALUES
(1001, 5, @python_subject_id, 'STUDY_LOG', 'Python 예외 처리 학습 기록', 'try except else finally 흐름을 직접 실행해보며 정리했습니다.', 31, 2, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1002, 4, @python_subject_id, 'FREE', 'Python list comprehension 복습', '반복문을 한 줄로 줄이는 기준을 예제로 정리했습니다.', 25, 1, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1003, 1, @python_subject_id, 'FREE', 'Python list slicing 복습', '시작 인덱스와 끝 인덱스를 다르게 줬을 때 결과가 어떻게 나오는지 정리했습니다.', 70, 5, 0, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1004, 2, @python_subject_id, 'QUESTION', 'dict get과 setdefault 차이', '기본값을 읽기만 할 때와 실제로 넣을 때의 차이를 비교했습니다.', 99, 12, 0, 3, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1005, 3, @python_subject_id, 'STUDY_LOG', '함수를 작게 나누는 기준', '한 함수가 너무 길어졌을 때 어디서 분리하면 좋은지 고민했습니다.', 128, 19, 0, 5, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1006, 4, @python_subject_id, 'FREE', '파일 읽기 with 구문 정리', '파일을 닫는 처리를 자동으로 맡기는 with 구문을 다시 복습했습니다.', 157, 26, 0, 7, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1007, 5, @python_subject_id, 'QUESTION', '예외 처리 try except 흐름', '어떤 예외를 잡고 어떤 예외는 그대로 올릴지 기준을 세워봤습니다.', 186, 33, 0, 9, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1008, 1, @python_subject_id, 'STUDY_LOG', '리스트 정렬 key lambda 연습', '게시글 데이터를 좋아요 수 기준으로 정렬하는 예제를 만들어봤습니다.', 215, 40, 0, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1009, 2, @python_subject_id, 'FREE', '문자열 split과 join 활용', '검색어를 분리하고 다시 합치는 간단한 전처리를 연습했습니다.', 244, 47, 0, 4, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1010, 3, @python_subject_id, 'QUESTION', '가상환경 requirements 관리', '팀 프로젝트에서 dependency를 맞추기 위해 requirements 파일을 정리했습니다.', 273, 6, 0, 6, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1011, 4, @python_subject_id, 'STUDY_LOG', '간단한 로그 파일 분석', '텍스트 로그에서 특정 키워드가 몇 번 나오는지 세는 코드를 작성했습니다.', 302, 13, 0, 8, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1012, 5, @python_subject_id, 'FREE', 'dataclass를 써보면 좋은 경우', '단순 데이터 묶음은 class보다 dataclass가 깔끔한 상황을 봤습니다.', 71, 20, 0, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1013, 3, @python_subject_id, 'FREE', 'Python 자유에서 오늘 정리한 핵심 포인트', 'Python 학습 중 헷갈렸던 부분을 짧게 정리했습니다. 비슷한 부분을 공부하는 분들 의견도 궁금합니다.', 25, 1, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1014, 1, @python_subject_id, 'QUESTION', 'Python에서 None 체크 기준', '빈 문자열, 0, None을 구분해서 조건문을 쓰는 연습을 했습니다.', 100, 27, 0, 3, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1015, 2, @python_subject_id, 'STUDY_LOG', '반복문을 comprehension으로 바꾸기', '가독성이 좋아지는 경우와 오히려 복잡해지는 경우를 비교했습니다.', 129, 34, 0, 5, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1016, 3, @python_subject_id, 'FREE', '간단한 테스트 코드 작성', '입력과 결과가 명확한 함수부터 테스트를 붙이는 연습을 했습니다.', 158, 41, 0, 7, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1017, 4, @python_subject_id, 'QUESTION', 'API 응답 데이터 가공 연습', 'JSON 형태의 응답에서 필요한 값만 뽑아 화면용 데이터로 바꿔봤습니다.', 187, 48, 0, 9, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1018, 5, @python_subject_id, 'STUDY_LOG', '학습 루틴 자동화 아이디어', '매일 푼 문제와 복습 내용을 파일로 남기는 작은 자동화를 생각해봤습니다.', 219, 7, 0, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1019, 4, @python_subject_id, 'QUESTION', 'Python 질문에서 오늘 정리한 핵심 포인트', 'Python 학습 중 헷갈렸던 부분을 짧게 정리했습니다. 비슷한 부분을 공부하는 분들 의견도 궁금합니다.', 26, 1, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1020, 5, @python_subject_id, 'STUDY_LOG', 'Python 공부 일지에서 오늘 정리한 핵심 포인트', 'Python 학습 중 헷갈렸던 부분을 짧게 정리했습니다. 비슷한 부분을 공부하는 분들 의견도 궁금합니다.', 27, 1, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1021, 3, @python_subject_id, 'FREE', 'Python 자유 복습하면서 체크한 부분', '처음에는 어렵게 느껴졌는데 예제를 직접 따라가니 흐름이 조금씩 잡혔습니다. 다음에는 관련 문제도 같이 풀어볼 예정입니다.', 32, 2, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1022, 4, @python_subject_id, 'QUESTION', 'Python 질문 복습하면서 체크한 부분', '처음에는 어렵게 느껴졌는데 예제를 직접 따라가니 흐름이 조금씩 잡혔습니다. 다음에는 관련 문제도 같이 풀어볼 예정입니다.', 33, 2, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1023, 5, @python_subject_id, 'STUDY_LOG', 'Python 공부 일지 복습하면서 체크한 부분', '처음에는 어렵게 느껴졌는데 예제를 직접 따라가니 흐름이 조금씩 잡혔습니다. 다음에는 관련 문제도 같이 풀어볼 예정입니다.', 34, 2, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1024, 3, @python_subject_id, 'FREE', 'Python 자유 월간 인기 후보 정리', '이번 달 동안 반복해서 본 개념을 모아봤습니다. 나중에 다시 보기 좋도록 기준과 예외 상황을 함께 남겨둡니다.', 129, 7, 3, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1025, 4, @python_subject_id, 'QUESTION', 'Python 질문 월간 인기 후보 정리', '이번 달 동안 반복해서 본 개념을 모아봤습니다. 나중에 다시 보기 좋도록 기준과 예외 상황을 함께 남겨둡니다.', 138, 8, 4, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1026, 5, @python_subject_id, 'STUDY_LOG', 'Python 공부 일지 월간 인기 후보 정리', '이번 달 동안 반복해서 본 개념을 모아봤습니다. 나중에 다시 보기 좋도록 기준과 예외 상황을 함께 남겨둡니다.', 147, 9, 5, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1027, 3, @python_subject_id, 'QUESTION', 'Python 월간 질문 정리', 'None 처리와 기본값 설정에서 자주 헷갈리는 패턴을 모았습니다.', 134, 7, 3, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1028, 3, @web_subject_id, 'QUESTION', 'HTML form 전송 방식 질문', 'multipart form을 쓸 때 enctype을 빠뜨리면 어떤 문제가 생기는지 확인 중입니다.', 27, 2, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1029, 2, @web_subject_id, 'FREE', 'CSS grid 간격 조정 기록', '반응형에서 카드 간격이 무너지는 부분을 minmax로 정리했습니다.', 23, 1, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1030, 1, @web_subject_id, 'FREE', 'HTML semantic tag 사용 기준', 'section, article, aside를 어디에 쓰면 좋을지 게시판 화면 기준으로 정리했습니다.', 70, 5, 0, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1031, 2, @web_subject_id, 'QUESTION', 'CSS grid로 게시판 레이아웃 맞추기', '왼쪽 메뉴, 본문, 오른쪽 대시보드를 grid로 나누는 방식을 복습했습니다.', 99, 12, 0, 3, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1032, 3, @web_subject_id, 'STUDY_LOG', '버튼 간격을 일정하게 맞추는 방법', 'margin 대신 gap을 기준으로 버튼 그룹을 정리해봤습니다.', 128, 19, 0, 5, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1033, 4, @web_subject_id, 'FREE', '반응형에서 글씨가 잘릴 때 해결', 'min-width와 white-space를 조정해서 카드 안 텍스트가 깨지지 않게 했습니다.', 157, 26, 0, 7, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1034, 5, @web_subject_id, 'QUESTION', 'sticky와 fixed 차이 복습', '사이드바가 스크롤을 따라올 때 어떤 속성이 더 자연스러운지 비교했습니다.', 186, 33, 0, 9, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1035, 2, @web_subject_id, 'FREE', 'HTML/CSS/JS 자유에서 오늘 정리한 핵심 포인트', 'HTML/CSS/JS 학습 중 헷갈렸던 부분을 짧게 정리했습니다. 비슷한 부분을 공부하는 분들 의견도 궁금합니다.', 25, 1, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1036, 1, @web_subject_id, 'STUDY_LOG', '폼 placeholder 문구 정리', '입력창 안내 문구가 실제 사용자에게 부담스럽지 않게 보이도록 바꿔봤습니다.', 215, 40, 0, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1037, 2, @web_subject_id, 'FREE', '다크모드 버튼 색상 대비', '배경색과 버튼 글자색 대비가 낮을 때 가독성이 떨어지는 문제를 확인했습니다.', 244, 47, 0, 4, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1038, 3, @web_subject_id, 'QUESTION', '카드 hover 효과를 줄 때 기준', '과한 움직임보다 살짝 밝아지는 정도가 게시판에는 더 어울렸습니다.', 273, 6, 0, 6, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1039, 4, @web_subject_id, 'STUDY_LOG', '이미지 아이콘 object-fit 정리', '과목 아이콘이 잘리지 않도록 contain과 크기 기준을 맞췄습니다.', 302, 13, 0, 8, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1040, 5, @web_subject_id, 'FREE', '스크롤 영역을 나눌 때 UX 고민', '페이지 전체 스크롤과 패널 내부 스크롤을 어떻게 나눌지 정리했습니다.', 71, 20, 0, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1041, 1, @web_subject_id, 'QUESTION', 'JS 이벤트 위임 연습', '동적으로 생기는 댓글 버튼에 event delegation을 적용하는 방법을 복습했습니다.', 100, 27, 0, 3, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1042, 2, @web_subject_id, 'STUDY_LOG', 'aria-label을 넣어야 하는 버튼', '화살표나 아이콘만 있는 버튼에는 접근성 이름이 필요하다는 걸 확인했습니다.', 129, 34, 0, 5, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1043, 3, @web_subject_id, 'FREE', 'CSS animation 속도 조절', 'HOT 표시처럼 반복되는 효과는 너무 빠르면 시선을 빼앗는 것 같습니다.', 158, 41, 0, 7, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1044, 4, @web_subject_id, 'QUESTION', 'textarea 자동 높이 조절 고민', '댓글 입력창이 내용에 맞게 커지는 UI를 적용할지 고민 중입니다.', 187, 48, 0, 9, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1045, 5, @web_subject_id, 'STUDY_LOG', '게시글 목록 카드 높이 맞추기', '제목 길이가 달라도 목록 간격이 흔들리지 않게 정리하는 방법을 봤습니다.', 219, 7, 2, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1046, 3, @web_subject_id, 'QUESTION', 'HTML/CSS/JS 질문에서 오늘 정리한 핵심 포인트', 'HTML/CSS/JS 학습 중 헷갈렸던 부분을 짧게 정리했습니다. 비슷한 부분을 공부하는 분들 의견도 궁금합니다.', 26, 1, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1047, 4, @web_subject_id, 'STUDY_LOG', 'HTML/CSS/JS 공부 일지에서 오늘 정리한 핵심 포인트', 'HTML/CSS/JS 학습 중 헷갈렸던 부분을 짧게 정리했습니다. 비슷한 부분을 공부하는 분들 의견도 궁금합니다.', 27, 1, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1048, 2, @web_subject_id, 'FREE', 'HTML/CSS/JS 자유 복습하면서 체크한 부분', '처음에는 어렵게 느껴졌는데 예제를 직접 따라가니 흐름이 조금씩 잡혔습니다. 다음에는 관련 문제도 같이 풀어볼 예정입니다.', 32, 2, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1049, 3, @web_subject_id, 'QUESTION', 'HTML/CSS/JS 질문 복습하면서 체크한 부분', '처음에는 어렵게 느껴졌는데 예제를 직접 따라가니 흐름이 조금씩 잡혔습니다. 다음에는 관련 문제도 같이 풀어볼 예정입니다.', 33, 2, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1050, 4, @web_subject_id, 'STUDY_LOG', 'HTML/CSS/JS 공부 일지 복습하면서 체크한 부분', '처음에는 어렵게 느껴졌는데 예제를 직접 따라가니 흐름이 조금씩 잡혔습니다. 다음에는 관련 문제도 같이 풀어볼 예정입니다.', 34, 2, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1051, 2, @web_subject_id, 'FREE', 'HTML/CSS/JS 자유 월간 인기 후보 정리', '이번 달 동안 반복해서 본 개념을 모아봤습니다. 나중에 다시 보기 좋도록 기준과 예외 상황을 함께 남겨둡니다.', 129, 7, 3, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1052, 3, @web_subject_id, 'QUESTION', 'HTML/CSS/JS 질문 월간 인기 후보 정리', '이번 달 동안 반복해서 본 개념을 모아봤습니다. 나중에 다시 보기 좋도록 기준과 예외 상황을 함께 남겨둡니다.', 138, 8, 4, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1053, 4, @web_subject_id, 'STUDY_LOG', 'HTML/CSS/JS 공부 일지 월간 인기 후보 정리', '이번 달 동안 반복해서 본 개념을 모아봤습니다. 나중에 다시 보기 좋도록 기준과 예외 상황을 함께 남겨둡니다.', 147, 9, 5, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1054, 2, @web_subject_id, 'STUDY_LOG', 'HTML/CSS 월간 회고', '레이아웃을 잡을 때 반복해서 틀린 부분을 체크리스트로 정리했습니다.', 128, 6, 2, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1055, 1, @sql_subject_id, 'FREE', 'SQL JOIN 조건 위치 정리', 'ON 절과 WHERE 절에 조건을 둘 때 결과가 달라지는 경우를 정리했습니다.', 70, 5, 0, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1056, 5, @sql_subject_id, 'STUDY_LOG', 'SQL 실행계획 읽어본 기록', '인덱스가 잡히는 조건과 풀스캔이 나는 조건을 비교했습니다.', 29, 2, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1057, 4, @sql_subject_id, 'FREE', 'SQL JOIN 복습 메모', 'INNER JOIN과 LEFT JOIN 결과 차이를 작은 테이블로 다시 확인했습니다.', 24, 1, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1058, 2, @sql_subject_id, 'QUESTION', '인덱스를 탔는지 확인하는 기본 순서', '실행계획에서 type, key, rows를 먼저 보고 병목을 찾는 연습을 했습니다.', 99, 12, 0, 3, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1059, 3, @sql_subject_id, 'STUDY_LOG', 'GROUP BY 결과가 예상과 다를 때', '집계 기준 컬럼을 잘못 잡았을 때 생기는 문제를 예제로 정리했습니다.', 128, 19, 0, 5, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1060, 4, @sql_subject_id, 'FREE', '서브쿼리와 JOIN 중 어떤 걸 쓸까요?', '읽기 쉬움과 성능을 같이 고려해서 선택하는 기준을 고민했습니다.', 157, 26, 0, 7, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1061, 5, @sql_subject_id, 'QUESTION', 'MyBatis foreach insert 복습', '여러 row를 한 번에 넣는 batch insert 문법을 다시 확인했습니다.', 186, 33, 0, 9, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1062, 1, @sql_subject_id, 'STUDY_LOG', '날짜별 게시글 통계 쿼리', 'created_at을 기준으로 일자별 게시글 수를 집계하는 쿼리를 연습했습니다.', 215, 40, 0, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1063, 2, @sql_subject_id, 'FREE', 'NULL 비교에서 실수한 부분', '= NULL이 아니라 IS NULL을 써야 하는 이유를 다시 정리했습니다.', 244, 47, 0, 4, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1064, 3, @sql_subject_id, 'QUESTION', 'LIKE 검색에서 와일드카드 위치', '앞쪽 와일드카드가 index 사용에 미치는 영향을 확인했습니다.', 273, 6, 0, 6, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1065, 4, @sql_subject_id, 'STUDY_LOG', 'COUNT와 EXISTS 선택 기준', '존재 여부만 확인할 때 EXISTS가 더 어울리는 상황을 정리했습니다.', 302, 13, 0, 8, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1066, 5, @sql_subject_id, 'FREE', '게시판 정렬 쿼리 개선 메모', '좋아요순과 조회수순 정렬에서 함께 볼 보조 정렬 기준을 정했습니다.', 71, 20, 0, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1067, 1, @sql_subject_id, 'QUESTION', '트랜잭션 처리 흐름 질문', '댓글 등록 후 게시글 댓글 수를 갱신할 때 트랜잭션 범위를 어디까지 잡을지 궁금합니다.', 100, 27, 0, 3, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1068, 2, @sql_subject_id, 'STUDY_LOG', '중복 데이터 방지 unique key', '좋아요와 스크랩 테이블에서 중복 row를 막는 방법을 정리했습니다.', 130, 34, 0, 5, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1069, 3, @sql_subject_id, 'FREE', '페이징 offset이 커질 때 고민', '데이터가 많아질 때 offset pagination의 한계를 확인했습니다.', 158, 41, 0, 7, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1070, 5, @sql_subject_id, 'FREE', 'SQL 자유에서 오늘 정리한 핵심 포인트', 'SQL 학습 중 헷갈렸던 부분을 짧게 정리했습니다. 비슷한 부분을 공부하는 분들 의견도 궁금합니다.', 25, 1, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1071, 4, @sql_subject_id, 'QUESTION', 'CASE WHEN으로 상태 표시하기', '게시글 상태를 화면용 문구로 바꿔 보여주는 쿼리를 연습했습니다.', 188, 48, 0, 9, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1072, 5, @sql_subject_id, 'STUDY_LOG', 'SQL 작성 순서 개인 루틴', 'SELECT보다 FROM과 WHERE를 먼저 생각하면 쿼리 흐름이 더 잘 잡혔습니다.', 218, 8, 0, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1073, 2, @sql_subject_id, 'QUESTION', 'SQL 질문에서 오늘 정리한 핵심 포인트', 'SQL 학습 중 헷갈렸던 부분을 짧게 정리했습니다. 비슷한 부분을 공부하는 분들 의견도 궁금합니다.', 26, 1, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1074, 3, @sql_subject_id, 'STUDY_LOG', 'SQL 공부 일지에서 오늘 정리한 핵심 포인트', 'SQL 학습 중 헷갈렸던 부분을 짧게 정리했습니다. 비슷한 부분을 공부하는 분들 의견도 궁금합니다.', 27, 1, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1075, 5, @sql_subject_id, 'FREE', 'SQL 자유 복습하면서 체크한 부분', '처음에는 어렵게 느껴졌는데 예제를 직접 따라가니 흐름이 조금씩 잡혔습니다. 다음에는 관련 문제도 같이 풀어볼 예정입니다.', 32, 2, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1076, 2, @sql_subject_id, 'QUESTION', 'SQL 질문 복습하면서 체크한 부분', '처음에는 어렵게 느껴졌는데 예제를 직접 따라가니 흐름이 조금씩 잡혔습니다. 다음에는 관련 문제도 같이 풀어볼 예정입니다.', 33, 2, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1077, 3, @sql_subject_id, 'STUDY_LOG', 'SQL 공부 일지 복습하면서 체크한 부분', '처음에는 어렵게 느껴졌는데 예제를 직접 따라가니 흐름이 조금씩 잡혔습니다. 다음에는 관련 문제도 같이 풀어볼 예정입니다.', 34, 2, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1078, 5, @sql_subject_id, 'FREE', 'SQL 자유 월간 인기 후보 정리', '이번 달 동안 반복해서 본 개념을 모아봤습니다. 나중에 다시 보기 좋도록 기준과 예외 상황을 함께 남겨둡니다.', 129, 7, 3, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1079, 2, @sql_subject_id, 'QUESTION', 'SQL 질문 월간 인기 후보 정리', '이번 달 동안 반복해서 본 개념을 모아봤습니다. 나중에 다시 보기 좋도록 기준과 예외 상황을 함께 남겨둡니다.', 138, 8, 4, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1080, 5, @sql_subject_id, 'QUESTION', 'SQL 월간 질문 모음', '서브쿼리와 JOIN 중 어떤 쪽이 읽기 쉬운지 사례별로 비교했습니다.', 136, 6, 2, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (1081, 3, @sql_subject_id, 'STUDY_LOG', 'SQL 공부 일지 월간 인기 후보 정리', '이번 달 동안 반복해서 본 개념을 모아봤습니다. 나중에 다시 보기 좋도록 기준과 예외 상황을 함께 남겨둡니다.', 147, 9, 5, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)
ON DUPLICATE KEY UPDATE
  writer_id = VALUES(writer_id),
  subject_id = VALUES(subject_id),
  board_type = VALUES(board_type),
  title = VALUES(title),
  content = VALUES(content),
  view_count = VALUES(view_count),
  like_count = VALUES(like_count),
  comment_count = VALUES(comment_count),
  scrap_count = VALUES(scrap_count),
  status = VALUES(status),
  updated_at = VALUES(updated_at),
  deleted_at = VALUES(deleted_at);

INSERT INTO comments (
  comment_id, post_id, parent_comment_id, writer_id, content, status, created_at, updated_at, deleted_at, deleted_by_admin_id
)
WITH RECURSIVE community_seed_numbers (n) AS (
  SELECT 1
  UNION ALL
  SELECT n + 1
  FROM community_seed_numbers
  WHERE n < 48
)
SELECT
  100000 + (community_post.post_id - 1000) * 10 + community_seed_numbers.n,
  community_post.post_id,
  NULL,
  5 + community_seed_numbers.n,
  CONCAT('커뮤니티 샘플 댓글 ', community_seed_numbers.n, ' · ', community_post.title, ' 내용을 참고해 다음 학습에 활용해 보세요.'),
  'ACTIVE',
  community_post.created_at,
  community_post.updated_at,
  NULL,
  NULL
FROM community_posts AS community_post
JOIN community_seed_numbers ON community_seed_numbers.n <= community_post.comment_count
WHERE community_post.post_id BETWEEN 1001 AND 1081
ON DUPLICATE KEY UPDATE
  post_id = VALUES(post_id),
  parent_comment_id = VALUES(parent_comment_id),
  writer_id = VALUES(writer_id),
  content = VALUES(content),
  status = VALUES(status),
  updated_at = VALUES(updated_at),
  deleted_at = VALUES(deleted_at),
  deleted_by_admin_id = VALUES(deleted_by_admin_id);

INSERT INTO post_likes (like_id, post_id, user_id, created_at)
WITH RECURSIVE community_seed_numbers (n) AS (
  SELECT 1
  UNION ALL
  SELECT n + 1
  FROM community_seed_numbers
  WHERE n < 48
)
SELECT
  200000 + (community_post.post_id - 1000) * 100 + community_seed_numbers.n,
  community_post.post_id,
  5 + community_seed_numbers.n,
  community_post.created_at
FROM community_posts AS community_post
JOIN community_seed_numbers ON community_seed_numbers.n <= community_post.like_count
WHERE community_post.post_id BETWEEN 1001 AND 1081
ON DUPLICATE KEY UPDATE
  post_id = VALUES(post_id),
  user_id = VALUES(user_id);

INSERT INTO post_scraps (scrap_id, post_id, user_id, created_at)
WITH RECURSIVE community_seed_numbers (n) AS (
  SELECT 1
  UNION ALL
  SELECT n + 1
  FROM community_seed_numbers
  WHERE n < 48
)
SELECT
  300000 + (community_post.post_id - 1000) * 100 + community_seed_numbers.n,
  community_post.post_id,
  5 + community_seed_numbers.n,
  community_post.created_at
FROM community_posts AS community_post
JOIN community_seed_numbers ON community_seed_numbers.n <= community_post.scrap_count
WHERE community_post.post_id BETWEEN 1001 AND 1081
ON DUPLICATE KEY UPDATE
  post_id = VALUES(post_id),
  user_id = VALUES(user_id);


INSERT INTO reports (
  report_id, target_type, target_id, reporter_id, reason_code, status, handled_by, handled_at, created_at, updated_at
)
VALUES
  (1, 'POST', 3, 2, 'SPAM', 'RESOLVED', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  target_type = VALUES(target_type),
  target_id = VALUES(target_id),
  reporter_id = VALUES(reporter_id),
  reason_code = VALUES(reason_code),
  status = VALUES(status),
  handled_by = VALUES(handled_by),
  handled_at = VALUES(handled_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO dummy_payments (
  payment_id, order_no, user_id, product_id, payment_method, payment_status, pg_provider, pg_transaction_id, amount, paid_at, created_at, updated_at
)
VALUES
  (1, 'SEED-PREMIUM-0001', 3, @premium_product_id, 'CARD', 'PAID', NULL, NULL, 9900.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  order_no = VALUES(order_no),
  user_id = VALUES(user_id),
  product_id = VALUES(product_id),
  payment_method = VALUES(payment_method),
  payment_status = VALUES(payment_status),
  pg_provider = VALUES(pg_provider),
  pg_transaction_id = VALUES(pg_transaction_id),
  amount = VALUES(amount),
  paid_at = VALUES(paid_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO premium_grants (
  grant_id, user_id, payment_id, grant_type, status, granted_at, expires_at, created_at, updated_at
)
VALUES
  (1, 3, 1, 'LIFETIME', 'ACTIVE', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  payment_id = VALUES(payment_id),
  grant_type = VALUES(grant_type),
  status = VALUES(status),
  granted_at = VALUES(granted_at),
  expires_at = VALUES(expires_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO notices (
  notice_id, writer_id, title, content, status, published_at, created_at, updated_at
)
VALUES
  (1, 1, 'Knowva 로컬 개발 샘플 공지', '샘플 계정과 샘플 데이터를 사용해 화면 흐름을 검수할 수 있습니다.', 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 1, 'AI 코딩테스트 안내', 'AI 시험은 문제 생성, 테스트케이스 생성 보조, 해설 생성, 코드 리뷰에 ChatGPT API를 사용합니다.', 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  writer_id = VALUES(writer_id),
  title = VALUES(title),
  content = VALUES(content),
  status = VALUES(status),
  published_at = VALUES(published_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO admin_operation_logs (
  log_id, admin_id, action_type, target_type, target_id, target_name, change_detail, result_status, created_at
)
VALUES
  (1, 1, 'CREATE_NOTICE', 'NOTICE', 1, 'Knowva 로컬 개발 샘플 공지', '공지사항을 등록하고 게시 상태로 설정', 'SUCCESS', CURRENT_TIMESTAMP),
  (2, 1, 'HANDLE_REPORT', 'REPORT', 1, 'Java 질문 게시글 신고', '스팸 신고를 처리하고 신고 상태를 RESOLVED로 변경', 'SUCCESS', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  admin_id = VALUES(admin_id),
  action_type = VALUES(action_type),
  target_type = VALUES(target_type),
  target_id = VALUES(target_id),
  target_name = VALUES(target_name),
  change_detail = VALUES(change_detail),
  result_status = VALUES(result_status);

INSERT INTO content_recommendations (
  content_id, subject_id, title, url, content_type, recommendation_slot, is_active, created_at, updated_at
)
VALUES
  (1, @java_subject_id, 'Java 공식 튜토리얼', 'https://dev.java/learn/', 'ARTICLE', 'BEGINNER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, @python_subject_id, 'Python 공식 튜토리얼', 'https://docs.python.org/3/tutorial/', 'ARTICLE', 'BEGINNER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, @web_subject_id, 'MDN JavaScript Guide', 'https://developer.mozilla.org/docs/Web/JavaScript/Guide', 'ARTICLE', 'BEGINNER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, @sql_subject_id, 'MySQL 8 Reference Manual', 'https://dev.mysql.com/doc/refman/8.0/en/', 'DOCS', 'REFERENCE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, @java_subject_id, 'Java 입문 풀코스', 'https://www.youtube.com/watch?v=eIrMbAQSU34', 'VIDEO', 'BEGINNER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, @java_subject_id, 'Spring Boot 기초 흐름', 'https://www.youtube.com/watch?v=vtPkZShrvXQ', 'VIDEO', 'BEGINNER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (7, @java_subject_id, 'Java 객체지향 복습', 'https://www.youtube.com/watch?v=grEKMHGYyns', 'VIDEO', 'REFERENCE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (8, @python_subject_id, 'Python 입문 풀코스', 'https://www.youtube.com/watch?v=rfscVS0vtbw', 'VIDEO', 'BEGINNER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (9, @python_subject_id, 'Python 데이터 처리 기초', 'https://www.youtube.com/watch?v=LHBE6Q9XlzI', 'VIDEO', 'REFERENCE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (10, @python_subject_id, 'Python 프로젝트 연습', 'https://www.youtube.com/watch?v=8ext9G7xspg', 'VIDEO', 'TRENDING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (11, @web_subject_id, 'HTML CSS 입문 풀코스', 'https://www.youtube.com/watch?v=mU6anWqZJcc', 'VIDEO', 'BEGINNER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (12, @web_subject_id, 'JavaScript 입문 풀코스', 'https://www.youtube.com/watch?v=PkZNo7MFNFg', 'VIDEO', 'REFERENCE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (13, @web_subject_id, '반응형 웹 레이아웃 연습', 'https://www.youtube.com/watch?v=srvUrASNj0s', 'VIDEO', 'TRENDING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (14, @sql_subject_id, 'SQL 입문 풀코스', 'https://www.youtube.com/watch?v=HXV3zeQKqGY', 'VIDEO', 'BEGINNER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (15, @sql_subject_id, 'MySQL 기초 정리', 'https://www.youtube.com/watch?v=7S_tz1z_5bA', 'VIDEO', 'REFERENCE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (16, @sql_subject_id, 'SQL JOIN 복습', 'https://www.youtube.com/watch?v=9yeOJ0ZMUYw', 'VIDEO', 'TRENDING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  subject_id = VALUES(subject_id),
  title = VALUES(title),
  url = VALUES(url),
  content_type = VALUES(content_type),
  recommendation_slot = VALUES(recommendation_slot),
  is_active = VALUES(is_active),
  updated_at = CURRENT_TIMESTAMP;



-- -----------------------------------------------------------------------------
-- Demo account fixture reconciliation (v2.5.4)
-- -----------------------------------------------------------------------------
-- The seed temporarily uses legacy fixture identities while building related
-- records. Before COMMIT, all demo content is reassigned to the final user and
-- login/profile/curriculum data is rebuilt from users_data.sql.
-- Final accounts: admin (user_id = 1), sample general learner (user_id = 2),
-- sample Premium learner (user_id = 3), and demo user (user_id = 6).
-- This script disables sql_safe_updates only for its current session because
-- MySQL Workbench can reject a key predicate when the optimizer selects a scan.
-- Only temporary OAuth/community fixture users (user_id 4 through 53) are
-- reconciled. Users 2 and 3 keep their original sample-data records.

UPDATE lessons
SET created_by = 1
WHERE created_by BETWEEN 4 AND 53
  AND lesson_id > 0;

UPDATE practice_problems
SET created_by = 1
WHERE created_by BETWEEN 4 AND 53
  AND problem_id > 0;

UPDATE lesson_bookmarks
SET user_id = 6
WHERE user_id BETWEEN 4 AND 53
  AND bookmark_id > 0;

UPDATE level_test_attempts
SET user_id = 6
WHERE user_id BETWEEN 4 AND 53
  AND attempt_id > 0;

UPDATE practice_set_attempts
SET user_id = 6
WHERE user_id BETWEEN 4 AND 53
  AND set_attempt_id > 0;

UPDATE practice_submissions
SET user_id = 6
WHERE user_id BETWEEN 4 AND 53
  AND submission_id > 0;

UPDATE score_events
SET user_id = 6
WHERE user_id BETWEEN 4 AND 53
  AND score_event_id > 0;

UPDATE exam_sessions
SET user_id = 6
WHERE user_id BETWEEN 4 AND 53
  AND exam_id > 0;

UPDATE ai_analysis_reports
SET user_id = 6
WHERE user_id BETWEEN 4 AND 53
  AND report_id > 0;

UPDATE community_posts
SET writer_id = 6
WHERE writer_id BETWEEN 4 AND 53
  AND post_id > 0;

UPDATE post_attachments
SET uploader_id = 6
WHERE uploader_id BETWEEN 4 AND 53
  AND attachment_id > 0;

UPDATE comments
SET writer_id = 6
WHERE writer_id BETWEEN 4 AND 53
  AND comment_id > 0;

UPDATE comments
SET deleted_by_admin_id = 1
WHERE deleted_by_admin_id BETWEEN 4 AND 53
  AND comment_id > 0;

UPDATE reports
SET reporter_id = 6
WHERE reporter_id BETWEEN 4 AND 53
  AND report_id > 0;

UPDATE reports
SET handled_by = 1
WHERE handled_by BETWEEN 4 AND 53
  AND report_id > 0;

UPDATE dummy_payments
SET user_id = 6
WHERE user_id BETWEEN 4 AND 53
  AND payment_id > 0;

UPDATE premium_grants
SET user_id = 6
WHERE user_id BETWEEN 4 AND 53
  AND grant_id > 0;

UPDATE payment_refunds
SET user_id = 6
WHERE user_id BETWEEN 4 AND 53
  AND refund_id > 0;

UPDATE notices
SET writer_id = 1
WHERE writer_id BETWEEN 4 AND 53
  AND notice_id > 0;

UPDATE admin_operation_logs
SET admin_id = 1
WHERE admin_id BETWEEN 4 AND 53
  AND log_id > 0;

DELETE FROM post_likes
WHERE user_id BETWEEN 4 AND 53
  AND like_id > 0;

DELETE FROM post_scraps
WHERE user_id BETWEEN 4 AND 53
  AND scrap_id > 0;

DELETE FROM attendance_records
WHERE user_id BETWEEN 4 AND 53
  AND attendance_id > 0;

DELETE FROM wrong_answers
WHERE user_id BETWEEN 4 AND 53
  AND wrong_answer_id > 0;

DELETE FROM ranking_scores
WHERE user_id BETWEEN 4 AND 53
  AND ranking_id > 0;

DELETE FROM password_reset_tokens
WHERE user_id BETWEEN 4 AND 53
  AND token_id > 0;

DELETE FROM social_accounts
WHERE user_id BETWEEN 4 AND 53
  AND social_account_id > 0;

DELETE FROM user_credentials
WHERE user_id BETWEEN 4 AND 53
  AND credential_id > 0;

DELETE FROM user_settings
WHERE user_id BETWEEN 4 AND 53
  AND setting_id > 0;

DELETE FROM user_learning_profiles
WHERE user_id BETWEEN 4 AND 53
  AND profile_id > 0;

DELETE FROM user_subject_enrollments
WHERE user_id BETWEEN 4 AND 53
  AND enrollment_id > 0;

DELETE FROM user_lesson_progress
WHERE user_id BETWEEN 4 AND 53
  AND lesson_progress_id > 0;

DELETE FROM learning_progress
WHERE user_id BETWEEN 4 AND 53
  AND progress_id > 0;

DELETE FROM user_level_unlocks
WHERE user_id BETWEEN 4 AND 53
  AND unlock_id > 0;

DELETE FROM users
WHERE user_id BETWEEN 4 AND 53
  AND user_id <> 6;

-- Rebuild only the final demo account and curriculum fixture after temporary
-- identities and related rows have been reconciled.
UPDATE user_credentials
SET password_hash = '$2y$10$U2ho1jIzQKb7PGRQPJE9w.GWHsQyfDGuE3ZC6FDyxZ33E57MZhvDq',
    password_updated_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE login_email = 'admin@knowva.local'
  AND credential_id > 0;

INSERT INTO users (
  user_id, email, nickname, role, status, profile_image_url, last_login_at, withdrawn_at, created_at, updated_at
)
VALUES
  (6, 'hg102938363736@gmail.com', '이현겸', 'ROLE_USER', 'ACTIVE', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  email = VALUES(email),
  nickname = VALUES(nickname),
  role = VALUES(role),
  status = VALUES(status),
  profile_image_url = VALUES(profile_image_url),
  last_login_at = VALUES(last_login_at),
  withdrawn_at = VALUES(withdrawn_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_credentials (
  credential_id, user_id, login_email, password_hash, email_verified_at, password_updated_at, failed_login_count, locked_until, created_at, updated_at
)
VALUES
  (4, 6, 'hg102938363736@gmail.com', '$2y$10$19xlEkYp72sWGY6EmxavCeUeDKZhjTJIZwohv2DWVdenpCbKGAMmm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  login_email = VALUES(login_email),
  password_hash = VALUES(password_hash),
  email_verified_at = VALUES(email_verified_at),
  password_updated_at = VALUES(password_updated_at),
  failed_login_count = VALUES(failed_login_count),
  locked_until = VALUES(locked_until),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_settings (
  setting_id, user_id, theme, notification_enabled, accessibility_mode, reduced_motion_enabled, created_at, updated_at
)
VALUES
  (6, 6, 'SYSTEM', 1, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  theme = VALUES(theme),
  notification_enabled = VALUES(notification_enabled),
  accessibility_mode = VALUES(accessibility_mode),
  reduced_motion_enabled = VALUES(reduced_motion_enabled),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_learning_profiles (
  profile_id, user_id, primary_subject_id, learning_goal, current_level_code, total_score, grade_code, created_at, updated_at
)
VALUES
  (6, 6, @java_subject_id, 'Java 기초를 탄탄히 다지고 실전 문제 해결 능력을 키우고 싶습니다.', 'SILVER', 1450, 'INTERMEDIATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  primary_subject_id = VALUES(primary_subject_id),
  learning_goal = VALUES(learning_goal),
  current_level_code = VALUES(current_level_code),
  total_score = VALUES(total_score),
  grade_code = VALUES(grade_code),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_level_unlocks (
  unlock_id, user_id, subject_id, level_code, unlock_source, unlocked_by_exam_id, unlocked_at, created_at
)
VALUES
  (600001, 6, @java_subject_id, 'BRONZE', 'ONBOARDING', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (600002, 6, @java_subject_id, 'SILVER', 'ADMIN_ADJUST', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  level_code = VALUES(level_code),
  unlock_source = VALUES(unlock_source),
  unlocked_by_exam_id = VALUES(unlocked_by_exam_id),
  unlocked_at = VALUES(unlocked_at);

INSERT INTO user_subject_enrollments (
  user_id, subject_id, status, start_mode, enrolled_at
)
VALUES
  (6, @java_subject_id, 'ACTIVE', 'BASIC', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO learning_progress (
  progress_id, user_id, subject_id, node_id, lesson_completed, practice_passed, progress_rate, completed_at, created_at, updated_at
)
SELECT
  600000 + node.node_id AS progress_id,
  6 AS user_id,
  node.subject_id,
  node.node_id,
  1 AS lesson_completed,
  IF(node.level_code = 'SILVER' AND node.planet_no = 5, 0, 1) AS practice_passed,
  IF(node.level_code = 'SILVER' AND node.planet_no = 5, 90.00, 100.00) AS progress_rate,
  IF(node.level_code = 'SILVER' AND node.planet_no = 5, NULL, CURRENT_TIMESTAMP) AS completed_at,
  CURRENT_TIMESTAMP AS created_at,
  CURRENT_TIMESTAMP AS updated_at
FROM curriculum_nodes node
WHERE node.subject_id = @java_subject_id
  AND node.node_type = 'PLANET'
  AND node.level_code IN ('BRONZE', 'SILVER')
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  node_id = VALUES(node_id),
  lesson_completed = VALUES(lesson_completed),
  practice_passed = VALUES(practice_passed),
  progress_rate = VALUES(progress_rate),
  completed_at = VALUES(completed_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_lesson_progress (
  lesson_progress_id, user_id, lesson_id, theory_completed, practice_passed, progress_rate, completed_at, created_at, updated_at
)
SELECT
  600000 + lesson.lesson_id AS lesson_progress_id,
  6 AS user_id,
  lesson.lesson_id,
  1 AS theory_completed,
  1 AS practice_passed,
  100.00 AS progress_rate,
  CURRENT_TIMESTAMP AS completed_at,
  CURRENT_TIMESTAMP AS created_at,
  CURRENT_TIMESTAMP AS updated_at
FROM lessons lesson
JOIN curriculum_nodes node ON node.node_id = lesson.node_id
WHERE node.subject_id = @java_subject_id
  AND node.node_type = 'PLANET'
  AND (
    node.level_code = 'BRONZE'
    OR (node.level_code = 'SILVER' AND node.planet_no < 5)
    OR (node.level_code = 'SILVER' AND node.planet_no = 5 AND lesson.sort_order < 10)
  )
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  lesson_id = VALUES(lesson_id),
  theory_completed = VALUES(theory_completed),
  practice_passed = VALUES(practice_passed),
  progress_rate = VALUES(progress_rate),
  completed_at = VALUES(completed_at),
  updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- Demo user practice and analysis fixture
-- -----------------------------------------------------------------------------
-- user_id=6은 Java Bronze를 마치고 Silver 5행성 9번 레슨까지 학습한 시연 계정이다.
-- 학습 진도만 두면 분석 대시보드가 기준으로 삼는 GRADED exam_sessions가 없어
-- 빈 상태가 된다. 아래 데이터는 실제 레슨 풀이와 코딩테스트 이력을 함께 만든다.
-- 고정 ID + upsert로 재실행해도 동일한 이력이 유지된다.

INSERT INTO practice_set_attempts (
  set_attempt_id, user_id, subject_id, node_id, lesson_id,
  total_count, correct_count, status, passed, completed_at, created_at, updated_at
)
VALUES
  (610110, 6, @java_subject_id, 1, 110, 10, 8, 'COMPLETED', 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 18 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 18 DAY), CURRENT_TIMESTAMP),
  (611210, 6, @java_subject_id, 12, 1210, 10, 9, 'COMPLETED', 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY), CURRENT_TIMESTAMP),
  (611509, 6, @java_subject_id, 15, 1509, 10, 8, 'COMPLETED', 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  node_id = VALUES(node_id),
  lesson_id = VALUES(lesson_id),
  total_count = VALUES(total_count),
  correct_count = VALUES(correct_count),
  status = VALUES(status),
  passed = VALUES(passed),
  completed_at = VALUES(completed_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO practice_set_items (
  set_item_id, set_attempt_id, problem_id, sort_order, created_at
)
SELECT
  (attempt.set_attempt_id * 100) + problem_step.problem_no AS set_item_id,
  attempt.set_attempt_id,
  (attempt.lesson_id * 100) + problem_step.problem_no AS problem_id,
  problem_step.problem_no,
  attempt.created_at
FROM practice_set_attempts attempt
CROSS JOIN (
  SELECT 1 AS problem_no UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
  SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
) problem_step
WHERE attempt.user_id = 6
  AND attempt.set_attempt_id IN (610110, 611210, 611509)
ON DUPLICATE KEY UPDATE
  set_attempt_id = VALUES(set_attempt_id),
  problem_id = VALUES(problem_id),
  sort_order = VALUES(sort_order);

INSERT INTO practice_submissions (
  submission_id, set_attempt_id, set_item_id, user_id, problem_id,
  submission_context, submitted_answer, is_correct, is_skipped, solved_at, created_at
)
SELECT
  (attempt.set_attempt_id * 100) + item.sort_order AS submission_id,
  attempt.set_attempt_id,
  item.set_item_id,
  attempt.user_id,
  item.problem_id,
  'PRACTICE_SET',
  CASE
    WHEN item.sort_order <= attempt.correct_count THEN problem.answer_text
    WHEN item.sort_order = 9 THEN '조건을 먼저 확인한다'
    ELSE '예외를 무시하고 계속 진행한다'
  END AS submitted_answer,
  IF(item.sort_order <= attempt.correct_count, 1, 0) AS is_correct,
  0 AS is_skipped,
  attempt.completed_at,
  attempt.created_at
FROM practice_set_attempts attempt
JOIN practice_set_items item ON item.set_attempt_id = attempt.set_attempt_id
JOIN practice_problems problem ON problem.problem_id = item.problem_id
WHERE attempt.user_id = 6
  AND attempt.set_attempt_id IN (610110, 611210, 611509)
ON DUPLICATE KEY UPDATE
  set_attempt_id = VALUES(set_attempt_id),
  set_item_id = VALUES(set_item_id),
  user_id = VALUES(user_id),
  problem_id = VALUES(problem_id),
  submission_context = VALUES(submission_context),
  submitted_answer = VALUES(submitted_answer),
  is_correct = VALUES(is_correct),
  is_skipped = VALUES(is_skipped),
  solved_at = VALUES(solved_at);

INSERT INTO wrong_answers (
  wrong_answer_id, user_id, set_attempt_id, problem_id, last_submission_id,
  wrong_count, review_status, retry_bonus_awarded, created_at, updated_at
)
VALUES
  (6101509, 6, 611509, 150909, 61150909, 2, 'OPEN', 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), CURRENT_TIMESTAMP),
  (6101510, 6, 611509, 150910, 61150910, 1, 'SOLVED', 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  set_attempt_id = VALUES(set_attempt_id),
  problem_id = VALUES(problem_id),
  last_submission_id = VALUES(last_submission_id),
  wrong_count = VALUES(wrong_count),
  review_status = VALUES(review_status),
  retry_bonus_awarded = VALUES(retry_bonus_awarded),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO exam_sessions (
  exam_id, user_id, subject_id, level_code, status, result_status,
  total_problem_count, correct_count, retry_count,
  started_at, submitted_at, graded_at, created_at, updated_at
)
VALUES
  (610001, 6, @java_subject_id, 'BRONZE', 'GRADED', 'PASSED', 3, 2, 1,
   DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), CURRENT_TIMESTAMP),
  (610002, 6, @java_subject_id, 'SILVER', 'GRADED', 'FAILED', 3, 1, 2,
   DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  subject_id = VALUES(subject_id),
  level_code = VALUES(level_code),
  status = VALUES(status),
  result_status = VALUES(result_status),
  total_problem_count = VALUES(total_problem_count),
  correct_count = VALUES(correct_count),
  retry_count = VALUES(retry_count),
  started_at = VALUES(started_at),
  submitted_at = VALUES(submitted_at),
  graded_at = VALUES(graded_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO ai_exam_problems (
  ai_problem_id, exam_id, problem_no, prompt, test_case_spec, ai_raw_response, status, created_at, updated_at
)
VALUES
  (6100011, 610001, 1, '두 정수 a, b를 받아 합을 반환하는 add 메서드를 작성하세요.',
   JSON_OBJECT('cases', JSON_ARRAY(JSON_OBJECT('input', '2, 3', 'expected', '5'), JSON_OBJECT('input', '10, -4', 'expected', '6'))),
   JSON_OBJECT('problems', JSON_ARRAY(JSON_OBJECT('starterCode', 'public static int add(int a, int b) { return 0; }'), JSON_OBJECT('starterCode', 'public static String grade(int score) { return ""; }'), JSON_OBJECT('starterCode', 'public static int countEven(int[] values) { return 0; }'))),
   'GENERATED', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), CURRENT_TIMESTAMP),
  (6100012, 610001, 2, '점수가 70점 이상이면 "PASS", 아니면 "RETRY"를 반환하는 grade 메서드를 작성하세요.',
   JSON_OBJECT('cases', JSON_ARRAY(JSON_OBJECT('input', '75', 'expected', 'PASS'), JSON_OBJECT('input', '60', 'expected', 'RETRY'))),
   JSON_OBJECT('problems', JSON_ARRAY(JSON_OBJECT('starterCode', 'public static int add(int a, int b) { return 0; }'), JSON_OBJECT('starterCode', 'public static String grade(int score) { return ""; }'), JSON_OBJECT('starterCode', 'public static int countEven(int[] values) { return 0; }'))),
   'GENERATED', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), CURRENT_TIMESTAMP),
  (6100013, 610001, 3, '정수 배열에서 짝수 개수를 반환하는 countEven 메서드를 작성하세요.',
   JSON_OBJECT('cases', JSON_ARRAY(JSON_OBJECT('input', '[1, 2, 4, 7]', 'expected', '2'))),
   JSON_OBJECT('problems', JSON_ARRAY(JSON_OBJECT('starterCode', 'public static int add(int a, int b) { return 0; }'), JSON_OBJECT('starterCode', 'public static String grade(int score) { return ""; }'), JSON_OBJECT('starterCode', 'public static int countEven(int[] values) { return 0; }'))),
   'GENERATED', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), CURRENT_TIMESTAMP),
  (6100021, 610002, 1, '문자열 배열에서 빈 문자열을 제외하고 가장 긴 값을 반환하는 longestWord 메서드를 작성하세요.',
   JSON_OBJECT('cases', JSON_ARRAY(JSON_OBJECT('input', '["java", "", "spring"]', 'expected', 'spring'))),
   JSON_OBJECT('problems', JSON_ARRAY(JSON_OBJECT('starterCode', 'public static String longestWord(String[] words) { return ""; }'), JSON_OBJECT('starterCode', 'public static int safeDivide(int a, int b) { return 0; }'), JSON_OBJECT('starterCode', 'public static String readText(Path path) throws IOException { return ""; }'))),
   'GENERATED', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (6100022, 610002, 2, 'b가 0이면 0을 반환하고, 아니면 a를 b로 나눈 값을 반환하는 safeDivide 메서드를 작성하세요.',
   JSON_OBJECT('cases', JSON_ARRAY(JSON_OBJECT('input', '10, 2', 'expected', '5'), JSON_OBJECT('input', '10, 0', 'expected', '0'))),
   JSON_OBJECT('problems', JSON_ARRAY(JSON_OBJECT('starterCode', 'public static String longestWord(String[] words) { return ""; }'), JSON_OBJECT('starterCode', 'public static int safeDivide(int a, int b) { return 0; }'), JSON_OBJECT('starterCode', 'public static String readText(Path path) throws IOException { return ""; }'))),
   'GENERATED', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (6100023, 610002, 3, 'Path를 받아 UTF-8 텍스트 파일의 내용을 읽어 반환하는 readText 메서드를 작성하세요.',
   JSON_OBJECT('cases', JSON_ARRAY(JSON_OBJECT('input', 'memo.txt', 'expected', 'file content'))),
   JSON_OBJECT('problems', JSON_ARRAY(JSON_OBJECT('starterCode', 'public static String longestWord(String[] words) { return ""; }'), JSON_OBJECT('starterCode', 'public static int safeDivide(int a, int b) { return 0; }'), JSON_OBJECT('starterCode', 'public static String readText(Path path) throws IOException { return ""; }'))),
   'GENERATED', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  exam_id = VALUES(exam_id),
  problem_no = VALUES(problem_no),
  prompt = VALUES(prompt),
  test_case_spec = VALUES(test_case_spec),
  ai_raw_response = VALUES(ai_raw_response),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO exam_answers (
  answer_id, exam_id, ai_problem_id, answer_text, passed_case_count, is_correct,
  ai_review, test_case_result, submitted_at, graded_at, created_at, updated_at
)
VALUES
  (6100011, 610001, 6100011, 'public static int add(int a, int b) { return a + b; }', 2, 1,
   '기본 연산과 반환값 처리가 정확합니다.', JSON_OBJECT('passed', 2, 'total', 2), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), CURRENT_TIMESTAMP),
  (6100012, 610001, 6100012, 'public static String grade(int score) { return score > 70 ? "PASS" : "RETRY"; }', 1, 0,
   '경계값 70에서 PASS가 되어야 합니다. 조건식의 비교 연산자를 다시 확인하세요.', JSON_OBJECT('passed', 1, 'total', 2), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), CURRENT_TIMESTAMP),
  (6100013, 610001, 6100013, 'public static int countEven(int[] values) { int count = 0; for (int value : values) { if (value % 2 == 0) count++; } return count; }', 1, 1,
   '배열 순회와 짝수 판별을 올바르게 구현했습니다.', JSON_OBJECT('passed', 1, 'total', 1), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), CURRENT_TIMESTAMP),
  (6100021, 610002, 6100021, 'public static String longestWord(String[] words) { return words[0]; }', 0, 0,
   '배열 전체를 순회하지 않아 가장 긴 문자열을 찾지 못했습니다. 빈 문자열도 제외해야 합니다.', JSON_OBJECT('passed', 0, 'total', 1), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (6100022, 610002, 6100022, 'public static int safeDivide(int a, int b) { return b == 0 ? 0 : a / b; }', 2, 1,
   '0으로 나누는 경우를 먼저 막아 예외 없이 처리했습니다.', JSON_OBJECT('passed', 2, 'total', 2), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (6100023, 610002, 6100023, 'public static String readText(Path path) throws IOException { return ""; }', 0, 0,
   '파일 읽기 로직과 IOException 처리가 빠져 있습니다. Files.readString 사용을 연습해 보세요.', JSON_OBJECT('passed', 0, 'total', 1), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  exam_id = VALUES(exam_id),
  ai_problem_id = VALUES(ai_problem_id),
  answer_text = VALUES(answer_text),
  passed_case_count = VALUES(passed_case_count),
  is_correct = VALUES(is_correct),
  ai_review = VALUES(ai_review),
  test_case_result = VALUES(test_case_result),
  submitted_at = VALUES(submitted_at),
  graded_at = VALUES(graded_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO ai_analysis_reports (
  report_id, user_id, exam_id, status, free_summary, premium_detail,
  analysis_error_code, retry_count, created_at, updated_at
)
VALUES
  (610001, 6, 610001, 'SUCCESS',
   '기본 연산과 배열 순회는 안정적입니다. 조건 경계값을 한 번 더 점검하면 정확도가 높아집니다.',
   JSON_OBJECT('strengths', JSON_ARRAY('기본 연산', '배열 순회'), 'weaknesses', JSON_ARRAY('조건 경계값'), 'nextActions', JSON_ARRAY('70점 경계값 문제 3개 복습')),
   NULL, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY), CURRENT_TIMESTAMP),
  (610002, 6, 610002, 'SUCCESS',
   '입력 검증은 잘 처리했지만, 배열 전체 순회와 파일 입출력 구현이 아직 부족합니다.',
   JSON_OBJECT('strengths', JSON_ARRAY('입력 검증', '예외 예방'), 'weaknesses', JSON_ARRAY('배열 순회', '파일 입출력'), 'nextActions', JSON_ARRAY('문자열 배열 순회 문제 3개', 'Files.readString 예제 복습')),
   NULL, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  exam_id = VALUES(exam_id),
  status = VALUES(status),
  free_summary = VALUES(free_summary),
  premium_detail = VALUES(premium_detail),
  analysis_error_code = VALUES(analysis_error_code),
  retry_count = VALUES(retry_count),
  updated_at = CURRENT_TIMESTAMP;

UPDATE user_level_unlocks
SET unlock_source = 'AI_EXAM',
    unlocked_by_exam_id = 610001,
    unlocked_at = DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY)
WHERE user_id = 6
  AND subject_id = @java_subject_id
  AND level_code = 'SILVER'
  AND unlock_id > 0;

-- -----------------------------------------------------------------------------
-- Ranking demo fixture
-- -----------------------------------------------------------------------------
-- /ranking의 WEEKLY 과목별·MONTHLY 통합 탭을 함께 검증하는 전용 계정 20개다.
-- ranking_scores를 직접 고정하지 않고 score_events·exam_sessions를 만든 뒤,
-- 서비스의 집계식과 같은 계산으로 점수를 채운다.
-- 예약 범위 200~224는 본 셋업의 계정 범위(1~53)와 겹치지 않는다.
SET @weekly_period_key = DATE_FORMAT(CURRENT_DATE, '%x-W%v');
SET @monthly_period_key = DATE_FORMAT(CURRENT_DATE, '%Y-%m');

-- FK 순서상 자식 테이블부터 삭제해 반복 실행도 동일한 결과를 만든다.
DELETE FROM ranking_scores WHERE user_id BETWEEN 200 AND 224;
DELETE FROM exam_sessions WHERE user_id BETWEEN 200 AND 224;
DELETE FROM score_events WHERE user_id BETWEEN 200 AND 224;
DELETE FROM user_learning_profiles WHERE user_id BETWEEN 200 AND 224;
DELETE FROM users WHERE user_id BETWEEN 200 AND 224;

INSERT INTO users (
  user_id, email, nickname, role, status, profile_image_url, last_login_at, withdrawn_at, created_at, updated_at
)
VALUES
  (200, 'ranking-demo-01@example.invalid', '자취생라면러버', 'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (201, 'ranking-demo-02@example.invalid', '조모임팀장각',   'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (202, 'ranking-demo-03@example.invalid', '중간고사폭망',   'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (203, 'ranking-demo-04@example.invalid', '카공인간',       'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (204, 'ranking-demo-05@example.invalid', '새내기감자',     'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (205, 'ranking-demo-06@example.invalid', '복학예정',       'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (206, 'ranking-demo-07@example.invalid', '장학금헌터',     'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (207, 'ranking-demo-08@example.invalid', '아싸탈출기',     'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (208, 'ranking-demo-09@example.invalid', '도서관죽순이',   'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (209, 'ranking-demo-10@example.invalid', '밤샘과제요정',   'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (210, 'ranking-demo-11@example.invalid', '학점보수공사',   'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (211, 'ranking-demo-12@example.invalid', '개강전멘탈',     'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (212, 'ranking-demo-13@example.invalid', '출첵요정',       'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (213, 'ranking-demo-14@example.invalid', '휴학각재는중',   'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (214, 'ranking-demo-15@example.invalid', '조교눈치보기',   'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (215, 'ranking-demo-16@example.invalid', '시험기간커피',   'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (216, 'ranking-demo-17@example.invalid', '종강까지디데이', 'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (217, 'ranking-demo-18@example.invalid', '동아리부장',     'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (218, 'ranking-demo-19@example.invalid', '인강정주행러',   'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (219, 'ranking-demo-20@example.invalid', '새터생존기',     'ROLE_USER', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 15명은 과목 하나, 5명은 2~3개를 수강해 MONTHLY 통합 평균도 확인한다.
DROP TEMPORARY TABLE IF EXISTS ranking_seed_enrollment;
CREATE TEMPORARY TABLE ranking_seed_enrollment (
  user_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,
  practice_count INT NOT NULL,
  has_exam TINYINT NOT NULL
);

INSERT INTO ranking_seed_enrollment (user_id, subject_id, practice_count, has_exam) VALUES
  (200, @java_subject_id,   7, 1),
  (201, @python_subject_id, 6, 1),
  (202, @web_subject_id,    4, 0),
  (203, @sql_subject_id,    8, 1),
  (204, @java_subject_id,   3, 0),
  (205, @java_subject_id,   5, 1),
  (205, @python_subject_id, 4, 0),
  (206, @python_subject_id, 9, 1),
  (207, @web_subject_id,    5, 0),
  (208, @sql_subject_id,    7, 1),
  (209, @java_subject_id,   4, 0),
  (209, @web_subject_id,    6, 1),
  (210, @python_subject_id, 3, 1),
  (211, @web_subject_id,    6, 0),
  (212, @sql_subject_id,    4, 1),
  (213, @java_subject_id,   9, 1),
  (214, @python_subject_id, 4, 0),
  (214, @sql_subject_id,    5, 1),
  (215, @web_subject_id,    2, 0),
  (216, @sql_subject_id,    7, 1),
  (217, @java_subject_id,   3, 0),
  (217, @python_subject_id, 3, 0),
  (217, @web_subject_id,    4, 1),
  (218, @python_subject_id, 6, 1),
  (219, @sql_subject_id,    5, 0),
  (219, @java_subject_id,   4, 1);

INSERT INTO score_events (
  user_id, subject_id, source_type, source_id, score_delta, reason_code, idempotency_key, created_at
)
WITH RECURSIVE seed_numbers (n) AS (
  SELECT 1
  UNION ALL
  SELECT n + 1 FROM seed_numbers WHERE n < 9
)
SELECT
  e.user_id,
  e.subject_id,
  'PRACTICE_SET',
  sn.n,
  70,
  'PRACTICE_SET_PASS',
  CONCAT('ranking-seed:', e.user_id, ':', e.subject_id, ':practice:', sn.n),
  CURRENT_TIMESTAMP
FROM ranking_seed_enrollment e
JOIN seed_numbers sn ON sn.n <= e.practice_count;

INSERT INTO exam_sessions (
  user_id, subject_id, level_code, status, result_status, total_problem_count, correct_count, retry_count, started_at, submitted_at, graded_at, created_at, updated_at
)
SELECT
  e.user_id, e.subject_id, 'BRONZE', 'GRADED', 'PASSED', 3, 3, 0,
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM ranking_seed_enrollment e
WHERE e.has_exam = 1;

-- WEEKLY 및 MONTHLY 모두 SUBJECT scope로 저장한다. MONTHLY 통합 탭은 이 행들을 사용자별 평균낸다.
INSERT INTO ranking_scores (
  user_id, subject_id, scope_type, scope_key, period_type, period_key, score, rank_no, calculated_at, created_at, updated_at
)
SELECT
  ranked.user_id, ranked.subject_id, 'SUBJECT', ranked.subject_code, 'WEEKLY', @weekly_period_key,
  ranked.score, ranked.rank_no, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
  SELECT
    agg.user_id, agg.subject_id, s.subject_code, agg.score,
    ROW_NUMBER() OVER (PARTITION BY agg.subject_id ORDER BY agg.score DESC, agg.user_id ASC) AS rank_no
  FROM (
    SELECT
      base.user_id, base.subject_id,
      COALESCE(base.practice_score, 0) + COALESCE(exam.exam_score, 0) AS score
    FROM (
      SELECT se.user_id, se.subject_id, SUM(se.score_delta) AS practice_score
      FROM score_events se
      WHERE se.user_id BETWEEN 200 AND 219
        AND se.subject_id IS NOT NULL
        AND se.reason_code NOT LIKE 'ATTENDANCE%'
      GROUP BY se.user_id, se.subject_id
    ) base
    LEFT JOIN (
      SELECT es.user_id, es.subject_id, COUNT(*) * 200 AS exam_score
      FROM exam_sessions es
      WHERE es.user_id BETWEEN 200 AND 219
        AND es.result_status = 'PASSED'
        AND es.submitted_at IS NOT NULL
      GROUP BY es.user_id, es.subject_id
    ) exam ON exam.user_id = base.user_id AND exam.subject_id = base.subject_id
  ) agg
  INNER JOIN subjects s ON s.subject_id = agg.subject_id
) ranked;

INSERT INTO ranking_scores (
  user_id, subject_id, scope_type, scope_key, period_type, period_key, score, rank_no, calculated_at, created_at, updated_at
)
SELECT
  ranked.user_id, ranked.subject_id, 'SUBJECT', ranked.subject_code, 'MONTHLY', @monthly_period_key,
  ranked.score, ranked.rank_no, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
  SELECT
    agg.user_id, agg.subject_id, s.subject_code, agg.score,
    ROW_NUMBER() OVER (PARTITION BY agg.subject_id ORDER BY agg.score DESC, agg.user_id ASC) AS rank_no
  FROM (
    SELECT
      base.user_id, base.subject_id,
      COALESCE(base.practice_score, 0) + COALESCE(exam.exam_score, 0) AS score
    FROM (
      SELECT se.user_id, se.subject_id, SUM(se.score_delta) AS practice_score
      FROM score_events se
      WHERE se.user_id BETWEEN 200 AND 219
        AND se.subject_id IS NOT NULL
        AND se.reason_code NOT LIKE 'ATTENDANCE%'
      GROUP BY se.user_id, se.subject_id
    ) base
    LEFT JOIN (
      SELECT es.user_id, es.subject_id, COUNT(*) * 200 AS exam_score
      FROM exam_sessions es
      WHERE es.user_id BETWEEN 200 AND 219
        AND es.result_status = 'PASSED'
        AND es.submitted_at IS NOT NULL
      GROUP BY es.user_id, es.subject_id
    ) exam ON exam.user_id = base.user_id AND exam.subject_id = base.subject_id
  ) agg
  INNER JOIN subjects s ON s.subject_id = agg.subject_id
) ranked;

DROP TEMPORARY TABLE IF EXISTS ranking_seed_enrollment;

COMMIT;

SET SESSION sql_safe_updates = @knowva_seed_sql_safe_updates;

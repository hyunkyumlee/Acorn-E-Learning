/*
  Knowva sample data - MySQL 8 / InnoDB / utf8mb4
  Source: Notion DB 명세 v1.9 / 레슨 단위 학습 구조
  Execute after docs/ddl/Knowva_DDL.sql.

  Execution contract
  - This is sample seed data, not a schema migration.
  - Run the full Knowva_DDL.sql first when resetting a local development DB.
  - A schema preflight runs before the first INSERT. If it fails, update the schema first.
  - All seed INSERT/UPDATE statements run in one transaction to prevent partial application.

  Sample login accounts
  - admin@knowva.local / Knowva1234! / ROLE_ADMIN
  - learner@knowva.local / Knowva1234! / ROLE_USER
  - premium@knowva.local / Knowva1234! / ROLE_USER + ACTIVE premium_grant
  - google OAuth sample: learner@knowva.local provider email, separate user_id, no password credential
  - github OAuth sample: learner@knowva.local provider email, separate user_id, no password credential

  Password hash
  - BCrypt(10), generated with Spring Security Crypto 7.0.5.
  - Plain sample password is only for local development/demo data.
*/

SET NAMES utf8mb4;
SET time_zone = '+09:00';

USE elearning;

DROP PROCEDURE IF EXISTS assert_knowva_seed_schema;
DELIMITER $$
CREATE PROCEDURE assert_knowva_seed_schema()
BEGIN
  DECLARE required_column_count INT DEFAULT 0;
  DECLARE pending_status_default_count INT DEFAULT 0;

  SELECT COUNT(*)
    INTO required_column_count
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND (table_name, column_name) IN (
      ('community_posts', 'view_count'),
      ('user_credentials', 'login_email'),
      ('user_credentials', 'email_verified_at'),
      ('social_accounts', 'provider_email_verified'),
      ('lessons', 'node_id'),
      ('practice_set_attempts', 'lesson_id'),
      ('practice_set_items', 'set_item_id'),
      ('subject_content_status_backups', 'backup_id'),
      ('dummy_payments', 'pg_provider'),
      ('dummy_payments', 'pg_transaction_id')
    );

  SELECT COUNT(*)
    INTO pending_status_default_count
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dummy_payments'
    AND column_name = 'payment_status'
    AND column_default = 'PENDING';

  IF required_column_count <> 10 OR pending_status_default_count <> 1 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Knowva sample data requires the current Knowva_DDL.sql. Run Knowva_DDL.sql first.';
  END IF;
END$$
DELIMITER ;

CALL assert_knowva_seed_schema();
DROP PROCEDURE assert_knowva_seed_schema;

START TRANSACTION;

SET @sample_password_hash = '$2a$10$rXWwp4H7mIiDJv.c2H9moOtZ3m/8cBMGOsMuaX0G7vW/A1W3tPCxy';

DELETE FROM subject_content_status_backups;

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
  (1, 2, @java_subject_id, 'QUESTION', '조건문에서 else if를 언제 쓰나요?', '점수 구간별로 다른 메시지를 보여주고 싶습니다.', 28, 1, 2, 1, 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), CURRENT_TIMESTAMP, NULL),
  (2, 3, @java_subject_id, 'STUDY_LOG', 'Bronze 5개 행성 완료 기록', '오늘 Java Bronze 행성을 모두 완료하고 AI 시험까지 통과했습니다.', 21, 1, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (3, 1, @java_subject_id, 'FREE', '샘플 데이터 안내', '로컬 개발용 샘플 게시글입니다. 관리자 화면 검수에 사용합니다.', 12, 0, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)
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
  comment_id, post_id, parent_comment_id, writer_id, content, status, created_at, updated_at, deleted_at
)
VALUES
  (1, 1, NULL, 3, '조건이 여러 구간으로 나뉘면 else if를 쓰면 됩니다.', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (2, 1, 1, 2, '예시 감사합니다. 점수 구간으로 적용해보겠습니다.', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (3, 2, NULL, 2, '축하합니다. 저도 Bronze부터 따라가겠습니다.', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)
ON DUPLICATE KEY UPDATE
  post_id = VALUES(post_id),
  parent_comment_id = VALUES(parent_comment_id),
  writer_id = VALUES(writer_id),
  content = VALUES(content),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP,
  deleted_at = VALUES(deleted_at);

INSERT INTO post_likes (like_id, post_id, user_id, created_at)
VALUES
  (1, 1, 3, CURRENT_TIMESTAMP),
  (2, 2, 2, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  post_id = VALUES(post_id),
  user_id = VALUES(user_id);

INSERT INTO post_scraps (scrap_id, post_id, user_id, created_at)
VALUES
  (1, 1, 2, CURRENT_TIMESTAMP)
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
  log_id, admin_id, action_type, target_type, target_id, result_status, created_at
)
VALUES
  (1, 1, 'CREATE_NOTICE', 'NOTICE', 1, 'SUCCESS', CURRENT_TIMESTAMP),
  (2, 1, 'HANDLE_REPORT', 'REPORT', 1, 'SUCCESS', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  admin_id = VALUES(admin_id),
  action_type = VALUES(action_type),
  target_type = VALUES(target_type),
  target_id = VALUES(target_id),
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

COMMIT;

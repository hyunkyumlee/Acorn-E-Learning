/*
  Knowva sample data - MySQL 8 / InnoDB / utf8mb4
  Execute after docs/ddl/Knowva_DDL.sql.

  Sample login accounts
  - admin@knowva.local / Knowva1234! / ROLE_ADMIN
  - learner@knowva.local / Knowva1234! / ROLE_USER
  - premium@knowva.local / Knowva1234! / ROLE_USER + ACTIVE premium_grant

  Password hash
  - BCrypt(10), generated with Spring Security Crypto 7.0.5.
  - Plain sample password is only for local development/demo data.
*/

SET NAMES utf8mb4;
SET time_zone = '+09:00';

USE elearning;

SET @sample_password_hash = '$2a$10$rXWwp4H7mIiDJv.c2H9moOtZ3m/8cBMGOsMuaX0G7vW/A1W3tPCxy';

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
  (3, 'premium@knowva.local', '프리미엄학습자', 'ROLE_USER', 'ACTIVE', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
  credential_id, user_id, password_hash, password_updated_at, failed_login_count, locked_until, created_at, updated_at
)
VALUES
  (1, 1, @sample_password_hash, CURRENT_TIMESTAMP, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 2, @sample_password_hash, CURRENT_TIMESTAMP, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 3, @sample_password_hash, CURRENT_TIMESTAMP, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  password_hash = VALUES(password_hash),
  password_updated_at = VALUES(password_updated_at),
  failed_login_count = VALUES(failed_login_count),
  locked_until = VALUES(locked_until),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO social_accounts (
  social_account_id, user_id, provider, provider_user_id, provider_email, is_active, connected_at, disconnected_at
)
VALUES
  (1, 3, 'GOOGLE', 'sample-google-premium-001', 'premium@knowva.local', 1, CURRENT_TIMESTAMP, NULL)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  provider = VALUES(provider),
  provider_user_id = VALUES(provider_user_id),
  provider_email = VALUES(provider_email),
  is_active = VALUES(is_active),
  connected_at = VALUES(connected_at),
  disconnected_at = VALUES(disconnected_at);

INSERT INTO user_settings (
  setting_id, user_id, theme, notification_enabled, accessibility_mode, reduced_motion_enabled, created_at, updated_at
)
VALUES
  (1, 1, 'SYSTEM', 1, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 2, 'LIGHT', 1, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 3, 'DARK', 1, 'HIGH_CONTRAST', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
  (3, 3, @java_subject_id, 'AI 코딩테스트와 Premium 분석을 확인합니다.', 'SILVER', 870, 'INTERMEDIATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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

INSERT INTO lessons (
  lesson_id, node_id, title, summary, content, example_code, sort_order, is_active, created_by, created_at, updated_at
)
VALUES
  (1, 1, '변수 선언과 출력', '값을 저장하고 출력하는 첫 Java 문법입니다.', '변수는 값을 저장하는 이름입니다. 자료형을 먼저 쓰고 변수명을 작성합니다.', 'int score = 100;\nSystem.out.println(score);', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 2, '조건문으로 흐름 나누기', '조건에 따라 다른 코드를 실행합니다.', 'if 문은 조건식이 true일 때 코드 블록을 실행합니다.', 'if (score >= 70) {\n    System.out.println("pass");\n}', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 3, '반복문으로 여러 번 실행하기', '같은 코드를 반복 실행합니다.', 'for 문은 시작값, 조건식, 증감식을 한 줄에 작성합니다.', 'for (int i = 0; i < 3; i++) {\n    System.out.println(i);\n}', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 4, '배열에 여러 값 담기', '같은 자료형 값을 묶어서 저장합니다.', '배열은 index를 사용해 값에 접근합니다.', 'int[] scores = {80, 90, 100};\nSystem.out.println(scores[0]);', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 5, '메서드로 코드 나누기', '반복되는 코드를 이름 붙여 분리합니다.', '메서드는 입력값을 받아 결과를 반환할 수 있습니다.', 'int add(int a, int b) {\n    return a + b;\n}', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  node_id = VALUES(node_id),
  title = VALUES(title),
  summary = VALUES(summary),
  content = VALUES(content),
  example_code = VALUES(example_code),
  sort_order = VALUES(sort_order),
  is_active = VALUES(is_active),
  created_by = VALUES(created_by),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO lesson_bookmarks (bookmark_id, user_id, lesson_id, created_at)
VALUES
  (1, 2, 1, CURRENT_TIMESTAMP),
  (2, 3, 3, CURRENT_TIMESTAMP)
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

INSERT INTO practice_set_attempts (
  set_attempt_id, user_id, node_id, total_count, correct_count, status, passed, completed_at, created_at, updated_at
)
VALUES
  (1, 2, 1, 10, 7, 'COMPLETED', 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (2, 3, 5, 10, 9, 'COMPLETED', 1, CURRENT_TIMESTAMP, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  node_id = VALUES(node_id),
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
  problem_id, subject_id, node_id, problem_type, question, answer_text, difficulty_code, created_by, is_active, created_at, updated_at
)
VALUES
  (1, @java_subject_id, 1, 'MULTIPLE_CHOICE', '정수 변수 score를 선언하는 코드로 올바른 것은?', 'int score = 10;', 'BRONZE', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, @java_subject_id, 1, 'FILL_BLANK', '문자열 name을 저장하려면 어떤 자료형을 사용하나요?', 'String', 'BRONZE', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, @java_subject_id, 2, 'MULTIPLE_CHOICE', 'score가 70 이상이면 pass를 출력하는 조건은?', 'score >= 70', 'BRONZE', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, @java_subject_id, 2, 'CODE_SHORT', '정수 n이 짝수면 true를 반환하는 조건식을 작성하세요.', 'n % 2 == 0', 'BRONZE', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, @java_subject_id, 3, 'MULTIPLE_CHOICE', '0부터 4까지 5번 반복하는 for 조건은?', 'i < 5', 'BRONZE', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, @java_subject_id, 3, 'CODE_SHORT', '1부터 5까지 합을 구할 때 누적 변수에 더하는 코드를 작성하세요.', 'sum += i;', 'BRONZE', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (7, @java_subject_id, 4, 'MULTIPLE_CHOICE', '배열 arr의 길이를 구하는 표현식은?', 'arr.length', 'BRONZE', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (8, @java_subject_id, 4, 'FILL_BLANK', '배열 첫 번째 값을 읽는 index는?', '0', 'BRONZE', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (9, @java_subject_id, 5, 'MULTIPLE_CHOICE', '메서드 결과를 반환하는 keyword는?', 'return', 'BRONZE', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (10, @java_subject_id, 5, 'CODE_SHORT', '두 정수 a, b의 합을 반환하는 문장을 작성하세요.', 'return a + b;', 'BRONZE', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  subject_id = VALUES(subject_id),
  node_id = VALUES(node_id),
  problem_type = VALUES(problem_type),
  question = VALUES(question),
  answer_text = VALUES(answer_text),
  difficulty_code = VALUES(difficulty_code),
  created_by = VALUES(created_by),
  is_active = VALUES(is_active),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO problem_choices (choice_id, problem_id, choice_label, choice_text, is_correct, sort_order)
VALUES
  (1, 1, 'A', 'int score = 10;', 1, 1), (2, 1, 'B', 'score int = 10;', 0, 2), (3, 1, 'C', 'number score = 10;', 0, 3), (4, 1, 'D', 'var score int;', 0, 4),
  (5, 3, 'A', 'score = 70', 0, 1), (6, 3, 'B', 'score >= 70', 1, 2), (7, 3, 'C', 'score < 70', 0, 3), (8, 3, 'D', 'score ! 70', 0, 4),
  (9, 5, 'A', 'i <= 5', 0, 1), (10, 5, 'B', 'i < 5', 1, 2), (11, 5, 'C', 'i == 5', 0, 3), (12, 5, 'D', 'i != 0', 0, 4),
  (13, 7, 'A', 'arr.size', 0, 1), (14, 7, 'B', 'arr.count', 0, 2), (15, 7, 'C', 'arr.length', 1, 3), (16, 7, 'D', 'length(arr)', 0, 4),
  (17, 9, 'A', 'break', 0, 1), (18, 9, 'B', 'continue', 0, 2), (19, 9, 'C', 'return', 1, 3), (20, 9, 'D', 'print', 0, 4)
ON DUPLICATE KEY UPDATE
  problem_id = VALUES(problem_id),
  choice_label = VALUES(choice_label),
  choice_text = VALUES(choice_text),
  is_correct = VALUES(is_correct),
  sort_order = VALUES(sort_order);

INSERT INTO practice_submissions (
  submission_id, set_attempt_id, user_id, problem_id, submission_context, submitted_answer, is_correct, is_skipped, solved_at, created_at
)
VALUES
  (1, 1, 2, 1, 'PRACTICE_SET', 'int score = 10;', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (2, 1, 2, 2, 'PRACTICE_SET', 'String', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (3, 1, 2, 3, 'PRACTICE_SET', 'score = 70', 0, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (4, 1, 2, 4, 'PRACTICE_SET', 'n / 2 == 0', 0, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (5, 1, 2, 5, 'PRACTICE_SET', 'i < 5', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (6, 1, 2, 6, 'PRACTICE_SET', 'sum += i;', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (7, 1, 2, 7, 'PRACTICE_SET', 'arr.length', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (8, 1, 2, 8, 'PRACTICE_SET', '0', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (9, 1, 2, 9, 'PRACTICE_SET', 'return', 1, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP),
  (10, 1, 2, 10, 'PRACTICE_SET', 'return a - b;', 0, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  set_attempt_id = VALUES(set_attempt_id),
  user_id = VALUES(user_id),
  problem_id = VALUES(problem_id),
  submission_context = VALUES(submission_context),
  submitted_answer = VALUES(submitted_answer),
  is_correct = VALUES(is_correct),
  is_skipped = VALUES(is_skipped),
  solved_at = VALUES(solved_at);

INSERT INTO wrong_answers (
  wrong_answer_id, user_id, problem_id, last_submission_id, wrong_count, review_status, retry_bonus_awarded, created_at, updated_at
)
VALUES
  (1, 2, 3, 3, 1, 'OPEN', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 2, 10, 10, 1, 'OPEN', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
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
  (1, 3, @java_subject_id, 'SUBJECT', 'JAVA', 'WEEKLY', DATE_FORMAT(CURRENT_DATE, '%x-W%v'), 870, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
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
  post_id, writer_id, subject_id, board_type, title, content, like_count, comment_count, scrap_count, status, created_at, updated_at, deleted_at
)
VALUES
  (1, 2, @java_subject_id, 'QUESTION', '조건문에서 else if를 언제 쓰나요?', '점수 구간별로 다른 메시지를 보여주고 싶습니다.', 1, 2, 1, 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), CURRENT_TIMESTAMP, NULL),
  (2, 3, @java_subject_id, 'STUDY_LOG', 'Bronze 5개 행성 완료 기록', '오늘 Java Bronze 행성을 모두 완료하고 AI 시험까지 통과했습니다.', 1, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  (3, 1, @java_subject_id, 'FREE', '샘플 데이터 안내', '로컬 개발용 샘플 게시글입니다. 관리자 화면 검수에 사용합니다.', 0, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)
ON DUPLICATE KEY UPDATE
  writer_id = VALUES(writer_id),
  subject_id = VALUES(subject_id),
  board_type = VALUES(board_type),
  title = VALUES(title),
  content = VALUES(content),
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
  payment_id, order_no, user_id, product_id, payment_method, payment_status, amount, paid_at, created_at, updated_at
)
VALUES
  (1, 'SEED-PREMIUM-0001', 3, @premium_product_id, 'CARD', 'PAID', 9900.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  order_no = VALUES(order_no),
  user_id = VALUES(user_id),
  product_id = VALUES(product_id),
  payment_method = VALUES(payment_method),
  payment_status = VALUES(payment_status),
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
  (4, @sql_subject_id, 'MySQL 8 Reference Manual', 'https://dev.mysql.com/doc/refman/8.0/en/', 'DOCS', 'REFERENCE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  subject_id = VALUES(subject_id),
  title = VALUES(title),
  url = VALUES(url),
  content_type = VALUES(content_type),
  recommendation_slot = VALUES(recommendation_slot),
  is_active = VALUES(is_active),
  updated_at = CURRENT_TIMESTAMP;

/*
  테스트 계정 목록용 데모 계정의 실제 DB 비밀번호를 화면 표시/복사용 기본값(Knowva1234!)과 분리한다.

  Demo accounts covered
  - admin@knowva.local (기존 user_id 1, ROLE_ADMIN) - password_hash만 갱신
  - hg102938363736@gmail.com (신규 user_id 6, ROLE_USER) - 계정 신규 생성

  Password policy
  - 테스트 계정 목록 UI는 계속 "Knowva1234!"를 표시/복사값으로 유지한다.
  - 아래 password_hash는 두 계정 모두 Knowva1234!가 아닌 실제 값이다.
  - BCrypt(10), $2y$ prefix (htpasswd -B로 생성). jBCrypt 기준
    $2a$/$2b$/$2y$는 동일하게 검증되므로 Spring Security BCryptPasswordEncoder와 호환.
  - 실제 평문 비밀번호 (화면에 노출 금지, 별도 보관):
      admin@knowva.local        -> KnowvaAdmin12!
      hg102938363736@gmail.com  -> KnowvaUser12!

  Idempotency
  - INSERT ... ON DUPLICATE KEY UPDATE 패턴이라 몇 번을 실행해도 안전하다.
*/

SET NAMES utf8mb4;
SET time_zone = '+09:00';

USE elearning;

START TRANSACTION;

SELECT subject_id INTO @java_subject_id FROM subjects WHERE subject_code = 'JAVA';

-- [수정] admin@knowva.local: 기존 계정, 비밀번호 해시만 화면 기본값과 다른 값으로 교체
UPDATE user_credentials
SET password_hash = '$2y$10$U2ho1jIzQKb7PGRQPJE9w.GWHsQyfDGuE3ZC6FDyxZ33E57MZhvDq',
    password_updated_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE login_email = 'admin@knowva.local';

-- [추가] hg102938363736@gmail.com: 신규 데모 사용자 계정
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

-- [추가] user_credentials: 신규 계정 로그인 정보
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

-- [추가] user_settings: 신규 계정 기본 설정
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

-- [추가] user_learning_profiles: 신규 계정 학습 프로필
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

/*
  적용 대상: hg102938363736@gmail.com (user_id 6), JAVA 과목 기준
  - BRONZE: 레벨 해금 + 5개 행성 전체 완료
  - SILVER: 레벨 해금 + 1~4번째 행성 전체 완료, 5번째(마지막) 행성은
    레슨 10개 중 9개까지 완료하고 마지막 레슨(lessons.sort_order = 10)만 미완료로 남김
  - GOLD: 해금하지 않음
*/

-- [추가] 레벨 해금: BRONZE + SILVER (GOLD는 해금하지 않음)
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

-- [추가] JAVA 과목 수강 등록
INSERT INTO user_subject_enrollments (
  user_id, subject_id, status, start_mode, enrolled_at
)
VALUES
  (6, @java_subject_id, 'ACTIVE', 'BASIC', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

-- [추가] 행성 단위 진행도: BRONZE 5개 + SILVER 1~4번째는 100% 완료, SILVER 5번째(마지막 행성)는 90%(마지막 레슨만 미완료)
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

-- [추가] 레슨 단위 진행도: BRONZE 전체 + SILVER 1~4번째 행성 전체 + SILVER 5번째(마지막) 행성은
-- 레슨 9개(sort_order 1~9)까지만 완료 처리, 마지막 레슨(sort_order = 10)은 완료 행을 넣지 않아 "남겨둔 상태"로 유지
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

COMMIT;

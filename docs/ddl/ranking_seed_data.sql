/*
  Knowva ranking seed data - WEEKLY(과목별)/MONTHLY(통합) 랭킹 확인용 데모 계정 20명
  File: docs/ddl/ranking_seed_data.sql
  Compatibility: Knowva_DDL.sql
  Execute after docs/ddl/Knowva_DDL.sql and docs/ddl/users_data.sql.

  Purpose
  - /ranking 화면의 "통합" 탭이 항상 비어 보이는 원인은 ranking_scores 테이블에
    period_type = 'MONTHLY' 행이 하나도 없기 때문이다(과목별 탭은 WEEKLY만 존재).
  - ranking_scores.score를 직접 하드코딩하지 않는다. 대신 실제 서비스 로직
    (RankingScoreMapper.insertWeeklySubjectRankingScores / insertMonthlySubjectRankingScores)과
    동일한 계산식으로 score_events(연습문제 통과)·exam_sessions(시험 합격) 데이터를 먼저
    만들고, 그 데이터를 집계해서 ranking_scores를 채운다.
    score = SUM(score_events.score_delta) + COUNT(합격한 exam_sessions) * 200
  - 계정 20명 중 일부는 과목 1개만, 일부는 2~3개 과목을 동시에 학습 중인 것으로 구성해서
    "통합(월간)" 점수가 여러 과목 점수의 평균으로 계산되는 케이스도 재현한다.
  - RankingService/RankingScoreMapper의 리그(BRONZE/SILVER/GOLD) 필터 제거 작업과
    같이 적용되는 것을 전제로 하므로 user_learning_profiles는 심지 않는다.

  Idempotency
  - user_id 200~224 고정 범위(이전 버전들이 이 범위를 썼음, 함께 정리됨). 다른 seed
    파일(1~53)과 겹치지 않음.
  - 실행할 때마다 이 범위(200~224)의 users/score_events/exam_sessions/ranking_scores를
    통째로 DELETE한 뒤 새로 만든다(upsert 아님) - 이전 실행/이전 버전의 흔적이 남지 않음.
  - period_key는 실행 시점 기준(이번 주/이번 달)으로 계산되고, score_events/exam_sessions도
    실행 시점(CURRENT_TIMESTAMP)으로 심기 때문에 "이번 주"이자 동시에 "이번 달" 활동으로
    자연스럽게 WEEKLY/MONTHLY 집계 조건에 모두 걸린다.
*/

SET NAMES utf8mb4;
SET time_zone = '+09:00';

USE elearning;

START TRANSACTION;

SELECT subject_id INTO @java_subject_id FROM subjects WHERE subject_code = 'JAVA';
SELECT subject_id INTO @python_subject_id FROM subjects WHERE subject_code = 'PYTHON';
SELECT subject_id INTO @web_subject_id FROM subjects WHERE subject_code = 'WEB';
SELECT subject_id INTO @sql_subject_id FROM subjects WHERE subject_code = 'SQL';

-- RankingService.currentWeeklyPeriodKey() / currentMonthlyPeriodKey()와 동일한 포맷
SET @weekly_period_key = DATE_FORMAT(CURRENT_DATE, '%x-W%v');
SET @monthly_period_key = DATE_FORMAT(CURRENT_DATE, '%Y-%m');

-- 예약 범위(200~224)를 항상 완전히 새로 만든다. FK 순서상 자식 테이블부터 지운다.
-- user_learning_profiles는 예전 버전(v1)이 심어둔 잔재라 같이 정리해야 users를 지울 수 있다.
DELETE FROM ranking_scores WHERE user_id BETWEEN 200 AND 224;
DELETE FROM exam_sessions WHERE user_id BETWEEN 200 AND 224;
DELETE FROM score_events WHERE user_id BETWEEN 200 AND 224;
DELETE FROM user_learning_profiles WHERE user_id BETWEEN 200 AND 224;
DELETE FROM users WHERE user_id BETWEEN 200 AND 224;

-- 계정 20개 (user_id 200~219). 대학생 느낌 닉네임.
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

-- 유저별 수강 과목 + 연습문제 통과 횟수(practice_count) + 시험 합격 여부(has_exam).
-- 15명은 과목 1개, 5명(205/209/214/217/219)은 2~3개 과목을 동시에 수강 중인 것으로 구성.
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

-- 연습문제 통과 기록 생성: 과목당 practice_count건씩, 건당 70점(PRACTICE_SET_PASS).
-- score_events가 실제 학습 활동 원본이고, ranking_scores는 이걸 집계한 결과일 뿐이다.
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

-- 시험 합격 기록 생성: has_exam=1인 과목마다 합격(PASSED) 시험 1건, 200점.
INSERT INTO exam_sessions (
  user_id, subject_id, level_code, status, result_status, total_problem_count, correct_count, retry_count, started_at, submitted_at, graded_at, created_at, updated_at
)
SELECT
  e.user_id, e.subject_id, 'BRONZE', 'GRADED', 'PASSED', 3, 3, 0,
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM ranking_seed_enrollment e
WHERE e.has_exam = 1;

-- WEEKLY 과목별 랭킹: RankingScoreMapper.insertWeeklySubjectRankingScores와 동일한
-- 계산식(연습점수 합 + 시험합격수*200)을 이 시드 유저(200~219) 범위로만 집계해서 채운다.
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

-- MONTHLY 과목별 랭킹(통합 탭 재료): 위와 같은 데이터, 같은 계산식을 MONTHLY로 한 번 더 채운다.
-- findMonthlyGlobalRankingFromSubjects가 이 SUBJECT-scope MONTHLY 행들을 유저별로
-- 평균 내서 "통합" 순위를 만들기 때문에, 과목 2개 이상 수강 중인 유저(205/209/214/217/219)는
-- 자동으로 여러 과목 점수의 평균으로 통합 점수가 계산된다.
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

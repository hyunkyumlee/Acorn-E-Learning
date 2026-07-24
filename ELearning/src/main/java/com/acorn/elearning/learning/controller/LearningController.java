package com.acorn.elearning.learning.controller;

import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.Subject;
import com.acorn.elearning.learning.model.UserLevelUnlock;
import com.acorn.elearning.learning.service.AttendanceService;
import com.acorn.elearning.learning.service.CurriculumService;
import com.acorn.elearning.learning.service.EnrollmentService;
import com.acorn.elearning.learning.service.LearningService;
import com.acorn.elearning.learning.service.ProgressService;
import com.acorn.elearning.learning.support.PlanetCatalog;
import com.acorn.elearning.learning.support.PlanetCatalog.PlanetView;
import com.acorn.elearning.learning.view.CelebrationView;
import com.acorn.elearning.learning.view.LearningDashboardView;
import com.acorn.elearning.learning.view.RoadmapLevelTab;
import com.acorn.elearning.learning.view.SubjectCardView;
import com.acorn.elearning.ranking.service.RankingService;
import com.acorn.elearning.security.SessionUser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;


@Controller
public class LearningController {

    /** roadmap fallback용 기본 과목: JAVA(subject_id=1). 선택 과목·프로필 주 과목이 모두 없을 때만 사용. */
    private static final Long DEFAULT_SUBJECT_ID = 1L;

    /** 로드맵 레벨 탭 표시 순서(낮은 난이도 → 높은 난이도). 표시 레벨 기본값도 첫 원소. */
    private static final List<String> LEVEL_ORDER = List.of("BRONZE", "SILVER", "GOLD");

    ////subjectid session정보저장
    public static final String SESSION_LEARNING_SUBJECT_ID = "LEARNING_SUBJECT_ID";

    /** 시작 선택 화면으로 되돌린 적이 있는지. 되돌리기는 세션당 한 번만 해야 로드맵 열람이 막히지 않는다. */
    private static final String SESSION_START_GUIDE_SHOWN = "LEARNING_START_GUIDE_SHOWN";

    /**
     * 축하 연출의 기준선: 직전에 로드맵을 봤을 때의 상태.
     * PLANETS = (과목:레벨)별로 그때 완료돼 있던 행성 수, LEVELS = 과목별로 그때 관문을 넘어 열려 있던 레벨.
     * 지금 상태가 기준선보다 늘었을 때만 축하하고, 기준선을 다시 지금 상태로 덮는다(같은 성취를 두 번 축하하지 않는다).
     * 처음 보는 (과목:레벨)·과목은 기준선만 세우고 축하하지 않는다 — 안 그러면 로그인할 때마다 지난 성취가 전부 터진다.
     */
    private static final String SESSION_CELEBRATED_PLANETS = "LEARNING_CELEBRATED_PLANETS";
    private static final String SESSION_CELEBRATED_LEVELS = "LEARNING_CELEBRATED_LEVELS";

    private final LearningService learningService;
    private final CurriculumService curriculumService;
    private final ProgressService progressService;
    private final AttendanceService attendanceService;
    private final RankingService rankingService;
    private final EnrollmentService enrollmentService;

    public LearningController(LearningService learningService,
                              CurriculumService curriculumService,
                              ProgressService progressService,
                              AttendanceService attendanceService,
                              RankingService rankingService,
                              EnrollmentService enrollmentService) {
        this.learningService = learningService;
        this.curriculumService = curriculumService;
        this.progressService = progressService;
        this.attendanceService = attendanceService;
        this.rankingService = rankingService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/learning")
    public String dashboard(
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "levelCode", required = false) String levelCode,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            ////subjectid session정보저장
            jakarta.servlet.http.HttpSession session,
            Model model) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        SessionUser user = sessionUser;

        // 수강신청 도입 이전부터 학습해 온 사용자가 자기 과목에서 잠기지 않도록 1회 보정한다.
        enrollmentService.ensureBackfill(user.userId());

        // 학습 메인 대시보드: 프로필/레벨/등급/점수/출석 streak
        LearningDashboardView dashboard = learningService.getLearningHome(user, subjectId);
        model.addAttribute("dashboard", dashboard);

        // 이번 주 요일별 출석(월~일 boolean 7) — 사이드바 주간 출석 도트
        model.addAttribute("weeklyAttendance", attendanceService.getWeeklyAttendance(user.userId()));

        List<Subject> subjects = learningService.getActiveSubjects();

        // 로드맵 대상 과목: 과목칩 선택(subjectId) 우선 → 프로필 주 과목 → JAVA fallback.
        Long roadmapSubjectId = (subjectId != null) ? subjectId
                : (dashboard.primarySubjectId() != null ? dashboard.primarySubjectId() : DEFAULT_SUBJECT_ID);

        // 관심 과목을 고르고 가입한 사용자는 그 과목의 시작 선택 화면을 먼저 본다.
        // 되돌리는 것은 과목을 지정하지 않은 첫 진입 한 번뿐이다. 과목을 지정해 들어온 요청(로드맵 보기, 과목칩)은
        // 되돌리지 않는다 — 되돌리면 그 화면의 '학습 로드맵으로'가 제자리를 맴돈다.
        boolean enrolled = enrollmentService.isEnrolled(user.userId(), roadmapSubjectId);
        if (subjectId == null
                && !enrolled
                && dashboard.primarySubjectId() != null
                && session.getAttribute(SESSION_START_GUIDE_SHOWN) == null) {
            session.setAttribute(SESSION_START_GUIDE_SHOWN, Boolean.TRUE);
            return "redirect:/learning/subjects/" + roadmapSubjectId;
        }

        // 시작 방식을 고르지 않았거나(미수강) 레벨 테스트를 마치지 않은 과목은 열린 레벨이 하나도 없다.
        // 로드맵은 잠긴 채로 보여 주고 시작 선택 화면으로 가는 안내만 띄운다 — 되돌리면 로드맵을 볼 수 없다.
        boolean needsStart = !enrolled
                || enrollmentService.requiresLevelTest(user.userId(), roadmapSubjectId);
        model.addAttribute("needsStart", needsStart);

        // 좌측 과목 목록: 과목별 수강 여부 · 현재 레벨 · 진행률
        List<SubjectCardView> subjectCards =
                learningService.getSubjectCards(user.userId(), roadmapSubjectId);
        model.addAttribute("subjectCards", subjectCards);

        // 과목 목록은 수강 중인 것만 보여 주고, 아직 담지 않은 과목은 '과목 추가'를 펼쳤을 때만 보여 준다.
        model.addAttribute("enrolledCards",
                subjectCards.stream().filter(SubjectCardView::enrolled).toList());
        model.addAttribute("availableCards",
                subjectCards.stream().filter(card -> !card.enrolled()).toList());

        // 진행 요약: 과목 전체 진행률 1개 + 레벨별 진행률 3개.
        // 레벨 테스트로 상위 레벨에 배정돼도 건너뛴 레벨은 0%로 남으므로, 왜 전체 진행률이 낮은지 화면에서 드러난다.
        model.addAttribute("subjectPercent", subjectCards.stream()
                .filter(SubjectCardView::selected)
                .map(SubjectCardView::progressPercent)
                .findFirst()
                .orElse(0));
        model.addAttribute("levelProgress",
                progressService.computeLevelProgress(user.userId(), roadmapSubjectId));

        String roadmapSubjectCode = subjectCodeOf(subjects, roadmapSubjectId);
        model.addAttribute("roadmapSubjectId", roadmapSubjectId);
        model.addAttribute("roadmapSubjectCode", roadmapSubjectCode);

        ////subjectid session정보저장
        session.setAttribute(SESSION_LEARNING_SUBJECT_ID, roadmapSubjectId);


        // 해금 기록은 한 번만 읽는다 — 표시 레벨·레벨 탭(어떤 레벨이 열렸나)·축하 연출(무엇으로 열렸나)이
        // 같은 기록을 본다.
        List<UserLevelUnlock> unlocks = curriculumService.getUnlocks(user.userId(), roadmapSubjectId);
        Set<String> unlockedLevels = CurriculumService.levelCodesOf(unlocks);

        // 표시 레벨: 요청 levelCode 우선 → 없으면 지금까지 연 가장 높은 레벨 → 그래도 없으면 최저 레벨.
        String selectedLevel = (levelCode != null && !levelCode.isBlank()) ? levelCode
                : highestUnlockedLevel(unlockedLevels, dashboard.currentLevelCode());
        model.addAttribute("selectedLevel", selectedLevel);

        // 레벨 탭: 해금된 레벨만 선택 가능, 현재 표시 레벨은 항상 활성으로 둔다.
        List<RoadmapLevelTab> levelTabs = LEVEL_ORDER.stream()
                .map(code -> new RoadmapLevelTab(
                        code,
                        unlockedLevels.contains(code) || code.equals(selectedLevel),
                        code.equals(selectedLevel)))
                .toList();
        model.addAttribute("levelTabs", levelTabs);

        // 로드맵은 선택 레벨의 노드(한 판)만 표시 — 레벨이 섞여 planet_no가 충돌하지 않게 레벨로 스코핑.
        List<CurriculumNode> roadmap = curriculumService.getRoadmap(roadmapSubjectId, selectedLevel);
        model.addAttribute("roadmap", roadmap);

        // 노드별 행성 표시 메타(아트 파일 · 이름 · 테마 스토리) = PlanetCatalog 단일 소스.
        // (레벨, planet_no)로 15종 중 하나. GATE 노드는 행성 아트가 아니므로 제외.
        Map<Long, PlanetView> nodePlanets = new LinkedHashMap<>();
        for (CurriculumNode node : roadmap) {
            if (!"GATE".equals(node.getNodeType())) {
                nodePlanets.put(node.getNodeId(), PlanetCatalog.resolve(
                        node.getLevelCode(), node.getPlanetNo(), node.getTitle(), roadmapSubjectCode));
            }
        }
        model.addAttribute("nodePlanets", nodePlanets);

        // hover 카드 "N개 레슨" 메타: 노드별 활성 레슨 수(nodeId → count)
        model.addAttribute("nodeLessonCounts", curriculumService.getLessonCountsByNodes(roadmap));

        // 로드맵 완료/현재/잠금 판정 = 선택 과목의 노드별 learning_progress 기준(평균 근사치 아님).
        var progress = progressService.computeRoadmapProgress(user.userId(), roadmapSubjectId, roadmap);

        int planetCount = progress.planetCount();
        int completedPlanets = progress.completedPlanets();
        model.addAttribute("completedPlanets", completedPlanets);
        model.addAttribute("planetCount", planetCount);
        model.addAttribute("progressPercent", progress.progressPercent());

        // 열리지 않은 레벨은 노드를 하나도 들어갈 수 없다. 레슨 진입 가드도 user_level_unlocks만 보므로
        // 이 판정을 빼면 잠긴 레벨의 행성이 클릭 가능한 모습으로 그려졌다가 레슨에서 403이 난다.
        boolean levelUnlocked = unlockedLevels.contains(selectedLevel);

        // 다음 레벨이 이미 열려 있으면 이 레벨은 지나온 레벨이다(레벨 테스트로 건너뛰었거나 게이트를 통과했거나).
        String nextLevel = nextLevel(selectedLevel);
        boolean levelPassed = nextLevel != null && unlockedLevels.contains(nextLevel);

        // 게이트 상태: 지나온 레벨이면 통과함(재응시 가능), 아니면 전 행성 완료 시 응시 가능(ready), 그 외 잠김(locked).
        String gateState = !levelUnlocked ? "locked"
                : (levelPassed ? "passed"
                        : ((planetCount > 0 && completedPlanets >= planetCount) ? "ready" : "locked"));
        model.addAttribute("gateState", gateState);

        // 노드 상태(done/current/open/locked) — 템플릿이 같은 판정을 되풀이하지 않게 여기서 한 번만 계산한다.
        // 지나온 레벨의 행성은 순차 잠금을 걸지 않고 모두 open으로 연다(상위 레벨로 배정된 사용자가 건너뛴
        // 레벨의 행성을 못 여는 것을 막는다). 학습한 적이 없으므로 완료로 치지는 않는다 — 진행률도 실제 학습 기준.
        // locked만 링크가 없다.
        Map<Long, String> nodeStates = new LinkedHashMap<>();
        for (CurriculumNode node : roadmap) {
            if (!levelUnlocked) {
                nodeStates.put(node.getNodeId(), "locked");
            } else if ("GATE".equals(node.getNodeType())) {
                nodeStates.put(node.getNodeId(), gateState);
            } else if (node.getPlanetNo() == null) {
                nodeStates.put(node.getNodeId(), "locked");
            } else if (node.getPlanetNo() <= completedPlanets) {
                nodeStates.put(node.getNodeId(), "done");
            } else if (levelPassed) {
                nodeStates.put(node.getNodeId(), "open");
            } else if (node.getPlanetNo() == completedPlanets + 1) {
                nodeStates.put(node.getNodeId(), "current");
            } else {
                nodeStates.put(node.getNodeId(), "locked");
            }
        }
        model.addAttribute("nodeStates", nodeStates);

        model.addAttribute("levelLocked", !levelUnlocked);

        // TodayMissionCard용 현재 학습 행성 = 완료수 다음 행성. (모두 완료면 null → 게이트 단계)
        // 잠긴 레벨에는 들어갈 행성이 없다 — 여기서 비우지 않으면 카드가 403이 나는 레슨으로 링크한다.
        CurriculumNode currentNode = !levelUnlocked ? null : roadmap.stream()
                .filter(node -> "PLANET".equals(node.getNodeType()))
                .filter(node -> node.getPlanetNo() != null
                        && node.getPlanetNo() == completedPlanets + 1)
                .findFirst()
                .orElse(null);
        model.addAttribute("currentNode", currentNode);

        // 현재 행성을 시작했는지(이론 또는 문제 중 하나라도 손댄 레슨이 있으면 시작) → 라벨 '학습 중'(시작함) vs '학습 전'(미시작).
        // 한 번도 안 한 과목은 1행성이 current지만 진행 전무 → '학습 전'으로 표시.
        boolean currentPlanetStarted = currentNode != null
                && curriculumService.getLessonProgressMap(user.userId(), currentNode.getNodeId())
                        .values().stream()
                        .anyMatch(pr -> pr != null
                                && (Boolean.TRUE.equals(pr.getTheoryCompleted())
                                    || Boolean.TRUE.equals(pr.getPracticePassed())));
        model.addAttribute("currentPlanetStarted", currentPlanetStarted);

        // 이 레벨에서 아직 학습을 시작하지 않은(완료 0·미시작) 첫 행성 = 진입 환영 시점.
        // 기준은 "지나온 레벨이냐"가 아니라 "이 레벨을 학습했느냐"(completedPlanets==0)다.
        // 레벨 테스트로 상위 레벨에 배정되면 그 아래 레벨도 열리지만(levelPassed) 학습 전이므로
        // 그 레벨의 첫 행성에서도 등장 연출이 떠야 한다. 반대로 게이트를 통과해 실제 학습을 마친
        // 아래 레벨은 completedPlanets>0이라 자동으로 제외된다. 그래서 levelPassed는 조건에 넣지 않는다.
        boolean roadmapEntryWelcome = currentNode != null && completedPlanets == 0 && !currentPlanetStarted;
        model.addAttribute("roadmapEntryWelcome", roadmapEntryWelcome);

        // 단원 진행 현황 '문제 풀이' 지표 = 현재 학습 단원의 required 레슨 중 문제 풀이(practice)를 통과한 수 / 전체.
        // practice_passed는 practice 세트 통과로만 기록되며 내 테이블(user_lesson_progress)에 있다.
        int currentUnitLessonTotal = currentNode != null
                ? curriculumService.countRequiredLessons(currentNode.getNodeId()) : 0;
        int currentUnitPracticePassed = currentNode != null
                ? curriculumService.countPracticePassedRequiredLessons(user.userId(), currentNode.getNodeId()) : 0;
        model.addAttribute("currentUnitLessonTotal", currentUnitLessonTotal);
        model.addAttribute("currentUnitPracticePassed", currentUnitPracticePassed);

        // 내 랭킹(주간 통합 기준) = ranking 도메인 read. 출석/streak 점수는 랭킹에서 제외됨. //
        Map<String, Object> myRanking =
                (Map<String, Object>) rankingService.myRanking(user, null, "WEEKLY").data().get("mySummary");
        model.addAttribute("myRanking", myRanking);

        // 직전에 본 로드맵보다 성취가 늘었으면 축하 연출을 한 번 띄운다.
        model.addAttribute("celebration", resolveCelebration(session, roadmapSubjectId, selectedLevel,
                CurriculumService.examUnlockedLevelCodesOf(unlocks),
                roadmap, nodePlanets, completedPlanets, planetCount, levelPassed));

        model.addAttribute("screen", "learning/main");
        return "learning/main";
    }

    /**
     * 직전에 본 로드맵 상태와 지금을 비교해 이번에 새로 생긴 성취를 찾는다. 새로 생긴 게 없으면 null.
     * 레벨 달성은 관문(AI 코딩테스트)을 넘어 열린 레벨만 축하한다 — 레벨 테스트로 배정된 레벨은
     * 스스로 넘은 관문이 아니고, 결과 화면이 이미 등급을 알려준다.
     */
    @SuppressWarnings("unchecked")
    private CelebrationView resolveCelebration(jakarta.servlet.http.HttpSession session,
                                               Long subjectId, String levelCode,
                                               Set<String> examUnlocked,
                                               List<CurriculumNode> roadmap,
                                               Map<Long, PlanetView> nodePlanets,
                                               int completedPlanets, int planetCount,
                                               boolean levelPassed) {
        // 표시 레벨은 요청 파라미터에서 온다. 아는 레벨일 때만 기준선을 기록해야 임의의 levelCode로
        // 세션에 키가 무한히 쌓이지 않는다.
        if (!LEVEL_ORDER.contains(levelCode)) {
            return null;
        }

        Map<String, Integer> seenPlanets =
                (Map<String, Integer>) session.getAttribute(SESSION_CELEBRATED_PLANETS);
        if (seenPlanets == null) {
            seenPlanets = new HashMap<>();
            session.setAttribute(SESSION_CELEBRATED_PLANETS, seenPlanets);
        }
        Map<String, Set<String>> seenLevels =
                (Map<String, Set<String>>) session.getAttribute(SESSION_CELEBRATED_LEVELS);
        if (seenLevels == null) {
            seenLevels = new HashMap<>();
            session.setAttribute(SESSION_CELEBRATED_LEVELS, seenLevels);
        }

        String subjectKey = String.valueOf(subjectId);
        Set<String> seenLevelCodes = seenLevels.get(subjectKey);

        String reachedLevel = null;
        if (seenLevelCodes == null) {
            // 이 세션에서 이 과목을 처음 본다 → 기준선만 세운다.
            seenLevels.put(subjectKey, new HashSet<>(examUnlocked));
        } else {
            // 한 번에 여러 레벨이 늘어날 일은 없지만, 늘어났다면 가장 높은 레벨 하나만 축하한다.
            reachedLevel = LEVEL_ORDER.stream()
                    .filter(examUnlocked::contains)
                    .filter(code -> !seenLevelCodes.contains(code))
                    .reduce((lower, higher) -> higher)
                    .orElse(null);
            seenLevelCodes.addAll(examUnlocked);
        }

        String planetKey = subjectKey + ":" + levelCode;
        Integer seenCount = seenPlanets.get(planetKey);
        seenPlanets.put(planetKey, completedPlanets);

        // 관문을 넘은 것이 행성 하나를 마친 것보다 큰 성취라 레벨 쪽을 먼저 보여 준다.
        if (reachedLevel != null) {
            return new CelebrationView("LEVEL", "레벨 달성", reachedLevel + " 레벨에 올랐어요",
                    "관문을 통과해 다음 레벨의 행성이 열렸어요.", null, reachedLevel);
        }
        if (seenCount == null || completedPlanets <= seenCount) {
            return null;
        }

        // 최고 레벨(GOLD)의 마지막 행성을 마치면 이 과목의 학습 여정이 끝난다. 관문 코딩테스트는 그 위의
        // 도전 과제이고 통과가 이 도메인에 기록되지 않으므로, 배울 내용을 다 끝낸 이 시점을 완주로 본다.
        // 카드에 담을 '다음 여정'(다른 과목) 목록은 이미 model에 있는 enrolled/availableCards를 템플릿이 쓴다.
        if (nextLevel(levelCode) == null && completedPlanets >= planetCount) {
            return new CelebrationView("COURSE", "여정 완료",
                    "이 과목의 모든 여행이 끝났어요!",
                    "또 다른 여정을 시작해볼까요?", null, levelCode);
        }

        // 행성은 앞에서부터 연속으로 완료되므로 방금 마친 행성 번호 = 지금의 완료 행성 수.
        CurriculumNode completed = roadmap.stream()
                .filter(node -> !"GATE".equals(node.getNodeType()))
                .filter(node -> node.getPlanetNo() != null && node.getPlanetNo() == completedPlanets)
                .findFirst()
                .orElse(null);
        if (completed == null) {
            return null;
        }

        PlanetView planet = nodePlanets.get(completed.getNodeId());
        // 지나온 레벨은 관문이 이미 통과 상태고 행성도 처음부터 다 열려 있다 — 같은 화면의 게이트 카드가
        // '이미 통과한 레벨 · 재응시'라고 말하는데 축하만 '이제 관문에 도전할 수 있어요'라고 하면 어긋난다.
        String message;
        if (completedPlanets >= planetCount) {
            message = levelPassed
                    ? "이 레벨의 행성을 모두 마쳤어요. 관문은 이미 통과했어요."
                    : "이 레벨의 행성을 모두 마쳤어요. 이제 관문에 도전할 수 있어요.";
        } else {
            message = levelPassed
                    ? "이어서 다음 행성도 학습할 수 있어요."
                    : "다음 행성으로 가는 길이 열렸어요.";
        }
        return new CelebrationView("PLANET", "행성 완료",
                (planet != null) ? planet.name() : completed.getTitle(),
                message,
                (planet != null) ? planet.file() : null,
                completed.getLevelCode());
    }

    /** 선택된 로드맵 과목의 코드(로드맵 제목 표시용). 목록에 없으면 '-'. */
    private String subjectCodeOf(List<Subject> subjects, Long subjectId) {
        return subjects.stream()
                .filter(subject -> subjectId.equals(subject.getSubjectId()))
                .map(Subject::getSubjectCode)
                .findFirst()
                .orElse("-");
    }

    /** 레벨 순서상 다음 레벨(없으면 null). 게이트 통과 판정(다음 레벨 해금 여부)용. */
    private String nextLevel(String level) {
        int i = LEVEL_ORDER.indexOf(level);
        return (i >= 0 && i + 1 < LEVEL_ORDER.size()) ? LEVEL_ORDER.get(i + 1) : null;
    }

    /**
     * levelCode 없이 들어왔을 때 보여 줄 판 = 지금까지 연 레벨 중 가장 높은 것.
     * 해금 기록이 없으면 프로필의 현재 레벨, 그것도 없으면 최저 레벨.
     *
     * <p>프로필의 current_level_code를 먼저 보지 않는 이유: 관문을 통과해 다음 레벨을 열어도
     * (UnlockService.unlockNextLevel) 그 컬럼은 갱신되지 않는다. 그것을 기준으로 삼으면
     * 새 판을 열어 두고도 계속 옛 판을 보여 주게 된다. 해금 기록이 실제 진도다.
     */
    private static String highestUnlockedLevel(Set<String> unlockedLevels, String profileLevel) {
        for (int i = LEVEL_ORDER.size() - 1; i >= 0; i--) {
            if (unlockedLevels.contains(LEVEL_ORDER.get(i))) {
                return LEVEL_ORDER.get(i);
            }
        }
        return (profileLevel != null) ? profileLevel : LEVEL_ORDER.get(0);
    }
}

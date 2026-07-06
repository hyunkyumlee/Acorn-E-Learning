package com.acorn.elearning.learning.controller;

import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.Subject;
import com.acorn.elearning.learning.service.AttendanceService;
import com.acorn.elearning.learning.service.CurriculumService;
import com.acorn.elearning.learning.service.LearningService;
import com.acorn.elearning.learning.service.ProgressService;
import com.acorn.elearning.learning.view.LearningDashboardView;
import com.acorn.elearning.security.SessionUser;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class LearningController {

    /** roadmap fallback용 기본 과목: JAVA(subject_id=1). 선택 과목·프로필 주 과목이 모두 없을 때만 사용. */
    private static final Long DEFAULT_SUBJECT_ID = 1L;

    /**
     * 개발용 fallback 사용자: 로그인/세션은 1번(auth) 담당이라 구현 전까지 세션이 비어 있다.
     * 세션이 없으면 샘플데이터의 learner(userId=2, 누비학습자)로 대시보드를 확인한다.
     * 로그인/세션이 붙으면 이 fallback은 자연히 사용되지 않는다.
     */
    private static final SessionUser DEV_FALLBACK_USER =
            new SessionUser(2L, "learner@knowva.local", "누비학습자", SessionUser.ROLE_USER, false);

    private final LearningService learningService;
    private final CurriculumService curriculumService;
    private final ProgressService progressService;
    private final AttendanceService attendanceService;

    public LearningController(LearningService learningService,
                              CurriculumService curriculumService,
                              ProgressService progressService,
                              AttendanceService attendanceService) {
        this.learningService = learningService;
        this.curriculumService = curriculumService;
        this.progressService = progressService;
        this.attendanceService = attendanceService;
    }

    @GetMapping("/learning")
    public String dashboard(
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model) {
        SessionUser user = (sessionUser != null) ? sessionUser : DEV_FALLBACK_USER;

        // 학습 메인 대시보드: 프로필/레벨/등급/점수/출석 streak
        LearningDashboardView dashboard = learningService.getLearningHome(user);
        model.addAttribute("dashboard", dashboard);

        // 이번 주 요일별 출석(월~일 boolean 7) — 사이드바 주간 출석 도트
        model.addAttribute("weeklyAttendance", attendanceService.getWeeklyAttendance(user.userId()));

        List<Subject> subjects = learningService.getActiveSubjects();
        model.addAttribute("subjects", subjects);

        // 로드맵 대상 과목: 과목칩 선택(subjectId) 우선 → 프로필 주 과목 → JAVA fallback.
        Long roadmapSubjectId = (subjectId != null) ? subjectId
                : (dashboard.primarySubjectId() != null ? dashboard.primarySubjectId() : DEFAULT_SUBJECT_ID);
        model.addAttribute("roadmapSubjectId", roadmapSubjectId);
        model.addAttribute("roadmapSubjectCode", subjectCodeOf(subjects, roadmapSubjectId));

        List<CurriculumNode> roadmap = curriculumService.getRoadmap(roadmapSubjectId);
        model.addAttribute("roadmap", roadmap);

        // hover 카드 "N개 레슨" 메타: 노드별 활성 레슨 수(nodeId → count)
        model.addAttribute("nodeLessonCounts", curriculumService.getLessonCountsByNodes(roadmap));

        // 로드맵 완료/현재/잠금 판정 = 선택 과목의 노드별 learning_progress 기준(평균 근사치 아님).
        var progress = progressService.computeRoadmapProgress(user.userId(), roadmapSubjectId, roadmap);
        int completedPlanets = progress.completedPlanets();
        model.addAttribute("completedPlanets", completedPlanets);
        model.addAttribute("planetCount", progress.planetCount());
        model.addAttribute("progressPercent", progress.progressPercent());

        // TodayMissionCard용 현재 학습 행성 = 완료수 다음 행성. (모두 완료면 null → 게이트 단계)
        CurriculumNode currentNode = roadmap.stream()
                .filter(node -> "PLANET".equals(node.getNodeType()))
                .filter(node -> node.getPlanetNo() != null
                        && node.getPlanetNo() == completedPlanets + 1)
                .findFirst()
                .orElse(null);
        model.addAttribute("currentNode", currentNode);

        model.addAttribute("screen", "learning/main");
        return "learning/main";
    }

    /** 선택된 로드맵 과목의 코드(로드맵 제목 표시용). 목록에 없으면 '-'. */
    private String subjectCodeOf(List<Subject> subjects, Long subjectId) {
        return subjects.stream()
                .filter(subject -> subjectId.equals(subject.getSubjectId()))
                .map(Subject::getSubjectCode)
                .findFirst()
                .orElse("-");
    }
}

package com.acorn.elearning.learning.controller;

import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.service.CurriculumService;
import com.acorn.elearning.learning.service.LearningService;
import com.acorn.elearning.learning.view.LearningDashboardView;
import com.acorn.elearning.security.SessionUser;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class LearningController {

    /** roadmap fallback용 기본 과목: JAVA(subject_id=1). 프로필의 primary_subject_id가 없을 때만 사용. */
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

    public LearningController(LearningService learningService, CurriculumService curriculumService) {
        this.learningService = learningService;
        this.curriculumService = curriculumService;
    }

    @GetMapping("/learning")
    public String dashboard(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model) {
        SessionUser user = (sessionUser != null) ? sessionUser : DEV_FALLBACK_USER;

        // 학습 메인 대시보드: 프로필/레벨/진행률/출석 streak
        LearningDashboardView dashboard = learningService.getLearningHome(user);
        model.addAttribute("dashboard", dashboard);

        // 과목 목록 + 사용자 주 과목 기준 로드맵 (주 과목이 없으면 JAVA로 fallback)
        model.addAttribute("subjects", learningService.getActiveSubjects());
        Long roadmapSubjectId = (dashboard.primarySubjectId() != null)
                ? dashboard.primarySubjectId() : DEFAULT_SUBJECT_ID;
        List<CurriculumNode> roadmap = curriculumService.getRoadmap(roadmapSubjectId);
        model.addAttribute("roadmap", roadmap);

        // 로드맵 시각 상태(완료/현재/잠금)용 보조 값.
        // ⚠️ 임시 근사치: 노드별 progress가 아직 안 붙어서, dashboard.progressRate(주 과목 노드 평균 %)로
        //   "완료 행성 수"를 환산한다. 노드 단위 learning_progress가 붙으면 노드별 완료 여부로 교체할 것.
        long planetCount = roadmap.stream()
                .filter(node -> "PLANET".equals(node.getNodeType()))
                .count();
        int completedPlanets = (int) Math.floor(planetCount * dashboard.progressRate() / 100.0);
        model.addAttribute("planetCount", planetCount);
        model.addAttribute("completedPlanets", completedPlanets);

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
}

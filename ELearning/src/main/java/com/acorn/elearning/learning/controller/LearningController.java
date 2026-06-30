package com.acorn.elearning.learning.controller;

import com.acorn.elearning.learning.service.CurriculumService;
import com.acorn.elearning.learning.service.LearningService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LearningController {

    /** 데모용 기본 과목: JAVA(subject_id=1). 추후 SessionUser의 primary_subject_id로 대체 예정. */
    private static final Long DEFAULT_SUBJECT_ID = 1L;

    private final LearningService learningService;
    private final CurriculumService curriculumService;

    public LearningController(LearningService learningService, CurriculumService curriculumService) {
        this.learningService = learningService;
        this.curriculumService = curriculumService;
    }

    @GetMapping("/learning")
    public String dashboard(Model model) {
        // TODO 추후 signature에 HttpSession/SessionUser를 추가하고 과목도 사용자 기준으로 선택한다.
        // 현재는 subjects 활성 목록 + 기본 과목(JAVA)의 커리큘럼 로드맵을 학습 메인에 표시한다.
        model.addAttribute("subjects", learningService.getActiveSubjects());
        model.addAttribute("roadmap", curriculumService.getRoadmap(DEFAULT_SUBJECT_ID));
        model.addAttribute("screen", "learning/main");
        return "learning/main";
    }
}

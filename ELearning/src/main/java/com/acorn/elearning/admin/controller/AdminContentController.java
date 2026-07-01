package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.form.SubjectForm;
import com.acorn.elearning.admin.form.CurriculumNodeForm;
import com.acorn.elearning.admin.form.LessonForm;
import com.acorn.elearning.admin.form.ProblemForm;
import com.acorn.elearning.admin.mapper.AdminLessonMapper;
import com.acorn.elearning.admin.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminContentController {


    private final AdminContentService service;


    @GetMapping("/admin/courses")
    public String courses(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AdminManagePageView view = adminContentService.courses(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.

        model.addAttribute("subjectList", service.findAllSubject());

        model.addAttribute("curriculumList", service.findAllCurriculumNode());

        model.addAttribute("subjectForm", new SubjectForm());
        model.addAttribute("curriculumNodeForm", new CurriculumNodeForm());

        model.addAttribute("screen", "admin/courses");
        return "admin/courses";
    }

    @GetMapping("/admin/theory")
    public String theory(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AdminManagePageView view = adminContentService.theory(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.

        model.addAttribute("theoryList", service.findAllAdminLesson());
        model.addAttribute("lessonForm", new LessonForm());
        model.addAttribute("screen", "admin/theory");
        return "admin/theory";
    }

    @GetMapping("/admin/problems")
    public String problems(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AdminManagePageView view = adminContentService.problems(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.

        model.addAttribute("problemList", service.findAllAdminProblem());
        model.addAttribute("problemForm", new ProblemForm());
        model.addAttribute("screen", "admin/problems");
        return "admin/problems";
    }
}

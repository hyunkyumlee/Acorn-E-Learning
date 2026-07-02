package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.form.SubjectForm;
import com.acorn.elearning.admin.form.CurriculumNodeForm;
import com.acorn.elearning.admin.form.LessonForm;
import com.acorn.elearning.admin.form.ProblemForm;
import com.acorn.elearning.admin.service.AdminContentService;
import com.acorn.elearning.learning.model.Subject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<Subject> subjectList = service.findAllSubject();
        Map<Long, String> subjectNameMap = subjectList.stream()
                .collect(Collectors.toMap(Subject::getSubjectId, Subject::getSubjectName));

        model.addAttribute("subjectList", subjectList);
        model.addAttribute("subjectNameMap", subjectNameMap);

        model.addAttribute("curriculumList", service.findAllCurriculumNode());

        model.addAttribute("subjectForm", new SubjectForm());
        model.addAttribute("curriculumNodeForm", new CurriculumNodeForm());

        model.addAttribute("screen", "admin/courses");
        return "admin/courses";
    }

    @PostMapping("/admin/courses/subjects")
    public String regCourses(SubjectForm form){

        if (form.getSubjectId() == null) {
            service.createSubject(form);
        } else{
            service.updateSubject(form);
        }

        return "redirect:/admin/courses";
    }


//    @PostMapping("/admin/courses/subjects/{subjectId}/delete")
//    public String deleteSubjects(@PathVariable Long subjectId,
//                                 RedirectAttributes redirectAttributes){
//
//        int deleteCount = service.deleteSubject(subjectId);
//       if(deleteCount == 1){
//            redirectAttributes.addFlashAttribute("message", "과목이 삭제되었습니다.");
//       }else{
//            redirectAttributes.addFlashAttribute("errorMessage", "삭제할 과목을 찾을 수 없습니다.");
//       }
//
//       return "redirect:/admin/courses";
//
//    }

    @PostMapping("/admin/courses/curriculum-nodes")
    public String regCurriculumNode(CurriculumNodeForm form){

        if (form.getNodeId() == null) {
            service.createCurriculumNode(form);
        } else{
            service.updateCurriculumNode(form);
        }

        return "redirect:/admin/courses?tab=curriculum";
    }


    @GetMapping("/admin/theory")
    public String theory(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AdminManagePageView view = adminContentService.theory(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.

        model.addAttribute("theoryList", service.findAllAdminLesson());
        model.addAttribute("subjectList", service.findAllSubject());
        model.addAttribute("curriculumList", service.findAllCurriculumNode());
        model.addAttribute("lessonForm", new LessonForm());
        model.addAttribute("screen", "admin/theory");
        return "admin/theory";
    }

    @PostMapping("/admin/theory")
    public String regTheory(LessonForm form){

        if (form.getLessonId() == null) {
            service.createLesson(form);
        } else {
            service.updateLesson(form);
        }

        return "redirect:/admin/theory";
    }



    @GetMapping("/admin/problems")
    public String problems(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AdminManagePageView view = adminContentService.problems(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.

        model.addAttribute("problemList", service.findAllAdminProblem());
        model.addAttribute("subjectList", service.findAllSubject());
        model.addAttribute("curriculumList", service.findAllCurriculumNode());
        model.addAttribute("problemForm", new ProblemForm());
        model.addAttribute("screen", "admin/problems");
        return "admin/problems";
    }

    @PostMapping("/admin/problems")
    public String regProblem(ProblemForm form){

        if (form.getProblemId() == null) {
            service.createProblem(form);
        } else {
            service.updateProblem(form);
        }

        return "redirect:/admin/problems";
    }

    @PostMapping("/admin/problems/{problemId}/delete")
    public String deleteProblem(@PathVariable Long problemId,
                                RedirectAttributes redirectAttributes) {

        int deleteCount = service.deleteProblem(problemId);

        if (deleteCount == 1) {
            redirectAttributes.addFlashAttribute("message", "문제가 삭제되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "삭제할 문제를 찾을 수 없습니다.");
        }

        return "redirect:/admin/problems";
    }
}

package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.form.SubjectForm;
import com.acorn.elearning.admin.form.CurriculumNodeForm;
import com.acorn.elearning.admin.form.LessonForm;
import com.acorn.elearning.admin.form.ProblemForm;
import com.acorn.elearning.admin.service.AdminContentService;
import com.acorn.elearning.learning.model.Subject;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
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
    public String regCourses(SubjectForm form,
                             @SessionAttribute(name= SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                             RedirectAttributes redirectAttributes){

        if (form.getSubjectId() == null) {
            service.createSubject(form, sessionUser.userId());
            redirectAttributes.addFlashAttribute("message", "과목이 등록되었습니다.");
        } else{
            service.updateSubject(form, sessionUser.userId());
            redirectAttributes.addFlashAttribute("message", "과목이 수정되었습니다.");
        }

        return "redirect:/admin/courses";
    }

    @PostMapping("/admin/courses/curriculum-nodes")
    public String regCurriculumNode(CurriculumNodeForm form,
                                    @SessionAttribute(name= SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                                    RedirectAttributes redirectAttributes){

        if (form.getNodeId() == null) {
            service.createCurriculumNode(form, sessionUser.userId());
            redirectAttributes.addFlashAttribute("message", "커리큘럼이 등록되었습니다.");
        } else{
            service.updateCurriculumNode(form, sessionUser.userId());
            redirectAttributes.addFlashAttribute("message", "커리큘럼이 수정되었습니다.");
        }

        return "redirect:/admin/courses?tab=curriculum";
    }


    @GetMapping("/admin/theory")
    public String theory(Model model) {
        model.addAttribute("theoryList", service.findAllAdminLesson());
        model.addAttribute("subjectList", service.findAllSubject());
        model.addAttribute("curriculumList", service.findAllCurriculumNode());
        model.addAttribute("lessonForm", new LessonForm());
        model.addAttribute("screen", "admin/theory");
        return "admin/theory";
    }

    @PostMapping("/admin/theory")
    public String regTheory(LessonForm form,
                            @SessionAttribute(name= SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                            RedirectAttributes redirectAttributes){

        if (form.getLessonId() == null) {
            service.createLesson(form, sessionUser.userId());
            redirectAttributes.addFlashAttribute("message", "이론 자료가 등록되었습니다.");
        } else {
            service.updateLesson(form, sessionUser.userId());
            redirectAttributes.addFlashAttribute("message", "이론 자료가 수정되었습니다.");
        }

        return "redirect:/admin/theory";
    }

    @PostMapping("/admin/theory/{lessonId}/delete")
    public String deleteTheory(@PathVariable Long lessonId,
                               @SessionAttribute(name= SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                               RedirectAttributes redirectAttributes) {

        int deleteCount = service.deleteLesson(lessonId, sessionUser.userId());

        if (deleteCount == 1) {
            redirectAttributes.addFlashAttribute("message", "이론 자료가 삭제되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "삭제할 이론 자료를 찾을 수 없습니다.");
        }

        return "redirect:/admin/theory";
    }



    @GetMapping("/admin/problems")
    public String problems(Model model) {

        model.addAttribute("problemList", service.findAllAdminProblem());
        model.addAttribute("subjectList", service.findAllSubject());
        model.addAttribute("curriculumList", service.findAllCurriculumNode());
        model.addAttribute("problemForm", new ProblemForm());
        model.addAttribute("screen", "admin/problems");
        return "admin/problems";
    }

    @PostMapping("/admin/problems")
    public String regProblem(ProblemForm form,
                             @SessionAttribute(name= SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                             RedirectAttributes redirectAttributes){

        if (form.getProblemId() == null) {
            service.createProblem(form, sessionUser.userId());
            redirectAttributes.addFlashAttribute("message", "문제가 등록되었습니다.");
        } else {
            service.updateProblem(form, sessionUser.userId());
            redirectAttributes.addFlashAttribute("message", "문제가 수정되었습니다.");
        }

        return "redirect:/admin/problems";
    }

    @PostMapping("/admin/problems/{problemId}/delete")
    public String deleteProblem(@PathVariable Long problemId,
                                @SessionAttribute(name= SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                                RedirectAttributes redirectAttributes) {

        int deleteCount = service.deleteProblem(problemId, sessionUser.userId());

        if (deleteCount == 1) {
            redirectAttributes.addFlashAttribute("message", "문제가 삭제되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "삭제할 문제를 찾을 수 없습니다.");
        }

        return "redirect:/admin/problems";
    }
}

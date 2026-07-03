package com.acorn.elearning.practice.controller;

import com.acorn.elearning.auth.service.SessionService;
import com.acorn.elearning.practice.dto.response.PracticeSetResponse;
import com.acorn.elearning.practice.form.CreatePracticeSetForm;
import com.acorn.elearning.practice.form.PracticeAnswerForm;
import com.acorn.elearning.practice.form.PracticeSetCompleteForm;
import com.acorn.elearning.practice.model.PracticeProblem;
import com.acorn.elearning.practice.service.PracticeService;
import com.acorn.elearning.practice.service.ProblemService;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PracticeController {

    private final ProblemService problemService;
    private final PracticeService practiceService;


    public PracticeController(ProblemService problemService, PracticeService practiceService) {
        this.problemService = problemService;
        this.practiceService = practiceService;
    }

    @GetMapping("/learning/practice")
    public String index(
            // 1. 세션에서 로그인 사용자 정보 가져오기
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                        Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // PracticeSetView view = practiceService.index(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.

        // 2. 로그인 검증
        if (sessionUser == null) {
            return "redirect:/login";
        }
        // 3. 문제 목록 조회
        List<PracticeProblem> problems = problemService.getProblems(sessionUser.userId(), "BRONZE");

        // 3. 데이터 바인딩 (Model에 담기)
        model.addAttribute("problems", problems);
        model.addAttribute("practiceAnswerForm", new PracticeAnswerForm()); // 폼 객체 바인딩
        model.addAttribute("screen", "learning/practice"); // 화면 식별자

        return "learning/practice";
    }

    @PostMapping("/learning/practice/sets")
    public String createSet(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Validated CreatePracticeSetForm form,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. 유효성 검사
        if (bindingResult.hasErrors()) {
           return "learning/practice";
        }

        // 2. 세션 사용자 확인
        if (sessionUser == null) {
            return "redirect:/login";
        }

        // 3. 서비스 계층에 세트 생성 위임
        practiceService.createPracticeSet(sessionUser, form);

        // 4. 성공 메시지 설정 및 리다이렉트
        redirectAttributes.addFlashAttribute("message", "연습 세트 생성");
        return "redirect:/learning/practice";

    }

    @GetMapping("/learning/practice/problems/{problemId}")
    public String problemDetail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long problemId, Model model) {

        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // PracticeSetView view = practiceService.problemDetail(sessionUser, problemId);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.

        // 1. 세션 사용자 확인
        if (sessionUser == null) {
            return "redirect:/login";
        }

        // 2. ProblemService를 통해 문제 상세 정보 조회
        PracticeProblem problem = problemService.getProblem(problemId);

        // 3. 모델 바인딩
        model.addAttribute("view", problem); // 문제 정보
        model.addAttribute("form", new PracticeAnswerForm()); // 답안 입력용 폼
        model.addAttribute("screen", "learning/practice"); // 화면 식별자
        return "learning/practice";
    }

    @PostMapping("/learning/practice/sets/{setAttemptId}/answers")
    public String submitAnswer(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long setAttemptId,
            @Validated PracticeAnswerForm form, // 답안 데이터
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // problemService.submitAnswer(sessionUser, form, problemId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");

        // 1. 로그인 검증
        if (sessionUser == null) {
            return "redirect:/login";
        }

        // 2. 유효성 검사
        if (bindingResult.hasErrors()) {
            // 에러 발생 시 연습 페이지로
            return "learning/practice";
        }

        // 3. 서비스 계층을 통해 채점 및 데이터 저장 로직 수행
        PracticeSetCompleteForm completeForm = new PracticeSetCompleteForm();
        completeForm.setSetAttemptId(setAttemptId);
        completeForm.setAnswers(form.getAnswers());

        practiceService.submitAnswers(sessionUser, completeForm);

        // 4. 결과 메시지 추가 및 리다이렉트
        redirectAttributes.addFlashAttribute("message", "제출 완료");

        // 채점 후 결과페이지 또는 결과화면으로 변경하기
        return "redirect:/learning/practice/result" + setAttemptId;
    }

    @PostMapping("/learning/practice/sets/{setAttemptId}/complete")
    public String completeSet(
                    @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                    @PathVariable Long setAttemptId,
                    RedirectAttributes redirectAttributes
    ) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/learning"; }

        // 1. 로그인 검증
        if (sessionUser == null) return "redirect:/login";

       // 2. 서비스 호출
        PracticeSetResponse response = practiceService.completeSet(sessionUser, setAttemptId);

        // 3.결과 확인 및 메시지 처리
        redirectAttributes.addFlashAttribute("nextStepData", response.data());

        //결과페이지 또는 결과화면으로 변경하기
        return "redirect:/learning/practice/result" + setAttemptId;
    }
}

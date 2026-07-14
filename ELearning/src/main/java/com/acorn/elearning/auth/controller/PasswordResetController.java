package com.acorn.elearning.auth.controller;

import com.acorn.elearning.auth.form.PasswordForgotForm;
import com.acorn.elearning.auth.form.PasswordResetForm;
import com.acorn.elearning.auth.service.PasswordResetService;
import com.acorn.elearning.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    // 1) 비밀번호 찾기 — 이메일 입력 화면
    @GetMapping("/forgot")
    public String forgotForm(Model model) {
        model.addAttribute("passwordForgotForm", new PasswordForgotForm());
        model.addAttribute("screen", "auth/password-forgot");
        return "auth/password-forgot";
    }

    // 2) 이메일 제출 → 재설정 메일 발송
    @PostMapping("/forgot")
    public String forgot(@Valid @ModelAttribute("passwordForgotForm") PasswordForgotForm form,
                         BindingResult bindingResult,
                         Model model) {
        model.addAttribute("screen", "auth/password-forgot");
        if (bindingResult.hasErrors()) {
            return "auth/password-forgot";
        }
        try {
            PasswordResetService.ForgotResult result = passwordResetService.requestReset(form.getEmail());
            if (result == PasswordResetService.ForgotResult.SOCIAL_ONLY) {
                // 소셜 전용 계정 분기 (확장 기능 범위 문서의 요구사항)
                model.addAttribute("errorMessage", "소셜 로그인으로 가입된 계정입니다. Google 또는 GitHub 로그인을 이용해 주세요.");
                return "auth/password-forgot";
            }
            model.addAttribute("sent", true);              // 발송 완료 상태로 같은 화면 재사용
            model.addAttribute("sentEmail", form.getEmail());
            return "auth/password-forgot";
        } catch (BusinessException ex) {                   // 메일 발송 실패 등
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/password-forgot";
        }
    }

    // 3) 메일 링크 진입 → 토큰 검증 후 새 비밀번호 입력 화면
    @GetMapping("/reset")
    public String resetForm(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("screen", "auth/password-reset");
        try {
            passwordResetService.validateToken(token);
        } catch (BusinessException ex) {
            model.addAttribute("invalid", true);           // 만료(30분 경과)/사용됨/위조 → 재설정 불가 화면
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/password-reset";
        }
        PasswordResetForm form = new PasswordResetForm();
        form.setToken(token);
        model.addAttribute("passwordResetForm", form);
        return "auth/password-reset";
    }

    // 4) 새 비밀번호 저장
    @PostMapping("/reset")
    public String reset(@Valid @ModelAttribute("passwordResetForm") PasswordResetForm form,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        model.addAttribute("screen", "auth/password-reset");
        if (!bindingResult.hasErrors() && !form.getNewPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "새 비밀번호가 서로 일치하지 않습니다.");
        }
        if (bindingResult.hasErrors()) {
            return "auth/password-reset";
        }
        try {
            passwordResetService.resetPassword(form.getToken(), form.getNewPassword());
        } catch (BusinessException ex) {
            model.addAttribute("invalid", true);
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/password-reset";
        }
        redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 변경되었습니다. 새 비밀번호로 로그인해 주세요.");
        return "redirect:/login";
    }
}
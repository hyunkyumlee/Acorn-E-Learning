package com.acorn.elearning.user.controller;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.payment.view.PaymentHistoryView;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.dto.response.UserProfileResponse;
import com.acorn.elearning.user.dto.response.UserSettingsResponse;
import com.acorn.elearning.user.form.PasswordChangeForm;
import com.acorn.elearning.user.form.ProfileForm;
import com.acorn.elearning.user.form.SecurityForm;
import com.acorn.elearning.user.form.SystemSettingsForm;
import com.acorn.elearning.user.form.WithdrawUserForm;
import com.acorn.elearning.user.service.SettingsService;
import com.acorn.elearning.user.service.UserActivityService;
import com.acorn.elearning.user.view.WithdrawConfirmView;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SettingsController {
    private final SettingsService settingsService;
    private final UserActivityService userActivityService;

    public SettingsController(SettingsService settingsService, UserActivityService userActivityService) {
        this.settingsService = settingsService;
        this.userActivityService = userActivityService;
    }

    @GetMapping("/settings")
    public String index(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        return "redirect:/settings/profile";
    }

    @GetMapping("/settings/profile")
    public String profile(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        UserProfileResponse view = settingsService.profile(sessionUser);
        addProfileModel(model, view);
        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("form") ProfileForm form,
            BindingResult bindingResult,
            HttpSession httpSession,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            addProfileModel(model, settingsService.profile(sessionUser));
            return "settings/profile";
        }

        UserProfileResponse updated = settingsService.updateProfile(sessionUser, form);
        httpSession.setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(updated.userId(), updated.email(), updated.nickname(), updated.role(), sessionUser.premiumActive()));
        redirectAttributes.addFlashAttribute("message", "회원 정보가 저장되었습니다.");
        return "redirect:/settings/profile";
    }

    @GetMapping("/settings/security")
    public String security(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        UserProfileResponse view = settingsService.security(sessionUser);
        addSecurityModel(model, view);
        return "settings/security";
    }

    @PostMapping("/settings/security")
    public String updateSecurity(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("form") SecurityForm form,
            BindingResult bindingResult,
            HttpSession httpSession,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            addSecurityModel(model, settingsService.security(sessionUser));
            return "settings/security";
        }

        try {
            UserProfileResponse updated = settingsService.updateSecurity(sessionUser, form);
            httpSession.setAttribute(
                    SessionUser.SESSION_KEY,
                    new SessionUser(updated.userId(), updated.email(), updated.nickname(), updated.role(), sessionUser.premiumActive()));
            redirectAttributes.addFlashAttribute("message", "이메일이 저장되었습니다.");
        } catch (BusinessException exception) {
            bindingResult.rejectValue("email", "duplicate", exception.getMessage());
            addSecurityModel(model, settingsService.security(sessionUser));
            return "settings/security";
        }
        return "redirect:/settings/security";
    }

    @PostMapping("/settings/security/password")
    public String changePassword(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("passwordForm") PasswordChangeForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            addSecurityModel(model, settingsService.security(sessionUser));
            return "settings/security";
        }

        try {
            settingsService.changePassword(sessionUser, form);
            redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
        } catch (BusinessException exception) {
            bindingResult.reject("passwordChange", exception.getMessage());
            addSecurityModel(model, settingsService.security(sessionUser));
            return "settings/security";
        }
        return "redirect:/settings/security";
    }

    @GetMapping("/settings/social")
    public String social(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("screen", "settings/social");
        model.addAttribute("view", settingsService.social(sessionUser));
        return "settings/social";
    }

    @PostMapping("/settings/social/{provider}/disconnect")
    public String disconnectSocial(@PathVariable String provider) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "settings/social"; }
        // SessionUser sessionUser = currentSessionUser();
        // settingsService.disconnectSocial(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/settings/social";
    }

    @GetMapping("/settings/system")
    public String system(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        UserSettingsResponse view = settingsService.system(sessionUser);
        addSystemModel(model, view);
        return "settings/system";
    }

    @PostMapping("/settings/system")
    public String updateSystem(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("form") SystemSettingsForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            addSystemModel(model, settingsService.system(sessionUser));
            return "settings/system";
        }

        settingsService.updateSettings(sessionUser, form);
        redirectAttributes.addFlashAttribute("message", "시스템 설정이 저장되었습니다.");
        return "redirect:/settings/system";
    }

    @GetMapping("/settings/payment")
    public String paymentHistory(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        PaymentHistoryView view = userActivityService.paymentHistoryView(sessionUser, page, size);
        model.addAttribute("screen", "settings/payment");
        model.addAttribute("view", view);
        return "settings/payment";
    }

    @GetMapping("/settings/withdraw")
    public String withdrawConfirm(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        addWithdrawModel(model, settingsService.withdrawConfirm(sessionUser));
        return "settings/withdraw";
    }

    @PostMapping("/settings/withdraw")
    public String withdraw(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("form") WithdrawUserForm form,
            BindingResult bindingResult,
            HttpSession httpSession,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            addWithdrawModel(model, settingsService.withdrawConfirm(sessionUser));
            return "settings/withdraw";
        }

        settingsService.withdraw(sessionUser, form);
        httpSession.invalidate();
        return "redirect:/login?withdrawn=1";
    }

    private void addProfileModel(Model model, UserProfileResponse view) {
        model.addAttribute("screen", "settings/profile");
        model.addAttribute("view", view);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", profileForm(view));
        }
    }

    private ProfileForm profileForm(UserProfileResponse view) {
        ProfileForm form = new ProfileForm();
        form.setNickname(view.nickname());
        form.setLearningGoal(view.learningGoal());
        return form;
    }

    private void addSecurityModel(Model model, UserProfileResponse view) {
        model.addAttribute("screen", "settings/security");
        model.addAttribute("view", view);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", securityForm(view));
        }
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new PasswordChangeForm());
        }
    }

    private SecurityForm securityForm(UserProfileResponse view) {
        SecurityForm form = new SecurityForm();
        form.setEmail(view.email());
        return form;
    }

    private void addSystemModel(Model model, UserSettingsResponse view) {
        model.addAttribute("screen", "settings/system");
        model.addAttribute("view", view);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", systemSettingsForm(view));
        }
    }

    private SystemSettingsForm systemSettingsForm(UserSettingsResponse view) {
        SystemSettingsForm form = new SystemSettingsForm();
        form.setTheme(view.theme());
        form.setDarkModeEnabled("DARK".equals(view.theme()));
        form.setNotificationEnabled(view.notificationEnabled());
        form.setReducedMotionEnabled(view.reducedMotionEnabled());
        form.setDisplayLanguage(view.displayLanguage());
        return form;
    }

    private void addWithdrawModel(Model model, WithdrawConfirmView view) {
        model.addAttribute("screen", "settings/withdraw");
        model.addAttribute("view", view);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new WithdrawUserForm());
        }
    }
}

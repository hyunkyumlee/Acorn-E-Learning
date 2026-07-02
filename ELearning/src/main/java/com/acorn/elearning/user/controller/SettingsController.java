package com.acorn.elearning.user.controller;

import com.acorn.elearning.payment.view.PaymentHistoryView;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.service.UserActivityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class SettingsController {
    private final UserActivityService userActivityService;

    public SettingsController(UserActivityService userActivityService) {
        this.userActivityService = userActivityService;
    }

    @GetMapping("/settings")
    public String index(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SettingsHomeView view = settingsService.index(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "settings/index");
        return "settings/index";
    }

    @GetMapping("/settings/profile")
    public String profile(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UserProfileResponse view = settingsService.profile(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "settings/profile");
        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfile() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "settings/index"; }
        // SessionUser sessionUser = currentSessionUser();
        // settingsService.updateProfile(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/settings/profile";
    }

    @GetMapping("/settings/security")
    public String security(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UserSettingsResponse view = settingsService.security(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "settings/security");
        return "settings/security";
    }

    @PostMapping("/settings/security")
    public String updateSecurity() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "settings/security"; }
        // SessionUser sessionUser = currentSessionUser();
        // settingsService.updateSecurity(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/settings/security";
    }

    @GetMapping("/settings/social")
    public String social(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SocialAccountView view = settingsService.social(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "settings/social");
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
    public String system(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UserSettingsResponse view = settingsService.system(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "settings/system");
        return "settings/system";
    }

    @PostMapping("/settings/system")
    public String updateSystem() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "settings/system"; }
        // SessionUser sessionUser = currentSessionUser();
        // settingsService.updateSystem(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
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
    public String withdrawConfirm(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // WithdrawConfirmView view = settingsService.withdrawConfirm(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "settings/withdraw");
        return "settings/withdraw";
    }

    @PostMapping("/settings/withdraw")
    public String withdraw() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "settings/payment"; }
        // SessionUser sessionUser = currentSessionUser();
        // userService.withdraw(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/login?withdrawn=1";
    }
}

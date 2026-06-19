package com.acorn.elearning.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PaymentController {

    @GetMapping("/payments")
    public String index(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PaymentChoiceView view = dummyPaymentService.index(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "payment/index");
        return "payment/index";
    }

    @GetMapping("/payments/card")
    public String cardForm(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PaymentChoiceView view = dummyPaymentService.cardForm(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "payment/card");
        return "payment/card";
    }

    @GetMapping("/payments/bank")
    public String bankForm(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PaymentChoiceView view = dummyPaymentService.bankForm(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "payment/bank");
        return "payment/bank";
    }

    @PostMapping("/payments/dummy")
    public String dummyPay() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "payment/index"; }
        // SessionUser sessionUser = currentSessionUser();
        // dummyPaymentService.dummyPay(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/payments/complete";
    }

    @GetMapping("/payments/complete")
    public String complete(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PaymentResultView view = dummyPaymentService.complete(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "payment/complete");
        return "payment/complete";
    }
}

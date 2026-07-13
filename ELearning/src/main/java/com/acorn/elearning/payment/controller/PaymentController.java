package com.acorn.elearning.payment.controller;

import com.acorn.elearning.common.idempotency.IdempotencyTokenService;
import com.acorn.elearning.config.TossPaymentsProperties;
import com.acorn.elearning.payment.dto.response.PaymentDetailResponse;
import com.acorn.elearning.payment.dto.response.PaymentProductListResponse;
import com.acorn.elearning.payment.dto.response.PaymentResultResponse;
import com.acorn.elearning.payment.form.DummyPaymentForm;
import com.acorn.elearning.payment.service.DummyPaymentService;
import com.acorn.elearning.payment.service.KakaoPayService;
import com.acorn.elearning.payment.service.PaymentAccessService;
import com.acorn.elearning.payment.service.TossPaymentService;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Optional;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PaymentController {
    private static final String PRODUCT_CODE = "PREMIUM_LIFETIME";
    private static final String FORM_TYPE = "PAYMENT_DUMMY";

    private final DummyPaymentService dummyPaymentService;
    private final KakaoPayService kakaoPayService;
    private final PaymentAccessService paymentAccessService;
    private final IdempotencyTokenService idempotencyTokenService;
    private final TossPaymentsProperties tossPaymentsProperties;
    private final TossPaymentService tossPaymentService;

    public PaymentController(
            DummyPaymentService dummyPaymentService,
            KakaoPayService kakaoPayService,
            PaymentAccessService paymentAccessService,
            IdempotencyTokenService idempotencyTokenService,
            TossPaymentsProperties tossPaymentsProperties,
            TossPaymentService tossPaymentService
    ) {
        this.dummyPaymentService = dummyPaymentService;
        this.kakaoPayService = kakaoPayService;
        this.paymentAccessService = paymentAccessService;
        this.idempotencyTokenService = idempotencyTokenService;
        this.tossPaymentsProperties = tossPaymentsProperties;
        this.tossPaymentService = tossPaymentService;
    }

    @GetMapping("/payments")
    public String index(Model model) {
        model.addAttribute("screen", "payment/index");
        addProductModel(model);
        return "payment/index";
    }

    @GetMapping("/payments/card")
    public String cardForm(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            HttpSession httpSession,
            Model model
    ) {
        SessionUser currentUser = currentUser(sessionUser);
        if (currentUser == null) {
            return "redirect:/login";
        }
        preparePaymentForm(model, httpSession, currentUser, "CARD");
        return "payment/card";
    }

    @GetMapping("/payments/bank")
    public String bankForm() {
        return "redirect:/payments/kakao-pay";
    }

    @GetMapping("/payments/kakao-pay")
    public String kakaoPayForm(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            HttpSession httpSession,
            Model model
    ) {
        SessionUser currentUser = currentUser(sessionUser);
        if (currentUser == null) {
            return "redirect:/login";
        }
        preparePaymentForm(model, httpSession, currentUser, DummyPaymentService.METHOD_KAKAO_PAY);
        return "payment/kakao";
    }

    @PostMapping("/payments/dummy")
    public String dummyPay(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid DummyPaymentForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        SessionUser currentUser = currentUser(sessionUser);
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("screen", "payment/card");
            addProductModel(model);
            model.addAttribute("idempotencyToken", form.getIdempotencyToken());
            return paymentFormView(form.getPaymentMethod());
        }

        PaymentResultResponse result = dummyPaymentService.pay(currentUser, form);
        redirectAttributes.addAttribute("paymentId", result.paymentId());
        return "redirect:/payments/complete";
    }

    @PostMapping("/payments/kakao-pay/ready")
    public String kakaoPayReady(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid DummyPaymentForm form,
            BindingResult bindingResult,
            HttpSession httpSession,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        SessionUser currentUser = currentUser(sessionUser);
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (paymentAccessService.hasPremiumAccess(currentUser)) {
            redirectAttributes.addFlashAttribute("paymentError", "이미 Premium 권한이 활성화되어 있습니다.");
            return "redirect:/payments/kakao-pay";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("screen", "payment/kakao");
            addProductModel(model);
            model.addAttribute("idempotencyToken", form.getIdempotencyToken());
            return "payment/kakao";
        }

        String redirectUrl = kakaoPayService.ready(currentUser, form, httpSession);
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/payments/kakao-pay/success")
    public String kakaoPaySuccess(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "pg_token", required = false) String pgToken,
            HttpSession httpSession,
            RedirectAttributes redirectAttributes
    ) {
        SessionUser currentUser = currentUser(sessionUser);
        if (currentUser == null) {
            return "redirect:/login";
        }

        PaymentResultResponse result = kakaoPayService.approve(currentUser, pgToken, httpSession);
        redirectAttributes.addAttribute("paymentId", result.paymentId());
        return "redirect:/payments/complete";
    }

    @GetMapping("/payments/kakao-pay/cancel")
    public String kakaoPayCancel(
            HttpSession httpSession,
            RedirectAttributes redirectAttributes
    ) {
        kakaoPayService.clear(httpSession);
        redirectAttributes.addFlashAttribute("paymentError", "카카오페이 결제가 취소되었습니다.");
        return "redirect:/payments";
    }

    @GetMapping("/payments/kakao-pay/fail")
    public String kakaoPayFail(
            HttpSession httpSession,
            RedirectAttributes redirectAttributes
    ) {
        kakaoPayService.clear(httpSession);
        redirectAttributes.addFlashAttribute("paymentError", "카카오페이 결제에 실패했습니다. 다시 시도해주세요.");
        return "redirect:/payments";
    }

    @GetMapping("/payments/toss/success")
    public String tossSuccess(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Integer amount,
            RedirectAttributes redirectAttributes
    ) {
        SessionUser currentUser = currentUser(sessionUser);
        if (currentUser == null) {
            return "redirect:/login";
        }

        PaymentResultResponse result = tossPaymentService.approve(currentUser, paymentKey, orderId, amount);
        redirectAttributes.addAttribute("paymentId", result.paymentId());
        return "redirect:/payments/complete";
    }

    @GetMapping("/payments/toss/fail")
    public String tossFail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam String orderId,
            @RequestParam(required = false) String code,
            RedirectAttributes redirectAttributes
    ) {
        SessionUser currentUser = currentUser(sessionUser);
        if (currentUser == null) {
            return "redirect:/login";
        }

        tossPaymentService.handleFailure(currentUser, orderId, code);
        String message = "PAY_PROCESS_CANCELED".equals(code) || "USER_CANCEL".equals(code)
                ? "토스페이먼츠 결제가 취소되었습니다."
                : "토스페이먼츠 결제에 실패했습니다. 다시 시도해주세요.";
        redirectAttributes.addFlashAttribute("paymentError", message);
        return "redirect:/payments/card";
    }

    @GetMapping("/payments/complete")
    public String complete(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "paymentId", required = false) Long paymentId,
            Model model
    ) {
        SessionUser currentUser = currentUser(sessionUser);
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("screen", "payment/complete");
        if (paymentId != null) {
            PaymentDetailResponse detail = dummyPaymentService.detail(currentUser, paymentId);
            model.addAttribute("detail", detail);
        } else {
            Optional<PaymentResultResponse> result = dummyPaymentService.latestResult(currentUser.userId());
            result.ifPresent(value -> model.addAttribute("result", value));
        }
        return "payment/complete";
    }

    private void preparePaymentForm(
            Model model,
            HttpSession httpSession,
            SessionUser sessionUser,
            String paymentMethod
    ) {
        DummyPaymentForm form = new DummyPaymentForm();
        form.setProductCode(PRODUCT_CODE);
        form.setPaymentMethod(paymentMethod);
        form.setIdempotencyToken(idempotencyTokenService.issue(FORM_TYPE, httpSession, sessionUser.userId()).token());

        model.addAttribute("screen", paymentFormView(paymentMethod));
        model.addAttribute("form", form);
        addProductModel(model);
        model.addAttribute("idempotencyToken", form.getIdempotencyToken());
        model.addAttribute("premiumActive", paymentAccessService.hasPremiumAccess(sessionUser));
        if (DummyPaymentService.METHOD_CARD.equals(paymentMethod)) {
            model.addAttribute("tossClientKey", tossPaymentsProperties.getClientKey());
        }
    }

    private String paymentFormView(String paymentMethod) {
        if (DummyPaymentService.METHOD_KAKAO_PAY.equals(paymentMethod)) {
            return "payment/kakao";
        }
        return "payment/card";
    }

    private SessionUser currentUser(SessionUser sessionUser) {
        return sessionUser;
    }

    private void addProductModel(Model model) {
        PaymentProductListResponse response = paymentAccessService.products();
        PaymentProductListResponse.Product product = response.products().stream()
                .filter(item -> PRODUCT_CODE.equals(item.productCode()))
                .findFirst()
                .orElseGet(() -> response.products().isEmpty() ? null : response.products().get(0));

        model.addAttribute("products", response.products());
        model.addAttribute("product", product);
        model.addAttribute("productCode", product == null ? PRODUCT_CODE : product.productCode());
        model.addAttribute("productName", product == null ? "Premium Lifetime" : product.productName());
        model.addAttribute("productPriceLabel", product == null ? "9,900원" : priceLabel(product.price()));
    }

    private String priceLabel(BigDecimal price) {
        if (price == null) {
            return "0원";
        }
        return NumberFormat.getIntegerInstance(Locale.KOREA).format(price) + "원";
    }
}

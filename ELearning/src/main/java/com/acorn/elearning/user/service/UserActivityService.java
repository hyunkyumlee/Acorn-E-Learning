package com.acorn.elearning.user.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.payment.dto.response.PremiumAccessResponse;
import com.acorn.elearning.payment.mapper.DummyPaymentMapper;
import com.acorn.elearning.payment.model.PaymentHistoryItem;
import com.acorn.elearning.payment.service.PaymentAccessService;
import com.acorn.elearning.payment.view.PaymentHistoryView;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.dto.response.PaymentHistoryPageResponse;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserActivityService {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private final DummyPaymentMapper dummyPaymentMapper;
    private final PaymentAccessService paymentAccessService;

    public UserActivityService(
            DummyPaymentMapper dummyPaymentMapper,
            PaymentAccessService paymentAccessService
    ) {
        this.dummyPaymentMapper = dummyPaymentMapper;
        this.paymentAccessService = paymentAccessService;
    }

    @Transactional(readOnly = true)
    public PaymentHistoryPageResponse payments(SessionUser sessionUser) {
        return payments(sessionUser, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    @Transactional(readOnly = true)
    public PaymentHistoryPageResponse payments(SessionUser sessionUser, int page, int size) {
        Long userId = requireUserId(sessionUser);
        int safePage = normalizePage(page);
        int safeSize = normalizeSize(size);
        long totalElements = dummyPaymentMapper.countHistoryByUserId(userId);
        List<PaymentHistoryItem> historyItems = totalElements == 0
                ? List.of()
                : dummyPaymentMapper.findHistoryByUserId(userId, offset(safePage, safeSize), safeSize);
        PremiumAccessResponse premiumAccess = paymentAccessService.premiumAccess(userId);

        return PaymentHistoryPageResponse.of(
                premiumAccess,
                historyItems,
                safePage,
                safeSize,
                totalElements
        );
    }

    @Transactional(readOnly = true)
    public PaymentHistoryView paymentHistoryView(SessionUser sessionUser, int page, int size) {
        return PaymentHistoryView.from(payments(sessionUser, page, size));
    }

    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // Long userId = sessionUser.userId();
        // User user = userMapper.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // userMapper.update(applyForm(user, form));
        // return Map.of("user", UserProfileResponse.from(user));
        return Map.of("action", action, "status", "SKELETON");
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }

    private int normalizePage(int page) {
        return Math.max(page, DEFAULT_PAGE);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private int offset(int page, int size) {
        long offset = (long) page * size;
        return offset > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) offset;
    }
}

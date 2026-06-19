package com.acorn.elearning.analysis.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AiAnalysisService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // PremiumGrant grant = premiumGrantMapper.findActiveByUserId(userId).orElseThrow(() -> new BusinessException(ErrorCode.AUTH_FORBIDDEN));
        // AiAnalysisReport report = aiAnalysisReportMapper.findById(reportId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // aiRequestLogMapper.insert(AiRequestLog.pending(userId, reportId));
        // return Map.of("report", AnalysisReportResponse.from(report));
        return Map.of("action", action, "status", "SKELETON");
    }
}

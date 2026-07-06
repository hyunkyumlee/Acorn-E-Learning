package com.acorn.elearning.analysis.controller;

import com.acorn.elearning.analysis.dto.request.AnalysisRetryRequest;
import com.acorn.elearning.analysis.dto.request.GenerateAnalysisRequest;
import com.acorn.elearning.analysis.dto.response.AnalysisAutoRefreshResponse;
import com.acorn.elearning.analysis.dto.response.AnalysisReportResponse;
import com.acorn.elearning.analysis.dto.response.AnalysisStatusResponse;
import com.acorn.elearning.analysis.service.AiAnalysisService;
import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.security.SessionUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalysisApiController {
    private final AiAnalysisService aiAnalysisService;

    public AnalysisApiController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @PostMapping("/api/analyses")
    public ApiResponse<AnalysisReportResponse> generate(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @RequestBody GenerateAnalysisRequest request
    ) {
        return ApiResponse.success(aiAnalysisService.generate(sessionUser, request));
    }

    @PostMapping("/api/analyses/latest/refresh")
    public ApiResponse<AnalysisAutoRefreshResponse> refreshLatest(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        return ApiResponse.success(aiAnalysisService.refreshLatestIfRequired(sessionUser));
    }

    @GetMapping("/api/analyses/{reportId}")
    public ApiResponse<AnalysisReportResponse> detail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long reportId
    ) {
        return ApiResponse.success(aiAnalysisService.detail(sessionUser, reportId));
    }

    @PostMapping("/api/analyses/{reportId}/retry")
    public ApiResponse<AnalysisReportResponse> retry(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long reportId,
            @Valid @RequestBody AnalysisRetryRequest request
    ) {
        return ApiResponse.success(aiAnalysisService.retry(sessionUser, reportId));
    }

    @GetMapping("/api/analyses/{reportId}/status")
    public ApiResponse<AnalysisStatusResponse> status(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long reportId
    ) {
        return ApiResponse.success(aiAnalysisService.status(sessionUser, reportId));
    }
}

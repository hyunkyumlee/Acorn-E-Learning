package com.acorn.elearning.analysis.controller;

import com.acorn.elearning.common.response.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalysisApiController {

    @PostMapping("/api/analyses")
    public ApiResponse<Map<String, Object>> generate() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // GenerateAnalysisForm form = request body 또는 form binding 값으로 받으세요.
        // AnalysisReportResponse response = aiAnalysisService.generate(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("ANALYSIS-001");
    }

    @GetMapping("/api/analyses/{reportId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long reportId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ExamSessionResponse response = aiAnalysisService.detail(sessionUser, reportId);
        // return ApiResponse.success(response);
        return ok("ANALYSIS-002");
    }

    @PostMapping("/api/analyses/{reportId}/retry")
    public ApiResponse<Map<String, Object>> retry(@PathVariable Long reportId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AnalysisRetryForm form = request body 또는 form binding 값으로 받으세요.
        // ExamStatusResponse response = aiAnalysisService.retry(sessionUser, form, reportId);
        // return ApiResponse.success(response);
        return ok("ANALYSIS-003");
    }

    @GetMapping("/api/analyses/{reportId}/status")
    public ApiResponse<Map<String, Object>> status(@PathVariable Long reportId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ExamStatusResponse response = aiAnalysisService.status(sessionUser, reportId);
        // return ApiResponse.success(response);
        return ok("ANALYSIS-004");
    }

    private ApiResponse<Map<String, Object>> ok(String endpointId) {
        // TODO: 개별 endpoint method에서 service 호출과 Response DTO 변환을 끝내면 이 helper를 제거하세요.
        // return ApiResponse.success(response); 형태가 최종 구현입니다.
        return ApiResponse.success(Map.of("endpointId", endpointId, "status", "SKELETON"));
    }
}

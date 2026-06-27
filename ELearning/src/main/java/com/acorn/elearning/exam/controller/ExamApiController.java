package com.acorn.elearning.exam.controller;

import com.acorn.elearning.common.response.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExamApiController {

    @GetMapping("/api/exams/eligibility")
    public ApiResponse<Map<String, Object>> eligibility() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ExamStatusResponse response = aiExamService.eligibility(sessionUser);
        // return ApiResponse.success(response);
        return ok("EXAM-001");
    }

    @PostMapping("/api/exams")
    public ApiResponse<Map<String, Object>> create() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CreateExamForm form = request body 또는 form binding 값으로 받으세요.
        // ExamSessionResponse response = aiExamService.create(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("EXAM-002");
    }

    @GetMapping("/api/exams/{examId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long examId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ExamSessionResponse response = aiExamService.detail(sessionUser, examId);
        // return ApiResponse.success(response);
        return ok("EXAM-003");
    }

    @PostMapping("/api/exams/{examId}/answers/{aiProblemId}")
    public ApiResponse<Map<String, Object>> answer(@PathVariable Long examId, @PathVariable Long aiProblemId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SaveExamAnswerForm form = request body 또는 form binding 값으로 받으세요.
        // return ApiResponse.success(response);
        return ok("EXAM-004");
    }

    @PostMapping("/api/exams/{examId}/submit")
    public ApiResponse<Map<String, Object>> submit(@PathVariable Long examId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ExamSubmitForm form = request body 또는 form binding 값으로 받으세요.
        // return ApiResponse.success(response);
        return ok("EXAM-005");
    }

    @PostMapping("/api/exams/{examId}/retry-execution")
    public ApiResponse<Map<String, Object>> retryExecution(@PathVariable Long examId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // return ApiResponse.success(response);
        return ok("EXAM-006");
    }

    @GetMapping("/api/exams/{examId}/result")
    public ApiResponse<Map<String, Object>> result(@PathVariable Long examId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // return ApiResponse.success(response);
        return ok("EXAM-007");
    }

    @GetMapping("/api/exams/{examId}/status")
    public ApiResponse<Map<String, Object>> status(@PathVariable Long examId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // return ApiResponse.success(response);
        return ok("EXAM-008");
    }

    private ApiResponse<Map<String, Object>> ok(String endpointId) {
        // TODO: 개별 endpoint method에서 service 호출과 Response DTO 변환을 끝내면 이 helper를 제거하세요.
        // return ApiResponse.success(response); 형태가 최종 구현입니다.
        return ApiResponse.success(Map.of("endpointId", endpointId, "status", "SKELETON"));
    }
}

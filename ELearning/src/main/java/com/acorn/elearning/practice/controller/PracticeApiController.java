package com.acorn.elearning.practice.controller;

import com.acorn.elearning.common.response.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PracticeApiController {

    @PostMapping("/api/practice/sets")
    public ApiResponse<Map<String, Object>> createSet() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CreateSetForm form = request body 또는 form binding 값으로 받으세요.
        // CreateSetResponse response = practiceService.createSet(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("PRACTICE-001");
    }

    @GetMapping("/api/practice/sets/{setAttemptId}")
    public ApiResponse<Map<String, Object>> set(@PathVariable Long setAttemptId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SetResponse response = practiceService.set(sessionUser, setAttemptId);
        // return ApiResponse.success(response);
        return ok("PRACTICE-002");
    }

    @GetMapping("/api/practice/problems/{problemId}")
    public ApiResponse<Map<String, Object>> problem(@PathVariable Long problemId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PracticeProblemDetailResponse response = problemService.problem(sessionUser, problemId);
        // return ApiResponse.success(response);
        return ok("PRACTICE-002-DETAIL");
    }

    @PostMapping("/api/practice/sets/{setAttemptId}/answers")
    public ApiResponse<Map<String, Object>> answer(@PathVariable Long setAttemptId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SaveExamAnswerForm form = request body 또는 form binding 값으로 받으세요.
        // ExamSubmitResponse response = problemService.answer(sessionUser, form, setAttemptId);
        // return ApiResponse.success(response);
        return ok("PRACTICE-003");
    }

    @PostMapping("/api/practice/sets/{setAttemptId}/complete")
    public ApiResponse<Map<String, Object>> complete(@PathVariable Long setAttemptId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CompleteForm form = request body 또는 form binding 값으로 받으세요.
        // CompleteResponse response = practiceService.complete(sessionUser, form, setAttemptId);
        // return ApiResponse.success(response);
        return ok("PRACTICE-004");
    }

    @GetMapping("/api/reviews/wrong-answers")
    public ApiResponse<Map<String, Object>> wrongAnswers() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // WrongAnswerPageResponse response = practiceService.wrongAnswers(sessionUser);
        // return ApiResponse.success(response);
        return ok("REVIEW-001");
    }

    @GetMapping("/api/reviews/wrong-answers/{wrongAnswerId}")
    public ApiResponse<Map<String, Object>> wrongAnswer(@PathVariable Long wrongAnswerId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // WrongAnswerDetailResponse response = practiceService.wrongAnswer(sessionUser, wrongAnswerId);
        // return ApiResponse.success(response);
        return ok("REVIEW-002");
    }

    @PostMapping("/api/reviews/wrong-answers/{wrongAnswerId}/retry")
    public ApiResponse<Map<String, Object>> retry(@PathVariable Long wrongAnswerId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AnalysisRetryForm form = request body 또는 form binding 값으로 받으세요.
        // ExamStatusResponse response = practiceService.retry(sessionUser, form, wrongAnswerId);
        // return ApiResponse.success(response);
        return ok("REVIEW-003");
    }

    @PatchMapping("/api/reviews/wrong-answers/{wrongAnswerId}/reviewed")
    public ApiResponse<Map<String, Object>> reviewed(@PathVariable Long wrongAnswerId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ReviewedForm form = request body 또는 form binding 값으로 받으세요.
        // ReviewedResponse response = practiceService.reviewed(sessionUser, form, wrongAnswerId);
        // return ApiResponse.success(response);
        return ok("REVIEW-004");
    }

    private ApiResponse<Map<String, Object>> ok(String endpointId) {
        // TODO: 개별 endpoint method에서 service 호출과 Response DTO 변환을 끝내면 이 helper를 제거하세요.
        // return ApiResponse.success(response); 형태가 최종 구현입니다.
        return ApiResponse.success(Map.of("endpointId", endpointId, "status", "SKELETON"));
    }
}

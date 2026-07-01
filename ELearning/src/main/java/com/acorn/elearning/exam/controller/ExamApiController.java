package com.acorn.elearning.exam.controller;

import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.exam.dto.request.CreateExamRequest;
import com.acorn.elearning.exam.dto.request.ExamSubmitRequest;
import com.acorn.elearning.exam.dto.request.SaveExamAnswerRequest;
import com.acorn.elearning.exam.dto.response.ExamEligibilityResponse;
import com.acorn.elearning.exam.dto.response.ExamCodeRunResponse;
import com.acorn.elearning.exam.dto.response.ExamProblemStepResponse;
import com.acorn.elearning.exam.dto.response.ExamResultResponse;
import com.acorn.elearning.exam.dto.response.ExamSessionResponse;
import com.acorn.elearning.exam.dto.response.ExamStatusResponse;
import com.acorn.elearning.exam.dto.response.ExamSubmitResponse;
import com.acorn.elearning.exam.service.AiExamService;
import com.acorn.elearning.exam.service.ExamCodeRunService;
import com.acorn.elearning.security.SessionUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExamApiController {
    private final AiExamService aiExamService;
    private final ExamCodeRunService examCodeRunService;

    public ExamApiController(AiExamService aiExamService, ExamCodeRunService examCodeRunService) {
        this.aiExamService = aiExamService;
        this.examCodeRunService = examCodeRunService;
    }

    @GetMapping("/api/exams/eligibility")
    public ApiResponse<ExamEligibilityResponse> eligibility(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        return ApiResponse.success(aiExamService.eligibility(sessionUser));
    }

    @PostMapping("/api/exams")
    public ApiResponse<ExamSessionResponse> create(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @RequestBody CreateExamRequest request
    ) {
        return ApiResponse.success(aiExamService.create(sessionUser, request));
    }

    @GetMapping("/api/exams/{examId}")
    public ApiResponse<ExamSessionResponse> detail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId
    ) {
        return ApiResponse.success(aiExamService.detail(sessionUser, examId));
    }

    @GetMapping("/api/exams/{examId}/problems/{problemNo}")
    public ApiResponse<ExamProblemStepResponse> problem(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            @PathVariable Integer problemNo
    ) {
        return ApiResponse.success(ExamProblemStepResponse.from(aiExamService.detail(sessionUser, examId), problemNo));
    }

    @PostMapping("/api/exams/{examId}/answers/{aiProblemId}")
    public ApiResponse<ExamSessionResponse> saveAnswer(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            @PathVariable Long aiProblemId,
            @Valid @RequestBody SaveExamAnswerRequest request
    ) {
        return ApiResponse.success(aiExamService.saveAnswer(sessionUser, examId, aiProblemId, request));
    }

    @PostMapping("/api/exams/{examId}/problems/{problemNo}/answers/{aiProblemId}")
    public ApiResponse<ExamProblemStepResponse> saveStepAnswer(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            @PathVariable Integer problemNo,
            @PathVariable Long aiProblemId,
            @Valid @RequestBody SaveExamAnswerRequest request
    ) {
        ExamSessionResponse exam = aiExamService.saveAnswer(sessionUser, examId, aiProblemId, request);
        return ApiResponse.success(ExamProblemStepResponse.from(exam, problemNo));
    }

    @PostMapping("/api/exams/{examId}/problems/{problemNo}/answers/{aiProblemId}/test-run")
    public ApiResponse<ExamCodeRunResponse> testRun(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            @PathVariable Integer problemNo,
            @PathVariable Long aiProblemId,
            @Valid @RequestBody SaveExamAnswerRequest request
    ) {
        return ApiResponse.success(examCodeRunService.run(sessionUser, examId, aiProblemId, request));
    }

    @PostMapping("/api/exams/{examId}/submit")
    public ApiResponse<ExamSubmitResponse> submit(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            @Valid @RequestBody ExamSubmitRequest request
    ) {
        return ApiResponse.success(aiExamService.submit(sessionUser, examId));
    }

    @PostMapping("/api/exams/{examId}/problems/{problemNo}/submit")
    public ApiResponse<ExamSubmitResponse> submitStep(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            @PathVariable Integer problemNo,
            @Valid @RequestBody ExamSubmitRequest request
    ) {
        return ApiResponse.success(aiExamService.submit(sessionUser, examId));
    }

    @PostMapping("/api/exams/{examId}/retry-execution")
    public ApiResponse<ExamSubmitResponse> retryExecution(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId
    ) {
        return ApiResponse.success(aiExamService.retryExecution(sessionUser, examId));
    }

    @GetMapping("/api/exams/{examId}/result")
    public ApiResponse<ExamResultResponse> result(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId
    ) {
        return ApiResponse.success(aiExamService.result(sessionUser, examId));
    }

    @GetMapping("/api/exams/{examId}/status")
    public ApiResponse<ExamStatusResponse> status(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId
    ) {
        return ApiResponse.success(aiExamService.status(sessionUser, examId));
    }
}

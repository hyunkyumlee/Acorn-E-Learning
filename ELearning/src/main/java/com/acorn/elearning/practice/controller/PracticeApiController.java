package com.acorn.elearning.practice.controller;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.practice.dto.request.FreeCodingRunRequest;
import com.acorn.elearning.practice.dto.response.FreeCodingRunResponse;
import com.acorn.elearning.practice.dto.response.PracticeSetResponse;
import com.acorn.elearning.practice.form.CreatePracticeSetForm;
import com.acorn.elearning.practice.form.PracticeSetCompleteForm;
import com.acorn.elearning.practice.form.ReviewWrongAnswerForm;
import com.acorn.elearning.practice.form.WrongAnswerRetryForm;
import com.acorn.elearning.practice.service.PracticeService;
import com.acorn.elearning.practice.service.FreeCodingService;
import com.acorn.elearning.practice.service.WrongAnswerService;
import com.acorn.elearning.practice.view.PracticeSetView;
import com.acorn.elearning.practice.view.WrongAnswerDetailView;
import com.acorn.elearning.practice.view.WrongAnswerPageView;
import com.acorn.elearning.security.SessionUser;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PracticeApiController {

    private final PracticeService practiceService;
    private final WrongAnswerService wrongAnswerService;
    private final FreeCodingService freeCodingService;

    public PracticeApiController(PracticeService practiceService,
                                 WrongAnswerService wrongAnswerService,
                                 FreeCodingService freeCodingService) {
        this.practiceService = practiceService;
        this.wrongAnswerService = wrongAnswerService;
        this.freeCodingService = freeCodingService;
    }

    @PostMapping("/api/practice/free-coding/run")
    public ApiResponse<FreeCodingRunResponse> runFreeCoding(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @RequestBody FreeCodingRunRequest request
    ) {
        return ApiResponse.success(freeCodingService.run(sessionUser, request));
    }

    @PostMapping("/api/practice/sets")
    public ApiResponse<Map<String, Object>> createSet(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Validated @ModelAttribute CreatePracticeSetForm form,
            BindingResult bindingResult
    ) {
        requireSessionUser(sessionUser);

        if (bindingResult.hasErrors()) {
            return validationFail(bindingResult);
        }

        PracticeSetView view = practiceService.createPracticeSet(sessionUser, form);
        return ApiResponse.success(Map.of(
                "title", view.title(),
                "status", view.status(),
                "attributes", view.attributes()
        ));
    }

    @GetMapping("/api/practice/sets/{setAttemptId}")
    public ApiResponse<Map<String, Object>> set(
            @PathVariable Long setAttemptId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        requireSessionUser(sessionUser);

        throw new BusinessException(
                ErrorCode.COMMON_NOT_FOUND,
                "아직 구현되지 않은 API입니다."
        );
    }

    @GetMapping("/api/practice/problems/{problemId}")
    public ApiResponse<Map<String, Object>> problem(
            @PathVariable Long problemId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        requireSessionUser(sessionUser);

        throw new BusinessException(
                ErrorCode.COMMON_NOT_FOUND,
                "아직 구현되지 않은 API입니다."
        );
    }

    @PostMapping("/api/practice/sets/{setAttemptId}/answers")
    public ApiResponse<Map<String, Object>> answer(
            @PathVariable Long setAttemptId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Validated @ModelAttribute PracticeSetCompleteForm form,
            BindingResult bindingResult
    ) {
        requireSessionUser(sessionUser);

        if (bindingResult.hasErrors()) {
            return validationFail(bindingResult);
        }

        if (form.getSetAttemptId() == null) {
            form.setSetAttemptId(setAttemptId);
        }

        if (!setAttemptId.equals(form.getSetAttemptId())) {
            throw new BusinessException(
                    ErrorCode.COMMON_VALIDATION_FAILED,
                    "세트 정보가 일치하지 않습니다."
            );
        }

        return ApiResponse.success(Map.of(
                "message", "현재 구현에서는 complete API를 통해 답안 제출을 처리합니다."
        ));
    }

    @PostMapping("/api/practice/sets/{setAttemptId}/complete")
    public ApiResponse<Map<String, Object>> complete(
            @PathVariable Long setAttemptId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        requireSessionUser(sessionUser);

        PracticeSetResponse response = practiceService.completeSet(sessionUser, setAttemptId);
        return ApiResponse.success(response.data());
    }

    @GetMapping("/api/reviews/wrong-answers")
    public ApiResponse<Map<String, Object>> wrongAnswers(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "nodeId", required = false) Long nodeId,
            @RequestParam(name = "lessonId", required = false) Long lessonId
    ) {
        requireSessionUser(sessionUser);

        WrongAnswerPageView view = wrongAnswerService.list(sessionUser, nodeId, lessonId);
        return ApiResponse.success(Map.of(
                "title", view.title(),
                "status", view.status(),
                "attributes", view.attributes()
        ));
    }

    @GetMapping("/api/reviews/wrong-answers/{wrongAnswerId}")
    public ApiResponse<Map<String, Object>> wrongAnswer(
            @PathVariable Long wrongAnswerId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        requireSessionUser(sessionUser);

        WrongAnswerDetailView view = wrongAnswerService.detail(sessionUser, wrongAnswerId);
        return ApiResponse.success(Map.of(
                "title", view.title(),
                "status", view.status(),
                "attributes", view.attributes()
        ));
    }

    @PostMapping("/api/reviews/wrong-answers/{wrongAnswerId}/retry")
    public ApiResponse<Map<String, Object>> retry(
            @PathVariable Long wrongAnswerId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Validated @ModelAttribute WrongAnswerRetryForm form,
            BindingResult bindingResult
    ) {
        requireSessionUser(sessionUser);

        if (bindingResult.hasErrors()) {
            return validationFail(bindingResult);
        }

        boolean correct = wrongAnswerService.retry(sessionUser, form, wrongAnswerId);

        return ApiResponse.success(Map.of(
                "wrongAnswerId", wrongAnswerId,
                "correct", correct,
                "message", correct ? "정답입니다." : "오답입니다."
        ));
    }

    @PatchMapping("/api/reviews/wrong-answers/{wrongAnswerId}/reviewed")
    public ApiResponse<Map<String, Object>> reviewed(
            @PathVariable Long wrongAnswerId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Validated @ModelAttribute ReviewWrongAnswerForm form,
            BindingResult bindingResult
    ) {
        requireSessionUser(sessionUser);

        if (bindingResult.hasErrors()) {
            return validationFail(bindingResult);
        }

        wrongAnswerService.markReviewed(sessionUser, wrongAnswerId);

        return ApiResponse.success(Map.of(
                "wrongAnswerId", wrongAnswerId,
                "message", "검토 완료 처리되었습니다."
        ));
    }

    private void requireSessionUser(SessionUser sessionUser) {
        if (sessionUser == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
    }

    private ApiResponse<Map<String, Object>> validationFail(BindingResult bindingResult) {
        List<ApiResponse.FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(error -> new ApiResponse.FieldError(
                        error.getField(),
                        error.getDefaultMessage() == null ? "입력값을 확인해주세요." : error.getDefaultMessage()
                ))
                .toList();

        return ApiResponse.fail(
                ErrorCode.COMMON_VALIDATION_FAILED.message(),
                ErrorCode.COMMON_VALIDATION_FAILED.code(),
                "입력값을 확인해주세요.",
                fieldErrors
        );
    }
}

package com.acorn.elearning.learning.controller;

import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.learning.dto.response.LessonBookmarkPageResponse;
import com.acorn.elearning.learning.dto.response.LessonBookmarkResponse;
import com.acorn.elearning.learning.service.LessonService;
import com.acorn.elearning.security.SessionUser;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
public class LearningApiController {

    // 로그인/세션 미연결 구간 dev fallback 사용자(샘플 learner)
    private static final SessionUser DEV_FALLBACK_USER =
            new SessionUser(2L, "learner@knowva.local", "누비학습자", SessionUser.ROLE_USER, false);

    private final LessonService lessonService;

    public LearningApiController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/api/subjects")
    public ApiResponse<Map<String, Object>> subjects() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SubjectListResponse response = learningService.subjects(sessionUser);
        // return ApiResponse.success(response);
        return ok("LEARN-001");
    }

    @GetMapping("/api/learning/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // LearningDashboardResponse response = learningService.dashboard(sessionUser);
        // return ApiResponse.success(response);
        return ok("LEARN-002");
    }

    @GetMapping("/api/subjects/{subjectId}/curriculum")
    public ApiResponse<Map<String, Object>> curriculum(@PathVariable Long subjectId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CurriculumResponse response = curriculumService.curriculum(sessionUser, subjectId);
        // return ApiResponse.success(response);
        return ok("LEARN-003");
    }

    @GetMapping("/api/lessons/{lessonId}")
    public ApiResponse<Map<String, Object>> lesson(@PathVariable Long lessonId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // LessonDetailResponse response = lessonService.lesson(sessionUser, lessonId);
        // return ApiResponse.success(response);
        return ok("LEARN-004");
    }

    @PostMapping("/api/lessons/{lessonId}/complete")
    public ApiResponse<Map<String, Object>> completeLesson(@PathVariable Long lessonId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CompleteLessonForm form = request body 또는 form binding 값으로 받으세요.
        // ProgressUpdateResponse response = lessonService.completeLesson(sessionUser, form, lessonId);
        // return ApiResponse.success(response);
        return ok("LEARN-005");
    }

    @PostMapping("/api/lessons/{lessonId}/bookmark")
    public ApiResponse<LessonBookmarkResponse> bookmark(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long lessonId) {
        SessionUser user = (sessionUser != null) ? sessionUser : DEV_FALLBACK_USER;
        return ApiResponse.success(lessonService.addBookmark(user, lessonId));
    }

    @DeleteMapping("/api/lessons/{lessonId}/bookmark")
    public ApiResponse<LessonBookmarkResponse> deleteBookmark(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long lessonId) {
        SessionUser user = (sessionUser != null) ? sessionUser : DEV_FALLBACK_USER;
        return ApiResponse.success(lessonService.removeBookmark(user, lessonId));
    }

    @GetMapping("/api/lessons/bookmarks")
    public ApiResponse<LessonBookmarkPageResponse> bookmarks(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        SessionUser user = (sessionUser != null) ? sessionUser : DEV_FALLBACK_USER;
        return ApiResponse.success(lessonService.getBookmarks(user, subjectId, page, size));
    }

    @GetMapping("/api/level-tests/questions")
    public ApiResponse<Map<String, Object>> levelQuestions() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // LevelTestQuestionListResponse response = levelTestService.levelQuestions(sessionUser);
        // return ApiResponse.success(response);
        return ok("LEVEL-001");
    }

    @PostMapping("/api/level-tests/attempts")
    public ApiResponse<Map<String, Object>> submitLevelTest() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // LevelTestForm form = request body 또는 form binding 값으로 받으세요.
        // LevelTestResultResponse response = levelTestService.submitLevelTest(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("LEVEL-002");
    }

    private ApiResponse<Map<String, Object>> ok(String endpointId) {
        // TODO: 개별 endpoint method에서 service 호출과 Response DTO 변환을 끝내면 이 helper를 제거하세요.
        // return ApiResponse.success(response); 형태가 최종 구현입니다.
        return ApiResponse.success(Map.of("endpointId", endpointId, "status", "SKELETON"));
    }
}

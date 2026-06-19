package com.acorn.elearning.admin.controller;

import com.acorn.elearning.common.response.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminApiController {

    @GetMapping("/api/admin/stats")
    public ApiResponse<Map<String, Object>> stats() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AdminStatsResponse response = adminStatsService.stats(sessionUser);
        // return ApiResponse.success(response);
        return ok("ADMIN-001");
    }

    @GetMapping("/api/admin/users")
    public ApiResponse<Map<String, Object>> users() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AdminUserPageResponse response = adminUserService.users(sessionUser);
        // return ApiResponse.success(response);
        return ok("ADMIN-010");
    }

    @PatchMapping("/api/admin/users/{userId}/status")
    public ApiResponse<Map<String, Object>> userStatus(@PathVariable Long userId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UserStatusForm form = request body 또는 form binding 값으로 받으세요.
        // AdminUserResponse response = adminUserService.userStatus(sessionUser, form, userId);
        // return ApiResponse.success(response);
        return ok("ADMIN-010");
    }

    @GetMapping("/api/admin/subjects")
    public ApiResponse<Map<String, Object>> subjects() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SubjectListResponse response = adminContentService.subjects(sessionUser);
        // return ApiResponse.success(response);
        return ok("ADMIN-020");
    }

    @PostMapping("/api/admin/subjects")
    public ApiResponse<Map<String, Object>> createSubject() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SubjectForm form = request body 또는 form binding 값으로 받으세요.
        // SubjectManageResponse response = adminContentService.createSubject(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("ADMIN-020");
    }

    @PatchMapping("/api/admin/subjects/{subjectId}")
    public ApiResponse<Map<String, Object>> updateSubject(@PathVariable Long subjectId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SubjectForm form = request body 또는 form binding 값으로 받으세요.
        // SubjectManageResponse response = adminContentService.updateSubject(sessionUser, form, subjectId);
        // return ApiResponse.success(response);
        return ok("ADMIN-020");
    }

    @PatchMapping("/api/admin/subjects/{subjectId}/status")
    public ApiResponse<Map<String, Object>> subjectStatus(@PathVariable Long subjectId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UpdateStatusForm form = request body 또는 form binding 값으로 받으세요.
        // SubjectManageResponse response = adminContentService.subjectStatus(sessionUser, form, subjectId);
        // return ApiResponse.success(response);
        return ok("ADMIN-020");
    }

    @GetMapping("/api/admin/curriculum-nodes")
    public ApiResponse<Map<String, Object>> nodes() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CurriculumNodeManageResponse response = adminContentService.nodes(sessionUser);
        // return ApiResponse.success(response);
        return ok("ADMIN-020");
    }

    @PostMapping("/api/admin/curriculum-nodes")
    public ApiResponse<Map<String, Object>> createNode() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CurriculumNodeForm form = request body 또는 form binding 값으로 받으세요.
        // CurriculumNodeManageResponse response = adminContentService.createNode(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("ADMIN-020");
    }

    @PatchMapping("/api/admin/curriculum-nodes/{nodeId}")
    public ApiResponse<Map<String, Object>> updateNode(@PathVariable Long nodeId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CurriculumNodeForm form = request body 또는 form binding 값으로 받으세요.
        // CurriculumNodeManageResponse response = adminContentService.updateNode(sessionUser, form, nodeId);
        // return ApiResponse.success(response);
        return ok("ADMIN-020");
    }

    @PatchMapping("/api/admin/curriculum-nodes/{nodeId}/status")
    public ApiResponse<Map<String, Object>> nodeStatus(@PathVariable Long nodeId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UpdateStatusForm form = request body 또는 form binding 값으로 받으세요.
        // CurriculumNodeManageResponse response = adminContentService.nodeStatus(sessionUser, form, nodeId);
        // return ApiResponse.success(response);
        return ok("ADMIN-020");
    }

    @GetMapping("/api/admin/lessons")
    public ApiResponse<Map<String, Object>> lessons() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // LessonManageResponse response = adminContentService.lessons(sessionUser);
        // return ApiResponse.success(response);
        return ok("ADMIN-030");
    }

    @PostMapping("/api/admin/lessons")
    public ApiResponse<Map<String, Object>> createLesson() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // LessonForm form = request body 또는 form binding 값으로 받으세요.
        // LessonManageResponse response = adminContentService.createLesson(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("ADMIN-030");
    }

    @PatchMapping("/api/admin/lessons/{lessonId}")
    public ApiResponse<Map<String, Object>> updateLesson(@PathVariable Long lessonId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // LessonForm form = request body 또는 form binding 값으로 받으세요.
        // LessonManageResponse response = adminContentService.updateLesson(sessionUser, form, lessonId);
        // return ApiResponse.success(response);
        return ok("ADMIN-030");
    }

    @PatchMapping("/api/admin/lessons/{lessonId}/status")
    public ApiResponse<Map<String, Object>> lessonStatus(@PathVariable Long lessonId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UpdateStatusForm form = request body 또는 form binding 값으로 받으세요.
        // LessonManageResponse response = adminContentService.lessonStatus(sessionUser, form, lessonId);
        // return ApiResponse.success(response);
        return ok("ADMIN-030");
    }

    @GetMapping("/api/admin/problems")
    public ApiResponse<Map<String, Object>> problems() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ProblemManageResponse response = adminContentService.problems(sessionUser);
        // return ApiResponse.success(response);
        return ok("ADMIN-040");
    }

    @PostMapping("/api/admin/problems")
    public ApiResponse<Map<String, Object>> createProblem() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ProblemForm form = request body 또는 form binding 값으로 받으세요.
        // ProblemManageResponse response = adminContentService.createProblem(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("ADMIN-040");
    }

    @PatchMapping("/api/admin/problems/{problemId}")
    public ApiResponse<Map<String, Object>> updateProblem(@PathVariable Long problemId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ProblemForm form = request body 또는 form binding 값으로 받으세요.
        // ProblemManageResponse response = adminContentService.updateProblem(sessionUser, form, problemId);
        // return ApiResponse.success(response);
        return ok("ADMIN-040");
    }

    @PatchMapping("/api/admin/problems/{problemId}/status")
    public ApiResponse<Map<String, Object>> problemStatus(@PathVariable Long problemId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UpdateStatusForm form = request body 또는 form binding 값으로 받으세요.
        // ProblemManageResponse response = adminContentService.problemStatus(sessionUser, form, problemId);
        // return ApiResponse.success(response);
        return ok("ADMIN-040");
    }

    @GetMapping("/api/admin/community/posts")
    public ApiResponse<Map<String, Object>> posts() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PostPageResponse response = adminCommunityService.posts(sessionUser);
        // return ApiResponse.success(response);
        return ok("ADMIN-050");
    }

    @PatchMapping("/api/admin/community/posts/{postId}/status")
    public ApiResponse<Map<String, Object>> postStatus(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CommunityStatusForm form = request body 또는 form binding 값으로 받으세요.
        // AdminCommunityActionResponse response = adminCommunityService.postStatus(sessionUser, form, postId);
        // return ApiResponse.success(response);
        return ok("ADMIN-050");
    }

    @PatchMapping("/api/admin/community/comments/{commentId}/status")
    public ApiResponse<Map<String, Object>> commentStatus(@PathVariable Long commentId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CommunityStatusForm form = request body 또는 form binding 값으로 받으세요.
        // AdminCommunityActionResponse response = adminCommunityService.commentStatus(sessionUser, form, commentId);
        // return ApiResponse.success(response);
        return ok("ADMIN-050");
    }

    @GetMapping("/api/admin/reports")
    public ApiResponse<Map<String, Object>> reports() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ReportPageResponse response = adminCommunityService.reports(sessionUser);
        // return ApiResponse.success(response);
        return ok("ADMIN-060");
    }

    @PatchMapping("/api/admin/reports/{reportId}")
    public ApiResponse<Map<String, Object>> report(@PathVariable Long reportId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ReportHandleForm form = request body 또는 form binding 값으로 받으세요.
        // ReportResponse response = adminCommunityService.report(sessionUser, form, reportId);
        // return ApiResponse.success(response);
        return ok("ADMIN-060");
    }

    @GetMapping("/api/admin/notices")
    public ApiResponse<Map<String, Object>> notices() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // NoticePageResponse response = adminNoticeService.notices(sessionUser);
        // return ApiResponse.success(response);
        return ok("ADMIN-070");
    }

    @PostMapping("/api/admin/notices")
    public ApiResponse<Map<String, Object>> createNotice() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // NoticeForm form = request body 또는 form binding 값으로 받으세요.
        // NoticeResponse response = adminNoticeService.createNotice(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("ADMIN-070");
    }

    @PatchMapping("/api/admin/notices/{noticeId}")
    public ApiResponse<Map<String, Object>> updateNotice(@PathVariable Long noticeId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // NoticeForm form = request body 또는 form binding 값으로 받으세요.
        // NoticeResponse response = adminNoticeService.updateNotice(sessionUser, form, noticeId);
        // return ApiResponse.success(response);
        return ok("ADMIN-070");
    }

    @GetMapping("/api/admin/operation-logs")
    public ApiResponse<Map<String, Object>> logs() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AdminOperationLogPageResponse response = adminLogService.logs(sessionUser);
        // return ApiResponse.success(response);
        return ok("ADMIN-080");
    }

    private ApiResponse<Map<String, Object>> ok(String endpointId) {
        // TODO: 개별 endpoint method에서 service 호출과 Response DTO 변환을 끝내면 이 helper를 제거하세요.
        // return ApiResponse.success(response); 형태가 최종 구현입니다.
        return ApiResponse.success(Map.of("endpointId", endpointId, "status", "SKELETON"));
    }
}

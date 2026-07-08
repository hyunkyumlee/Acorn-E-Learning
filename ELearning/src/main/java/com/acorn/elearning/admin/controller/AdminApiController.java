package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.dto.request.UpdateUserStatusRequest;
import com.acorn.elearning.admin.dto.response.*;
import com.acorn.elearning.admin.form.CurriculumNodeForm;
import com.acorn.elearning.admin.form.SubjectForm;
import com.acorn.elearning.admin.service.*;
import com.acorn.elearning.common.response.ApiResponse;

import java.util.List;
import java.util.Map;

import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AdminApiController {

    private final AdminStatsService adminStatsService;
    private final AdminUserService adminUserService;
    private final AdminReportService adminReportService;
    private final AdminNoticeService adminNoticeService;
    private final AdminContentService adminContentService;
    private final AdminCommunityService adminCommunityService;
    private final AdminLogService adminLogService;

    @GetMapping("/api/admin/stats")
    public ApiResponse<AdminStatsResponse> stats(
            @RequestParam(defaultValue = "all") String summaryScope,
            @RequestParam(required = false) String periodUnit,
            @RequestParam(required = false) String subject) {
        return ApiResponse.success(
                adminStatsService.getStats(summaryScope, periodUnit, subject)
        );
    }

    @GetMapping("/api/admin/users")
    public ApiResponse<List<AdminUserManageRowResponse>> users() {

        return ApiResponse.success(adminUserService.findAll());
    }

    @PatchMapping("/api/admin/users/{userId}/status")
    public ApiResponse<Map<String, Object>> userStatus(
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequest request,
            @SessionAttribute(name= SessionUser.SESSION_KEY, required = false) SessionUser sessionUser)
    {

        if(sessionUser == null){
            return ApiResponse.success(Map.of(
                    "updated", false,
                    "message", "로그인이 필요합니다."
            ));
        }
        int updated = adminUserService.updateStatus(userId, request.status(), sessionUser.userId());

        return ApiResponse.success(Map.of(
                "userId", userId,
                "status", request.status(),
                "updated", updated == 1
        ));
    }

    @GetMapping("/api/admin/subjects")
    public ApiResponse<List<SubjectManageResponse>> subjects() {

        return ApiResponse.success(adminContentService.findSubjectResponse());
    }

    @PostMapping("/api/admin/subjects")
    public ApiResponse<Map<String, Object>> createSubject(
            @RequestBody SubjectForm form,
            @SessionAttribute (name=SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
            )
    {
        if(sessionUser == null){
            return ApiResponse.success(Map.of(
                    "updated", false,
                    "message", "로그인이 필요합니다."
            ));
        }

        int created = adminContentService.createSubject(form, sessionUser.userId());

        return ApiResponse.success(Map.of(
                "created", created == 1,
                "subjectName", form.getSubjectName()
        ));

    }

    @PatchMapping("/api/admin/subjects/{subjectId}")
    public ApiResponse<Map<String, Object>> updateSubject(
            @PathVariable Long subjectId,
            @RequestBody SubjectForm form,
            @SessionAttribute (name=SessionUser.SESSION_KEY, required = false) SessionUser sessionUser)
    {
        if(sessionUser == null){
            return ApiResponse.success(Map.of(
                    "updated", false,
                    "message", "로그인이 필요합니다."
            ));
        }

        form.setSubjectId(subjectId);

        int updated = adminContentService.updateSubject(form, sessionUser.userId());

        return ApiResponse.success(Map.of(
                "subjectId", subjectId,
                "updated", updated == 1,
                "subjectName", form.getSubjectName()
        ));

    }

    @PatchMapping("/api/admin/subjects/{subjectId}/status")
    public ApiResponse<Map<String, Object>> subjectStatus(
            @PathVariable Long subjectId,
            @RequestBody SubjectForm form,
            @SessionAttribute (name=SessionUser.SESSION_KEY, required = false) SessionUser sessionUser)
    {
        if(sessionUser == null){
            return ApiResponse.success(Map.of(
                    "updated", false,
                    "message", "로그인이 필요합니다."
            ));
        }

        form.setSubjectId(subjectId);

        int updated = adminContentService.updateSubjectStatus(
                subjectId, form.getIsActive(), sessionUser.userId()
        );

        return ApiResponse.success(Map.of(
                "subjectId", subjectId,
                "isActive", form.getIsActive(),
                "updated", updated == 1
        ));

    }

    @GetMapping("/api/admin/curriculum-nodes")
    public ApiResponse<List<CurriculumNodeManageResponse>> nodes() {
        return ApiResponse.success(adminContentService.findCurriculumNodeResponse());
    }

    @PostMapping("/api/admin/curriculum-nodes")
    public ApiResponse<Map<String, Object>> createNode(
            @RequestBody CurriculumNodeForm form,
            @SessionAttribute (name=SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
            )
    {
        if(sessionUser == null){
            return ApiResponse.success(Map.of(
                    "created", false,
                    "message", "로그인이 필요합니다."
            ));
        }

        int created = adminContentService.createCurriculumNode(form, sessionUser.userId());

        return ApiResponse.success(Map.of(
                "created", created == 1,
                "curriculumNodeName", form.getTitle()
        ));

    }

    @PatchMapping("/api/admin/curriculum-nodes/{nodeId}")
    public ApiResponse<Map<String, Object>> updateNode(
            @PathVariable Long nodeId,
            @RequestBody CurriculumNodeForm form,
            @SessionAttribute (name=SessionUser.SESSION_KEY, required = false) SessionUser sessionUser)

    {
        if(sessionUser == null){
            return ApiResponse.success(Map.of(
                    "updated", false,
                    "message", "로그인이 필요합니다."
            ));
        }

        form.setNodeId(nodeId);

        int updated = adminContentService.updateCurriculumNode(form, sessionUser.userId());

        return ApiResponse.success(Map.of(
                "nodeId", nodeId,
                "updated", updated == 1,
                "title", form.getTitle()
        ));
    }

    @PatchMapping("/api/admin/curriculum-nodes/{nodeId}/status")
    public ApiResponse<Map<String, Object>> nodeStatus(
            @PathVariable Long nodeId,
            @RequestBody CurriculumNodeForm form,
            @SessionAttribute(name= SessionUser.SESSION_KEY, required = false) SessionUser sessionUser)

    {
        if(sessionUser == null){
            return ApiResponse.success(Map.of(
                    "updated", false,
                    "message", "로그인이 필요합니다."
            ));
        }


        int updated = adminContentService.updateCurriculumNodeStatus(
                nodeId, form.getIsActive(), sessionUser.userId()
        );

        return ApiResponse.success(Map.of(
                "nodeId", nodeId,
                "isActive", form.getIsActive(),
                "updated", updated == 1
        ));
    }

    @GetMapping("/api/admin/lessons")
    public ApiResponse<List<AdminLessonManageRowResponse>> lessons() {
        return ApiResponse.success(adminContentService.findAllAdminLesson());
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

package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.dto.request.UpdateUserStatusRequest;
import com.acorn.elearning.admin.dto.response.*;
import com.acorn.elearning.admin.form.CommunityStatusForm;
import com.acorn.elearning.admin.form.CurriculumNodeForm;
import com.acorn.elearning.admin.form.LessonForm;
import com.acorn.elearning.admin.form.ProblemForm;
import com.acorn.elearning.admin.form.ReportHandleForm;
import com.acorn.elearning.admin.form.SubjectForm;
import com.acorn.elearning.admin.model.AdminOperationLog;
import com.acorn.elearning.admin.model.Notice;
import com.acorn.elearning.admin.service.AdminCommunityService;
import com.acorn.elearning.admin.service.AdminContentService;
import com.acorn.elearning.admin.service.AdminLogService;
import com.acorn.elearning.admin.service.AdminNoticeService;
import com.acorn.elearning.admin.service.AdminReportService;
import com.acorn.elearning.admin.service.AdminStatsService;
import com.acorn.elearning.admin.service.AdminUserService;
import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;
import java.util.Map;

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
        return ApiResponse.success(adminStatsService.getStats(summaryScope, periodUnit, subject));
    }

    @GetMapping("/api/admin/users")
    public ApiResponse<List<AdminUserManageRowResponse>> users() {
        return ApiResponse.success(adminUserService.findAll());
    }

    @GetMapping("/api/admin/users/{userId}/detail")
    public ApiResponse<AdminUserDetailResponse> userDetail(@PathVariable Long userId)
    {
        return ApiResponse.success(adminUserService.findDetailById(userId));
    }

    @PatchMapping("/api/admin/users/{userId}/status")
    public ApiResponse<Map<String, Object>> userStatus(
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequest request,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        int updated = adminUserService.updateStatus(userId, request.status(), sessionUser.userId());

        return ApiResponse.success(Map.of(
                "userId", userId,
                "status", request.status() == null ? "" : request.status(),
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
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("created");
        }

        int created = adminContentService.createSubject(form, sessionUser.userId());

        return ApiResponse.success(Map.of(
                "created", created == 1,
                "subjectName", form.getSubjectName() == null ? "" : form.getSubjectName()
        ));
    }

    @PatchMapping("/api/admin/subjects/{subjectId}")
    public ApiResponse<Map<String, Object>> updateSubject(
            @PathVariable Long subjectId,
            @RequestBody SubjectForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        form.setSubjectId(subjectId);
        int updated = adminContentService.updateSubject(form, sessionUser.userId());

        return ApiResponse.success(Map.of(
                "subjectId", subjectId,
                "updated", updated == 1,
                "subjectName", form.getSubjectName() == null ? "" : form.getSubjectName()
        ));
    }

    @PatchMapping("/api/admin/subjects/{subjectId}/status")
    public ApiResponse<Map<String, Object>> subjectStatus(
            @PathVariable Long subjectId,
            @RequestBody SubjectForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        int updated = adminContentService.updateSubjectStatus(subjectId, form.getIsActive(), sessionUser.userId());

        return ApiResponse.success(Map.of(
                "subjectId", subjectId,
                "isActive", form.getIsActive() == null ? "" : form.getIsActive(),
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
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("created");
        }

        int created = adminContentService.createCurriculumNode(form, sessionUser.userId());

        return ApiResponse.success(Map.of(
                "created", created == 1,
                "curriculumNodeName", form.getTitle() == null ? "" : form.getTitle()
        ));
    }

    @PatchMapping("/api/admin/curriculum-nodes/{nodeId}")
    public ApiResponse<Map<String, Object>> updateNode(
            @PathVariable Long nodeId,
            @RequestBody CurriculumNodeForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        form.setNodeId(nodeId);
        int updated = adminContentService.updateCurriculumNode(form, sessionUser.userId());

        return ApiResponse.success(Map.of(
                "nodeId", nodeId,
                "updated", updated == 1,
                "title", form.getTitle() == null ? "" : form.getTitle()
        ));
    }

    @PatchMapping("/api/admin/curriculum-nodes/{nodeId}/status")
    public ApiResponse<Map<String, Object>> nodeStatus(
            @PathVariable Long nodeId,
            @RequestBody CurriculumNodeForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        int updated = adminContentService.updateCurriculumNodeStatus(
                nodeId, form.getIsActive(), sessionUser.userId()
        );

        return ApiResponse.success(Map.of(
                "nodeId", nodeId,
                "isActive", form.getIsActive() == null ? "" : form.getIsActive(),
                "updated", updated == 1
        ));
    }

    @GetMapping("/api/admin/lessons")
    public ApiResponse<List<AdminLessonManageRowResponse>> lessons() {
        return ApiResponse.success(adminContentService.findAllAdminLesson());
    }

    @PostMapping("/api/admin/lessons")
    public ApiResponse<Map<String, Object>> createLesson(
            @RequestBody LessonForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("created");
        }

        int created = adminContentService.createLesson(form, sessionUser.userId());

        return ApiResponse.success(Map.of(
                "created", created == 1,
                "lessonTitle", form.getTitle() == null ? "" : form.getTitle()
        ));
    }

    @PatchMapping("/api/admin/lessons/{lessonId}")
    public ApiResponse<Map<String, Object>> updateLesson(
            @PathVariable Long lessonId,
            @RequestBody LessonForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        form.setLessonId(lessonId);
        int updated = adminContentService.updateLesson(form, sessionUser.userId());

        return ApiResponse.success(Map.of(
                "lessonId", lessonId,
                "updated", updated == 1,
                "lessonTitle", form.getTitle() == null ? "" : form.getTitle()
        ));
    }

    @PatchMapping("/api/admin/lessons/{lessonId}/status")
    public ApiResponse<Map<String, Object>> lessonStatus(
            @PathVariable Long lessonId,
            @RequestBody LessonForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        int updated = adminContentService.updateLessonStatus(
                lessonId, form.getIsActive(), sessionUser.userId()
        );

        return ApiResponse.success(Map.of(
                "lessonId", lessonId,
                "isActive", form.getIsActive() == null ? "" : form.getIsActive(),
                "updated", updated == 1
        ));
    }

    @GetMapping("/api/admin/problems")
    public ApiResponse<List<AdminProblemManageRowResponse>> problems() {
        return ApiResponse.success(adminContentService.findAllAdminProblem());
    }

    @PostMapping("/api/admin/problems")
    public ApiResponse<Map<String, Object>> createProblem(
            @RequestBody ProblemForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("created");
        }

        if (hasRequiredProblemValueMissing(form)) {
            return requiredMissing("created");
        }

        int created = adminContentService.createProblem(form, sessionUser.userId());

        return ApiResponse.success(Map.of(
                "created", created == 1,
                "question", form.getQuestion() == null ? "" : form.getQuestion()
        ));
    }

    @PatchMapping("/api/admin/problems/{problemId}")
    public ApiResponse<Map<String, Object>> updateProblem(
            @PathVariable Long problemId,
            @RequestBody ProblemForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        if (hasRequiredProblemValueMissing(form)) {
            return requiredMissing("updated");
        }

        form.setProblemId(problemId);
        int updated = adminContentService.updateProblem(form, sessionUser.userId());

        return ApiResponse.success(Map.of(
                "problemId", problemId,
                "updated", updated == 1,
                "question", form.getQuestion() == null ? "" : form.getQuestion()
        ));
    }

    @PatchMapping("/api/admin/problems/{problemId}/status")
    public ApiResponse<Map<String, Object>> problemStatus(
            @PathVariable Long problemId,
            @RequestBody ProblemForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        int updated = adminContentService.updateProblemStatus(
                problemId, form.getIsActive(), sessionUser.userId()
        );

        return ApiResponse.success(Map.of(
                "problemId", problemId,
                "isActive", form.getIsActive() == null ? "" : form.getIsActive(),
                "updated", updated == 1
        ));
    }

    @GetMapping("/api/admin/community/posts")
    public ApiResponse<AdminCommunityPageResponse> posts(
            @RequestParam(defaultValue = "1") int postPage,
            @RequestParam(defaultValue = "1") int commentPage,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String boardType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(
                adminCommunityService.findPage(postPage, commentPage, size, boardType, status, keyword)
        );
    }

    @PatchMapping("/api/admin/community/posts/{postId}/status")
    public ApiResponse<Map<String, Object>> postStatus(
            @PathVariable Long postId,
            @RequestBody CommunityStatusForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        int updated = adminCommunityService.updatePostStatus(postId, form, sessionUser);

        return ApiResponse.success(Map.of(
                "postId", postId,
                "status", form.getStatus() == null ? "" : form.getStatus().trim().toUpperCase(),
                "updated", updated == 1
        ));
    }

    @PatchMapping("/api/admin/community/comments/{commentId}/status")
    public ApiResponse<Map<String, Object>> commentStatus(
            @PathVariable Long commentId,
            @RequestBody CommunityStatusForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        int updated = adminCommunityService.updateCommentStatus(commentId, form, sessionUser);

        return ApiResponse.success(Map.of(
                "commentId", commentId,
                "status", form.getStatus() == null ? "" : form.getStatus().trim().toUpperCase(),
                "updated", updated == 1
        ));
    }

    @GetMapping("/api/admin/reports")
    public ApiResponse<AdminPageResponse<ReportPageResponse.ReportItem>> reports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String reportDate) {
        return ApiResponse.success(adminReportService.findPage(page, size, targetType, status, reportDate));
    }

    @PatchMapping("/api/admin/reports/{reportId}")
    public ApiResponse<Map<String, Object>> report(
            @PathVariable Long reportId,
            @RequestBody ReportHandleForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        int updated = adminReportService.handle(reportId, form, sessionUser);

        return ApiResponse.success(Map.of(
                "reportId", reportId,
                "status", form.getStatus() == null ? "" : form.getStatus(),
                "updated", updated == 1
        ));
    }

    @GetMapping("/api/admin/notices")
    public ApiResponse<AdminPageResponse<Notice>> notices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminNoticeService.findPage(page, size, keyword, period, status));
    }

    @PostMapping("/api/admin/notices")
    public ApiResponse<Map<String, Object>> createNotice(
            @RequestBody Notice notice,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("created");
        }

        int created = adminNoticeService.insert(notice, sessionUser);

        return ApiResponse.success(Map.of(
                "created", created == 1,
                "noticeTitle", notice.getTitle() == null ? "" : notice.getTitle()
        ));
    }

    @PatchMapping("/api/admin/notices/{noticeId}")
    public ApiResponse<Map<String, Object>> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody Notice notice,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {

        if (sessionUser == null) {
            return loginRequired("updated");
        }

        notice.setNoticeId(noticeId);
        int updated = adminNoticeService.update(notice, sessionUser);

        return ApiResponse.success(Map.of(
                "noticeId", noticeId,
                "updated", updated == 1,
                "noticeTitle", notice.getTitle() == null ? "" : notice.getTitle()
        ));
    }

    @GetMapping("/api/admin/operation-logs")
    public ApiResponse<List<AdminOperationLog>> logs() {
        return ApiResponse.success(adminLogService.findAll());
    }

    private ApiResponse<Map<String, Object>> loginRequired(String resultKey) {
        return ApiResponse.success(Map.of(
                resultKey, false,
                "message", "로그인이 필요합니다."
        ));
    }

    private ApiResponse<Map<String, Object>> requiredMissing(String resultKey) {
        return ApiResponse.success(Map.of(
                resultKey, false,
                "message", "필수 입력값이 누락되었습니다."
        ));
    }

    private boolean hasRequiredProblemValueMissing(ProblemForm form) {
        return form.getLessonId() == null
                || form.getProblemType() == null
                || form.getQuestion() == null
                || form.getDifficultyCode() == null;
    }
}

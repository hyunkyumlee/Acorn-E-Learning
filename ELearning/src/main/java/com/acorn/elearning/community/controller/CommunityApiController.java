package com.acorn.elearning.community.controller;

import com.acorn.elearning.common.response.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommunityApiController {

    @GetMapping("/api/community/posts")
    public ApiResponse<Map<String, Object>> posts() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PostPageResponse response = postService.posts(sessionUser);
        // return ApiResponse.success(response);
        return ok("COM-001");
    }

    @GetMapping("/api/community/posts/{postId}")
    public ApiResponse<Map<String, Object>> post(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PostDetailResponse response = postService.post(sessionUser, postId);
        // return ApiResponse.success(response);
        return ok("COM-001");
    }

    @PostMapping("/api/community/posts")
    public ApiResponse<Map<String, Object>> createPost() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PostForm form = request body 또는 form binding 값으로 받으세요.
        // PostDetailResponse response = postService.createPost(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("COM-002");
    }

    @PatchMapping("/api/community/posts/{postId}")
    public ApiResponse<Map<String, Object>> updatePost(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PostForm form = request body 또는 form binding 값으로 받으세요.
        // PostDetailResponse response = postService.updatePost(sessionUser, form, postId);
        // return ApiResponse.success(response);
        return ok("COM-002");
    }

    @DeleteMapping("/api/community/posts/{postId}")
    public ApiResponse<Map<String, Object>> deletePost(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // DeletePostForm form = request body 또는 form binding 값으로 받으세요.
        // PostDetailResponse response = postService.deletePost(sessionUser, form, postId);
        // return ApiResponse.success(response);
        return ok("COM-002");
    }

    @PostMapping("/api/community/posts/{postId}/attachments")
    public ApiResponse<Map<String, Object>> attachments(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AttachmentsForm form = request body 또는 form binding 값으로 받으세요.
        // AttachmentListResponse response = attachmentService.attachments(sessionUser, form, postId);
        // return ApiResponse.success(response);
        return ok("COM-003");
    }

    @DeleteMapping("/api/community/attachments/{attachmentId}")
    public ApiResponse<Map<String, Object>> deleteAttachment(@PathVariable Long attachmentId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // DeleteAttachmentForm form = request body 또는 form binding 값으로 받으세요.
        // AttachmentListResponse response = attachmentService.deleteAttachment(sessionUser, form, attachmentId);
        // return ApiResponse.success(response);
        return ok("COM-003");
    }

    @GetMapping("/api/community/posts/{postId}/comments")
    public ApiResponse<Map<String, Object>> comments(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CommentListResponse response = commentService.comments(sessionUser, postId);
        // return ApiResponse.success(response);
        return ok("COM-004");
    }

    @PostMapping("/api/community/posts/{postId}/comments")
    public ApiResponse<Map<String, Object>> createComment(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CommentForm form = request body 또는 form binding 값으로 받으세요.
        // CommentResponse response = commentService.createComment(sessionUser, form, postId);
        // return ApiResponse.success(response);
        return ok("COM-004");
    }

    @PatchMapping("/api/community/comments/{commentId}")
    public ApiResponse<Map<String, Object>> updateComment(@PathVariable Long commentId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CommentForm form = request body 또는 form binding 값으로 받으세요.
        // CommentResponse response = commentService.updateComment(sessionUser, form, commentId);
        // return ApiResponse.success(response);
        return ok("COM-004");
    }

    @DeleteMapping("/api/community/comments/{commentId}")
    public ApiResponse<Map<String, Object>> deleteComment(@PathVariable Long commentId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // DeleteCommentForm form = request body 또는 form binding 값으로 받으세요.
        // CommentResponse response = commentService.deleteComment(sessionUser, form, commentId);
        // return ApiResponse.success(response);
        return ok("COM-004");
    }

    @PostMapping("/api/community/posts/{postId}/likes")
    public ApiResponse<Map<String, Object>> like(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // LikeForm form = request body 또는 form binding 값으로 받으세요.
        // ReactionResponse response = reactionService.like(sessionUser, form, postId);
        // return ApiResponse.success(response);
        return ok("COM-005");
    }

    @DeleteMapping("/api/community/posts/{postId}/likes")
    public ApiResponse<Map<String, Object>> unlike(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UnlikeForm form = request body 또는 form binding 값으로 받으세요.
        // ReactionResponse response = reactionService.unlike(sessionUser, form, postId);
        // return ApiResponse.success(response);
        return ok("COM-005");
    }

    @PostMapping("/api/community/posts/{postId}/scraps")
    public ApiResponse<Map<String, Object>> scrap(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ScrapForm form = request body 또는 form binding 값으로 받으세요.
        // ReactionResponse response = reactionService.scrap(sessionUser, form, postId);
        // return ApiResponse.success(response);
        return ok("COM-005");
    }

    @DeleteMapping("/api/community/posts/{postId}/scraps")
    public ApiResponse<Map<String, Object>> unscrap(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UnscrapForm form = request body 또는 form binding 값으로 받으세요.
        // ReactionResponse response = reactionService.unscrap(sessionUser, form, postId);
        // return ApiResponse.success(response);
        return ok("COM-005");
    }

    @PostMapping("/api/community/reports")
    public ApiResponse<Map<String, Object>> report() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ReportHandleForm form = request body 또는 form binding 값으로 받으세요.
        // ReportResponse response = reportService.report(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("COM-006");
    }

    @GetMapping("/api/community/profile/me")
    public ApiResponse<Map<String, Object>> profile() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // CommunityProfileResponse response = userActivityService.profile(sessionUser);
        // return ApiResponse.success(response);
        return ok("COM-007");
    }

    private ApiResponse<Map<String, Object>> ok(String endpointId) {
        // TODO: 개별 endpoint method에서 service 호출과 Response DTO 변환을 끝내면 이 helper를 제거하세요.
        // return ApiResponse.success(response); 형태가 최종 구현입니다.
        return ApiResponse.success(Map.of("endpointId", endpointId, "status", "SKELETON"));
    }
}

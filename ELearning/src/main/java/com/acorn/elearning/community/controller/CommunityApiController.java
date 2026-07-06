package com.acorn.elearning.community.controller;

import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.community.dto.request.CreateCommentRequest;
import com.acorn.elearning.community.dto.request.CreatePostRequest;
import com.acorn.elearning.community.dto.request.CreateReportRequest;
import com.acorn.elearning.community.dto.request.UpdateCommentRequest;
import com.acorn.elearning.community.dto.request.UpdatePostRequest;
import com.acorn.elearning.community.dto.response.AttachmentListResponse;
import com.acorn.elearning.community.dto.response.CommentListResponse;
import com.acorn.elearning.community.dto.response.CommentResponse;
import com.acorn.elearning.community.dto.response.CommunityProfileResponse;
import com.acorn.elearning.community.dto.response.PostDetailResponse;
import com.acorn.elearning.community.dto.response.PostPageResponse;
import com.acorn.elearning.community.dto.response.ReactionResponse;
import com.acorn.elearning.community.dto.response.ReportResponse;
import com.acorn.elearning.community.form.PostSearchCondition;
import com.acorn.elearning.community.service.AttachmentService;
import com.acorn.elearning.community.service.CommentService;
import com.acorn.elearning.community.service.PostService;
import com.acorn.elearning.community.service.ReactionService;
import com.acorn.elearning.community.service.ReportService;
import com.acorn.elearning.security.SessionUser;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class CommunityApiController {
    private final PostService postService;
    private final AttachmentService attachmentService;
    private final CommentService commentService;
    private final ReactionService reactionService;
    private final ReportService reportService;

    public CommunityApiController(
            PostService postService,
            AttachmentService attachmentService,
            CommentService commentService,
            ReactionService reactionService,
            ReportService reportService
    ) {
        this.postService = postService;
        this.attachmentService = attachmentService;
        this.commentService = commentService;
        this.reactionService = reactionService;
        this.reportService = reportService;
    }

    @GetMapping("/api/community/posts")
    public ApiResponse<PostPageResponse> posts(PostSearchCondition condition) {
        return ApiResponse.success(postService.page(condition));
    }

    @GetMapping("/api/community/posts/{postId}")
    public ApiResponse<PostDetailResponse> post(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId
    ) {
        return ApiResponse.success(postService.detail(sessionUser, postId));
    }

    @PostMapping("/api/community/posts")
    public ApiResponse<PostDetailResponse> createPost(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @RequestBody CreatePostRequest request
    ) {
        return ApiResponse.success(postService.detail(sessionUser, postService.create(sessionUser, request.toForm()).getPostId()));
    }

    @PatchMapping("/api/community/posts/{postId}")
    public ApiResponse<PostDetailResponse> updatePost(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        return ApiResponse.success(postService.detail(sessionUser, postService.update(sessionUser, postId, request.toForm()).getPostId()));
    }

    @DeleteMapping("/api/community/posts/{postId}")
    public ApiResponse<Map<String, Object>> deletePost(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId
    ) {
        postService.delete(sessionUser, postId);
        return ApiResponse.success(Map.of("postId", postId, "deleted", true));
    }

    @PostMapping("/api/community/posts/{postId}/attachments")
    public ApiResponse<AttachmentListResponse> attachments(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            @RequestParam(name = "files", required = false) java.util.List<MultipartFile> files
    ) {
        return ApiResponse.success(new AttachmentListResponse(attachmentService.addMetadata(sessionUser, postId, files)));
    }

    @DeleteMapping("/api/community/attachments/{attachmentId}")
    public ApiResponse<Map<String, Object>> deleteAttachment(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long attachmentId
    ) {
        attachmentService.delete(sessionUser, attachmentId);
        return ApiResponse.success(Map.of("attachmentId", attachmentId, "deleted", true));
    }

    @GetMapping("/api/community/posts/{postId}/comments")
    public ApiResponse<CommentListResponse> comments(@PathVariable Long postId) {
        return ApiResponse.success(commentService.comments(postId));
    }

    @PostMapping("/api/community/posts/{postId}/comments")
    public ApiResponse<CommentResponse> createComment(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return ApiResponse.success(commentService.create(sessionUser, postId, request.toForm()));
    }

    @PatchMapping("/api/community/comments/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        return ApiResponse.success(commentService.update(sessionUser, commentId, request.toForm()));
    }

    @DeleteMapping("/api/community/comments/{commentId}")
    public ApiResponse<Map<String, Object>> deleteComment(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long commentId
    ) {
        commentService.delete(sessionUser, commentId);
        return ApiResponse.success(Map.of("commentId", commentId, "deleted", true));
    }

    @PostMapping("/api/community/posts/{postId}/likes")
    public ApiResponse<ReactionResponse> like(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId
    ) {
        return ApiResponse.success(reactionService.like(sessionUser, postId));
    }

    @DeleteMapping("/api/community/posts/{postId}/likes")
    public ApiResponse<ReactionResponse> unlike(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId
    ) {
        return ApiResponse.success(reactionService.unlike(sessionUser, postId));
    }

    @PostMapping("/api/community/posts/{postId}/scraps")
    public ApiResponse<ReactionResponse> scrap(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId
    ) {
        return ApiResponse.success(reactionService.scrap(sessionUser, postId));
    }

    @DeleteMapping("/api/community/posts/{postId}/scraps")
    public ApiResponse<ReactionResponse> unscrap(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId
    ) {
        return ApiResponse.success(reactionService.unscrap(sessionUser, postId));
    }

    @PostMapping("/api/community/reports")
    public ApiResponse<ReportResponse> report(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @RequestBody CreateReportRequest request
    ) {
        return ApiResponse.success(reportService.create(sessionUser, request.toForm()));
    }

    @GetMapping("/api/community/profile/me")
    public ApiResponse<CommunityProfileResponse> profile(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        return ApiResponse.success(postService.profile(sessionUser));
    }

    private ApiResponse<Map<String, Object>> ok(String endpointId) {
        return ApiResponse.success(Map.of("endpointId", endpointId, "status", "IMPLEMENTED"));
    }
}

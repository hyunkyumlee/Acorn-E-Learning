package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.form.CommunityStatusForm;
import com.acorn.elearning.admin.service.AdminCommunityService;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminCommunityController {

    private final AdminCommunityService service;

    @GetMapping("/admin/community")
    public String community(Model model,
                            @RequestParam(defaultValue = "1") int postPage,
                            @RequestParam(defaultValue = "1") int commentPage,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String tab,
                            @RequestParam(required = false) String boardType,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String keyword) {

        String selectedTab = tab == null || tab.isBlank() ? "posts" : tab;
        boolean commentTab = "comments".equals(selectedTab);
        String postBoardType = commentTab ? null : boardType;
        String postStatus = commentTab ? null : status;
        String postKeyword = commentTab ? null : keyword;
        String commentStatus = commentTab ? status : null;
        String commentKeyword = commentTab ? keyword : null;

        model.addAttribute("communityPage", service.findPage(
                postPage,
                commentPage,
                size,
                postBoardType,
                postStatus,
                postKeyword,
                commentStatus,
                commentKeyword
        ));
        model.addAttribute("selectedTab", selectedTab);
        model.addAttribute("selectedBoardType", boardType);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedPostBoardType", postBoardType);
        model.addAttribute("selectedPostStatus", postStatus);
        model.addAttribute("selectedPostKeyword", postKeyword);
        model.addAttribute("selectedCommentStatus", commentStatus);
        model.addAttribute("selectedCommentKeyword", commentKeyword);

        model.addAttribute("screen", "admin/community");
        return "admin/community";
    }

    @PostMapping("/admin/community/posts/{postId}/status")
    public String updatePostStatus(
            @PathVariable Long postId,
            CommunityStatusForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            RedirectAttributes redirectAttributes
    ) {

        if (sessionUser == null || sessionUser.userId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인한 관리자 정보가 없습니다.");
            return "redirect:/login";
        }

        int updated = service.updatePostStatus(postId, form, sessionUser);

        if (updated == 1) {
            redirectAttributes.addFlashAttribute("message", "게시글 상태가 변경되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "상태를 변경할 게시글을 찾을 수 없습니다.");
        }

        return "redirect:/admin/community";
    }

    @PostMapping("/admin/community/comments/{commentId}/status")
    public String updateCommentStatus(
            @PathVariable Long commentId,
            CommunityStatusForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            RedirectAttributes redirectAttributes
    ) {

        if (sessionUser == null || sessionUser.userId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인한 관리자 정보가 없습니다.");
            return "redirect:/login";
        }

        int updated = service.updateCommentStatus(commentId, form, sessionUser);

        if (updated == 1) {
            redirectAttributes.addFlashAttribute("message", "댓글 상태가 변경되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "상태를 변경할 댓글을 찾을 수 없습니다.");
        }


        return "redirect:/admin/community";
    }
}

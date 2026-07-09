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

        model.addAttribute("communityPage", service.findPage(
                postPage,
                commentPage,
                size,
                boardType,
                status,
                keyword
        ));
        model.addAttribute("selectedTab", tab == null || tab.isBlank() ? "posts" : tab);
        model.addAttribute("selectedBoardType", boardType);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedKeyword", keyword);

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

        service.updatePostStatus(postId, form, sessionUser);
        redirectAttributes.addFlashAttribute("message", "게시글 상태가 변경되었습니다.");

        return "redirect:/admin/community";
    }

    @PostMapping("/admin/community/comments/{commentId}/status")
    public String updateCommentStatus(
            @PathVariable Long commentId,
            CommunityStatusForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            RedirectAttributes redirectAttributes
    ) {

        service.updateCommentStatus(commentId, form, sessionUser);
        redirectAttributes.addFlashAttribute("message", "댓글 상태가 변경되었습니다.");

        return "redirect:/admin/community";
    }
}

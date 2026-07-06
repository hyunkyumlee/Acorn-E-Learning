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
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class AdminCommunityController {

    private final AdminCommunityService service;

    @GetMapping("/admin/community")
    public String community(Model model) {

        model.addAttribute("communityPage", service.findPage());

        model.addAttribute("screen", "admin/community");
        return "admin/community";
    }

    @PostMapping("/admin/community/posts/{postId}/status")
    public String updatePostStatus(
            @PathVariable Long postId,
            CommunityStatusForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {

        service.updatePostStatus(postId, form, sessionUser);

        return "redirect:/admin/community";
    }

    @PostMapping("/admin/community/comments/{commentId}/status")
    public String updateCommentStatus(
            @PathVariable Long commentId,
            CommunityStatusForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {

        service.updateCommentStatus(commentId, form, sessionUser);

        return "redirect:/admin/community";
    }
}

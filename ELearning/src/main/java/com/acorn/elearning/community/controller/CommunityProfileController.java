package com.acorn.elearning.community.controller;

import com.acorn.elearning.community.dto.response.CommunityProfileResponse;
import com.acorn.elearning.community.service.PostService;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class CommunityProfileController {
    private final PostService postService;

    public CommunityProfileController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/community/profile")
    public String me(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        CommunityProfileResponse profile = postService.profile(sessionUser);
        model.addAttribute("screen", "community/profile");
        model.addAttribute("view", profile);
        model.addAttribute("activeSubjectId", "");
        model.addAttribute("activeBoardType", "");
        model.addAttribute("loggedIn", true);
        model.addAttribute("profileName", sessionUser.nickname());
        model.addAttribute("profileImageUrl", sessionUser.profileImageUrl());
        model.addAttribute("profileEmail", sessionUser.email());
        model.addAttribute("profileSummary", profile);
        return "community/profile";
    }
}

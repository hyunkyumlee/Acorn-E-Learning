package com.acorn.elearning.community.controller;

import com.acorn.elearning.community.dto.response.CommunityProfileResponse;
import com.acorn.elearning.community.service.PostService;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            @RequestParam(name = "subjectId", required = false) String subjectId,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "direction", required = false) String direction,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        CommunityProfileResponse profile = postService.profile(sessionUser);
        Long selectedSubjectId = normalizeSubjectId(subjectId);
        String selectedSort = normalizeSort(sort);
        String selectedDirection = normalizeDirection(direction);
        model.addAttribute("screen", "community/profile");
        model.addAttribute("view", profile);
        model.addAttribute("filteredMyPosts", profile.filteredMyPosts(selectedSubjectId, selectedSort, selectedDirection));
        model.addAttribute("selectedSubjectId", selectedSubjectId);
        model.addAttribute("selectedSort", selectedSort);
        model.addAttribute("selectedDirection", selectedDirection);
        model.addAttribute("activeSubjectId", "");
        model.addAttribute("activeBoardType", "");
        model.addAttribute("loggedIn", true);
        model.addAttribute("profileName", sessionUser.nickname());
        model.addAttribute("profileImageUrl", sessionUser.profileImageUrl());
        model.addAttribute("profileEmail", sessionUser.email());
        model.addAttribute("profileSummary", profile);
        return "community/profile";
    }

    private Long normalizeSubjectId(String subjectId) {
        if (subjectId == null) {
            return null;
        }
        try {
            long parsedSubjectId = Long.parseLong(subjectId);
            return parsedSubjectId == 1L || parsedSubjectId == 2L || parsedSubjectId == 3L || parsedSubjectId == 4L
                    ? parsedSubjectId
                    : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String normalizeSort(String sort) {
        if ("views".equalsIgnoreCase(sort)) {
            return "views";
        }
        if ("popular".equalsIgnoreCase(sort)) {
            return "popular";
        }
        return "latest";
    }

    private String normalizeDirection(String direction) {
        return "asc".equalsIgnoreCase(direction) ? "asc" : "desc";
    }
}

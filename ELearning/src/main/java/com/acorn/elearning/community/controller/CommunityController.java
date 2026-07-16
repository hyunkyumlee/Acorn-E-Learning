package com.acorn.elearning.community.controller;

import com.acorn.elearning.community.dto.response.PostPageResponse;
import com.acorn.elearning.community.form.PostSearchCondition;
import com.acorn.elearning.community.mapper.CommunityNoticeMapper;
import com.acorn.elearning.community.service.PostService;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class CommunityController {
    private static final long DEFAULT_SUBJECT_ID = 1L;
    private static final int HOME_POST_LIMIT = 5;
    private static final int HOME_NOTICE_LIMIT = 4;

    private final PostService postService;
    private final CommunityNoticeMapper communityNoticeMapper;

    public CommunityController(PostService postService, CommunityNoticeMapper communityNoticeMapper) {
        this.postService = postService;
        this.communityNoticeMapper = communityNoticeMapper;
    }

    @GetMapping("/community")
    public String home(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(required = false) Long subjectId,
            Model model
    ) {
        long selectedSubjectId = normalizeSubjectId(subjectId);
        PostSearchCondition latestCondition = new PostSearchCondition();
        latestCondition.setSubjectId(selectedSubjectId);
        latestCondition.setSize(HOME_POST_LIMIT);

        PostSearchCondition weeklyHotCondition = hotCondition("week", selectedSubjectId);
        PostSearchCondition monthlyHotCondition = hotCondition("month", selectedSubjectId);

        PostPageResponse view = postService.page(latestCondition);
        model.addAttribute("screen", "community/index");
        model.addAttribute("selectedSubjectName", subjectName(selectedSubjectId));
        model.addAttribute("view", view);
        model.addAttribute("notices", communityNoticeMapper.findPublishedNotices(HOME_NOTICE_LIMIT));
        model.addAttribute("weeklyHotView", postService.page(weeklyHotCondition));
        model.addAttribute("monthlyHotView", postService.page(monthlyHotCondition));
        addCommunityShell(model, sessionUser, selectedSubjectId);
        return "community/index";
    }

    private PostSearchCondition hotCondition(String period, long subjectId) {
        PostSearchCondition condition = new PostSearchCondition();
        condition.setSubjectId(subjectId);
        condition.setSort("hot");
        condition.setPeriod(period);
        condition.setSize(HOME_POST_LIMIT);
        return condition;
    }

    private long normalizeSubjectId(Long subjectId) {
        if (subjectId == null) {
            return DEFAULT_SUBJECT_ID;
        }
        return switch (subjectId.intValue()) {
            case 1, 2, 3, 4 -> subjectId;
            default -> DEFAULT_SUBJECT_ID;
        };
    }

    private String subjectName(long subjectId) {
        return switch ((int) subjectId) {
            case 2 -> "Python";
            case 3 -> "HTML/CSS/JS";
            case 4 -> "SQL";
            default -> "Java";
        };
    }

    private void addCommunityShell(Model model, SessionUser sessionUser, long subjectId) {
        model.addAttribute("activeSubjectId", Long.toString(subjectId));
        model.addAttribute("activeBoardType", "");
        model.addAttribute("loggedIn", sessionUser != null);
        model.addAttribute("profileName", sessionUser == null ? "guest" : sessionUser.nickname());
        model.addAttribute("profileImageUrl", sessionUser == null ? null : sessionUser.profileImageUrl());
        model.addAttribute("profileEmail", sessionUser == null ? "로그인하면 커뮤니티 활동을 확인할 수 있어." : sessionUser.email());
        if (sessionUser != null) {
            model.addAttribute("profileSummary", postService.profile(sessionUser));
        }
    }
}

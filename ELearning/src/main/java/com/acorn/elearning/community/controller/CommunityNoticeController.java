package com.acorn.elearning.community.controller;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.community.mapper.CommunityNoticeMapper;
import com.acorn.elearning.community.model.CommunityNotice;
import com.acorn.elearning.community.service.PostService;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class CommunityNoticeController {
    private final CommunityNoticeMapper communityNoticeMapper;
    private final PostService postService;

    public CommunityNoticeController(CommunityNoticeMapper communityNoticeMapper, PostService postService) {
        this.communityNoticeMapper = communityNoticeMapper;
        this.postService = postService;
    }

    @GetMapping("/community/notices/{noticeId}")
    public String detail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long noticeId,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            Model model
    ) {
        CommunityNotice notice = communityNoticeMapper.findPublishedNoticeById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "공지사항을 찾을 수 없습니다."));

        model.addAttribute("screen", "community/notice-detail");
        model.addAttribute("notice", notice);
        model.addAttribute("activeSubjectId", subjectId == null ? "" : subjectId.toString());
        model.addAttribute("activeBoardType", "");
        model.addAttribute("loggedIn", sessionUser != null);
        model.addAttribute("profileName", sessionUser == null ? "guest" : sessionUser.nickname());
        model.addAttribute("profileImageUrl", sessionUser == null ? null : sessionUser.profileImageUrl());
        model.addAttribute("profileEmail", sessionUser == null ? "로그인하면 커뮤니티 활동을 확인할 수 있어." : sessionUser.email());
        if (sessionUser != null) {
            model.addAttribute("profileSummary", postService.profile(sessionUser));
        }
        return "community/notice-detail";
    }
}

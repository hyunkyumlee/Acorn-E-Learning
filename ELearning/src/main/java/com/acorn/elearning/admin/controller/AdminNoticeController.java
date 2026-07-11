package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.model.Notice;
import com.acorn.elearning.admin.service.AdminNoticeService;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminNoticeService service;


    @GetMapping("/admin/notices")
    public String notices(Model model,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String period,
                          @RequestParam(required = false) String status) {

        model.addAttribute("noticePage", service.findPage(page, size, keyword, period, status));
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("screen", "admin/notices");
        return "admin/notices";
    }

    @PostMapping("/admin/notices")
    public String create(Notice notice,
                         @SessionAttribute(name= SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                         RedirectAttributes redirectAttributes
    ) {

        if (sessionUser == null || sessionUser.userId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인한 관리자 정보가 없습니다.");
            return "redirect:/login";
        }

        int created = service.insert(notice, sessionUser);

        if (created == 1) {
            redirectAttributes.addFlashAttribute("message", "공지사항이 등록되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "공지사항 등록에 실패했습니다.");
        }
        return "redirect:/admin/notices";
    }

    @PostMapping("/admin/notices/{noticeId}")
    public String update(@PathVariable Long noticeId, Notice notice,
                         @SessionAttribute(name= SessionUser.SESSION_KEY, required = false)SessionUser sessionUser,
                         RedirectAttributes redirectAttributes)
    {

        if (sessionUser == null || sessionUser.userId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인한 관리자 정보가 없습니다.");
            return "redirect:/login";
        }
        notice.setNoticeId(noticeId);

        int updated = service.update(notice, sessionUser);

        if (updated == 1) {
            redirectAttributes.addFlashAttribute("message", "공지사항이 수정되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "수정할 공지사항을 찾을 수 없습니다.");
        }
        return "redirect:/admin/notices";
    }

    @PostMapping("/admin/notices/{noticeId}/delete")
    public String delete(
            @PathVariable Long noticeId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            RedirectAttributes redirectAttributes
    ) {

        if (sessionUser == null || sessionUser.userId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인한 관리자 정보가 없습니다.");
            return "redirect:/login";
        }

        int deleted = service.delete(noticeId, sessionUser);

        if (deleted == 1) {
            redirectAttributes.addFlashAttribute("message", "공지사항이 삭제되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "삭제할 공지사항을 찾을 수 없습니다.");
        }
        return "redirect:/admin/notices";
    }
}

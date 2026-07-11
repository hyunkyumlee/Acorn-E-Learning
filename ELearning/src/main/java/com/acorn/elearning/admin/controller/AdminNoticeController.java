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

        service.insert(notice, sessionUser);
        redirectAttributes.addFlashAttribute("message", "공지사항이 등록되었습니다.");

        return "redirect:/admin/notices";
    }

    @PostMapping("/admin/notices/{noticeId}")
    public String update(@PathVariable Long noticeId, Notice notice,
                         @SessionAttribute(name= SessionUser.SESSION_KEY, required = false)SessionUser sessionUser,
                         RedirectAttributes redirectAttributes)
    {


        notice.setNoticeId(noticeId);

        service.update(notice, sessionUser);
        redirectAttributes.addFlashAttribute("message", "공지사항이 수정되었습니다.");

        return "redirect:/admin/notices";
    }

    @PostMapping("/admin/notices/{noticeId}/delete")
    public String delete(
            @PathVariable Long noticeId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            RedirectAttributes redirectAttributes
    ) {
        service.delete(noticeId, sessionUser);
        redirectAttributes.addFlashAttribute("message", "공지사항이 삭제되었습니다.");
        return "redirect:/admin/notices";
    }
}

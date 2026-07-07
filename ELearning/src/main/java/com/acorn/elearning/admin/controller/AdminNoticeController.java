package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.model.Notice;
import com.acorn.elearning.admin.service.AdminNoticeService;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminNoticeService service;


    @GetMapping("/admin/notices")
    public String notices(Model model) {

       model.addAttribute("noticeList", service.findAll());

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

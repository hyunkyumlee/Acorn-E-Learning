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
                         @SessionAttribute(name= SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {

        service.insert(notice, sessionUser);

        return "redirect:/admin/notices";
    }

    @PostMapping("/admin/notices/{noticeId}")
    public String update(@PathVariable Long noticeId, Notice notice,
                         @SessionAttribute(name= SessionUser.SESSION_KEY, required = false)SessionUser sessionUser)
    {


        notice.setNoticeId(noticeId);

        service.update(notice, sessionUser);

        return "redirect:/admin/notices";
    }

    @PostMapping("/admin/notices/{noticeId}/delete")
    public String delete(
            @PathVariable Long noticeId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        service.delete(noticeId, sessionUser);
        return "redirect:/admin/notices";
    }
}

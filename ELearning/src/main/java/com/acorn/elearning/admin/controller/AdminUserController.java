package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.dto.response.AdminUserManageRowResponse;
import com.acorn.elearning.admin.form.SubjectForm;
import com.acorn.elearning.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService service;


    @GetMapping("/admin/users")
    public String users(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AdminManagePageView view = adminUserService.users(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.

        List<AdminUserManageRowResponse> userList = service.findAll();
        model.addAttribute("userList", userList);

        model.addAttribute("userStatusForm", new SubjectForm());

        model.addAttribute("screen", "admin/users");
        return "admin/adminUsers";
    }
}

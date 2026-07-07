package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.dto.response.AdminUserManageRowResponse;
import com.acorn.elearning.admin.form.SubjectForm;
import com.acorn.elearning.admin.service.AdminContentService;
import com.acorn.elearning.admin.service.AdminUserService;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService service;
    private final AdminContentService contentService;


    @GetMapping("/admin/users")
    public String users(Model model) {

        List<AdminUserManageRowResponse> userList = service.findAll();
        model.addAttribute("userList", userList);
        model.addAttribute("subjectList", contentService.findAllSubject());

        model.addAttribute("userStatusForm", new SubjectForm());

        model.addAttribute("screen", "admin/users");
        return "admin/adminUsers";
    }

    @PostMapping("/admin/users/{userId}/status")
    public String updateUserStatus(@PathVariable Long userId,
                                   @RequestParam String status,
                                   @SessionAttribute(name=SessionUser.SESSION_KEY, required = false)SessionUser sessionUser,
                                   RedirectAttributes redirectAttributes)
    {

        if (sessionUser == null || sessionUser.userId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인한 관리자 정보가 없습니다.");
            return "redirect:/login";
        }

        int statusResult = service.updateStatus(userId, status, sessionUser.userId());

        if(statusResult == 1) {
            redirectAttributes.addFlashAttribute("message", "상태가 변경되었습니다.");
        }else{
            redirectAttributes.addFlashAttribute("errorMessage", "상태가 변경되지 않았습니다. 다시 시도하세요.");
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{userId}/role")
    public String updateUserRole(@PathVariable Long userId,
                                 @RequestParam String role,
                                 @SessionAttribute(name=SessionUser.SESSION_KEY, required = false)SessionUser sessionUser,
                                 RedirectAttributes redirectAttributes)
    {

        if (sessionUser == null || sessionUser.userId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인한 관리자 정보가 없습니다.");
            return "redirect:/login";
        }

        int roleResult = service.updateRole(userId, role, sessionUser.userId());

        if(roleResult == 1){
            redirectAttributes.addFlashAttribute("message", "권한이 변경되었습니다.");
        }else{
            redirectAttributes.addFlashAttribute("errorMessage", "권한 변경에 실패하였습니다.");
        }

        return "redirect:/admin/users";
    }


}

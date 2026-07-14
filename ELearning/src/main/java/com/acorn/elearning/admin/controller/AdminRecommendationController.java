package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.form.RecommendationForm;
import com.acorn.elearning.admin.service.AdminRecommendationService;
import com.acorn.elearning.admin.service.AdminContentService;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminRecommendationController {

    private final AdminRecommendationService service;
    private final AdminContentService contentService;

    @GetMapping("/admin/recommendations")
    public String recommendations(Model model,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) Long subjectId,
                                  @RequestParam(required = false) String contentType,
                                  @RequestParam(required = false) Boolean isActive,
                                  @RequestParam(required = false) String keyword)
    {

        model.addAttribute("recommendationPage", service.findPage(page, size, subjectId, contentType, isActive, keyword ));
        model.addAttribute("subjectList", contentService.findAllSubject());
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedContentType", contentType);
        model.addAttribute("selectedIsActive", isActive);
        model.addAttribute("selectedKeyword", keyword);

        model.addAttribute("screen", "admin/recommendations");
        return "admin/recommendations";

    }


    @PostMapping("/admin/recommendations")
    public String create(RecommendationForm form,
                         @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                         RedirectAttributes redirectAttributes)
    {
        if(sessionUser == null || sessionUser.userId() == null){
            redirectAttributes.addFlashAttribute("errorMessage", "로그인한 관리자 정보가 없습니다.");
            return "redirect:/login";
        }

        int created = service.create(form, sessionUser.userId());

        if(created == 1){
            redirectAttributes.addFlashAttribute("message", "추천 콘텐츠가 등록되었습니다.");
        }else{
            redirectAttributes.addFlashAttribute("errorMessage", "추천 콘텐츠 등록에 실패하였습니다.");
        }

        return "redirect:/admin/recommendations";
    }

    @PostMapping("/admin/recommendations/{contentId}")
    public String update(RecommendationForm form,
                         @PathVariable Long contentId,
                         @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                         RedirectAttributes redirectAttributes)
    {
        if(sessionUser == null || sessionUser.userId() == null){
            redirectAttributes.addFlashAttribute("errorMessage", "로그인한 관리자 정보가 없습니다.");
            return "redirect:/login";
        }

        int updated = service.update(contentId, form, sessionUser.userId());

        if(updated == 1){
            redirectAttributes.addFlashAttribute("message", "추천 콘텐츠가 수정되었습니다.");
        }else{
            redirectAttributes.addFlashAttribute("errorMessage", "수정할 추천 콘텐츠를 찾을 수 없습니다.");
        }

        return "redirect:/admin/recommendations";
    }


    @PostMapping("/admin/recommendations/{contentId}/delete")
    public String delete(@PathVariable Long contentId,
                         @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                         RedirectAttributes redirectAttributes)
    {
        if(sessionUser == null || sessionUser.userId() == null){
            redirectAttributes.addFlashAttribute("errorMessage", "로그인한 관리자 정보가 없습니다.");
            return "redirect:/login";
        }

        int deleted = service.delete(contentId, sessionUser.userId());

        if(deleted == 1){
            redirectAttributes.addFlashAttribute("message", "추천 콘텐츠를 삭제하였습니다.");
        }else{
            redirectAttributes.addFlashAttribute("errorMessage", "삭제할 추천 콘텐츠를 찾을 수 없습니다.");
        }

        return "redirect:/admin/recommendations";
    }
}

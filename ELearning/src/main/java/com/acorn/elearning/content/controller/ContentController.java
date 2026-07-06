package com.acorn.elearning.content.controller;

import com.acorn.elearning.content.service.ContentRecommendationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContentController {
    private final ContentRecommendationService contentRecommendationService;

    public ContentController(ContentRecommendationService contentRecommendationService) {
        this.contentRecommendationService = contentRecommendationService;
    }

    @GetMapping({"/content/recommendations", "/community/recommendations"})
    public String recommendations(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false, name = "slot") String slot,
            Model model
    ) {
        Long activeSubjectId = subjectId == null ? 1L : subjectId;
        model.addAttribute("screen", "content/recommendations");
        model.addAttribute("activeSubjectId", activeSubjectId);
        model.addAttribute("subjectLabel", subjectLabel(activeSubjectId));
        model.addAttribute("videoView", contentRecommendationService.recommendations(activeSubjectId, "VIDEO", slot));
        model.addAttribute("articleView", contentRecommendationService.recommendations(activeSubjectId, "ARTICLE", slot));
        model.addAttribute("docsView", contentRecommendationService.recommendations(activeSubjectId, "DOCS", slot));
        model.addAttribute("courseView", contentRecommendationService.recommendations(activeSubjectId, "COURSE", slot));
        return "content/recommendations";
    }

    private String subjectLabel(Long subjectId) {
        if (subjectId == null) {
            return "JAVA";
        }
        return switch (subjectId.intValue()) {
            case 2 -> "Python";
            case 3 -> "HTML/CSS/JS";
            case 4 -> "SQL";
            default -> "JAVA";
        };
    }
}

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

    @GetMapping("/community/recommendations")
    public String recommendations(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false, name = "slot") String slot,
            Model model
    ) {
        model.addAttribute("screen", "community/recommendations");
        model.addAttribute("view", contentRecommendationService.recommendations(subjectId, contentType, slot));
        return "community/recommendations";
    }
}

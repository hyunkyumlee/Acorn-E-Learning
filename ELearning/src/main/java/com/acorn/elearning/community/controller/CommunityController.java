package com.acorn.elearning.community.controller;

import com.acorn.elearning.community.form.PostSearchCondition;
import com.acorn.elearning.community.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommunityController {
    private final PostService postService;

    public CommunityController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/community")
    public String home(Model model) {
        model.addAttribute("screen", "community/index");
        model.addAttribute("view", postService.page(new PostSearchCondition()));
        return "community/index";
    }
}

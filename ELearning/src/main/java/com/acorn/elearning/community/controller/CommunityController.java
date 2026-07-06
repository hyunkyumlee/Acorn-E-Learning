package com.acorn.elearning.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommunityController {
    @GetMapping("/community")
    public String home() {
        return "redirect:/community/board";
    }
}

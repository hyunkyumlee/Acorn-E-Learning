package com.acorn.elearning.ranking.controller;

import com.acorn.elearning.ranking.service.RankingService;
import com.acorn.elearning.ranking.view.RankingPageView;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class RankingController {
/*
    @GetMapping("/ranking")
    public String index(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // RankingPageView view = rankingService.index(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "ranking/index");
        return "ranking/index";
    }
*/

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/ranking")
    public String index(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "periodType", defaultValue = "WEEKLY") String periodType,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }

        RankingPageView view = rankingService.index(sessionUser, subjectId, periodType);
        model.addAttribute("view", view);
        model.addAttribute("screen", "ranking/index");
        return "ranking/index";
    }
}


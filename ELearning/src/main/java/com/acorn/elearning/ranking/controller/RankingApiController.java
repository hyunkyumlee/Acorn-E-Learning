package com.acorn.elearning.ranking.controller;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.common.response.ApiResponse;
import java.util.Map;

import com.acorn.elearning.ranking.dto.response.RankingPageResponse;
import com.acorn.elearning.ranking.dto.response.MyRankingResponse;
import com.acorn.elearning.ranking.service.RankingService;
import com.acorn.elearning.security.SessionUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
public class RankingApiController {

    private final RankingService rankingService;

    public RankingApiController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/api/rankings")
    public ApiResponse<Map<String, Object>> rankings(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "periodType", defaultValue = "WEEKLY") String periodType
    ) {
        if (sessionUser == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        RankingPageResponse response = rankingService.rankings(sessionUser, subjectId, periodType);
        return ApiResponse.success(response.data());
    }

    @GetMapping("/api/rankings/me")
    public ApiResponse<Map<String, Object>> myRanking(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "periodType", defaultValue = "WEEKLY") String periodType
    ) {
        if (sessionUser == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        MyRankingResponse response = rankingService.myRanking(sessionUser, subjectId, periodType);
        return ApiResponse.success(response.data());
    }
}

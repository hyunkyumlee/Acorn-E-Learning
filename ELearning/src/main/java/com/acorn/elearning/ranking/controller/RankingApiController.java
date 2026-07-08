package com.acorn.elearning.ranking.controller;

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
/*
    @GetMapping("/api/rankings")
    public ApiResponse<Map<String, Object>> rankings() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // RankingPageResponse response = rankingService.rankings(sessionUser);
        // return ApiResponse.success(response);
        return ok("RANK-001");
    }

    @GetMapping("/api/rankings/me")
    public ApiResponse<Map<String, Object>> myRanking() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // MyRankingResponse response = rankingService.myRanking(sessionUser);
        // return ApiResponse.success(response);
        return ok("RANK-002");
    }

    private ApiResponse<Map<String, Object>> ok(String endpointId) {
        // TODO: 개별 endpoint method에서 service 호출과 Response DTO 변환을 끝내면 이 helper를 제거하세요.
        // return ApiResponse.success(response); 형태가 최종 구현입니다.
        return ApiResponse.success(Map.of("endpointId", endpointId, "status", "SKELETON"));
    }
}
*/

    private final RankingService rankingService;

    public RankingApiController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/api/rankings")
    public ApiResponse<Map<String, Object>> rankings(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId", required = false) Long subjectId
    ) {
        if (sessionUser == null) {
            return ApiResponse.success(Map.of(
                    "status", "AUTH-401",
                    "message", "로그인이 필요합니다."
            ));
        }

        RankingPageResponse response = rankingService.rankings(sessionUser, subjectId);
        return ApiResponse.success(response.data());
    }

    @GetMapping("/api/rankings/me")
    public ApiResponse<Map<String, Object>> myRanking(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId", required = false) Long subjectId
    ) {
        if (sessionUser == null) {
            return ApiResponse.success(Map.of(
                    "status", "AUTH-401",
                    "message", "로그인이 필요합니다."
            ));
        }

        MyRankingResponse response = rankingService.myRanking(sessionUser, subjectId);
        return ApiResponse.success(response.data());
    }
}

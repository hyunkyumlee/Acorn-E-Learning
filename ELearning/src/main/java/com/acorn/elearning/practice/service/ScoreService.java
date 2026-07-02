package com.acorn.elearning.practice.service;

import java.util.Map;

import com.acorn.elearning.practice.mapper.ScoreEventMapper;
import com.acorn.elearning.practice.model.ScoreEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreService {
    /*
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // SessionUser sessionUser = currentSessionUser();
        // Object entity = domainMapper.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // domainMapper.update(applyForm(entity, form));
        // return Map.of("result", entity);
        return Map.of("action", action, "status", "SKELETON");
    }
    */

    private final ScoreEventMapper scoreEventMapper;

    public ScoreService(ScoreEventMapper scoreEventMapper) {
        this.scoreEventMapper = scoreEventMapper;
    }


    // 점수를 부여하고 이벤트를 기록
    @Transactional
    public void giveScore(Long userId, Long subjectId, int scoreDelta, String reasonCode, String idempotencyKey) {
        ScoreEvent event = new ScoreEvent();
        event.setUserId(userId);
        event.setSubjectId(subjectId);
        event.setScoreDelta(scoreDelta);
        event.setReasonCode(reasonCode);
        event.setIdempotencyKey(idempotencyKey);
        event.setSourceType("PRACTICE"); // 연습 문제 풀이 출처

        scoreEventMapper.insert(event);
    }
}

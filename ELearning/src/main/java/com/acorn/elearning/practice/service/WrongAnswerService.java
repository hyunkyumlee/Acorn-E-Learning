package com.acorn.elearning.practice.service;

import java.util.Map;

import com.acorn.elearning.practice.mapper.WrongAnswerMapper;
import com.acorn.elearning.practice.model.WrongAnswer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WrongAnswerService {
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

    private final WrongAnswerMapper wrongAnswerMapper;

    public WrongAnswerService(WrongAnswerMapper wrongAnswerMapper) {
        this.wrongAnswerMapper = wrongAnswerMapper;
    }

    // 오답을 데이터베이스에 기록합니다.
    @Transactional
    public void recordWrongAnswer(Long setAttemptId, Long userId, Long problemId, Long submissionId) {
        WrongAnswer wrongAnswer = new WrongAnswer();
        wrongAnswer.setSetAttemptId(setAttemptId);
        wrongAnswer.setUserId(userId);
        wrongAnswer.setProblemId(problemId);

        // 1. 오답 횟수 초기화 (처음 틀렸을 때는 1)
        wrongAnswer.setWrongCount(1);

        // 2. 제출 ID 기록 (나중에 상세 오답 노트 볼 때 참조) --// 모델에 있는 필드 활용 [cite: 649]
        wrongAnswer.setLastSubmissionId(submissionId);

        // 3. 상태 초기화
        wrongAnswer.setReviewStatus("PENDING");

        // 4. 재정답 보너스 여부 초기화
        wrongAnswer.setRetryBonusAwarded(false);

        wrongAnswerMapper.insertWrongAnswer(wrongAnswer);
    }
}
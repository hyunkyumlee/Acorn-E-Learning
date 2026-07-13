package com.acorn.elearning.practice.mapper;

import com.acorn.elearning.practice.model.PracticeSubmission;
import java.util.List;
import java.util.Optional;

public interface PracticeSubmissionMapper {
    // 1. 특정 제출 건 조회 (오답 노트 상세 보기 등에 활용)
    Optional<PracticeSubmission> findByIdSubmission(Long submissionId);
    // 2. 전체 제출 이력 조회 (관리자 통계나 본인 제출 이력 확인에 활용)
    List<PracticeSubmission> findAllSubmission();
    // 3. 답안 제출 시 기록 저장
    int insertSubmission(PracticeSubmission submission);
    // 4. 같은 set/problem/context 제출 조회
    Optional<PracticeSubmission> findBySetAttemptIdAndProblemIdAndContext(
            Long setAttemptId,
            Long problemId,
            String submissionContext
    );

    // 5. 기존 제출 수정
    int updateSubmission(PracticeSubmission submission);
    // 6. 결과계산
    List<PracticeSubmission> findBySetAttemptId(Long setAttemptId);
}
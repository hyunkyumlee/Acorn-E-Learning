package com.acorn.elearning.practice.mapper;

import com.acorn.elearning.practice.model.WrongAnswer;
import java.util.List;
import java.util.Optional;

public interface WrongAnswerMapper {
    // 1. 오답 등록
    int insertWrongAnswer(WrongAnswer wrongAnswer);

    // 2. 특정 오답 상세 조회
    Optional<WrongAnswer> findByIdWrongAnswer(Long wrongAnswerId);

    // 3. 사용자별/세트별 오답 전체 조회
    List<WrongAnswer> findAllWrongAnswersByUserId(Long userId);

    // 4. 재정답 완료 시 상태변경
    int updateWrongAnswer(Long wrongAnswerId);

}

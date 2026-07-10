package com.acorn.elearning.practice.mapper;

import com.acorn.elearning.practice.model.WrongAnswer;
import java.util.List;
import java.util.Optional;

public interface WrongAnswerMapper {
    int insertWrongAnswer(WrongAnswer wrongAnswer);

    Optional<WrongAnswer> findByIdWrongAnswer(Long wrongAnswerId);

    Optional<WrongAnswer> findByUserIdAndProblemId(Long userId, Long problemId);

    List<WrongAnswer> findAllWrongAnswersByUserId(Long userId);

    int markWrongAnswerSolved(Long wrongAnswerId);

    int updateWrongAnswerOnNewMistake(WrongAnswer wrongAnswer);

    //추가
    List<WrongAnswer> findWrongAnswersByUserIdAndNodeId(Long userId, Long nodeId);
    List<WrongAnswer> findWrongAnswersByUserIdAndLessonId(Long userId, Long lessonId);

}

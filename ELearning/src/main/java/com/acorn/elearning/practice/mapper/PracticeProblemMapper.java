package com.acorn.elearning.practice.mapper;

import com.acorn.elearning.practice.model.PracticeProblem;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface PracticeProblemMapper {

    //관리자 또는 오답용 문제 조회,수정, 삭제
    Optional<PracticeProblem> findById(Long id);
    List<PracticeProblem> findAll();
    int insert(PracticeProblem model);
    int update(PracticeProblem model);

    //lessonid기준 문제추출
    List<PracticeProblem> findPracticeProblemsByLessonId(
            @Param("lessonId") Long lessonId);
    //일반문제 10개 풀이용
    List<PracticeProblem> findPracticeProblems(
            @Param("subjectId") Long subjectId,
            @Param("nodeId") Long nodeId,
            @Param("difficultyCode") String difficultyCode
    );


}

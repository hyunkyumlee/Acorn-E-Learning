package com.acorn.elearning.practice.service;

import java.util.List;
import java.util.Map;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.practice.mapper.PracticeProblemMapper;
import com.acorn.elearning.practice.mapper.PracticeSetAttemptMapper;
import com.acorn.elearning.practice.model.PracticeProblem;
import org.springframework.stereotype.Service;

@Service
public class ProblemService {
   /* public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // SessionUser sessionUser = currentSessionUser();
        // Object entity = domainMapper.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // domainMapper.update(applyForm(entity, form));
        // return Map.of("result", entity);
        return Map.of("action", action, "status", "SKELETON");
        */
        private final PracticeProblemMapper practiceProblemMapper;

        public ProblemService(PracticeProblemMapper practiceProblemMapper) {
        this.practiceProblemMapper = practiceProblemMapper;

    }
      //lesson id에 맞는 문제 추출
      public List<PracticeProblem> getProblemsByLessonId(Long lessonId) {
          List<PracticeProblem> problems = practiceProblemMapper.findPracticeProblemsByLessonId(lessonId);

          if (problems.isEmpty()) {
              throw new BusinessException(
                      ErrorCode.COMMON_NOT_FOUND,
                      "해당 lesson에 맞는 문제가 없습니다."
              );
          }

          return problems;
      }

      // 문제조회
      public List<PracticeProblem> getProblems(Long subjectId, Long nodeId, String difficultyCode) {
          List<PracticeProblem> problems =
                  practiceProblemMapper.findPracticeProblems(subjectId, nodeId, difficultyCode);

          if (problems.isEmpty()) {
              throw new BusinessException(
                      ErrorCode.COMMON_NOT_FOUND,
                      "해당 과목/단원/난이도에 맞는 문제가 없습니다."
              );
          }

          return problems;
      }

        //문제의 상세 정보 조회 (채점 로직 등에서 정답 확인용으로 사용)
        public PracticeProblem getProblem(Long problemId) {
            return practiceProblemMapper.findById(problemId)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.COMMON_NOT_FOUND,
                            "존재하지 않는 문제입니다."
                    ));
        }



}

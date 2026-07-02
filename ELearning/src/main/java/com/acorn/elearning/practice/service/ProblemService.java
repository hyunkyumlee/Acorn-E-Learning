package com.acorn.elearning.practice.service;

import java.util.List;
import java.util.Map;

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


    // 생성자 주입
        public ProblemService(PracticeProblemMapper practiceProblemMapper) {
        this.practiceProblemMapper = practiceProblemMapper;

    }

      // 1. 학습을 위한 문제 10개를 조회합니다.
        public List<PracticeProblem> getProblems(Long subjectId, String difficultyCode) {
        List<PracticeProblem> problems = practiceProblemMapper.findPracticeProblems(subjectId, difficultyCode);

        if (problems.isEmpty()) {
            throw new IllegalArgumentException("해당 과목과 난이도에 맞는 문제가 없습니다.");
        }

        return problems;
    }

        //2.특정 문제의 상세 정보를 조회합니다. (채점 로직 등에서 정답 확인용으로 사용)
        public PracticeProblem getProblem(Long problemId) {
            return practiceProblemMapper.findById(problemId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제 ID입니다: " + problemId));
        }



}

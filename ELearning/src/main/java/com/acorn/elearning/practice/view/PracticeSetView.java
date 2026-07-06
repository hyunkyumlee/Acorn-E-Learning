package com.acorn.elearning.practice.view;

import com.acorn.elearning.practice.model.PracticeProblem;
import com.acorn.elearning.practice.model.ProblemChoice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record PracticeSetView(String title, String status, Map<String, Object> attributes) {
    /*
    public static PracticeSetView stub(String title) {
        return new PracticeSetView(title, "SKELETON", Map.of());
    }
    */

    // 서비스에서 호출하는 from 메서드
    public static PracticeSetView from(Long setAttemptId,
                                       List<PracticeProblem> problems,
                                       Map<Long, List<ProblemChoice>> choiceMap) {
        // 1. 정답(answerText)을 제외한 문제 데이터만 추출
        List<Map<String, Object>> safeProblems = problems.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("problemId", p.getProblemId());
                    map.put("question", p.getQuestion());
                    map.put("problemType", p.getProblemType());
                    map.put("difficultyCode", p.getDifficultyCode());

                    // 객관식 보기
                    List<Map<String, Object>> choices =
                            choiceMap.getOrDefault(p.getProblemId(), List.of())
                                    .stream()
                                    .map(c -> {
                                        Map<String, Object> choice = new HashMap<>();
                                        choice.put("choiceId", c.getChoiceId());
                                        choice.put("choiceLabel", c.getChoiceLabel());
                                        choice.put("choiceText", c.getChoiceText());
                                        return choice;
                                    })
                                    .toList();

                    map.put("choices", choices);

                    return map;
                })
                .collect(Collectors.toList());

        // 2. 뷰모델 구성
        return new PracticeSetView(
                "실습 문제",
                "IN_PROGRESS",
                Map.of("setAttemptId", setAttemptId, "problems", safeProblems)
        );
    }

}

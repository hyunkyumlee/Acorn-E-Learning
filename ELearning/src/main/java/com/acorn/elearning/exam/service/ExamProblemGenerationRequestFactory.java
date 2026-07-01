package com.acorn.elearning.exam.service;

import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.exam.dto.request.CreateExamRequest;
import com.acorn.elearning.exam.service.ExamLearningScopeService.ExamLearningScope;
import java.util.Map;

final class ExamProblemGenerationRequestFactory {

    private ExamProblemGenerationRequestFactory() {
    }

    static ChatGptRequest create(CreateExamRequest request, ExamLearningScope learningScope, int problemCount) {
        return new ChatGptRequest(
                "exam-problem-generation",
                "exam-problem-v2",
                Map.of(
                        "instruction", """
                                Java main 함수로 풀 수 있는 코딩테스트 문제 3개를 JSON으로 생성하세요. 반드시 learnedScope.learnedItems와 learnedScope.allowedConcepts에 포함된 이론 학습 및 문제풀이 내용만 출제합니다.
                                learnedScope에 없는 문법, API, 자료구조, 알고리즘은 문제 해결에 필요하게 만들지 않습니다. 특히 BufferedReader, InputStreamReader, StringTokenizer는 learnedScope에 직접 등장하지 않으면 starterCode와 정답 요구사항에 포함하지 않습니다.
                                각 문제는 prompt, starterCode, testCases를 포함합니다. starterCode는 입력을 읽는 기본 틀을 포함하고, 사용자가 구현해야 할 로직 영역만 TODO 주석으로 비워 둡니다. testCases는 input, expectedOutput 필드를 가진 배열입니다.
                                """,
                        "subjectId", request.subjectId(),
                        "levelCode", request.levelCode(),
                        "problemCount", problemCount,
                        "learnedScope", learningScope));
    }
}

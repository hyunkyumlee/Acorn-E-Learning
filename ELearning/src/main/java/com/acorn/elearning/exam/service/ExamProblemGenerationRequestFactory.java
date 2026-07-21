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
                "exam-problem-v4",
                Map.of(
                        "instruction", """
                                Java main 함수로 풀 수 있는 코딩테스트 문제 3개를 JSON으로 생성하세요. learnedScope.learnedItems는 학습자가 이론 학습과 문제풀이를 모두 완료한 필수 레슨 목록입니다. 반드시 learnedScope.learnedItems와 learnedScope.allowedConcepts에 포함된 레슨 내용만 출제합니다.
                                learnedScope에 없는 문법, API, 자료구조, 알고리즘은 문제 해결에 필요하게 만들지 않습니다. 특히 BufferedReader, InputStreamReader, StringTokenizer는 learnedScope에 직접 등장하지 않으면 정답 요구사항에 포함하지 않습니다.
                                각 문제는 prompt, solutionCode, testCases를 포함합니다. solutionCode는 Markdown 코드 펜스 없이 제공하는 완전한 Java 소스이며, public class Solution의 main 함수에서 입력을 읽고 각 testCases의 expectedOutput과 일치하는 결과를 출력해야 합니다. solutionCode에는 TODO나 미완성 로직을 남기지 마세요. testCases는 input, expectedOutput 필드를 가진 배열입니다. starterCode 필드는 생성하지 마세요. 학습자에게는 서버가 공통 TODO 골격을 제공합니다.
                                """,
                        "subjectId", request.subjectId(),
                        "levelCode", request.levelCode(),
                        "problemCount", problemCount,
                        "learnedScope", learningScope));
    }
}

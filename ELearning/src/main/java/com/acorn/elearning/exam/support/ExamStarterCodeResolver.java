package com.acorn.elearning.exam.support;

import com.acorn.elearning.exam.model.AiExamProblem;

public final class ExamStarterCodeResolver {
    private static final String DEFAULT_STARTER_CODE = """
            import java.util.Scanner;

            public class Solution {
                public static void main(String[] args) {
                    Scanner scanner = new Scanner(System.in);

                    // TODO 여기에 문제 풀이 로직을 작성하세요.
                    // 예: int n = scanner.nextInt();

                    // TODO 정답을 System.out.println으로 출력하세요.
                }
            }
            """;

    private ExamStarterCodeResolver() {}

    public static String starterCode(AiExamProblem ignoredProblem) {
        return DEFAULT_STARTER_CODE;
    }

    public static String defaultStarterCode() {
        return DEFAULT_STARTER_CODE;
    }
}

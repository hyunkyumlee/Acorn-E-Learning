package com.acorn.elearning.exam.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acorn.elearning.exam.model.AiExamProblem;
import org.junit.jupiter.api.Test;

class ExamStarterCodeResolverTest {

    @Test
    void starterCode_does_not_expose_solution_embedded_in_ai_response() {
        AiExamProblem problem = new AiExamProblem();
        problem.setProblemNo(1);
        problem.setAiRawResponse("""
                {
                  "problems": [
                    {
                      "starterCode": "import java.util.Scanner; public class Solution { public static void main(String[] args) { Scanner scanner = new Scanner(System.in); int a = scanner.nextInt(); int b = scanner.nextInt(); System.out.println(a + b); } }"
                    }
                  ]
                }
                """);

        String starterCode = ExamStarterCodeResolver.starterCode(problem);

        assertEquals(ExamStarterCodeResolver.defaultStarterCode(), starterCode);
        assertTrue(starterCode.contains("\n  public static void main"));
        assertTrue(starterCode.contains("\n    Scanner scanner"));
        assertTrue(starterCode.contains("TODO"));
        assertFalse(starterCode.contains("System.out.println(a + b);"));
    }

    @Test
    void solutionCode_returns_raw_solution_for_matching_problem() {
        AiExamProblem problem = new AiExamProblem();
        problem.setProblemNo(2);
        problem.setAiRawResponse("""
                {
                  "problems": [
                    {"starterCode": "public class Solution { public static void main(String[] args) { System.out.println(1); } }"},
                    {"starterCode": "public class Solution { public static void main(String[] args) { System.out.println(2); } }"}
                  ]
                }
                """);

        String solutionCode = ExamStarterCodeResolver.solutionCode(problem).orElseThrow();

        assertEquals("public class Solution { public static void main(String[] args) { System.out.println(2); } }", solutionCode);
    }
}

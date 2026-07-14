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
        assertTrue(starterCode.contains("TODO"));
        assertFalse(starterCode.contains("System.out.println(a + b);"));
    }
}

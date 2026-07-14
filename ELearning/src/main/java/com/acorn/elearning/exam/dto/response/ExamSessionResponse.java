package com.acorn.elearning.exam.dto.response;

import com.acorn.elearning.exam.model.AiExamProblem;
import com.acorn.elearning.exam.model.ExamAnswer;
import com.acorn.elearning.exam.model.ExamSession;
import com.acorn.elearning.exam.support.ExamPromptNormalizer;
import com.acorn.elearning.exam.support.ExamStarterCodeResolver;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record ExamSessionResponse(
        Long examId,
        Long subjectId,
        String levelCode,
        String status,
        String resultStatus,
        Integer totalProblemCount,
        Integer correctCount,
        List<Problem> problems
) {
    public static ExamSessionResponse from(ExamSession session, List<AiExamProblem> problems) {
        return from(session, problems, List.of());
    }

    public static ExamSessionResponse from(ExamSession session, List<AiExamProblem> problems, List<ExamAnswer> answers) {
        Map<Long, ExamAnswer> answersByProblemId = answers.stream()
                .collect(Collectors.toMap(ExamAnswer::getAiProblemId, Function.identity()));
        return new ExamSessionResponse(
                session.getExamId(),
                session.getSubjectId(),
                session.getLevelCode(),
                session.getStatus(),
                session.getResultStatus(),
                session.getTotalProblemCount(),
                session.getCorrectCount(),
                problems.stream()
                        .map(problem -> Problem.from(problem, answersByProblemId.get(problem.getAiProblemId())))
                        .toList());
    }

    public record Problem(Long aiProblemId, Integer problemNo, String prompt, String status, String answerText, boolean answered) {
        static Problem from(AiExamProblem problem) {
            return from(problem, null);
        }

        static Problem from(AiExamProblem problem, ExamAnswer answer) {
            String answerText = answer == null || answer.getAnswerText() == null ? "" : answer.getAnswerText();
            boolean answered = !answerText.isBlank();
            return new Problem(
                    problem.getAiProblemId(),
                    problem.getProblemNo(),
                    ExamPromptNormalizer.normalize(problem.getPrompt()),
                    problem.getStatus(),
                    answered ? answerText : ExamStarterCodeResolver.starterCode(problem),
                    answered);
        }
    }
}

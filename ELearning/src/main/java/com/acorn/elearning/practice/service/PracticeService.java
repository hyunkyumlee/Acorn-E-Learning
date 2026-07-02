package com.acorn.elearning.practice.service;

import java.util.List;
import com.acorn.elearning.practice.form.CreatePracticeSetForm;
import com.acorn.elearning.practice.mapper.PracticeProblemMapper;
import com.acorn.elearning.practice.mapper.PracticeSetAttemptMapper;
import com.acorn.elearning.practice.model.PracticeProblem;
import com.acorn.elearning.practice.model.PracticeSetAttempt;
import com.acorn.elearning.practice.view.PracticeSetView;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PracticeService {
   /* public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // List<PracticeProblem> problems = practiceProblemMapper.findAvailable(userId, subjectId);
        // PracticeSetAttempt attempt = PracticeSetAttempt.start(userId, problems);
        // practiceSetAttemptMapper.insert(attempt);
        // return Map.of("attempt", PracticeSetResponse.from(attempt, problems));
        return Map.of("action", action, "status", "SKELETON");
    }
    */
    private final PracticeProblemMapper practiceProblemMapper;
    private final PracticeSetAttemptMapper practiceSetAttemptMapper;

    // 생성자 주입
    public PracticeService(PracticeProblemMapper practiceProblemMapper,
                           PracticeSetAttemptMapper practiceSetAttemptMapper) {
        this.practiceProblemMapper = practiceProblemMapper;
        this.practiceSetAttemptMapper = practiceSetAttemptMapper;
    }

    @Transactional
    public PracticeSetView createPracticeSet(SessionUser user, CreatePracticeSetForm form) {

        // 1. 사용자의 level/subject 기준 문제 10개 조회
        List<PracticeProblem> problems = practiceProblemMapper.findPracticeProblems(
                form.getSubjectId(),
                form.getDifficultyCode()
        );

        // DB에 등록된 문제가 10개 미만일 경우
        if (problems.isEmpty()) {
            throw new IllegalArgumentException("해당 과목과 난이도에 맞는 문제가 없습니다.");
        }

        // 2. practice_set_attempts 생성
        PracticeSetAttempt attempt = new PracticeSetAttempt();
        attempt.setUserId(user.userId());
        attempt.setNodeId(form.getNodeId());
        attempt.setTotalCount(problems.size()); // 10개
        attempt.setCorrectCount(0); // 시작할때 정답은 0개
        attempt.setStatus("IN_PROGRESS"); // 풀이 진행 중
        attempt.setPassed(false); // 아직 통과 못함

        // Mapper를 통해 DB에 Insert (insert 직후 attempt 객체 안에 자동 생성된 PK값이 담김)
        practiceSetAttemptMapper.insertAttempt(attempt);

        // 3. 문제와 선택지를 화면용 ViewModel로 반환
        // (화면에 넘길 때 정답(answerText)은 숨기고 넘기기)
        return PracticeSetView.from(attempt.getSetAttemptId(), problems);
    }
}

package com.acorn.elearning.learning.form;

import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * 레벨 테스트 제출 폼(SR-004, LEVEL-002).
 * - subjectId : 어떤 과목의 레벨 테스트인지(과목 선택이 선행됨).
 * - answers   : 문항별 선택 답안. key=question_id, value=선택한 choice_id.
 *               Thymeleaf에서 answers[${questionId}] 로 바인딩된다. (null이면 미응답)
 * 정답 개수 계산/등급 산정은 Service(LevelTestService.submitAndApply)에서 처리한다.
 */
@Getter
@Setter
public class LevelTestForm {

    @NotNull
    private Long subjectId;

    /** question_id -> choice_id. 바인딩을 위해 빈 Map으로 초기화해 둔다. */
    private Map<Long, Long> answers = new LinkedHashMap<>();
}

package com.acorn.elearning.user.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.acorn.elearning.user.model.UserLearningProfile;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MyPageSummaryResponseTest {

    @Test
    void learningSummaryUsesPrimarySubjectUnlockInsteadOfStaleProfileLevel() {
        UserLearningProfile profile = new UserLearningProfile();
        profile.setPrimarySubjectId(1L);
        profile.setCurrentLevelCode("GOLD");

        MyPageSummaryResponse.LearningSummary summary = MyPageSummaryResponse.LearningSummary.from(
                profile,
                null,
                List.of(),
                List.of(),
                Map.of(1L, List.of(
                        new LearningStatusPageResponse.SubjectLevelProgress("BRONZE", 40, true),
                        new LearningStatusPageResponse.SubjectLevelProgress("SILVER", 0, false),
                        new LearningStatusPageResponse.SubjectLevelProgress("GOLD", 0, false)
                ))
        );

        assertEquals("BRONZE", summary.currentLevelCode());
    }
}

package com.acorn.elearning.user.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.acorn.elearning.learning.model.LearningProgress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LearningStatusPageResponseTest {

    @Test
    void recentItemsUsesSubjectOverviewProgressWhenHigherLevelIsUnlocked() {
        LearningProgress progress = new LearningProgress();
        progress.setSubjectId(1L);
        progress.setUpdatedAt(LocalDateTime.of(2026, 7, 24, 9, 0));

        List<LearningStatusPageResponse.LearningStatusItem> items = LearningStatusPageResponse.recentItems(
                List.of(progress),
                Map.of(1L, List.of(
                        new LearningStatusPageResponse.SubjectLevelProgress("BRONZE", 60, true),
                        new LearningStatusPageResponse.SubjectLevelProgress("SILVER", 0, true),
                        new LearningStatusPageResponse.SubjectLevelProgress("GOLD", 0, false)
                )),
                3
        );

        assertEquals(20, items.get(0).progressRate());
        assertEquals("20%", items.get(0).progressRateLabel());
    }
}

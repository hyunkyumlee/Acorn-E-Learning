    package com.acorn.elearning.ranking.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class RankingScore {
        private Long rankingId;
private Long userId;
private Long subjectId;
private String scopeType;
private String scopeKey;
private String periodType;
private String periodKey;
private Integer score;
private Integer rankNo;
private LocalDateTime calculatedAt;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }

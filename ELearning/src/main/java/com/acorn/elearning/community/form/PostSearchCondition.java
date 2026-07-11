package com.acorn.elearning.community.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostSearchCondition {
    private Long subjectId;
    private String boardType;
    private String keyword;
    private String sort = "latest";
    private String period;
    private int page = 1;
    private int size = 20;

    public int offset() {
        return (normalizedPage() - 1) * normalizedSize();
    }

    public int getOffset() {
        return offset();
    }

    public int normalizedPage() {
        return Math.max(page, 1);
    }

    public int getNormalizedPage() {
        return normalizedPage();
    }

    public int normalizedSize() {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, 50);
    }

    public int getNormalizedSize() {
        return normalizedSize();
    }

    public boolean hotSort() {
        return "hot".equalsIgnoreCase(sort);
    }

    public boolean likeSort() {
        return "likes".equalsIgnoreCase(sort);
    }

    public boolean viewSort() {
        return "views".equalsIgnoreCase(sort);
    }

    public boolean weeklyPeriod() {
        return "week".equalsIgnoreCase(period);
    }

    public boolean monthlyPeriod() {
        return "month".equalsIgnoreCase(period);
    }
}

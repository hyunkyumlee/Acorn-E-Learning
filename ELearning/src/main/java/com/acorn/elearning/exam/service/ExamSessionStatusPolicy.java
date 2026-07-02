package com.acorn.elearning.exam.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.model.ExamSession;

public final class ExamSessionStatusPolicy {
    public static final String CREATED = "CREATED";
    public static final String READY = "READY";
    public static final String FAILED = "FAILED";
    public static final String GRADED = "GRADED";

    private ExamSessionStatusPolicy() {}

    public static boolean graded(ExamSession session) {
        return GRADED.equals(session.getStatus());
    }

    public static void requireEditable(ExamSession session) {
        if (!READY.equals(session.getStatus())) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "답안을 수정할 수 없는 시험 상태입니다.");
        }
    }

    public static void requireRunnable(ExamSession session) {
        if (!READY.equals(session.getStatus())) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "실행 테스트를 할 수 없는 시험 상태입니다.");
        }
    }

    public static void requireSubmittable(ExamSession session) {
        if (!READY.equals(session.getStatus())) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "제출할 수 없는 시험 상태입니다.");
        }
    }
}

package com.acorn.elearning.user.dto.response;

import com.acorn.elearning.exam.model.ExamSession;
import com.acorn.elearning.learning.model.AttendanceRecord;
import com.acorn.elearning.learning.model.LearningProgress;
import com.acorn.elearning.learning.model.LevelTestAttempt;
import com.acorn.elearning.payment.dto.response.PremiumAccessResponse;
import com.acorn.elearning.payment.model.DummyPayment;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.model.User;
import com.acorn.elearning.user.model.UserLearningProfile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public record MyPageSummaryResponse(
        UserSummary user,
        PremiumSummary premium,
        LearningSummary learning,
        ExamSummary exam,
        PaymentSummary latestPayment
) {
    public static MyPageSummaryResponse of(
            SessionUser sessionUser,
            User user,
            PremiumAccessResponse premiumAccess,
            UserLearningProfile learningProfile,
            AttendanceRecord latestAttendance,
            List<AttendanceRecord> attendanceRecords,
            List<LearningProgress> progressItems,
            List<ExamSession> examSessions,
            List<LevelTestAttempt> levelTestAttempts,
            DummyPayment latestPayment
    ) {
        return new MyPageSummaryResponse(
                UserSummary.from(sessionUser, user),
                PremiumSummary.from(premiumAccess),
                LearningSummary.from(learningProfile, latestAttendance, attendanceRecords, progressItems),
                ExamSummary.from(examSessions, levelTestAttempts),
                PaymentSummary.from(latestPayment)
        );
    }

    public record UserSummary(
            Long userId,
            String email,
            String nickname,
            String role,
            String profileImageUrl,
            String roleLabel
    ) {
        public static UserSummary from(SessionUser sessionUser, User user) {
            String email = user == null ? sessionUser.email() : user.getEmail();
            String nickname = user == null ? sessionUser.nickname() : user.getNickname();
            String role = user == null ? sessionUser.role() : user.getRole();
            return new UserSummary(
                    sessionUser.userId(),
                    email,
                    nickname,
                    role,
                    user == null ? null : user.getProfileImageUrl(),
                    roleLabel(role)
            );
        }

        private static String roleLabel(String role) {
            if (SessionUser.ROLE_ADMIN.equals(role)) {
                return "관리자";
            }
            if (SessionUser.ROLE_USER.equals(role)) {
                return "학습자";
            }
            return hasText(role) ? role : "-";
        }
    }

    public record PremiumSummary(
            boolean premiumActive,
            String grantType,
            LocalDateTime grantedAt,
            LocalDateTime expiresAt,
            String statusLabel,
            String grantTypeLabel,
            String grantedAtLabel,
            String expiresAtLabel
    ) {
        public static PremiumSummary from(PremiumAccessResponse premiumAccess) {
            PremiumAccessResponse safeAccess = premiumAccess == null
                    ? PremiumAccessResponse.inactive()
                    : premiumAccess;
            boolean active = safeAccess.premiumActive();
            return new PremiumSummary(
                    active,
                    safeAccess.grantType(),
                    safeAccess.grantedAt(),
                    safeAccess.expiresAt(),
                    active ? "Premium 활성" : "Premium 비활성",
                    grantTypeLabel(safeAccess.grantType()),
                    formatDateTime(safeAccess.grantedAt()),
                    active && safeAccess.expiresAt() == null ? "무제한" : formatDateTime(safeAccess.expiresAt())
            );
        }

        private static String grantTypeLabel(String grantType) {
            if ("LIFETIME".equals(grantType)) {
                return "Lifetime";
            }
            return hasText(grantType) ? grantType : "-";
        }
    }

    public record LearningSummary(
            Long primarySubjectId,
            String learningGoal,
            String currentLevelCode,
            Integer totalScore,
            String gradeCode,
            int streakCount,
            LocalDate latestAttendanceDate,
            int progressCount,
            int completedCount,
            BigDecimal averageProgressRate,
            String totalScoreLabel,
            String streakLabel,
            String latestAttendanceLabel,
            String averageProgressRateLabel,
            String primarySubjectLabel,
            List<String> attendanceDates,
            int initialCalendarYear,
            int initialCalendarMonth,
            String initialCalendarLabel
    ) {
        public static LearningSummary from(
                UserLearningProfile learningProfile,
                AttendanceRecord latestAttendance,
                List<AttendanceRecord> attendanceRecords,
                List<LearningProgress> progressItems
        ) {
            List<LearningProgress> safeProgressItems = progressItems == null ? List.of() : progressItems;
            List<String> safeAttendanceDates = attendanceDates(attendanceRecords);
            LocalDate initialCalendarDate = latestAttendance == null || latestAttendance.getAttendanceDate() == null
                    ? LocalDate.now()
                    : latestAttendance.getAttendanceDate();
            BigDecimal averageProgressRate = averageProgressRate(safeProgressItems);
            int streakCount = latestAttendance == null || latestAttendance.getStreakCount() == null
                    ? 0
                    : latestAttendance.getStreakCount();
            Integer totalScore = learningProfile == null ? null : learningProfile.getTotalScore();

            return new LearningSummary(
                    learningProfile == null ? null : learningProfile.getPrimarySubjectId(),
                    learningProfile == null ? null : learningProfile.getLearningGoal(),
                    learningProfile == null ? null : learningProfile.getCurrentLevelCode(),
                    totalScore,
                    learningProfile == null ? null : learningProfile.getGradeCode(),
                    streakCount,
                    latestAttendance == null ? null : latestAttendance.getAttendanceDate(),
                    safeProgressItems.size(),
                    completedCount(safeProgressItems),
                    averageProgressRate,
                    totalScore == null ? "0점" : totalScore + "점",
                    streakCount + "일",
                    latestAttendance == null ? "-" : formatDate(latestAttendance.getAttendanceDate()),
                    averageProgressRate.setScale(0, RoundingMode.HALF_UP) + "%",
                    subjectLabel(learningProfile == null ? null : learningProfile.getPrimarySubjectId()),
                    safeAttendanceDates,
                    initialCalendarDate.getYear(),
                    initialCalendarDate.getMonthValue(),
                    yearMonthLabel(initialCalendarDate)
            );
        }

        private static List<String> attendanceDates(List<AttendanceRecord> attendanceRecords) {
            if (attendanceRecords == null || attendanceRecords.isEmpty()) {
                return List.of();
            }
            return attendanceRecords.stream()
                    .map(AttendanceRecord::getAttendanceDate)
                    .filter(date -> date != null)
                    .distinct()
                    .sorted(Comparator.naturalOrder())
                    .map(LocalDate::toString)
                    .toList();
        }

        private static int completedCount(List<LearningProgress> progressItems) {
            return (int) progressItems.stream()
                    .filter(item -> Boolean.TRUE.equals(item.getLessonCompleted())
                            || Boolean.TRUE.equals(item.getPracticePassed())
                            || item.getCompletedAt() != null)
                    .count();
        }

        private static BigDecimal averageProgressRate(List<LearningProgress> progressItems) {
            List<BigDecimal> rates = progressItems.stream()
                    .map(LearningProgress::getProgressRate)
                    .filter(rate -> rate != null)
                    .toList();
            if (rates.isEmpty()) {
                return BigDecimal.ZERO;
            }
            BigDecimal total = rates.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            return total.divide(BigDecimal.valueOf(rates.size()), 2, RoundingMode.HALF_UP);
        }
    }

    private static String subjectLabel(Long subjectId) {
        if (subjectId == null) {
            return "설정 안 됨";
        }
        if (subjectId == 1L) {
            return "Java";
        }
        if (subjectId == 2L) {
            return "Python";
        }
        if (subjectId == 3L) {
            return "Web";
        }
        if (subjectId == 4L) {
            return "SQL";
        }
        return "주 과목 #" + subjectId;
    }

    public record ExamSummary(
            List<ExamResultItem> items,
            List<ExamResultItem> previewItems,
            boolean empty
    ) {
        public static ExamSummary from(
                List<ExamSession> examSessions,
                List<LevelTestAttempt> levelTestAttempts
        ) {
            List<ExamResultItem> items = new ArrayList<>();
            if (examSessions != null) {
                examSessions.stream()
                        .map(ExamResultItem::from)
                        .forEach(items::add);
            }
            if (levelTestAttempts != null) {
                levelTestAttempts.stream()
                        .map(ExamResultItem::from)
                        .forEach(items::add);
            }
            List<ExamResultItem> sortedItems = items.stream()
                    .sorted(Comparator.comparing(ExamResultItem::submittedAtForSort).reversed())
                    .toList();
            return new ExamSummary(
                    sortedItems,
                    sortedItems.stream().limit(2).toList(),
                    sortedItems.isEmpty()
            );
        }
    }

    public record ExamResultItem(
            String title,
            String subjectLabel,
            String levelLabel,
            String resultLabel,
            String resultFilter,
            String resultClass,
            String submittedAtLabel,
            String scoreLabel,
            LocalDateTime submittedAtForSort
    ) {
        private static final LocalDateTime EMPTY_DATE = LocalDateTime.MIN;

        public static ExamResultItem from(ExamSession session) {
            boolean passed = "PASSED".equals(session.getResultStatus());
            String levelLabel = levelLabel(session.getLevelCode());
            String subject = MyPageSummaryResponse.subjectLabel(session.getSubjectId());
            return new ExamResultItem(
                    subject + " " + levelLabel,
                    subject,
                    levelLabel,
                    resultLabel(passed),
                    passed ? "PASS" : "RETRY",
                    passed ? "pass" : "retry",
                    formatDate(session.getSubmittedAt()),
                    scoreLabel(session.getCorrectCount(), session.getTotalProblemCount()),
                    session.getSubmittedAt() == null ? EMPTY_DATE : session.getSubmittedAt()
            );
        }

        public static ExamResultItem from(LevelTestAttempt attempt) {
            boolean passed = passed(attempt.getCorrectCount(), attempt.getTotalCount());
            String levelLabel = levelLabel(attempt.getResultLevelCode());
            String subject = MyPageSummaryResponse.subjectLabel(attempt.getSubjectId());
            return new ExamResultItem(
                    subject + " " + levelLabel,
                    subject,
                    levelLabel,
                    resultLabel(passed),
                    passed ? "PASS" : "RETRY",
                    passed ? "pass" : "retry",
                    formatDate(attempt.getSubmittedAt()),
                    scoreLabel(attempt.getCorrectCount(), attempt.getTotalCount()),
                    attempt.getSubmittedAt() == null ? EMPTY_DATE : attempt.getSubmittedAt()
            );
        }

        private static boolean passed(Integer correctCount, Integer totalCount) {
            if (correctCount == null || totalCount == null || totalCount == 0) {
                return false;
            }
            return correctCount * 100 >= totalCount * 60;
        }

        private static String resultLabel(boolean passed) {
            return passed ? "합격" : "재시";
        }

        private static String levelLabel(String levelCode) {
            return hasText(levelCode) ? "Lv." + levelCode : "Lv.-";
        }

        private static String scoreLabel(Integer correctCount, Integer totalCount) {
            int correct = correctCount == null ? 0 : correctCount;
            int total = totalCount == null ? 0 : totalCount;
            return correct + "/" + total;
        }
    }

    public record PaymentSummary(
            Long paymentId,
            String orderNo,
            String paymentMethod,
            String paymentStatus,
            BigDecimal amount,
            LocalDateTime paidAt,
            String paymentMethodLabel,
            String paymentStatusLabel,
            String amountLabel,
            String paidAtLabel
    ) {
        public static PaymentSummary from(DummyPayment payment) {
            if (payment == null) {
                return empty();
            }
            return new PaymentSummary(
                    payment.getPaymentId(),
                    payment.getOrderNo(),
                    payment.getPaymentMethod(),
                    payment.getPaymentStatus(),
                    payment.getAmount(),
                    payment.getPaidAt(),
                    paymentMethodLabel(payment.getPaymentMethod()),
                    paymentStatusLabel(payment.getPaymentStatus()),
                    amountLabel(payment.getAmount()),
                    formatDateTime(payment.getPaidAt())
            );
        }

        private static PaymentSummary empty() {
            return new PaymentSummary(null, null, null, null, null, null, "-", "-", "0원", "-");
        }

        private static String paymentMethodLabel(String paymentMethod) {
            if ("CARD".equals(paymentMethod)) {
                return "신용카드";
            }
            if ("BANK_TRANSFER".equals(paymentMethod)) {
                return "무통장 입금";
            }
            return hasText(paymentMethod) ? paymentMethod : "-";
        }

        private static String paymentStatusLabel(String paymentStatus) {
            if ("PAID".equals(paymentStatus)) {
                return "결제 완료";
            }
            if ("READY".equals(paymentStatus)) {
                return "결제 대기";
            }
            if ("FAILED".equals(paymentStatus)) {
                return "결제 실패";
            }
            return hasText(paymentStatus) ? paymentStatus : "-";
        }

        private static String amountLabel(BigDecimal amount) {
            if (amount == null) {
                return "0원";
            }
            return NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원";
        }
    }

    private static String formatDate(LocalDate value) {
        return value == null ? "-" : value.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
    }

    private static String formatDate(LocalDateTime value) {
        return value == null ? "-" : value.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }

    private static String yearMonthLabel(LocalDate value) {
        return value == null ? "-" : value.format(DateTimeFormatter.ofPattern("yyyy년 M월"));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

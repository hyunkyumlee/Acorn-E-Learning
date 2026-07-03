package com.acorn.elearning.user.dto.response;

import com.acorn.elearning.learning.model.LearningProgress;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record LearningStatusPageResponse(
        List<SubjectFilter> filters,
        List<LearningStatusItem> items,
        String selectedSubject,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {
    private static final String ALL_SUBJECTS = "ALL";
    private static final List<SubjectMeta> KNOWN_SUBJECTS = List.of(
            new SubjectMeta(1L, "JAVA", "Java", "J", "java", "/assets/images/icons/subject/java.png"),
            new SubjectMeta(2L, "PYTHON", "Python", "Py", "python", "/assets/images/icons/subject/python.png"),
            new SubjectMeta(3L, "WEB", "Web", "Web", "web", "/assets/images/icons/subject/web.png"),
            new SubjectMeta(4L, "SQL", "SQL", "SQL", "sql", "/assets/images/icons/subject/sql.png")
    );

    public static LearningStatusPageResponse of(
            List<LearningProgress> progressItems,
            String subject,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        String selectedSubject = normalizeSubject(subject);
        Map<Long, List<LearningProgress>> progressBySubject = progressBySubject(progressItems);
        List<SubjectMeta> subjects = subjects(progressBySubject);
        List<SubjectFilter> filters = filters(subjects, selectedSubject);
        List<LearningStatusItem> allItems = subjects.stream()
                .filter(meta -> ALL_SUBJECTS.equals(selectedSubject) || meta.code().equals(selectedSubject))
                .map(meta -> LearningStatusItem.from(meta, progressBySubject.getOrDefault(meta.subjectId(), List.of())))
                .toList();

        int totalElements = allItems.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);
        List<LearningStatusItem> pageItems = fromIndex >= toIndex ? List.of() : allItems.subList(fromIndex, toIndex);

        return new LearningStatusPageResponse(
                filters,
                pageItems,
                selectedSubject,
                safePage,
                safeSize,
                totalElements,
                totalPages,
                safePage == 0,
                totalPages == 0 || safePage >= totalPages - 1,
                pageItems.isEmpty()
        );
    }

    private static Map<Long, List<LearningProgress>> progressBySubject(List<LearningProgress> progressItems) {
        Map<Long, List<LearningProgress>> result = new LinkedHashMap<>();
        if (progressItems == null || progressItems.isEmpty()) {
            return result;
        }
        progressItems.stream()
                .filter(progress -> progress.getSubjectId() != null)
                .sorted(Comparator
                        .comparing(LearningProgress::getSubjectId)
                        .thenComparing(progress -> progress.getNodeId() == null ? Long.MAX_VALUE : progress.getNodeId()))
                .forEach(progress -> result
                        .computeIfAbsent(progress.getSubjectId(), ignored -> new ArrayList<>())
                        .add(progress));
        return result;
    }

    private static List<SubjectMeta> subjects(Map<Long, List<LearningProgress>> progressBySubject) {
        Map<Long, SubjectMeta> result = new LinkedHashMap<>();
        KNOWN_SUBJECTS.forEach(subject -> result.put(subject.subjectId(), subject));
        progressBySubject.keySet().forEach(subjectId ->
                result.putIfAbsent(subjectId, new SubjectMeta(
                        subjectId,
                        "SUBJECT_" + subjectId,
                        "과목 #" + subjectId,
                        String.valueOf(subjectId),
                        "default",
                        null
                )));
        return List.copyOf(result.values());
    }

    private static List<SubjectFilter> filters(List<SubjectMeta> subjects, String selectedSubject) {
        List<SubjectFilter> result = new ArrayList<>();
        result.add(new SubjectFilter(ALL_SUBJECTS, "전체", ALL_SUBJECTS.equals(selectedSubject)));
        subjects.forEach(subject -> result.add(new SubjectFilter(
                subject.code(),
                subject.name(),
                subject.code().equals(selectedSubject)
        )));
        return List.copyOf(result);
    }

    private static String normalizeSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            return ALL_SUBJECTS;
        }
        String normalized = subject.trim().toUpperCase();
        boolean known = KNOWN_SUBJECTS.stream().anyMatch(meta -> meta.code().equals(normalized));
        if (ALL_SUBJECTS.equals(normalized) || known || normalized.startsWith("SUBJECT_")) {
            return normalized;
        }
        return ALL_SUBJECTS;
    }

    public int previousPage() {
        return Math.max(page - 1, 0);
    }

    public int nextPage() {
        if (totalPages == 0) {
            return 0;
        }
        return Math.min(page + 1, totalPages - 1);
    }

    public List<Integer> pageNumbers() {
        int visiblePages = Math.max(totalPages, 1);
        return java.util.stream.IntStream.range(0, visiblePages)
                .boxed()
                .toList();
    }

    public record SubjectFilter(
            String subject,
            String label,
            boolean active
    ) {}

    public record LearningStatusItem(
            Long subjectId,
            String subjectCode,
            String subjectName,
            String thumbLabel,
            String thumbClass,
            String thumbIconPath,
            String currentLevelLabel,
            int progressRate,
            String progressRateLabel,
            String continueUrl
    ) {
        public static LearningStatusItem from(SubjectMeta subject, List<LearningProgress> progressItems) {
            List<LearningProgress> rows = progressItems == null ? List.of() : progressItems;
            int progressRate = averageProgressRate(rows);
            int level = Math.max(1, completedCount(rows) + 1);
            return new LearningStatusItem(
                    subject.subjectId(),
                    subject.code(),
                    subject.name(),
                    subject.thumbLabel(),
                    subject.thumbClass(),
                    subject.thumbIconPath(),
                    levelLabel(level),
                    progressRate,
                    progressRate + "%",
                    "/learning?subjectId=" + subject.subjectId()
            );
        }

        private static String levelLabel(int level) {
            if (level >= 6) {
                return "GOLD";
            }
            if (level >= 3) {
                return "SILVER";
            }
            return "BRONZE";
        }

        private static int completedCount(List<LearningProgress> progressItems) {
            return (int) progressItems.stream()
                    .filter(item -> Boolean.TRUE.equals(item.getLessonCompleted())
                            || Boolean.TRUE.equals(item.getPracticePassed())
                            || item.getCompletedAt() != null)
                    .count();
        }

        private static int averageProgressRate(List<LearningProgress> progressItems) {
            List<BigDecimal> rates = progressItems.stream()
                    .map(LearningProgress::getProgressRate)
                    .filter(rate -> rate != null)
                    .toList();
            if (rates.isEmpty()) {
                return 0;
            }
            BigDecimal total = rates.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            return total.divide(BigDecimal.valueOf(rates.size()), 0, RoundingMode.HALF_UP).intValue();
        }
    }

    private record SubjectMeta(
            Long subjectId,
            String code,
            String name,
            String thumbLabel,
            String thumbClass,
            String thumbIconPath
    ) {}
}

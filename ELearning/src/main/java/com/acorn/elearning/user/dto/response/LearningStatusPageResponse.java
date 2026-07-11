package com.acorn.elearning.user.dto.response;

import com.acorn.elearning.learning.model.LearningProgress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record LearningStatusPageResponse(
        List<SubjectFilter> filters,
        List<LearningStatusItem> items,
        String selectedSubject,
        boolean levelDetail,
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
        return of(progressItems, Map.of(), subject, page, size);
    }

    public static LearningStatusPageResponse of(
            List<LearningProgress> progressItems,
            Map<Long, List<SubjectLevelProgress>> progressBySubjectLevel,
            String subject,
            int page,
            int size
    ) {
        String selectedSubject = normalizeSubject(subject);
        Map<Long, List<LearningProgress>> progressBySubject = progressBySubject(progressItems);
        List<SubjectMeta> subjects = subjects(progressBySubject);
        List<SubjectFilter> filters = filters(subjects, selectedSubject);
        boolean levelDetail = !ALL_SUBJECTS.equals(selectedSubject);
        List<LearningStatusItem> allItems = levelDetail
                ? subjects.stream()
                        .filter(meta -> meta.code().equals(selectedSubject))
                        .flatMap(meta -> levelItems(
                                meta,
                                progressBySubjectLevel == null ? null : progressBySubjectLevel.get(meta.subjectId())
                        ).stream())
                        .toList()
                : subjects.stream()
                        .map(meta -> LearningStatusItem.from(
                                meta,
                                progressBySubject.getOrDefault(meta.subjectId(), List.of()),
                                currentLevelProgress(progressBySubjectLevel == null ? null : progressBySubjectLevel.get(meta.subjectId()))
                        ))
                        .toList();

        int totalElements = allItems.size();
        int totalPages = totalElements == 0 ? 0 : 1;

        return new LearningStatusPageResponse(
                filters,
                allItems,
                selectedSubject,
                levelDetail,
                0,
                Math.max(totalElements, 1),
                totalElements,
                totalPages,
                true,
                true,
                allItems.isEmpty()
        );
    }

    public static List<LearningStatusItem> recentItems(List<LearningProgress> progressItems, int limit) {
        return recentItems(progressItems, Map.of(), limit);
    }

    public static List<LearningStatusItem> recentItems(
            List<LearningProgress> progressItems,
            Map<Long, List<SubjectLevelProgress>> progressBySubjectLevel,
            int limit
    ) {
        int safeLimit = Math.max(limit, 0);
        if (safeLimit == 0) {
            return List.of();
        }

        Map<Long, List<LearningProgress>> progressBySubject = progressBySubject(progressItems);
        if (progressBySubject.isEmpty()) {
            return List.of();
        }

        return subjects(progressBySubject).stream()
                .filter(meta -> progressBySubject.containsKey(meta.subjectId()))
                .sorted(Comparator
                        .comparing(
                                (SubjectMeta meta) -> latestActivityAt(progressBySubject.get(meta.subjectId())),
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                        .thenComparing(SubjectMeta::subjectId))
                .limit(safeLimit)
                .map(meta -> LearningStatusItem.from(
                        meta,
                        progressBySubject.getOrDefault(meta.subjectId(), List.of()),
                        currentLevelProgress(progressBySubjectLevel == null ? null : progressBySubjectLevel.get(meta.subjectId()))
                ))
                .toList();
    }

    private static List<LearningStatusItem> levelItems(
            SubjectMeta subject,
            List<SubjectLevelProgress> levelProgressItems
    ) {
        List<SubjectLevelProgress> levels = normalizedLevelProgresses(levelProgressItems);
        SubjectLevelProgress currentLevel = currentLevelProgress(levels);
        return levels.stream()
                .map(levelProgress -> LearningStatusItem.from(
                        subject,
                        List.of(),
                        levelProgress,
                        sameLevel(levelProgress, currentLevel)
                ))
                .toList();
    }

    private static SubjectLevelProgress currentLevelProgress(List<SubjectLevelProgress> levelProgressItems) {
        List<SubjectLevelProgress> levels = normalizedLevelProgresses(levelProgressItems);
        SubjectLevelProgress selected = levels.stream()
                .filter(SubjectLevelProgress::unlocked)
                .findFirst()
                .orElse(levels.get(0));
        for (SubjectLevelProgress level : levels) {
            if (!level.unlocked()) {
                continue;
            }
            selected = level;
            if (level.progressRate() < 100) {
                break;
            }
        }
        return selected;
    }

    private static List<SubjectLevelProgress> normalizedLevelProgresses(List<SubjectLevelProgress> levelProgressItems) {
        Map<String, SubjectLevelProgress> progressByLevel = new LinkedHashMap<>();
        if (levelProgressItems != null) {
            levelProgressItems.forEach(levelProgress -> {
                String levelCode = normalizeLevelCode(levelProgress == null ? null : levelProgress.levelCode());
                if (!levelCode.isBlank()) {
                    progressByLevel.put(levelCode, new SubjectLevelProgress(
                            levelCode,
                            progressRate(levelProgress == null ? 0 : levelProgress.progressRate()),
                            levelProgress != null && levelProgress.unlocked()
                    ));
                }
            });
        }
        return List.of("BRONZE", "SILVER", "GOLD").stream()
                .map(levelCode -> progressByLevel.getOrDefault(
                        levelCode,
                        new SubjectLevelProgress(levelCode, 0, false)
                ))
                .toList();
    }

    private static boolean sameLevel(SubjectLevelProgress first, SubjectLevelProgress second) {
        if (first == null || second == null) {
            return false;
        }
        return normalizeLevelCode(first.levelCode()).equals(normalizeLevelCode(second.levelCode()));
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

    private static String normalizeLevelCode(String levelCode) {
        return levelCode == null ? "" : levelCode.trim().toUpperCase();
    }

    private static int progressRate(int progressRate) {
        return Math.max(0, Math.min(100, progressRate));
    }

    private static LocalDateTime latestActivityAt(List<LearningProgress> progressItems) {
        if (progressItems == null || progressItems.isEmpty()) {
            return null;
        }
        return progressItems.stream()
                .map(LearningStatusPageResponse::activityAt)
                .filter(time -> time != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private static LocalDateTime activityAt(LearningProgress progress) {
        if (progress == null) {
            return null;
        }
        if (progress.getUpdatedAt() != null) {
            return progress.getUpdatedAt();
        }
        if (progress.getCompletedAt() != null) {
            return progress.getCompletedAt();
        }
        return progress.getCreatedAt();
    }

    public record SubjectFilter(
            String subject,
            String label,
            boolean active
    ) {}

    public record SubjectLevelProgress(
            String levelCode,
            int progressRate,
            boolean unlocked
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
            boolean locked,
            boolean currentLevel,
            String levelStatusLabel,
            String continueUrl
    ) {
        public static LearningStatusItem from(SubjectMeta subject, List<LearningProgress> progressItems) {
            return from(subject, progressItems, null);
        }

        public static LearningStatusItem from(
                SubjectMeta subject,
                List<LearningProgress> progressItems,
                SubjectLevelProgress levelProgress
        ) {
            return from(subject, progressItems, levelProgress, false);
        }

        public static LearningStatusItem from(
                SubjectMeta subject,
                List<LearningProgress> progressItems,
                SubjectLevelProgress levelProgress,
                boolean currentLevel
        ) {
            boolean locked = levelProgress != null && !levelProgress.unlocked();
            int progressRate = locked ? 0 : progressRate(levelProgress);
            return new LearningStatusItem(
                    subject.subjectId(),
                    subject.code(),
                    subject.name(),
                    subject.thumbLabel(),
                    subject.thumbClass(),
                    subject.thumbIconPath(),
                    levelLabel(levelProgress),
                    progressRate,
                    progressRate + "%",
                    locked,
                    currentLevel,
                    statusLabel(locked, currentLevel),
                    continueUrl(subject, levelProgress)
            );
        }

        private static int progressRate(SubjectLevelProgress levelProgress) {
            if (levelProgress == null) {
                return 0;
            }
            return Math.max(0, Math.min(100, levelProgress.progressRate()));
        }

        private static String levelLabel(SubjectLevelProgress levelProgress) {
            String levelCode = levelProgress == null ? null : levelProgress.levelCode();
            if (levelCode == null || levelCode.isBlank()) {
                return "-";
            }
            String normalized = levelCode.trim().toUpperCase();
            return switch (normalized) {
                case "BRONZE", "SILVER", "GOLD" -> normalized;
                default -> normalized;
            };
        }

        private static String statusLabel(boolean locked, boolean currentLevel) {
            if (locked) {
                return "잠김";
            }
            if (currentLevel) {
                return "진행 중";
            }
            return null;
        }

        private static String continueUrl(SubjectMeta subject, SubjectLevelProgress levelProgress) {
            String levelCode = normalizeLevelCode(levelProgress == null ? null : levelProgress.levelCode());
            String url = "/learning?subjectId=" + subject.subjectId();
            if (!levelCode.isBlank()) {
                url += "&levelCode=" + levelCode;
            }
            return url;
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

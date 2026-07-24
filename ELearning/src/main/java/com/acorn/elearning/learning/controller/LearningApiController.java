package com.acorn.elearning.learning.controller;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.learning.dto.request.LevelTestSubmitRequest;
import com.acorn.elearning.learning.dto.response.CurriculumResponse;
import com.acorn.elearning.learning.dto.response.LearningDashboardResponse;
import com.acorn.elearning.learning.dto.response.LessonBookmarkPageResponse;
import com.acorn.elearning.learning.dto.response.LessonBookmarkResponse;
import com.acorn.elearning.learning.dto.response.LessonDetailResponse;
import com.acorn.elearning.learning.dto.response.LevelTestQuestionListResponse;
import com.acorn.elearning.learning.dto.response.LevelTestResultResponse;
import com.acorn.elearning.learning.dto.response.ProgressUpdateResponse;
import com.acorn.elearning.learning.dto.response.SubjectListResponse;
import com.acorn.elearning.learning.form.LevelTestForm;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.Subject;
import com.acorn.elearning.learning.service.AttendanceService;
import com.acorn.elearning.learning.service.CurriculumService;
import com.acorn.elearning.learning.service.LearningService;
import com.acorn.elearning.learning.service.LessonService;
import com.acorn.elearning.learning.service.LevelTestService;
import com.acorn.elearning.learning.service.ProgressService;
import com.acorn.elearning.learning.view.LearningDashboardView;
import com.acorn.elearning.learning.view.LessonProgressView;
import com.acorn.elearning.learning.view.LevelTestResultView;
import com.acorn.elearning.practice.service.WrongAnswerService;
import com.acorn.elearning.ranking.service.RankingService;
import com.acorn.elearning.security.SessionUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
public class LearningApiController {

    /** 로드맵 fallback 기본 과목(JAVA, subject_id=1). 선택 과목·주 과목이 모두 없을 때만 사용. */
    private static final Long DEFAULT_SUBJECT_ID = 1L;

    private final LearningService learningService;
    private final CurriculumService curriculumService;
    private final LessonService lessonService;
    private final ProgressService progressService;
    private final AttendanceService attendanceService;
    private final LevelTestService levelTestService;
    private final WrongAnswerService wrongAnswerService;
    private final RankingService rankingService;

    public LearningApiController(LearningService learningService,
                                 CurriculumService curriculumService,
                                 LessonService lessonService,
                                 ProgressService progressService,
                                 AttendanceService attendanceService,
                                 LevelTestService levelTestService,
                                 WrongAnswerService wrongAnswerService,
                                 RankingService rankingService) {
        this.learningService = learningService;
        this.curriculumService = curriculumService;
        this.lessonService = lessonService;
        this.progressService = progressService;
        this.attendanceService = attendanceService;
        this.levelTestService = levelTestService;
        this.wrongAnswerService = wrongAnswerService;
        this.rankingService = rankingService;
    }

    @GetMapping("/api/subjects")
    public ApiResponse<SubjectListResponse> subjects(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly) {
        SessionUser user = resolve(sessionUser);
        // 버그 #36: 일반 회원이 activeOnly=false로 비활성 과목까지 조회 가능했음 — 관리자만 전체 조회 허용
        boolean effectiveActiveOnly = activeOnly || !user.admin();
        List<Subject> source = effectiveActiveOnly
                ? learningService.getActiveSubjects()
                : learningService.getAllSubjects();
        List<SubjectListResponse.Item> items = source.stream()
                .map(this::toSubjectItem)
                .toList();
        return ApiResponse.success(new SubjectListResponse(items));
    }

    @GetMapping("/api/learning/dashboard")
    public ApiResponse<LearningDashboardResponse> dashboard(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId", required = false) Long subjectId) {
        SessionUser user = resolve(sessionUser);

        LearningDashboardView home = learningService.getLearningHome(user, subjectId);
        List<Subject> subjects = learningService.getActiveSubjects();

        Long roadmapSubjectId = (subjectId != null) ? subjectId
                : (home.primarySubjectId() != null ? home.primarySubjectId() : DEFAULT_SUBJECT_ID);
        List<CurriculumNode> roadmap = curriculumService.getRoadmap(roadmapSubjectId);
        ProgressService.RoadmapProgress progress =
                progressService.computeRoadmapProgress(user.userId(), roadmapSubjectId, roadmap);
        boolean[] weekly = attendanceService.getWeeklyAttendance(user.userId());

        LearningDashboardResponse response = new LearningDashboardResponse(
                new LearningDashboardResponse.Profile(
                        home.nickname(), home.currentLevelCode(), home.gradeCode(), home.totalScore()),
                subjects.stream().map(this::toSubjectItem).toList(),
                new LearningDashboardResponse.RoadmapSummary(
                        roadmapSubjectId, subjectCodeOf(subjects, roadmapSubjectId),
                        progress.planetCount(), progress.completedPlanets()),
                new LearningDashboardResponse.ProgressSummary(progress.progressPercent()),
                new LearningDashboardResponse.Attendance(home.streakCount(), home.attendedToday(), weekly),
                rankingService.myRanking(user, null,"WEEKLY").data().get("mySummary"), // rankingSummary: 주간 통합 내 랭킹
                wrongAnswerService.summary(user));      // wrongAnswerSummary: 별도 도메인 read 호출
        return ApiResponse.success(response);
    }

    @GetMapping("/api/subjects/{subjectId}/curriculum")
    public ApiResponse<CurriculumResponse> curriculum(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long subjectId,
            @RequestParam(name = "levelCode", required = false) String levelCode) {
        SessionUser user = resolve(sessionUser);
        return ApiResponse.success(
                curriculumService.getCurriculumResponse(user.userId(), subjectId, levelCode));
    }

    @GetMapping("/api/lessons/{lessonId}")
    public ApiResponse<LessonDetailResponse> lesson(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long lessonId) {
        SessionUser user = resolve(sessionUser);
        return ApiResponse.success(lessonService.getLessonDetailResponse(user, lessonId));
    }

    @PostMapping("/api/lessons/{lessonId}/complete")
    public ApiResponse<ProgressUpdateResponse> completeLesson(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long lessonId) {
        SessionUser user = resolve(sessionUser);
        LessonProgressView view = lessonService.completeLesson(user, lessonId);
        return ApiResponse.success(ProgressUpdateResponse.from(view));
    }

    @PostMapping("/api/lessons/{lessonId}/bookmark")
    public ApiResponse<LessonBookmarkResponse> bookmark(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long lessonId) {
        return ApiResponse.success(lessonService.addBookmark(resolve(sessionUser), lessonId));
    }

    @DeleteMapping("/api/lessons/{lessonId}/bookmark")
    public ApiResponse<LessonBookmarkResponse> deleteBookmark(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long lessonId) {
        return ApiResponse.success(lessonService.removeBookmark(resolve(sessionUser), lessonId));
    }

    @GetMapping("/api/lessons/bookmarks")
    public ApiResponse<LessonBookmarkPageResponse> bookmarks(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ApiResponse.success(lessonService.getBookmarks(resolve(sessionUser), subjectId, page, size));
    }

    @GetMapping("/api/level-tests/questions")
    public ApiResponse<LevelTestQuestionListResponse> levelQuestions(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId") Long subjectId) {
        resolve(sessionUser);
        return ApiResponse.success(
                LevelTestQuestionListResponse.of(levelTestService.getQuestions(subjectId)));
    }

    @PostMapping("/api/level-tests/attempts")
    public ApiResponse<LevelTestResultResponse> submitLevelTest(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @RequestBody LevelTestSubmitRequest request) {
        SessionUser user = resolve(sessionUser);

        LevelTestForm form = new LevelTestForm();
        form.setSubjectId(request.subjectId());
        form.setAnswers(request.toAnswerMap());

        LevelTestResultView result = levelTestService.submitAndApply(user, form);
        List<String> unlockedLevels =
                levelTestService.getUnlockedLevelCodes(user.userId(), result.subjectId());
        return ApiResponse.success(LevelTestResultResponse.of(result, unlockedLevels));
    }

    /**
     * 로그인 세션이 없으면 401로 끊는다.
     * 이 API들은 화면 경로와 달리 로그인 인터셉터가 지키지 않으므로, 여기서 세션을 요구하지 않으면
     * 로그인하지 않은 요청이 특정 사용자의 학습 데이터를 읽고 그 사용자 이름으로 진행 기록을 남긴다.
     */
    private SessionUser resolve(SessionUser sessionUser) {
        if (sessionUser == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser;
    }

    private SubjectListResponse.Item toSubjectItem(Subject subject) {
        return new SubjectListResponse.Item(
                subject.getSubjectId(), subject.getSubjectCode(),
                subject.getSubjectName(), subject.getDescription());
    }

    private String subjectCodeOf(List<Subject> subjects, Long subjectId) {
        return subjects.stream()
                .filter(subject -> subjectId.equals(subject.getSubjectId()))
                .map(Subject::getSubjectCode)
                .findFirst()
                .orElse("-");
    }
}

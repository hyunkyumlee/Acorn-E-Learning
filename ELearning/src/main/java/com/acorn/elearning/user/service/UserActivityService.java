package com.acorn.elearning.user.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.community.mapper.CommunityPostMapper;
import com.acorn.elearning.community.mapper.PostLikeMapper;
import com.acorn.elearning.community.mapper.PostScrapMapper;
import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.exam.mapper.ExamSessionMapper;
import com.acorn.elearning.exam.model.ExamSession;
import com.acorn.elearning.learning.mapper.AttendanceRecordMapper;
import com.acorn.elearning.learning.mapper.LearningProfileReadMapper;
import com.acorn.elearning.learning.mapper.LearningProgressMapper;
import com.acorn.elearning.learning.mapper.LevelTestAttemptMapper;
import com.acorn.elearning.learning.model.AttendanceRecord;
import com.acorn.elearning.learning.model.LearningProgress;
import com.acorn.elearning.learning.model.LevelTestAttempt;
import com.acorn.elearning.payment.dto.response.PremiumAccessResponse;
import com.acorn.elearning.payment.mapper.DummyPaymentMapper;
import com.acorn.elearning.payment.model.DummyPayment;
import com.acorn.elearning.payment.model.PaymentHistoryItem;
import com.acorn.elearning.payment.service.PaymentAccessService;
import com.acorn.elearning.payment.view.PaymentHistoryView;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.dto.response.CommunityActivityPageResponse;
import com.acorn.elearning.user.dto.response.MyPageSummaryResponse;
import com.acorn.elearning.user.dto.response.LearningStatusPageResponse;
import com.acorn.elearning.user.dto.response.PaymentHistoryPageResponse;
import com.acorn.elearning.user.mapper.UserMapper;
import com.acorn.elearning.user.model.User;
import com.acorn.elearning.user.model.UserLearningProfile;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserActivityService {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final DateTimeFormatter COMMUNITY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final DummyPaymentMapper dummyPaymentMapper;
    private final PaymentAccessService paymentAccessService;
    private final UserMapper userMapper;
    private final CommunityPostMapper communityPostMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostScrapMapper postScrapMapper;
    private final LearningProfileReadMapper learningProfileReadMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final LearningProgressMapper learningProgressMapper;
    private final LevelTestAttemptMapper levelTestAttemptMapper;
    private final ExamSessionMapper examSessionMapper;

    public UserActivityService(
            DummyPaymentMapper dummyPaymentMapper,
            PaymentAccessService paymentAccessService,
            UserMapper userMapper,
            CommunityPostMapper communityPostMapper,
            PostLikeMapper postLikeMapper,
            PostScrapMapper postScrapMapper,
            LearningProfileReadMapper learningProfileReadMapper,
            AttendanceRecordMapper attendanceRecordMapper,
            LearningProgressMapper learningProgressMapper,
            LevelTestAttemptMapper levelTestAttemptMapper,
            ExamSessionMapper examSessionMapper
    ) {
        this.dummyPaymentMapper = dummyPaymentMapper;
        this.paymentAccessService = paymentAccessService;
        this.userMapper = userMapper;
        this.communityPostMapper = communityPostMapper;
        this.postLikeMapper = postLikeMapper;
        this.postScrapMapper = postScrapMapper;
        this.learningProfileReadMapper = learningProfileReadMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.learningProgressMapper = learningProgressMapper;
        this.levelTestAttemptMapper = levelTestAttemptMapper;
        this.examSessionMapper = examSessionMapper;
    }

    @Transactional(readOnly = true)
    public MyPageSummaryResponse mypage(SessionUser sessionUser) {
        Long userId = requireUserId(sessionUser);
        User user = userMapper.findById(userId).orElse(null);
        PremiumAccessResponse premiumAccess = paymentAccessService.premiumAccess(userId);
        UserLearningProfile learningProfile = learningProfileReadMapper.findByUserId(userId).orElse(null);
        AttendanceRecord latestAttendance = attendanceRecordMapper.findLatestByUserId(userId).orElse(null);
        List<AttendanceRecord> attendanceRecords = attendanceRecordMapper.findAll().stream()
                .filter(record -> userId.equals(record.getUserId()))
                .toList();
        List<LearningProgress> progressItems = learningProfile == null || learningProfile.getPrimarySubjectId() == null
                ? List.of()
                : learningProgressMapper.findByUserIdAndSubjectId(userId, learningProfile.getPrimarySubjectId());
        List<ExamSession> examSessions = examSessionMapper.findByUserId(userId);
        List<LevelTestAttempt> levelTestAttempts = levelTestAttemptMapper.findAll().stream()
                .filter(attempt -> userId.equals(attempt.getUserId()))
                .toList();
        DummyPayment latestPayment = dummyPaymentMapper.findLatestByUserId(userId).orElse(null);

        return MyPageSummaryResponse.of(
                sessionUser,
                user,
                premiumAccess,
                learningProfile,
                latestAttendance,
                attendanceRecords,
                progressItems,
                examSessions,
                levelTestAttempts,
                latestPayment
        );
    }

    @Transactional(readOnly = true)
    public PaymentHistoryPageResponse payments(SessionUser sessionUser) {
        return payments(sessionUser, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    @Transactional(readOnly = true)
    public LearningStatusPageResponse learningStatus(SessionUser sessionUser, String subject, int page) {
        Long userId = requireUserId(sessionUser);
        int safePage = normalizePage(page);
        List<LearningProgress> progressItems = learningProgressMapper.findAll().stream()
                .filter(progress -> userId.equals(progress.getUserId()))
                .toList();
        return LearningStatusPageResponse.of(progressItems, subject, safePage, DEFAULT_SIZE);
    }

    @Transactional(readOnly = true)
    public CommunityActivityPageResponse communityActivity(SessionUser sessionUser, String type, String category, String query, int page) {
        Long userId = requireUserId(sessionUser);
        String safeType = normalizeCommunityActivityType(type);
        List<CommunityPost> posts = switch (safeType) {
            case "SCRAPS" -> postScrapMapper.findPostsByUserId(userId);
            case "POSTS" -> communityPostMapper.findByWriterId(userId);
            default -> postLikeMapper.findPostsByUserId(userId);
        };
        return CommunityActivityPageResponse.of(
                safeType,
                category,
                query,
                normalizePage(page),
                posts.stream()
                        .map(post -> toCommunityPostItem(safeType, post))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public PaymentHistoryPageResponse payments(SessionUser sessionUser, int page, int size) {
        Long userId = requireUserId(sessionUser);
        int safePage = normalizePage(page);
        int safeSize = normalizeSize(size);
        long totalElements = dummyPaymentMapper.countHistoryByUserId(userId);
        List<PaymentHistoryItem> historyItems = totalElements == 0
                ? List.of()
                : dummyPaymentMapper.findHistoryByUserId(userId, offset(safePage, safeSize), safeSize);
        PremiumAccessResponse premiumAccess = paymentAccessService.premiumAccess(userId);

        return PaymentHistoryPageResponse.of(
                premiumAccess,
                historyItems,
                safePage,
                safeSize,
                totalElements
        );
    }

    @Transactional(readOnly = true)
    public PaymentHistoryView paymentHistoryView(SessionUser sessionUser, int page, int size) {
        return PaymentHistoryView.from(payments(sessionUser, page, size));
    }

    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // Long userId = sessionUser.userId();
        // User user = userMapper.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // userMapper.update(applyForm(user, form));
        // return Map.of("user", UserProfileResponse.from(user));
        return Map.of("action", action, "status", "SKELETON");
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }

    private int normalizePage(int page) {
        return Math.max(page, DEFAULT_PAGE);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private int offset(int page, int size) {
        long offset = (long) page * size;
        return offset > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) offset;
    }

    private String normalizeCommunityActivityType(String type) {
        if ("SCRAPS".equalsIgnoreCase(type) || "POSTS".equalsIgnoreCase(type)) {
            return type.toUpperCase(Locale.ROOT);
        }
        return "LIKED";
    }

    private CommunityActivityPageResponse.PostItem toCommunityPostItem(String type, CommunityPost post) {
        Long postId = post.getPostId();
        String category = normalizeBoardType(post.getBoardType());
        String categoryLabel = boardLabel(category);
        boolean writtenPost = "POSTS".equals(type);
        return new CommunityActivityPageResponse.PostItem(
                postId,
                category,
                categoryLabel,
                safeTitle(post.getTitle()),
                writtenPost ? "" : summarize(post.getContent()),
                categoryLabel + " 게시판",
                writtenPost ? "작성일" : "작성자",
                post.getCreatedAt() == null ? "" : COMMUNITY_DATE_FORMATTER.format(post.getCreatedAt()),
                safeCount(post.getLikeCount()),
                safeCount(post.getCommentCount()),
                "/community/posts/" + postId
        );
    }

    private String normalizeBoardType(String boardType) {
        String normalized = boardType == null ? "" : boardType.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "FREE", "QUESTION", "STUDY_LOG" -> normalized;
            default -> "FREE";
        };
    }

    private String boardLabel(String boardType) {
        return switch (boardType) {
            case "QUESTION" -> "질문";
            case "STUDY_LOG" -> "공부 일지";
            default -> "자유";
        };
    }

    private String safeTitle(String title) {
        return title == null || title.isBlank() ? "제목 없음" : title.trim();
    }

    private String summarize(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 80) {
            return normalized;
        }
        return normalized.substring(0, 77) + "...";
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }
}

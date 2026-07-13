package com.acorn.elearning.admin.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.acorn.elearning.admin.dto.response.AdminPageResponse;
import com.acorn.elearning.admin.mapper.NoticeMapper;
import com.acorn.elearning.admin.model.AdminOperationLog;
import com.acorn.elearning.admin.model.Notice;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.model.Lesson;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private final NoticeMapper mapper;

    private final AdminLogService adminLogService;

    public AdminPageResponse<Notice> findPage(int page, int size, String keyword, String period, String status){

        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int offset = (currentPage - 1) * pageSize;

        List<Notice> items = mapper.findPage(pageSize, offset, keyword, period, status);
        long totalCount = mapper.countAll(keyword, period, status);

        return new AdminPageResponse<>(items, currentPage, pageSize, totalCount);
    }

    public Optional<Notice> findById(Long id){
        return mapper.findById(id);
    }

    private AdminOperationLog operationLog(SessionUser sessionUser, String actionType, String targetType, Long targetId, String targetName, String changeDetail) {
        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(requireAdminId(sessionUser));
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setTargetName(targetName);
        log.setChangeDetail(changeDetail);
        log.setResultStatus("SUCCESS");
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }

    private Long requireAdminId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null){
            throw new BusinessException(ErrorCode.AUTH_REQUIRED, "로그인한 관리자 정보가 없습니다.");
        }
        return sessionUser.userId();
    }

    @Transactional
    public int insert(Notice model, SessionUser sessionUser) {
        Long adminId = requireAdminId(sessionUser);
        model.setWriterId(adminId);

        int inserted = mapper.insert(model);
        if (inserted == 1) {
            adminLogService.insert(operationLog(sessionUser, "NOTICE_CREATE", "NOTICE",
                    model.getNoticeId(), model.getTitle(), "공지사항을 등록"));
        }

        return inserted;
    }

    @Transactional
    public int update(Notice model, SessionUser sessionUser) {
        int updated = mapper.update(model);
        if (updated == 1) {
            adminLogService.insert(operationLog(sessionUser, "NOTICE_UPDATE", "NOTICE",
                    model.getNoticeId(), model.getTitle(), "공지사항을 수정"));
        }

        return updated;
    }

    @Transactional
    public int delete(Long noticeId, SessionUser sessionUser) {

        Notice notice = mapper.findById(noticeId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "삭제할 공지사항을 찾을 수 없습니다."
                ));

        int deleted = mapper.deleteById(noticeId);
        if (deleted == 1) {
            adminLogService.insert(operationLog(sessionUser, "NOTICE_DELETE", "NOTICE",
                    noticeId, notice.getTitle(), "공지사항을 삭제"));
        }

        return deleted;
    }


}

package com.acorn.elearning.admin.service;

import com.acorn.elearning.admin.dto.response.AdminPageResponse;
import com.acorn.elearning.admin.dto.response.AdminRecommendationDetailResponse;
import com.acorn.elearning.admin.dto.response.AdminRecommendationManageRowResponse;
import com.acorn.elearning.admin.form.RecommendationForm;
import com.acorn.elearning.admin.mapper.AdminRecommendationMapper;
import com.acorn.elearning.admin.model.AdminOperationLog;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class AdminRecommendationService {

    private final AdminRecommendationMapper mapper;
    private final AdminLogService service;

    public AdminPageResponse<AdminRecommendationManageRowResponse> findPage(
            int page,
            int size,
            Long subjectId,
            String contentType,
            Boolean isActive,
            String keyword
    )
    {

        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int offset = (currentPage - 1) * pageSize;

        List<AdminRecommendationManageRowResponse> items = mapper.findPage(pageSize, offset, subjectId, contentType, isActive, keyword);

        long totalCount = mapper.count(subjectId, contentType, isActive, keyword);

        return new AdminPageResponse<>(
                items, currentPage, pageSize, totalCount
        );
    }

    @Transactional
    public int create(RecommendationForm form, Long adminId)
    {
        validateForm(form);

        if (form.getIsActive() == null) {
            form.setIsActive(Boolean.TRUE);
        }

        int inserted = mapper.insert(form);

        if (inserted == 1) {
            service.insert(operationLog(
                    adminId,
                    "RECOMMENDATION_CREATE",
                    form.getContentId(),
                    form.getTitle(),
                    "추천 콘텐츠를 등록"
            ));
        }

        return inserted;
    }

    @Transactional
    public int update(Long contentId, RecommendationForm form, Long adminId)
    {
        validateForm(form);

        AdminRecommendationDetailResponse existing  = mapper.findById(contentId)
                .orElseThrow( () -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "수정할 추천 콘텐츠를 찾을 수 없습니다."
                ));

        if (form.getIsActive() == null) {
            form.setIsActive(existing.isActive());
        }

        boolean statusChanged = !Objects.equals(
                existing.isActive(),
                form.getIsActive()
        );

        int updated = mapper.update(contentId, form);

        String contentType = statusChanged ? "RECOMMENDATION_STATUS_UPDATE" : "RECOMMENDATION_UPDATE";
        String changeDetail = statusChanged
                ? (Boolean.TRUE.equals(form.getIsActive()) ? "콘텐츠를 활성화" : "콘텐츠를 비활성화")
                : "콘텐츠를 수정";

        if(updated == 1){
            service.insert(
                    operationLog(adminId, contentType, contentId,
                            form.getTitle(),  changeDetail
                            )
            );
        }

        return updated;

    }

    @Transactional
    public int delete(Long contentId, Long adminId)
    {
        AdminRecommendationDetailResponse existing = mapper.findById(contentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "삭제할 추천 콘텐츠 자료를 찾을 수 없습니다."
                ));

        int deleted = mapper.deleteById(contentId);

        if(deleted == 1){
            service.insert(
                    operationLog(adminId, "RECOMMENDATION_DELETE", contentId,
                            existing.title(), "추천 콘텐츠 삭제")
            );
        }

        return deleted;
    }

    private AdminOperationLog operationLog(
            Long adminId,
            String actionType,
            Long contentId,
            String title,
            String changeDetail
    ) {
        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(adminId);
        log.setActionType(actionType);
        log.setTargetType("CONTENT_RECOMMENDATION");
        log.setTargetId(contentId);
        log.setTargetName(title);
        log.setChangeDetail(changeDetail);
        log.setResultStatus("SUCCESS");
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }

    private void validateForm(RecommendationForm form) {
        if (form == null
                || form.getSubjectId() == null
                || form.getTitle() == null || form.getTitle().isBlank()
                || form.getUrl() == null || form.getUrl().isBlank()
                || form.getContentType() == null || form.getContentType().isBlank()
                || form.getRecommendationSlot() == null || form.getRecommendationSlot().isBlank()) {
            throw new BusinessException(
                    ErrorCode.COMMON_VALIDATION_FAILED,
                    "추천 콘텐츠 입력값이 올바르지 않습니다."
            );
        }
    }


}

package com.acorn.elearning.admin.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.acorn.elearning.admin.dto.response.AdminPageResponse;
import com.acorn.elearning.admin.dto.response.AdminRecommendationDetailResponse;
import com.acorn.elearning.admin.dto.response.AdminUserDetailResponse;
import com.acorn.elearning.admin.dto.response.AdminUserManageRowResponse;
import com.acorn.elearning.admin.mapper.AdminUserMapper;
import com.acorn.elearning.admin.model.AdminOperationLog;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserMapper mapper;
    private final AdminLogService service;

    public List<AdminUserManageRowResponse> findAll(){
        return mapper.findAll();
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse findDetailById(Long userId) {
        return mapper.findDetailById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "사용자 정보를 찾을 수 없습니다."
                ));
    }

    public AdminPageResponse<AdminUserManageRowResponse> findPage(
            int page,
            int size,
            String keyword,
            String subjectName,
            String gradeCode,
            String role,
            String status
    ) {
        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int offset = (currentPage - 1) * pageSize;

        List<AdminUserManageRowResponse> items =
                mapper.findPage(pageSize, offset, keyword, subjectName, gradeCode, role, status);
        long totalCount = mapper.countAll(keyword, subjectName, gradeCode, role, status);

        return new AdminPageResponse<>(items, currentPage, pageSize, totalCount);
    }

    public Optional<AdminUserManageRowResponse> findById(Long userId){
        return mapper.findById(userId);
    }

    private AdminOperationLog operationLog(Long adminId, String actionType, Long userId, String targetName, String changeDetail){

        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(adminId);
        log.setActionType(actionType);
        log.setTargetType("USER");
        log.setTargetId(userId);
        log.setTargetName(targetName);
        log.setChangeDetail(changeDetail);
        log.setResultStatus("SUCCESS");
        log.setCreatedAt(LocalDateTime.now());


        return log;
    }

    @Transactional
    public int updateStatus(Long userId, String status, Long adminId){

       if(! "ACTIVE".equals(status) && ! "SUSPENDED".equals(status)) {
           return 0;
       }

       if (userId.equals(adminId)) {
           throw new BusinessException(ErrorCode.ADMIN_SELF_MODIFY_FORBIDDEN);
       }

       AdminUserManageRowResponse user = mapper.findById(userId).orElse(null);

       if(user == null){
           return 0;
       }

       if ("SUSPENDED".equals(status) && isLastActiveAdmin(user)) {
           throw new BusinessException(ErrorCode.ADMIN_LAST_ADMIN_PROTECTED);
       }

        int updated = mapper.updateStatus(userId, status);

        if(updated == 1){

            String changeDetail = "SUSPENDED".equals(status)
                    ? "계정 상태를 정지로 변경"
                    : "계정 상태를 정상으로 변경";

            service.insert(
                    operationLog(adminId, "USER_STATUS_UPDATE", userId,
                            user.getEmail(),
                            changeDetail)
            );
        }

       return updated;
    }

    @Transactional
    public int updateRole(Long userId, String role, Long adminId){
        if(! "ROLE_USER".equals(role) && ! "ROLE_ADMIN".equals(role)){
            return 0;
        }

        if (userId.equals(adminId)) {
            throw new BusinessException(ErrorCode.ADMIN_SELF_MODIFY_FORBIDDEN);
        }

        AdminUserManageRowResponse user = mapper.findById(userId).orElse(null);

        if(user == null){
            return 0;
        }

        if ("ROLE_USER".equals(role) && isLastActiveAdmin(user)) {
            throw new BusinessException(ErrorCode.ADMIN_LAST_ADMIN_PROTECTED);
        }

        int updated = mapper.updateRole(userId, role);

        if(updated == 1){

            String changeDetail = user.getRole() + "에서" + role + "로 권한 변경";
            service.insert(
                    operationLog(adminId, "USER_ROLE_UPDATE",userId,
                            user.getEmail(),
                            changeDetail)
            );
        }

        return updated;
    }

    // 대상이 유일하게 남은 활성 관리자인지 확인 (상태 정지/권한 강등으로 관리자가 0명이 되는 것 방지)
    private boolean isLastActiveAdmin(AdminUserManageRowResponse user) {
        return "ROLE_ADMIN".equals(user.getRole())
                && "ACTIVE".equals(user.getStatus())
                && mapper.countAll(null, null, null, "ROLE_ADMIN", "ACTIVE") <= 1;
    }

}

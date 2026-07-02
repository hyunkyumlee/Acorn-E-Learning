package com.acorn.elearning.admin.service;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.acorn.elearning.admin.dto.response.AdminUserManageRowResponse;
import com.acorn.elearning.admin.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserMapper mapper;

    public List<AdminUserManageRowResponse> findAll(){
        return mapper.findAll();
    }

    public Optional<AdminUserManageRowResponse> findById(Long userId){
        return mapper.findById(userId);
    }

    public Optional<AdminUserManageRowResponse> findLearningSummaryByUserId(Long userId){
        return mapper.findLearningSummaryByUserId(userId);
    }



    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // SessionUser admin = currentAdminSessionUser();
        // Object target = targetMapper.findById(targetId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // targetMapper.update(applyStatusOrForm(target, form));
        // adminOperationLogMapper.insert(AdminOperationLog.changed(admin.userId(), target));
        // return Map.of("result", "updated");
        return Map.of("action", action, "status", "SKELETON");
    }
}

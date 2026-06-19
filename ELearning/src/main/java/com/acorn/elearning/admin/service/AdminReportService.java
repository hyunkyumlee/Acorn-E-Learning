package com.acorn.elearning.admin.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AdminReportService {
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

package com.acorn.elearning.admin.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.acorn.elearning.admin.mapper.AdminOperationLogMapper;
import com.acorn.elearning.admin.model.AdminOperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminLogService {

    private final AdminOperationLogMapper mapper;

    public List<AdminOperationLog> findAll(){

        return mapper.findAll();
    }

    public Optional<AdminOperationLog> findById(Long id){
        return mapper.findById(id);
    }

    public int insert(AdminOperationLog model) {
        return mapper.insert(model);
    }

    public int update(AdminOperationLog model) {
        return mapper.update(model);
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

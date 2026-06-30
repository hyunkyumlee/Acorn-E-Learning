package com.acorn.elearning.admin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.acorn.elearning.admin.mapper.NoticeMapper;
import com.acorn.elearning.admin.model.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private final NoticeMapper mapper;

    public List<Notice> findAll(){

        return mapper.findAll();
    }

    public Optional<Notice> findById(Long id){
        return mapper.findById(id);
    }

    public int insert(Notice model) {
        return mapper.insert(model);
    }

    public int update(Notice model) {
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

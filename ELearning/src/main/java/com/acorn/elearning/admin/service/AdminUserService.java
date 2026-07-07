package com.acorn.elearning.admin.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.acorn.elearning.admin.dto.response.AdminUserManageRowResponse;
import com.acorn.elearning.admin.mapper.AdminUserMapper;
import com.acorn.elearning.admin.model.AdminOperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserMapper mapper;
    private final AdminLogService service;

    public List<AdminUserManageRowResponse> findAll(){
        return mapper.findAll();
    }

    public Optional<AdminUserManageRowResponse> findById(Long userId){
        return mapper.findById(userId);
    }

    private AdminOperationLog operationLog(Long adminId, String actionType, Long userId){

        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(adminId);
        log.setActionType(actionType);
        log.setTargetType("USER");
        log.setTargetId(userId);
        log.setResultStatus("SUCCESS");
        log.setCreatedAt(LocalDateTime.now());

        return log;
    }

    public int updateStatus(Long userId, String status, Long adminId){



       if(! "ACTIVE".equals(status) && ! "SUSPENDED".equals(status)) {
           return 0;
       }

        int updated = mapper.updateStatus(userId, status);

        if(updated == 1){
            service.insert(
                    operationLog(adminId, "USER_STATUS_UPDATE", userId)
            );
        }

       return updated;
    }

    public int updateRole(Long userId, String role, Long adminId){
        if(! "ROLE_USER".equals(role) && ! "ROLE_ADMIN".equals(role)){
            return 0;
        }

        int updated = mapper.updateRole(userId, role);

        if(updated == 1){
            service.insert(
                    operationLog(adminId, "USER_ROLE_UPDATE",userId)
            );
        }

        return updated;
    }


}

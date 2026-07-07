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


    public int updateStatus(Long userId, String status){

       if(! "ACTIVE".equals(status) && ! "SUSPENDED".equals(status)) {
           return 0;
       }

       return mapper.updateStatus(userId, status);
    }

    public int updateRole(Long userId, String role){
        if(! "ROLE_USER".equals(role) && ! "ROLE_ADMIN".equals(role)){
            return 0;
        }

        return mapper.updateRole(userId, role);
    }


}

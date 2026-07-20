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


}

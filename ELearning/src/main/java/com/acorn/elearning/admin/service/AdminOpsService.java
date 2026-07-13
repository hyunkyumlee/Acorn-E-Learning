package com.acorn.elearning.admin.service;

import java.util.List;
import java.util.Map;

import com.acorn.elearning.admin.dto.response.AdminOperationLogPageResponse;
import com.acorn.elearning.admin.dto.response.AdminPageResponse;
import com.acorn.elearning.admin.mapper.AdminOperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminOpsService {

    private final AdminOperationLogMapper mapper;

    public AdminPageResponse<AdminOperationLogPageResponse> findOperationLogPage(
            int page,
            int size,
            String targetType,
            String actionCategory
    )
    {
        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int offset = (currentPage - 1) * pageSize;

        List<AdminOperationLogPageResponse> items = mapper.findPage(pageSize, offset, targetType, actionCategory);

        long totalCount = mapper.countAll(targetType, actionCategory);

        return new AdminPageResponse<>(
                items, currentPage, pageSize, totalCount
        );

    }
}

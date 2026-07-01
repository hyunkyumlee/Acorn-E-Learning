package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminProblemManageRowResponse;

import java.util.List;

public interface AdminProblemMapper {

    List<AdminProblemManageRowResponse> findAll();
}

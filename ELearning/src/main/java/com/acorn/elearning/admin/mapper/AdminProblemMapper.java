package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminProblemManageRowResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminProblemMapper {

    List<AdminProblemManageRowResponse> findAll();
    int deleteById(@Param("problemId") Long problemId);
}

package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminUserManageRowResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface AdminUserMapper {

    //목록 조회
    List<AdminUserManageRowResponse> findAll();

    List<AdminUserManageRowResponse> findPage(@Param("limit") int limit,
                                              @Param("offset") int offset,
                                              @Param("keyword") String keyword,
                                              @Param("subjectName") String subjectName,
                                              @Param("gradeCode") String gradeCode,
                                              @Param("role") String role,
                                              @Param("status") String status);

    long countAll(@Param("keyword") String keyword,
                  @Param("subjectName") String subjectName,
                  @Param("gradeCode") String gradeCode,
                  @Param("role") String role,
                  @Param("status") String status);
    //단건 상세조회
    Optional<AdminUserManageRowResponse> findById(@Param("userId") Long userId);

    //사용자 학습 진행률 조회
    //Optional<AdminUserManageRowResponse>findLearningSummaryByUserId(@Param("userId") Long userId);

    //상태 변경
    int updateStatus(@Param("userId") Long userId, @Param("status") String status);
    //권한 변경
    int updateRole(@Param("userId") Long userId, @Param("role") String role);

}

package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminRecommendationDetailResponse;
import com.acorn.elearning.admin.dto.response.AdminRecommendationManageRowResponse;
import com.acorn.elearning.admin.form.RecommendationForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface AdminRecommendationMapper {
    List<AdminRecommendationManageRowResponse> findPage(@Param("limit") int limit,
                                                        @Param("offset") int offset,
                                                        @Param("subjectId") Long subjectId,
                                                        @Param("contentType") String contentType,
                                                        @Param("isActive") Boolean isActive,
                                                        @Param("keyword") String keyword);

    long count(@Param("subjectId") Long subjectId,
               @Param("contentType") String contentType,
               @Param("isActive") Boolean isActive,
               @Param("keyword") String keyword);

    Optional<AdminRecommendationDetailResponse> findById(@Param("contentId") Long contentId);

    int insert(RecommendationForm form);

    int update(
            @Param("contentId") Long contentId,
            @Param("form") RecommendationForm form
    );

    int deleteById(@Param("contentId") Long contentId);

}

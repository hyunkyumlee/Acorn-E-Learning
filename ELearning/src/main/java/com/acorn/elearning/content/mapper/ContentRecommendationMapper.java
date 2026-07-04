package com.acorn.elearning.content.mapper;

import com.acorn.elearning.content.model.ContentRecommendation;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface ContentRecommendationMapper {
    Optional<ContentRecommendation> findById(@Param("id") Long id);
    List<ContentRecommendation> findAll();
    List<ContentRecommendation> findActive(
            @Param("subjectId") Long subjectId,
            @Param("contentType") String contentType,
            @Param("slot") String slot
    );
    int insert(ContentRecommendation model);
    int update(ContentRecommendation model);
}

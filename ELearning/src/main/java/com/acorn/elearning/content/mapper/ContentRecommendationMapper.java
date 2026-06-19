package com.acorn.elearning.content.mapper;

import com.acorn.elearning.content.model.ContentRecommendation;
import java.util.List;
import java.util.Optional;

public interface ContentRecommendationMapper {
    Optional<ContentRecommendation> findById(Long id);
    List<ContentRecommendation> findAll();
    int insert(ContentRecommendation model);
    int update(ContentRecommendation model);
}

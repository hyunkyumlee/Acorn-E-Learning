package com.acorn.elearning.practice.mapper;

import com.acorn.elearning.practice.model.ScoreEvent;
import java.util.List;
import java.util.Optional;

public interface ScoreEventMapper {
    Optional<ScoreEvent> findById(Long id);
    List<ScoreEvent> findAll();
    int insert(ScoreEvent model);
    int update(ScoreEvent model);
}

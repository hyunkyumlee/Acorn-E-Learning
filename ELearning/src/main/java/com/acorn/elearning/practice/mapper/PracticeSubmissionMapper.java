package com.acorn.elearning.practice.mapper;

import com.acorn.elearning.practice.model.PracticeSubmission;
import java.util.List;
import java.util.Optional;

public interface PracticeSubmissionMapper {
    Optional<PracticeSubmission> findById(Long id);
    List<PracticeSubmission> findAll();
    int insert(PracticeSubmission model);
    int update(PracticeSubmission model);
}

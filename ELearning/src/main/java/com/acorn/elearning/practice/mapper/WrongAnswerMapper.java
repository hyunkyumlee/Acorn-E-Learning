package com.acorn.elearning.practice.mapper;

import com.acorn.elearning.practice.model.WrongAnswer;
import java.util.List;
import java.util.Optional;

public interface WrongAnswerMapper {
    Optional<WrongAnswer> findById(Long id);
    List<WrongAnswer> findAll();
    int insert(WrongAnswer model);
    int update(WrongAnswer model);
}

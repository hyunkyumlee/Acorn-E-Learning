package com.acorn.elearning.practice.mapper;

import com.acorn.elearning.practice.model.PracticeSetItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PracticeSetItemMapper {

    int insertSetItem(PracticeSetItem item);

    PracticeSetItem findBySetAttemptIdAndProblemId(Long setAttemptId, Long problemId);
}

package com.acorn.elearning.practice.mapper;

import com.acorn.elearning.practice.model.PracticeSetAttempt;

public interface PracticeSetAttemptMapper {
    // void: 반환값 없이 객체에 ID 채움
    void insertAttempt(PracticeSetAttempt model);

    // int: 영향받은 행 수를 확인하여 로직에 활용
    int updateAttempt(PracticeSetAttempt model);
}

package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.user.model.UserLearningProfile;
import java.util.Optional;

/**
 * 학습 메인 대시보드 read 전용 매퍼.
 * user_learning_profiles(users 1:1 확장 공유 테이블)에서 사용자 프로필을 "읽기만" 한다.
 * 쓰기(insert/update)는 user/settings 도메인 소유이므로 여기엔 두지 않는다.
 * user 도메인의 UserLearningProfileMapper와는 namespace가 달라 서로 충돌하지 않는다.
 */
public interface LearningProfileReadMapper {
    Optional<UserLearningProfile> findByUserId(Long userId);
}

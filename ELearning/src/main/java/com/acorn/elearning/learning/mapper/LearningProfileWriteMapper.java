package com.acorn.elearning.learning.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 학습 도메인 전용 프로필 write 매퍼.
 * user_learning_profiles(users 1:1 확장 공유 테이블)에서 learning owner가 책임지는
 * "레벨 컬럼(current_level_code)"만 갱신한다. (분담범위 v2.4: 레벨/unlock write = 2번 조아진)
 *
 * ⚠️ 공유 테이블 전체 소유는 user/settings 도메인(6번)이라, 6번의 user/mapper/UserLearningProfileMapper는
 *    건드리지 않는다. 읽기용 LearningProfileReadMapper와 같은 방식으로 learning 패키지 안에 분리해 둔다.
 *    (namespace가 달라 user 도메인 매퍼와 충돌하지 않음)
 */
public interface LearningProfileWriteMapper {
    /** 레벨 테스트 결과 반영: 사용자 프로필의 current_level_code를 갱신한다. grade_code/total_score는 건드리지 않는다. */
    int updateLevel(@Param("userId") Long userId, @Param("currentLevelCode") String currentLevelCode);
}

package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.UserSubjectEnrollment;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface UserSubjectEnrollmentMapper {

    Optional<UserSubjectEnrollment> findById(Long id);

    List<UserSubjectEnrollment> findAll();

    /** 중복 수강신청 방지용: (user_id, subject_id) UNIQUE 키로 기존 신청 존재 여부 조회. */
    Optional<UserSubjectEnrollment> findByUserAndSubject(@Param("userId") Long userId,
                                                         @Param("subjectId") Long subjectId);

    /** 사용자가 수강 중(ACTIVE)인 과목 전체. 과목 잠금 판정의 source of truth. */
    List<UserSubjectEnrollment> findActiveByUser(@Param("userId") Long userId);

    /** status와 무관한 수강 이력 전체. backfill 여부 판정에 쓴다(해지한 과목을 되살리지 않기 위함). */
    List<UserSubjectEnrollment> findByUser(@Param("userId") Long userId);

    int insert(UserSubjectEnrollment model);

    int update(UserSubjectEnrollment model);
}

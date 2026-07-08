package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.model.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface NoticeMapper {
    Optional<Notice> findById(Long id);
    List<Notice> findAll();
    int insert(Notice model);
    int update(Notice model);
    int deleteById(Long noticeId);

    List<Notice> findPage(@Param("limit") int limit,
                          @Param("offset") int offest);

    long countAll();
}

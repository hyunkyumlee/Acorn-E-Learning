package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.CommunityNotice;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface CommunityNoticeMapper {
    List<CommunityNotice> findPublishedNotices(@Param("limit") int limit);
    Optional<CommunityNotice> findPublishedNoticeById(@Param("noticeId") Long noticeId);
}

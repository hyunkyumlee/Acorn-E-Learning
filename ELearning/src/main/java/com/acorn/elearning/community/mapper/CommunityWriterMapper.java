package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.CommunityWriterNickname;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CommunityWriterMapper {
    List<CommunityWriterNickname> findNicknamesByUserIds(@Param("userIds") Collection<Long> userIds);
}

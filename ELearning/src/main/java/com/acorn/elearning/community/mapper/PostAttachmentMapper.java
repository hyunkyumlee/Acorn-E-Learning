package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.PostAttachment;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface PostAttachmentMapper {
    Optional<PostAttachment> findById(@Param("id") Long id);
    List<PostAttachment> findAll();
    List<PostAttachment> findByPostId(@Param("postId") Long postId);
    int countByPostId(@Param("postId") Long postId);
    int insert(PostAttachment model);
    int update(PostAttachment model);
    int deleteById(@Param("attachmentId") Long attachmentId);
}

package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.PostAttachment;
import java.util.List;
import java.util.Optional;

public interface PostAttachmentMapper {
    Optional<PostAttachment> findById(Long id);
    List<PostAttachment> findAll();
    int insert(PostAttachment model);
    int update(PostAttachment model);
}

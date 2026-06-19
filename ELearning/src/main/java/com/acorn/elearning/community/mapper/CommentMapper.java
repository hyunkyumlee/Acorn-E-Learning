package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.Comment;
import java.util.List;
import java.util.Optional;

public interface CommentMapper {
    Optional<Comment> findById(Long id);
    List<Comment> findAll();
    int insert(Comment model);
    int update(Comment model);
}

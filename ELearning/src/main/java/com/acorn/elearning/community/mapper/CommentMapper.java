package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.Comment;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface CommentMapper {
    Optional<Comment> findById(@Param("id") Long id);
    List<Comment> findAll();
    List<Comment> findByPostId(@Param("postId") Long postId);
    List<Comment> findByWriterId(@Param("writerId") Long writerId);
    int insert(Comment model);
    int update(Comment model);
    int softDelete(@Param("commentId") Long commentId, @Param("writerId") Long writerId);
}

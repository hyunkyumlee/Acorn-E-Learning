package com.acorn.elearning.community.dto.response;

import com.acorn.elearning.community.model.Comment;
import java.util.List;

public record CommentListResponse(List<Comment> comments) {
}

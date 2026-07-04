package com.acorn.elearning.community.dto.response;

public record ReactionResponse(Long postId, String type, boolean active, int count) {
}

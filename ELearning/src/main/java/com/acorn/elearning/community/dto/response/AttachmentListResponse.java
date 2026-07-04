package com.acorn.elearning.community.dto.response;

import com.acorn.elearning.community.model.PostAttachment;
import java.util.List;

public record AttachmentListResponse(List<PostAttachment> attachments) {
}

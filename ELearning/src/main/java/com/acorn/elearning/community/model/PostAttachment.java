package com.acorn.elearning.community.model;

import java.time.LocalDateTime;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostAttachment {
    private Long attachmentId;
    private Long postId;
    private Long uploaderId;
    private String originalName;
    private String storedName;
    private String filePath;
    private Long fileSize;
    private LocalDateTime createdAt;

    public boolean isImage() {
        String name = storedName != null && !storedName.isBlank() ? storedName : originalName;
        if (name == null) {
            return false;
        }
        String lowerName = name.toLowerCase(Locale.ROOT);
        return lowerName.endsWith(".png")
                || lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".webp");
    }

    public String getFileUrl() {
        return attachmentId == null ? "" : "/community/attachments/" + attachmentId + "/file";
    }
}

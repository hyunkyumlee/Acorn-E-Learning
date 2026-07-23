package com.acorn.elearning.community.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.community.model.PostAttachment;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

final class AttachmentUploadPolicy {
    static final int MAX_FILES_PER_POST = 5;
    static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;
    static final long MAX_TOTAL_SIZE = 20L * 1024L * 1024L;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "pdf", "txt", "zip");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp", "application/pdf", "text/plain", "application/zip", "application/x-zip-compressed");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/webp");

    List<MultipartFile> nonEmptyFiles(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }
        return files.stream().filter(file -> file != null && !file.isEmpty()).toList();
    }

    void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw validation("첨부파일 1개 크기는 10MB를 넘을 수 없습니다.");
        }
        String extension = extension(originalName(file));
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw validation("지원하지 않는 첨부파일 확장자입니다.");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank() && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw validation("지원하지 않는 첨부파일 MIME type입니다.");
        }
    }

    void validateInlineImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw validation("이미지 파일이 필요합니다.");
        }
        if (image.getSize() > MAX_FILE_SIZE) {
            throw validation("이미지 1개 크기는 10MB를 넘을 수 없습니다.");
        }
        if (!IMAGE_EXTENSIONS.contains(extension(originalName(image)))) {
            throw validation("PNG, JPG, WEBP 이미지만 본문에 넣을 수 있습니다.");
        }
        String contentType = image.getContentType();
        if (contentType == null || contentType.isBlank() || !IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw validation("지원하지 않는 이미지 MIME type입니다.");
        }
    }

    String originalName(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw validation("첨부파일 이름이 필요합니다.");
        }
        return original;
    }

    String storedName(MultipartFile file) {
        return UUID.randomUUID() + "." + extension(originalName(file));
    }

    boolean isInlineImage(PostAttachment attachment) {
        return attachment.isImage()
                && attachment.getFilePath() != null
                && attachment.getFilePath().contains("/inline/");
    }

    private String extension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private BusinessException validation(String message) {
        return new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, message);
    }
}

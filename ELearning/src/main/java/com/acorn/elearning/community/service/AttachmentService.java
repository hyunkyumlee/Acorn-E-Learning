package com.acorn.elearning.community.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.community.mapper.CommunityPostMapper;
import com.acorn.elearning.community.mapper.PostAttachmentMapper;
import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.community.model.PostAttachment;
import com.acorn.elearning.security.SessionUser;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentService {
    private static final int MAX_FILES_PER_POST = 5;
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;
    private static final long MAX_TOTAL_SIZE = 20L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "pdf", "txt", "zip");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "application/pdf",
            "text/plain",
            "application/zip",
            "application/x-zip-compressed"
    );

    private final PostAttachmentMapper postAttachmentMapper;
    private final CommunityPostMapper communityPostMapper;

    public AttachmentService(PostAttachmentMapper postAttachmentMapper, CommunityPostMapper communityPostMapper) {
        this.postAttachmentMapper = postAttachmentMapper;
        this.communityPostMapper = communityPostMapper;
    }

    public Map<String, Object> stub(String action) {
        return Map.of("action", action, "status", "IMPLEMENTED");
    }

    @Transactional(readOnly = true)
    public List<PostAttachment> findByPostId(Long postId) {
        return postAttachmentMapper.findByPostId(postId);
    }

    @Transactional
    public List<PostAttachment> addMetadata(SessionUser sessionUser, Long postId, List<MultipartFile> files) {
        Long userId = requireUserId(sessionUser);
        CommunityPost post = requireActivePost(postId);
        requireOwner(post, userId);
        saveMetadata(postId, userId, files);
        return postAttachmentMapper.findByPostId(postId);
    }

    @Transactional
    public void saveMetadata(Long postId, Long uploaderId, List<MultipartFile> files) {
        List<MultipartFile> uploadFiles = nonEmptyFiles(files);
        if (uploadFiles.isEmpty()) {
            return;
        }

        int existingCount = postAttachmentMapper.countByPostId(postId);
        if (existingCount + uploadFiles.size() > MAX_FILES_PER_POST) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "첨부파일은 게시글당 최대 5개까지 가능합니다.");
        }

        long totalSize = uploadFiles.stream().mapToLong(MultipartFile::getSize).sum();
        if (totalSize > MAX_TOTAL_SIZE) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "첨부파일 전체 크기는 20MB를 넘을 수 없습니다.");
        }

        for (MultipartFile file : uploadFiles) {
            validateFile(file);
            PostAttachment attachment = new PostAttachment();
            attachment.setPostId(postId);
            attachment.setUploaderId(uploaderId);
            attachment.setOriginalName(originalName(file));
            attachment.setStoredName(storedName(file));
            attachment.setFilePath("community/" + postId + "/" + attachment.getStoredName());
            attachment.setFileSize(file.getSize());
            postAttachmentMapper.insert(attachment);
        }
    }

    @Transactional
    public void delete(SessionUser sessionUser, Long attachmentId) {
        Long userId = requireUserId(sessionUser);
        PostAttachment attachment = postAttachmentMapper.findById(attachmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "첨부파일을 찾을 수 없습니다."));
        CommunityPost post = requireActivePost(attachment.getPostId());
        requireOwner(post, userId);
        postAttachmentMapper.deleteById(attachmentId);
    }

    private List<MultipartFile> nonEmptyFiles(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }
        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "첨부파일 1개 크기는 10MB를 넘을 수 없습니다.");
        }
        String extension = extension(originalName(file));
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "지원하지 않는 첨부파일 확장자입니다.");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank() && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "지원하지 않는 첨부파일 MIME type입니다.");
        }
    }

    private String originalName(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "첨부파일 이름이 필요합니다.");
        }
        return original;
    }

    private String storedName(MultipartFile file) {
        return UUID.randomUUID() + "." + extension(originalName(file));
    }

    private String extension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private CommunityPost requireActivePost(Long postId) {
        return communityPostMapper.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "게시글을 찾을 수 없습니다."));
    }

    private void requireOwner(CommunityPost post, Long userId) {
        if (!userId.equals(post.getWriterId())) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }
}
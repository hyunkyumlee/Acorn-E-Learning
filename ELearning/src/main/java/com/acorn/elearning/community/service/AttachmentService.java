package com.acorn.elearning.community.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.community.mapper.CommunityPostMapper;
import com.acorn.elearning.community.mapper.PostAttachmentMapper;
import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.community.model.PostAttachment;
import com.acorn.elearning.security.SessionUser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/webp");
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DRAFT = "DRAFT";

    private final PostAttachmentMapper postAttachmentMapper;
    private final CommunityPostMapper communityPostMapper;
    private final Path uploadBasePath;

    public AttachmentService(
            PostAttachmentMapper postAttachmentMapper,
            CommunityPostMapper communityPostMapper,
            @Value("${knowva.upload.base-path:./uploads}") String configuredUploadBasePath
    ) {
        this.postAttachmentMapper = postAttachmentMapper;
        this.communityPostMapper = communityPostMapper;
        this.uploadBasePath = uploadBasePath(configuredUploadBasePath);
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
    public PostAttachment addInlineImage(SessionUser sessionUser, Long postId, MultipartFile image) {
        Long userId = requireUserId(sessionUser);
        CommunityPost post = requireWritablePost(postId);
        requireOwner(post, userId);
        validateInlineImage(image);
        ensureAttachmentCapacity(postId, 1, image.getSize());

        PostAttachment attachment = new PostAttachment();
        attachment.setPostId(postId);
        attachment.setUploaderId(userId);
        attachment.setOriginalName(originalName(image));
        attachment.setStoredName(storedName(image));
        attachment.setFilePath("community/" + postId + "/inline/" + attachment.getStoredName());
        attachment.setFileSize(image.getSize());
        saveFile(image, attachment.getFilePath());
        postAttachmentMapper.insert(attachment);
        return attachment;
    }

    @Transactional
    public void saveMetadata(Long postId, Long uploaderId, List<MultipartFile> files) {
        List<MultipartFile> uploadFiles = nonEmptyFiles(files);
        if (uploadFiles.isEmpty()) {
            return;
        }

        long totalSize = uploadFiles.stream().mapToLong(MultipartFile::getSize).sum();
        ensureAttachmentCapacity(postId, uploadFiles.size(), totalSize);

        for (MultipartFile file : uploadFiles) {
            validateFile(file);
            PostAttachment attachment = new PostAttachment();
            attachment.setPostId(postId);
            attachment.setUploaderId(uploaderId);
            attachment.setOriginalName(originalName(file));
            attachment.setStoredName(storedName(file));
            attachment.setFilePath("community/" + postId + "/" + attachment.getStoredName());
            attachment.setFileSize(file.getSize());
            saveFile(file, attachment.getFilePath());
            postAttachmentMapper.insert(attachment);
        }
    }

    @Transactional(readOnly = true)
    public AttachmentFile attachmentFile(SessionUser sessionUser, Long attachmentId) {
        PostAttachment attachment = postAttachmentMapper.findById(attachmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "첨부파일을 찾을 수 없습니다."));
        CommunityPost post = communityPostMapper.findById(attachment.getPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "첨부파일을 찾을 수 없습니다."));
        if (!STATUS_ACTIVE.equals(post.getStatus()) || post.getDeletedAt() != null) {
            Long userId = requireUserId(sessionUser);
            if (!STATUS_DRAFT.equals(post.getStatus()) || !userId.equals(post.getWriterId())) {
                throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "첨부파일을 찾을 수 없습니다.");
            }
        }

        Path targetPath = resolveStoragePath(attachment.getFilePath());
        if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "첨부파일을 찾을 수 없습니다.");
        }
        try {
            Resource resource = new UrlResource(targetPath.toUri());
            return new AttachmentFile(attachment, resource, contentType(targetPath));
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "첨부파일을 찾을 수 없습니다.");
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
        deleteFile(attachment.getFilePath());
    }

    @Transactional
    public void deleteForPost(
            SessionUser sessionUser,
            Long postId,
            Long attachmentId
    ) {
        Long userId = requireUserId(sessionUser);

        // 1. 수정하려는 게시글 A가 존재하고 본인 글인지 확인
        CommunityPost currentPost = requireActivePost(postId);
        requireOwner(currentPost, userId);

        // 2. 삭제하려는 첨부파일 조회
        PostAttachment attachment = postAttachmentMapper.findById(attachmentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "첨부파일을 찾을 수 없습니다."
                ));

        // 3. 가장 중요한 관계 검증
        if (!Objects.equals(attachment.getPostId(), postId)) {
            throw new BusinessException(
                    ErrorCode.COMMON_NOT_FOUND,
                    "현재 게시글의 첨부파일이 아닙니다."
            );
        }

        // 4. DB 행 삭제 후 실제 파일 삭제
        postAttachmentMapper.deleteByIdAndPostId(attachmentId, postId);
        deleteFile(attachment.getFilePath());
    }

    @Transactional
    public void removeUnreferencedInlineImages(Long postId, String markdown) {
        String renderedMarkdown = markdown == null ? "" : markdown;
        postAttachmentMapper.findByPostId(postId).stream()
                .filter(this::isInlineImage)
                .filter(attachment -> !renderedMarkdown.contains(attachment.getFileUrl()))
                .forEach(attachment -> {
                    postAttachmentMapper.deleteById(attachment.getAttachmentId());
                    deleteFile(attachment.getFilePath());
                });
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

    private void validateInlineImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "이미지 파일이 필요합니다.");
        }
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "이미지 1개 크기는 10MB를 넘을 수 없습니다.");
        }
        String extension = extension(originalName(image));
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "PNG, JPG, WEBP 이미지만 본문에 넣을 수 있습니다.");
        }
        String contentType = image.getContentType();
        if (contentType == null || contentType.isBlank() || !IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "지원하지 않는 이미지 MIME type입니다.");
        }
    }

    private void ensureAttachmentCapacity(Long postId, int additionalCount, long additionalSize) {
        List<PostAttachment> existingAttachments = postAttachmentMapper.findByPostId(postId);
        int existingCount = existingAttachments.size();
        if (existingCount + additionalCount > MAX_FILES_PER_POST) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "첨부파일은 게시글당 최대 5개까지 가능합니다.");
        }
        long existingSize = existingAttachments.stream()
                .mapToLong(attachment -> attachment.getFileSize() == null ? 0L : attachment.getFileSize())
                .sum();
        if (existingSize + additionalSize > MAX_TOTAL_SIZE) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "첨부파일 전체 크기는 20MB를 넘을 수 없습니다.");
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

    private Path uploadBasePath(String configuredUploadBasePath) {
        String basePath = configuredUploadBasePath == null || configuredUploadBasePath.isBlank()
                ? "./uploads"
                : configuredUploadBasePath;
        return Path.of(basePath).toAbsolutePath().normalize();
    }

    private void saveFile(MultipartFile file, String relativePath) {
        Path targetPath = resolveStoragePath(relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "첨부파일 저장에 실패했습니다.");
        }
    }

    private void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolveStoragePath(relativePath));
        } catch (IOException ignored) {
        }
    }

    private Path resolveStoragePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "첨부파일을 찾을 수 없습니다.");
        }
        Path targetPath = uploadBasePath.resolve(relativePath).normalize();
        if (!targetPath.startsWith(uploadBasePath)) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "첨부파일 경로가 올바르지 않습니다.");
        }
        return targetPath;
    }

    private String contentType(Path targetPath) {
        try {
            String contentType = Files.probeContentType(targetPath);
            return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    private CommunityPost requireActivePost(Long postId) {
        return communityPostMapper.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "게시글을 찾을 수 없습니다."));
    }

    private CommunityPost requireWritablePost(Long postId) {
        CommunityPost post = communityPostMapper.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        if ((!STATUS_ACTIVE.equals(post.getStatus()) && !STATUS_DRAFT.equals(post.getStatus()))
                || post.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        return post;
    }

    private boolean isInlineImage(PostAttachment attachment) {
        return attachment.isImage()
                && attachment.getFilePath() != null
                && attachment.getFilePath().contains("/inline/");
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

    public record AttachmentFile(PostAttachment attachment, Resource resource, String contentType) {
    }
}

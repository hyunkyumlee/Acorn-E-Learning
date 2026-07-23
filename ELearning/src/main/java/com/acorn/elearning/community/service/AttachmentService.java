package com.acorn.elearning.community.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.community.mapper.CommunityPostMapper;
import com.acorn.elearning.community.mapper.PostAttachmentMapper;
import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.community.model.PostAttachment;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.storage.ObjectStorage;
import com.acorn.elearning.storage.StorageException;
import com.acorn.elearning.storage.StorageObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentService {
    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DRAFT = "DRAFT";

    private final PostAttachmentMapper postAttachmentMapper;
    private final CommunityPostMapper communityPostMapper;
    private final ObjectStorage objectStorage;
    private final AttachmentUploadPolicy uploadPolicy = new AttachmentUploadPolicy();

    public AttachmentService(
            PostAttachmentMapper postAttachmentMapper,
            CommunityPostMapper communityPostMapper,
            ObjectStorage objectStorage) {
        this.postAttachmentMapper = postAttachmentMapper;
        this.communityPostMapper = communityPostMapper;
        this.objectStorage = objectStorage;
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
        uploadPolicy.validateInlineImage(image);
        ensureAttachmentCapacity(postId, 1, image.getSize());

        PostAttachment attachment = new PostAttachment();
        attachment.setPostId(postId);
        attachment.setUploaderId(userId);
        attachment.setOriginalName(uploadPolicy.originalName(image));
        attachment.setStoredName(uploadPolicy.storedName(image));
        attachment.setFilePath("community/" + postId + "/inline/" + attachment.getStoredName());
        attachment.setFileSize(image.getSize());
        storeAndInsert(image, attachment);
        return attachment;
    }

    @Transactional
    public void saveMetadata(Long postId, Long uploaderId, List<MultipartFile> files) {
        List<MultipartFile> uploadFiles = uploadPolicy.nonEmptyFiles(files);
        if (uploadFiles.isEmpty()) {
            return;
        }
        long totalSize = uploadFiles.stream().mapToLong(MultipartFile::getSize).sum();
        ensureAttachmentCapacity(postId, uploadFiles.size(), totalSize);
        List<String> storedKeys = new ArrayList<>();
        try {
            for (MultipartFile file : uploadFiles) {
                uploadPolicy.validateFile(file);
                PostAttachment attachment = new PostAttachment();
                attachment.setPostId(postId);
                attachment.setUploaderId(uploaderId);
                attachment.setOriginalName(uploadPolicy.originalName(file));
                attachment.setStoredName(uploadPolicy.storedName(file));
                attachment.setFilePath("community/" + postId + "/" + attachment.getStoredName());
                attachment.setFileSize(file.getSize());
                storeFile(file, attachment.getFilePath());
                storedKeys.add(attachment.getFilePath());
                postAttachmentMapper.insert(attachment);
            }
        } catch (RuntimeException exception) {
            storedKeys.forEach(this::deleteFileQuietly);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public AttachmentFile attachmentFile(SessionUser sessionUser, Long attachmentId) {
        PostAttachment attachment = postAttachmentMapper.findById(attachmentId)
                .orElseThrow(() -> notFound("첨부파일을 찾을 수 없습니다."));
        CommunityPost post = communityPostMapper.findById(attachment.getPostId())
                .orElseThrow(() -> notFound("첨부파일을 찾을 수 없습니다."));
        if (!STATUS_ACTIVE.equals(post.getStatus()) || post.getDeletedAt() != null) {
            Long userId = requireUserId(sessionUser);
            if (!STATUS_DRAFT.equals(post.getStatus()) || !userId.equals(post.getWriterId())) {
                throw notFound("첨부파일을 찾을 수 없습니다.");
            }
        }
        try {
            StorageObject stored = objectStorage.get(attachment.getFilePath());
            Resource resource = new InputStreamResource(stored.stream());
            return new AttachmentFile(attachment, resource, stored.contentType());
        } catch (IOException | StorageException exception) {
            throw notFound("첨부파일을 찾을 수 없습니다.");
        }
    }

    @Transactional
    public void delete(SessionUser sessionUser, Long attachmentId) {
        Long userId = requireUserId(sessionUser);
        PostAttachment attachment = postAttachmentMapper.findById(attachmentId)
                .orElseThrow(() -> notFound("첨부파일을 찾을 수 없습니다."));
        CommunityPost post = requireActivePost(attachment.getPostId());
        requireOwner(post, userId);
        postAttachmentMapper.deleteById(attachmentId);
        deleteFileQuietly(attachment.getFilePath());
    }

    @Transactional
    public void deleteForPost(SessionUser sessionUser, Long postId, Long attachmentId) {
        Long userId = requireUserId(sessionUser);
        CommunityPost currentPost = requireActivePost(postId);
        requireOwner(currentPost, userId);
        PostAttachment attachment = postAttachmentMapper.findById(attachmentId)
                .orElseThrow(() -> notFound("첨부파일을 찾을 수 없습니다."));
        if (!Objects.equals(attachment.getPostId(), postId)) {
            throw notFound("현재 게시글의 첨부파일이 아닙니다.");
        }
        postAttachmentMapper.deleteByIdAndPostId(attachmentId, postId);
        deleteFileQuietly(attachment.getFilePath());
    }

    @Transactional
    public void removeUnreferencedInlineImages(Long postId, String markdown) {
        String renderedMarkdown = markdown == null ? "" : markdown;
        postAttachmentMapper.findByPostId(postId).stream()
                .filter(uploadPolicy::isInlineImage)
                .filter(attachment -> !renderedMarkdown.contains(attachment.getFileUrl()))
                .forEach(attachment -> {
                    postAttachmentMapper.deleteById(attachment.getAttachmentId());
                    deleteFileQuietly(attachment.getFilePath());
                });
    }

    private void storeAndInsert(MultipartFile file, PostAttachment attachment) {
        storeFile(file, attachment.getFilePath());
        try {
            postAttachmentMapper.insert(attachment);
        } catch (RuntimeException exception) {
            deleteFileQuietly(attachment.getFilePath());
            throw exception;
        }
    }

    private void storeFile(MultipartFile file, String key) {
        try (InputStream input = file.getInputStream()) {
            objectStorage.put(key, input, file.getSize(), file.getContentType());
        } catch (IOException | StorageException exception) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "첨부파일 저장에 실패했습니다.");
        }
    }

    private void deleteFileQuietly(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            objectStorage.delete(key);
        } catch (IOException | StorageException exception) {
            log.warn("Storage cleanup failed for attachment key", exception);
        }
    }

    private void ensureAttachmentCapacity(Long postId, int additionalCount, long additionalSize) {
        List<PostAttachment> existingAttachments = postAttachmentMapper.findByPostId(postId);
        if (existingAttachments.size() + additionalCount > AttachmentUploadPolicy.MAX_FILES_PER_POST) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "첨부파일은 게시글당 최대 5개까지 가능합니다.");
        }
        long existingSize = existingAttachments.stream()
                .mapToLong(attachment -> attachment.getFileSize() == null ? 0L : attachment.getFileSize())
                .sum();
        if (existingSize + additionalSize > AttachmentUploadPolicy.MAX_TOTAL_SIZE) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "첨부파일 전체 크기는 20MB를 넘을 수 없습니다.");
        }
    }

    private CommunityPost requireActivePost(Long postId) {
        return communityPostMapper.findActiveById(postId)
                .orElseThrow(() -> notFound("게시글을 찾을 수 없습니다."));
    }

    private CommunityPost requireWritablePost(Long postId) {
        CommunityPost post = communityPostMapper.findById(postId)
                .orElseThrow(() -> notFound("게시글을 찾을 수 없습니다."));
        if ((!STATUS_ACTIVE.equals(post.getStatus()) && !STATUS_DRAFT.equals(post.getStatus()))
                || post.getDeletedAt() != null) {
            throw notFound("게시글을 찾을 수 없습니다.");
        }
        return post;
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

    private BusinessException notFound(String message) {
        return new BusinessException(ErrorCode.COMMON_NOT_FOUND, message);
    }

    public record AttachmentFile(PostAttachment attachment, Resource resource, String contentType) {}
}

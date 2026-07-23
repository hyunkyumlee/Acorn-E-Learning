package com.acorn.elearning.storage;

import java.io.IOException;
import java.io.InputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3ObjectStorage implements ObjectStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String keyPrefix;

    public S3ObjectStorage(S3Client s3Client, String bucket, String keyPrefix) {
        this.s3Client = s3Client;
        this.bucket = requireValue(bucket, "S3 bucket");
        this.keyPrefix = normalizePrefix(keyPrefix);
    }

    @Override
    public void put(String key, InputStream input, long contentLength, String contentType) throws IOException {
        if (contentLength < 0) {
            throw new StorageException("S3 upload content length must be known");
        }
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(physicalKey(key))
                            .contentType(contentType == null || contentType.isBlank()
                                    ? "application/octet-stream"
                                    : contentType)
                            .build(),
                    RequestBody.fromInputStream(input, contentLength));
        } catch (RuntimeException exception) {
            throw new StorageException("Failed to store S3 object", exception);
        }
    }

    @Override
    public StorageObject get(String key) throws IOException {
        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(physicalKey(key))
                    .build());
            GetObjectResponse metadata = response.response();
            return new StorageObject(
                    response,
                    contentType(metadata.contentType(), key),
                    metadata.contentLength() == null ? -1L : metadata.contentLength());
        } catch (NoSuchKeyException exception) {
            throw new StorageNotFoundException(key);
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new StorageNotFoundException(key);
            }
            throw new StorageException("Failed to read S3 object", exception);
        } catch (RuntimeException exception) {
            throw new StorageException("Failed to read S3 object", exception);
        }
    }

    @Override
    public void delete(String key) throws IOException {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(physicalKey(key))
                    .build());
        } catch (RuntimeException exception) {
            throw new StorageException("Failed to delete S3 object", exception);
        }
    }

    @Override
    public boolean exists(String key) throws IOException {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(physicalKey(key))
                    .build());
            return true;
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                return false;
            }
            throw new StorageException("Failed to check S3 object", exception);
        } catch (RuntimeException exception) {
            throw new StorageException("Failed to check S3 object", exception);
        }
    }

    private String physicalKey(String key) {
        return keyPrefix + StorageKey.normalize(key);
    }

    private String normalizePrefix(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = StorageKey.normalize(value).replaceAll("/+$", "");
        return normalized.isBlank() ? "" : normalized + "/";
    }

    private String contentType(String metadataType, String key) {
        return metadataType == null || metadataType.isBlank()
                ? extensionContentType(key)
                : metadataType;
    }

    private String extensionContentType(String key) {
        String normalized = key.toLowerCase(java.util.Locale.ROOT);
        if (normalized.endsWith(".png")) return "image/png";
        if (normalized.endsWith(".jpg") || normalized.endsWith(".jpeg")) return "image/jpeg";
        if (normalized.endsWith(".webp")) return "image/webp";
        if (normalized.endsWith(".pdf")) return "application/pdf";
        if (normalized.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }

    private String requireValue(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(label + " must be configured for S3 storage");
        }
        return value;
    }
}

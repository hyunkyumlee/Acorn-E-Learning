package com.acorn.elearning.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import org.junit.jupiter.api.Test;

class S3ObjectStorageTest {

    @Test
    void uses_private_bucket_prefix_and_preserves_metadata() throws Exception {
        CapturingS3Client fake = new CapturingS3Client();
        S3ObjectStorage storage = new S3ObjectStorage(fake.client(), "knowva-bucket", "prod/");
        byte[] content = "s3-content".getBytes(StandardCharsets.UTF_8);

        storage.put("community/1/file.txt", new ByteArrayInputStream(content), content.length, "text/plain");
        try (StorageObject stored = storage.get("community/1/file.txt")) {
            assertArrayEquals(content, stored.stream().readAllBytes());
            assertEquals("text/plain", stored.contentType());
            assertEquals(content.length, stored.contentLength());
        }

        assertEquals("knowva-bucket", fake.bucket);
        assertEquals("prod/community/1/file.txt", fake.key);
        assertEquals("text/plain", fake.contentType);
    }

    @Test
    void maps_s3_not_found_to_storage_not_found() throws Exception {
        CapturingS3Client fake = new CapturingS3Client();
        fake.missing = true;
        S3ObjectStorage storage = new S3ObjectStorage(fake.client(), "knowva-bucket", "prod/");

        assertThrows(StorageNotFoundException.class, () -> storage.get("missing.png"));
        assertFalse(storage.exists("missing.png"));
    }

    @Test
    void rejects_path_escape_before_s3_call() {
        CapturingS3Client fake = new CapturingS3Client();
        S3ObjectStorage storage = new S3ObjectStorage(fake.client(), "knowva-bucket", "prod/");

        assertThrows(StorageException.class, () -> storage.get("../secret.txt"));
        assertTrue(fake.key == null);
    }

    private static final class CapturingS3Client {
        private String bucket;
        private String key;
        private String contentType;
        private boolean missing;

        private S3Client client() {
            return (S3Client) Proxy.newProxyInstance(
                    S3Client.class.getClassLoader(),
                    new Class<?>[]{S3Client.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("putObject".equals(name)) {
                            var request = (software.amazon.awssdk.services.s3.model.PutObjectRequest) args[0];
                            bucket = request.bucket();
                            key = request.key();
                            contentType = request.contentType();
                            return PutObjectResponse.builder().build();
                        }
                        if ("getObject".equals(name)) {
                            var request = (software.amazon.awssdk.services.s3.model.GetObjectRequest) args[0];
                            bucket = request.bucket();
                            key = request.key();
                            if (missing) {
                                throw NoSuchKeyException.builder().build();
                            }
                            byte[] bytes = "s3-content".getBytes(StandardCharsets.UTF_8);
                            GetObjectResponse response = GetObjectResponse.builder()
                                    .contentType("text/plain")
                                    .contentLength((long) bytes.length)
                                    .build();
                            return new ResponseInputStream<>(response, new ByteArrayInputStream(bytes));
                        }
                        if ("headObject".equals(name)) {
                            if (missing) {
                                throw S3Exception.builder().statusCode(404).build();
                            }
                            return HeadObjectResponse.builder().build();
                        }
                        if ("deleteObject".equals(name)) {
                            return DeleteObjectResponse.builder().build();
                        }
                        if ("close".equals(name)) {
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }

        private Object defaultValue(Class<?> type) {
            if (!type.isPrimitive()) return null;
            if (type == boolean.class) return false;
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0F;
            if (type == double.class) return 0D;
            if (type == char.class) return '\0';
            return null;
        }
    }
}

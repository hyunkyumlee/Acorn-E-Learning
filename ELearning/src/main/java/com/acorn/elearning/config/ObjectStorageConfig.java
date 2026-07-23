package com.acorn.elearning.config;

import com.acorn.elearning.storage.LocalObjectStorage;
import com.acorn.elearning.storage.MirrorObjectStorage;
import com.acorn.elearning.storage.ObjectStorage;
import com.acorn.elearning.storage.S3ObjectStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class ObjectStorageConfig {

    @Bean(destroyMethod = "close")
    public S3Client s3Client(StorageProperties properties) {
        return S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    @Bean
    public ObjectStorage objectStorage(
            StorageProperties properties,
            UploadProperties uploadProperties,
            S3Client s3Client) {
        LocalObjectStorage local = new LocalObjectStorage(localBasePath(uploadProperties));
        String mode = properties.getMode() == null ? "local" : properties.getMode().trim().toLowerCase();
        return switch (mode) {
            case "local" -> local;
            case "mirror" -> new MirrorObjectStorage(local, s3Storage(properties, s3Client));
            case "s3" -> s3Storage(properties, s3Client);
            default -> throw new IllegalStateException("Unsupported KNOWVA_STORAGE_MODE: " + mode);
        };
    }

    private S3ObjectStorage s3Storage(StorageProperties properties, S3Client s3Client) {
        return new S3ObjectStorage(s3Client, properties.getS3Bucket(), properties.getS3Prefix());
    }

    private java.nio.file.Path localBasePath(UploadProperties properties) {
        String configured = properties == null ? null : properties.basePath();
        return java.nio.file.Path.of(configured == null || configured.isBlank() ? "./uploads" : configured);
    }
}

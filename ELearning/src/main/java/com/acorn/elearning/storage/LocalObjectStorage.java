package com.acorn.elearning.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class LocalObjectStorage implements ObjectStorage {

    private final Path basePath;

    public LocalObjectStorage(Path basePath) {
        this.basePath = basePath.toAbsolutePath().normalize();
    }

    @Override
    public void put(String key, InputStream input, long contentLength, String contentType) throws IOException {
        Path target = resolve(key);
        Files.createDirectories(target.getParent());
        try (InputStream source = input) {
            Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new StorageException("Failed to store local object", exception);
        }
    }

    @Override
    public StorageObject get(String key) throws IOException {
        Path target = resolve(key);
        if (!Files.isRegularFile(target)) {
            throw new StorageNotFoundException(key);
        }
        try {
            InputStream stream = Files.newInputStream(target);
            return new StorageObject(stream, contentType(target), Files.size(target));
        } catch (IOException exception) {
            throw new StorageException("Failed to read local object", exception);
        }
    }

    @Override
    public void delete(String key) throws IOException {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException exception) {
            throw new StorageException("Failed to delete local object", exception);
        }
    }

    @Override
    public boolean exists(String key) throws IOException {
        return Files.isRegularFile(resolve(key));
    }

    private Path resolve(String key) {
        Path target = basePath.resolve(StorageKey.normalize(key)).normalize();
        if (!target.startsWith(basePath)) {
            throw new StorageException("Storage key is outside the configured root");
        }
        return target;
    }

    private String contentType(Path target) {
        try {
            String detected = Files.probeContentType(target);
            if (detected != null && !detected.isBlank()) {
                return detected;
            }
        } catch (IOException ignored) {
            // 확장자 fallback으로 내려간다.
        }
        String name = target.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".pdf")) return "application/pdf";
        if (name.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }
}

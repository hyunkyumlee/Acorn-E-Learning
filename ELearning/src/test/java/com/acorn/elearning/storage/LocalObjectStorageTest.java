package com.acorn.elearning.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalObjectStorageTest {

    @Test
    void puts_reads_and_deletes_object(@TempDir Path tempDir) throws Exception {
        LocalObjectStorage storage = new LocalObjectStorage(tempDir);
        byte[] content = "hello storage".getBytes(StandardCharsets.UTF_8);

        storage.put("community/1/example.txt", new ByteArrayInputStream(content), content.length, "text/plain");
        StorageObject stored = storage.get("community/1/example.txt");

        try (stored) {
            assertArrayEquals(content, stored.stream().readAllBytes());
            assertEquals("text/plain", stored.contentType());
            assertEquals(content.length, stored.contentLength());
        }
        storage.delete("community/1/example.txt");
        assertFalse(storage.exists("community/1/example.txt"));
    }

    @Test
    void rejects_path_escape(@TempDir Path tempDir) {
        LocalObjectStorage storage = new LocalObjectStorage(tempDir);

        assertThrows(StorageException.class, () -> storage.put(
                "../outside.txt", new ByteArrayInputStream(new byte[]{1}), 1, "text/plain"));
        assertThrows(StorageException.class, () -> storage.get("/absolute.txt"));
    }

    @Test
    void missing_object_is_distinguishable(@TempDir Path tempDir) {
        LocalObjectStorage storage = new LocalObjectStorage(tempDir);

        assertThrows(StorageNotFoundException.class, () -> storage.get("missing.txt"));
    }
}

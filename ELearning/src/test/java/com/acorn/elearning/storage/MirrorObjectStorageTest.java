package com.acorn.elearning.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MirrorObjectStorageTest {

    @Test
    void reads_from_remote_first_and_falls_back_to_local() throws Exception {
        InMemoryStorage local = new InMemoryStorage();
        InMemoryStorage remote = new InMemoryStorage();
        MirrorObjectStorage mirror = new MirrorObjectStorage(local, remote);
        byte[] content = "mirror".getBytes(StandardCharsets.UTF_8);

        mirror.put("profile-images/user-1.png", new ByteArrayInputStream(content), content.length, "image/png");
        try (StorageObject stored = mirror.get("profile-images/user-1.png")) {
            assertArrayEquals(content, stored.stream().readAllBytes());
            assertEquals("image/png", stored.contentType());
        }

        remote.delete("profile-images/user-1.png");
        try (StorageObject stored = mirror.get("profile-images/user-1.png")) {
            assertArrayEquals(content, stored.stream().readAllBytes());
        }
    }

    private static final class InMemoryStorage implements ObjectStorage {
        private final Map<String, byte[]> objects = new HashMap<>();
        private final Map<String, String> contentTypes = new HashMap<>();

        @Override
        public void put(String key, InputStream input, long contentLength, String contentType) throws IOException {
            objects.put(key, input.readAllBytes());
            contentTypes.put(key, contentType);
        }

        @Override
        public StorageObject get(String key) {
            byte[] value = objects.get(key);
            if (value == null) {
                throw new StorageNotFoundException(key);
            }
            return new StorageObject(new ByteArrayInputStream(value), contentTypes.get(key), value.length);
        }

        @Override
        public void delete(String key) {
            objects.remove(key);
            contentTypes.remove(key);
        }

        @Override
        public boolean exists(String key) {
            return objects.containsKey(key);
        }
    }
}

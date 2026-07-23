package com.acorn.elearning.storage;

final class StorageKey {

    private StorageKey() {}

    static String normalize(String key) {
        if (key == null || key.isBlank()) {
            throw new StorageException("Storage key must not be blank");
        }
        String normalized = key.replace('\\', '/');
        while (normalized.startsWith("./")) {
            normalized = normalized.substring(2);
        }
        if (normalized.isBlank() || normalized.startsWith("/") || hasParentSegment(normalized)) {
            throw new StorageException("Storage key is outside the configured root");
        }
        return normalized;
    }

    private static boolean hasParentSegment(String key) {
        for (String segment : key.split("/")) {
            if ("..".equals(segment)) {
                return true;
            }
        }
        return false;
    }
}

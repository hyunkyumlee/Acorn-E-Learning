package com.acorn.elearning.storage;

public class StorageNotFoundException extends StorageException {

    public StorageNotFoundException(String key) {
        super("Storage object not found: " + key);
    }
}

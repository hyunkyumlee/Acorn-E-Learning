package com.acorn.elearning.storage;

import java.io.IOException;

public class MirrorObjectStorage implements ObjectStorage {

    private final ObjectStorage local;
    private final ObjectStorage remote;

    public MirrorObjectStorage(ObjectStorage local, ObjectStorage remote) {
        this.local = local;
        this.remote = remote;
    }

    @Override
    public void put(String key, java.io.InputStream input, long contentLength, String contentType) throws IOException {
        local.put(key, input, contentLength, contentType);
        try (StorageObject localObject = local.get(key)) {
            remote.put(key, localObject.stream(), localObject.contentLength(), localObject.contentType());
        } catch (RuntimeException | IOException exception) {
            try {
                local.delete(key);
            } catch (RuntimeException | IOException ignored) {
                // 원본 예외를 유지하고 reconciliation 대상이 된다.
            }
            throw exception;
        }
    }

    @Override
    public StorageObject get(String key) throws IOException {
        try {
            return remote.get(key);
        } catch (StorageException | IOException remoteFailure) {
            return local.get(key);
        }
    }

    @Override
    public void delete(String key) throws IOException {
        Exception remoteFailure = null;
        try {
            remote.delete(key);
        } catch (RuntimeException | IOException exception) {
            remoteFailure = exception;
        }
        try {
            local.delete(key);
        } catch (RuntimeException | IOException exception) {
            if (remoteFailure != null) {
                if (remoteFailure instanceof IOException ioException) {
                    throw ioException;
                }
                throw (RuntimeException) remoteFailure;
            }
            throw exception;
        }
        if (remoteFailure instanceof IOException ioException) {
            throw ioException;
        }
        if (remoteFailure instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
    }

    @Override
    public boolean exists(String key) throws IOException {
        try {
            return remote.exists(key) || local.exists(key);
        } catch (RuntimeException | IOException remoteFailure) {
            return local.exists(key);
        }
    }
}

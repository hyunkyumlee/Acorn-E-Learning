package com.acorn.elearning.storage;

import java.io.IOException;
import java.io.InputStream;

public interface ObjectStorage {

    void put(String key, InputStream input, long contentLength, String contentType) throws IOException;

    StorageObject get(String key) throws IOException;

    void delete(String key) throws IOException;

    boolean exists(String key) throws IOException;
}

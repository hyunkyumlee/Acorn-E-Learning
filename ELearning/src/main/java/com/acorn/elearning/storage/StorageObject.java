package com.acorn.elearning.storage;

import java.io.IOException;
import java.io.InputStream;

public record StorageObject(InputStream stream, String contentType, long contentLength) implements AutoCloseable {

    @Override
    public void close() throws IOException {
        stream.close();
    }
}

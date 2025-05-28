package com.abel.example.common.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * @auther wangxu
 * @date 2025/05/16
 */
public class ProgressInputStream extends InputStream {
    private final InputStream wrappedStream;
    private final ProgressTracker tracker;
    private long bytesRead = 0;

    public ProgressInputStream(InputStream wrappedStream, ProgressTracker tracker) {
        this.wrappedStream = wrappedStream;
        this.tracker = tracker;
    }

    @Override
    public int read() throws IOException {
        int data = wrappedStream.read();
        if (data != -1) {
            bytesRead++;
            tracker.update(bytesRead);
        }
        return data;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = wrappedStream.read(b, off, len);
        if (count != -1) {
            bytesRead += count;
            tracker.update(bytesRead);
        }
        return count;
    }
}

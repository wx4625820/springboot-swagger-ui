package com.abel.example.common.util;

import java.util.function.LongConsumer;

/**
 * @auther wangxu
 * @date 2025/05/16
 */
public class ProgressTracker {
    private final long totalBytes;
    private final LongConsumer progressCallback;

    public ProgressTracker(long totalBytes, LongConsumer callback) {
        this.totalBytes = totalBytes;
        this.progressCallback = callback;
    }

    public void update(long bytesRead) {
        if (progressCallback != null) {
            progressCallback.accept(bytesRead);
        }
    }
}

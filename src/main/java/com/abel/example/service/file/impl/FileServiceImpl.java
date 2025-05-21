package com.abel.example.service.file.impl;


import com.abel.example.common.util.Utils;
import com.abel.example.service.file.FileService;
import com.abel.example.service.file.util.ProgressInputStream;
import com.abel.example.service.file.util.ProgressTracker;
import com.google.common.collect.Maps;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @auther wangxu
 * @date 2025/05/16
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {
    private final Map<String, Double> progressCache = Maps.newConcurrentMap();
    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;


    @Override
    public String uploadFile(MultipartFile file) {
        try {
            // 检查存储桶是否存在，不存在则创建
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID() + fileExtension;

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            // 返回预签名下载URL（默认7天有效期）
            return getDownloadUrl(uniqueFileName);
        } catch (Exception e) {
            log.error("FileServiceImpl#uploadFile e:{}", e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    private void updateProgress(String taskId, double progress) {
        progressCache.put(taskId, progress);
    }

    @Override
    public double getProgress(String taskId) {
        return progressCache.getOrDefault(taskId, 0.0);
    }


    @Async
    @Override
    public CompletableFuture<String> asyncUploadFileWithProgress(MultipartFile file, String uniqueFileName) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. 将MultipartFile内容复制到内存或临时文件
            File tempFile = null;
            try {
                // 创建临时文件（系统会自动清理）
                tempFile = File.createTempFile("upload-", ".tmp");
                file.transferTo(tempFile); // 将内容写入临时文件

                // 2. 检查存储桶
                if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                }

                // 3. 创建进度跟踪器
                ProgressTracker tracker = new ProgressTracker(file.getSize(), bytesRead -> {
                    double progress = (double) bytesRead / file.getSize() * 100;
                    updateProgress(uniqueFileName, progress);
                });

                // 4. 使用try-with-resources确保流关闭
                try (InputStream fileStream = new FileInputStream(tempFile);
                     ProgressInputStream progressStream = new ProgressInputStream(fileStream, tracker)) {

                    // 5. 执行上传
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(uniqueFileName)
                                    .stream(progressStream, file.getSize(), -1)
                                    .contentType(file.getContentType())
                                    .build());

                    // 6. 返回下载URL
                    return minioClient.getPresignedObjectUrl(
                            GetPresignedObjectUrlArgs.builder()
                                    .method(Method.GET)
                                    .bucket(bucketName)
                                    .object(uniqueFileName)
                                    .expiry(7, TimeUnit.DAYS)
                                    .build());
                }
            } catch (Exception e) {
                updateProgress(uniqueFileName, -1);
                log.error("FileServiceImpl#asyncUploadFileWithProgress e:{}", e.getMessage());
                throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
            } finally {
                // 8. 清理临时文件
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        });
    }

    /**
     * 获取文件的下载URL（非签名方式）
     *
     * @param fileName 存储在MinIO中的文件名
     * @return 不带签名的下载URL
     */
    @Override
    public String getDownloadUrl(String fileName) {
        try {

            String minioHost = Utils.getLocalIP();
            // 构建不带签名的URL
            return String.format("http://%s:9000/%s/%s",
                    minioHost,
                    bucketName,
                    fileName);
        } catch (Exception e) {
            log.error("FileServiceImpl#getDownloadUrl e:{}", e.getMessage());
            throw new RuntimeException("生成下载链接失败: " + e.getMessage(), e);
        }
    }
}

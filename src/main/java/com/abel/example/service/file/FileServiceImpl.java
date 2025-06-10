package com.abel.example.service.file;


import com.abel.example.common.util.Utils;
import com.abel.example.common.util.ProgressInputStream;
import com.abel.example.common.util.ProgressTracker;
import com.abel.example.service.user.UserService;
import com.google.common.collect.Maps;
import io.minio.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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


    @Autowired
    private UserService userService;


    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String userName = userService.getUserName();
            deleteAllUserVideos(userName); // 删除用户所有视频

            String originalFilename = file.getOriginalFilename();
            String objectName = userName + "/" + originalFilename;

            return uploadToMinio(file.getInputStream(), file.getSize(), file.getContentType(), objectName);
        } catch (Exception e) {
            log.error("FileServiceImpl#uploadFile e:{}", e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    private void updateProgress(String objectName, double progress) {
        progressCache.put(objectName, progress);
    }

    @Override
    public double getProgress(String originalFilename) {
        return progressCache.getOrDefault(originalFilename, 0.0);
    }

    @Override
    public String getFile() {
        try {
            String userName = userService.getUserName() + "/";
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(userName)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();
                return Paths.get(objectName).getFileName().toString();
            }

        } catch (Exception e) {
            log.error("获取视频文件列表失败: {}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean deleteFile(String fileName) {
        try {
            String objectName = userService.getUserName() + "/" + fileName;
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());

            log.info("删除文件成功: {}", fileName);
            return true;
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage(), e);
            return false;
        }
    }


    public CompletableFuture<String> asyncUploadFileWithProgressWrapper(MultipartFile file, String originalFilename) {
        try {
            Path tempFilePath = Files.createTempFile(UUID.randomUUID() + originalFilename, ".tmp");
            file.transferTo(tempFilePath.toFile());
            String userName = userService.getUserName();
            deleteAllUserVideos(userName); // 删除用户所有视频
            return asyncUploadFileWithProgress(tempFilePath, file.getContentType(), file.getSize(), originalFilename);
        } catch (IOException e) {
            log.error("FileServiceImpl#asyncUploadFileWithProgressWrapper error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save temporary file", e);
        }
    }

    @Async
    public CompletableFuture<String> asyncUploadFileWithProgress(Path tempFilePath, String contentType, long size, String originalFilename) {
        String objectName = userService.getUserName() + "/" + originalFilename;

        return CompletableFuture.supplyAsync(() -> {
            try {
                ProgressTracker tracker = new ProgressTracker(size, bytesRead -> {
                    double progress = (double) bytesRead / size * 100;
                    updateProgress(objectName, progress);
                });

                try (InputStream fileStream = Files.newInputStream(tempFilePath);
                     ProgressInputStream progressStream = new ProgressInputStream(fileStream, tracker)) {

                    return uploadToMinio(progressStream, size, contentType, objectName);
                }
            } catch (Exception e) {
                updateProgress(objectName, -1);
                log.error("FileServiceImpl#asyncUploadFileWithProgress error: {}", e.getMessage(), e);
                throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
            } finally {
                try {
                    Files.deleteIfExists(tempFilePath);
                } catch (IOException e) {
                    log.error("无法删除临时文件: {}", tempFilePath, e);
                }
            }
        });
    }


    /**
     * 获取文件的下载URL（非签名方式）
     *
     * @param objectName 存储在MinIO中的文件名
     * @return 不带签名的下载URL
     */
    @Override
    public String getDownloadUrl(String objectName) {
        try {
            String minioHost = Utils.getLocalIP();
            // 构建不带签名的URL
            return String.format("http://%s:9001/%s/%s",
                    minioHost,
                    bucketName,
                    objectName);
        } catch (Exception e) {
            log.error("FileServiceImpl#getDownloadUrl e:{}", e.getMessage());
            throw new RuntimeException("生成下载链接失败: " + e.getMessage(), e);
        }
    }

    private String uploadToMinio(InputStream inputStream, long size, String contentType, String objectName) throws Exception {
        // 检查存储桶是否存在，不存在则创建
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        // 上传文件到 MinIO
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, size, -1)
                        .contentType(contentType)
                        .build());

        // 返回下载链接
        return getDownloadUrl(objectName);
    }


    private void deleteAllUserVideos(String userName) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(userName + "/")
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(item.objectName())
                        .build());
            }

            log.info("删除用户[{}]所有视频成功", userName);
        } catch (Exception e) {
            return;
        }
    }

}

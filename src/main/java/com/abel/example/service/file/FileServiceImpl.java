package com.abel.example.service.file;

import com.abel.example.common.util.Utils;
import com.abel.example.common.util.ProgressInputStream;
import com.abel.example.common.util.ProgressTracker;
import com.abel.example.service.user.UserService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private final Map<String, Double> progressCache = Maps.newConcurrentMap();

    /**
     * 用 S3Client 替代 MinioClient
     */
    @Autowired
    private S3Client s3;

    /**
     * 保留你原来的字段名/占位符，外部配置可继续沿用
     */
    @Value("${rustfs.bucket-name}")
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

            ListObjectsV2Request req = ListObjectsV2Request.builder().bucket(bucketName).prefix(userName).build();

            // 与原逻辑等价：拿到第一个对象名，然后返回文件名部分
            for (ListObjectsV2Response page : s3.listObjectsV2Paginator(req)) {
                for (S3Object obj : page.contents()) {
                    String objectName = obj.key();
                    return Paths.get(objectName).getFileName().toString();
                }
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
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(objectName).build());
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

                try (InputStream fileStream = Files.newInputStream(tempFilePath); ProgressInputStream progressStream = new ProgressInputStream(fileStream, tracker)) {

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
     * 说明：RustFS 的 S3 API 在 9000 端口，对象直链为 http://host:9000/{bucket}/{key}
     */
    @Override
    public String getDownloadUrl(String objectName) {
        try {
            String host = Utils.getLocalIP();
            return String.format("http://%s:9000/%s/%s", host, bucketName, objectName);
        } catch (Exception e) {
            log.error("FileServiceImpl#getDownloadUrl e:{}", e.getMessage());
            throw new RuntimeException("生成下载链接失败: " + e.getMessage(), e);
        }
    }

    /**
     * 名称保持不变：内部实现改为 S3 PutObject
     */
    private String uploadToMinio(InputStream inputStream, long size, String contentType, String objectName) throws Exception {
        // 检查/创建桶（与原逻辑一致）
        if (!bucketExists(bucketName)) {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }

        PutObjectRequest req = PutObjectRequest.builder().bucket(bucketName).key(objectName).contentType(contentType).build();

        s3.putObject(req, RequestBody.fromInputStream(inputStream, size));

        return getDownloadUrl(objectName);
    }

    private boolean bucketExists(String bucket) {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        } catch (S3Exception e) { // 404/403 等情况按不存在处理
            return e.statusCode() == 404 ? false : true;
        } catch (Exception e) {
            log.warn("检查桶失败，按不存在处理: {}", e.getMessage());
            return false;
        }
    }

    private void deleteAllUserVideos(String userName) {
        try {
            ListObjectsV2Request req = ListObjectsV2Request.builder().bucket(bucketName).prefix(userName + "/").build();

            for (ListObjectsV2Response page : s3.listObjectsV2Paginator(req)) {
                for (S3Object obj : page.contents()) {
                    s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(obj.key()).build());
                }
            }
            log.info("删除用户[{}]所有视频成功", userName);
        } catch (Exception e) {
            return;
        }
    }
}

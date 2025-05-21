package com.abel.example.service.file;


import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinIOServiceImpl implements FileService {


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
            return getDownloadUrl(uniqueFileName, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }


    /**
     * 获取文件的下载URL（默认7天有效期）
     *
     * @param fileName 存储在MinIO中的文件名
     * @return 带签名的下载URL
     */
    @Override
    public String getDownloadUrl(String fileName) {
        String downloadUrl = getDownloadUrl(fileName, 7, TimeUnit.DAYS);
        return downloadUrl;
    }

    /**
     * 获取文件的下载URL（预签名方式）
     *
     * @param fileName 存储在MinIO中的文件名
     * @param duration 有效期时长
     * @param unit     时间单位
     * @return 带签名的下载URL
     */
    private String getDownloadUrl(String fileName, long duration, TimeUnit unit) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry((int) unit.toSeconds(duration))
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("生成下载链接失败: " + e.getMessage(), e);
        }
    }
}

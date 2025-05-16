package com.abel.example.config;


import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QiniuConfig {
    @Value("${qiniu.accessKey}")
    private String accessKey;

    @Value("${qiniu.secretKey}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Bean
    public Auth auth() {
        return Auth.create(accessKey, secretKey);
    }

    @Bean
    public com.qiniu.storage.Configuration qiniuConfiguration() {
        // 根据您的七牛云存储区域选择
        return new com.qiniu.storage.Configuration(Region.autoRegion());
    }

    @Bean
    public UploadManager uploadManager() {
        return new UploadManager(qiniuConfiguration());
    }
}

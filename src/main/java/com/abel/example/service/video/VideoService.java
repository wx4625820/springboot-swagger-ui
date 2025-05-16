package com.abel.example.service.video;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @auther wangxu
 * @date 2025/05/16
 */
public interface VideoService {
    String uploadVideo(MultipartFile file);
}

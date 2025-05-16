package com.abel.example.service.qiniu;

import com.qiniu.common.QiniuException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface QiniuService {
    String uploadVideo(MultipartFile file) throws IOException;

    String getDownloadUrl(String fileName) throws QiniuException;
}

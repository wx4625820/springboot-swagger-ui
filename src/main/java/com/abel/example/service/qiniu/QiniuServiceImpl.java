package com.abel.example.service.qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.DownloadUrl;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class QiniuServiceImpl implements QiniuService {

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private Auth auth;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.domain}")
    private String domain;


    @Override
    public String uploadVideo(MultipartFile file) throws IOException {
        // 生成文件名
        String fileName = "videos/" + UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
        try {

            // 生成上传凭证
            String upToken = auth.uploadToken(bucket, fileName, 3600, new StringMap()
                    .put("mimeLimit", "video/*"));

            // 执行上传
            Response response = uploadManager.put(file.getInputStream(), fileName, upToken, null, null);

            if (response.isOK()) {
                return fileName;
            }
        } catch (QiniuException ex) {
            Response r = ex.response;
            throw new IOException("七牛云上传失败: " + r.toString());
        }
        return fileName;
    }

    @Override
    public String getDownloadUrl(String fileKey) throws QiniuException {
        // 创建公开下载链接（不带签名）
        DownloadUrl url = new DownloadUrl(domain, true, fileKey);

        // 可选：设置下载时显示的文件名（原文件名）
        // url.setAttname(getOriginalFileName(fileKey));

        // 可选：添加视频处理参数（如转码、截图等）
        // url.setFop("avthumb/mp4/vb/1.25m"); // 转码为1.25Mbps的MP4

        return url.buildURL();
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}

package com.abel.example.service.video;

import com.abel.example.service.qiniu.QiniuService;
import com.abel.example.common.util.CommonUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * @auther wangxu
 * @date 2025/05/16
 */
@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private QiniuService qiniuService;

    @Override
    public String uploadVideo(MultipartFile file) {
        String result = null;
        try {
            // 检查文件类型
            if (!file.getContentType().startsWith("video/")) {
                return null;
            }
            // 上传文件
            String fileName = qiniuService.uploadVideo(file);

            String str = qiniuService.getDownloadUrl(fileName);


            System.out.println(str);

            if (fileName != null) {
                result = "http://swghrfkqt.hd-bkt.clouddn.com/" + fileName;
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        return result;
    }
}

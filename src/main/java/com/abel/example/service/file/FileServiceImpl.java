package com.abel.example.service.file;

import com.alibaba.fastjson.JSON;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;


@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileStorageService fileStorageService;//注入实列


    @Override
    public String uploadFile(MultipartFile file) {
        try {
            FileInfo fileInfo = fileStorageService.of(file).setSaveFilename(file.getOriginalFilename()).upload();
            return Objects.nonNull(fileInfo) ? fileInfo.getUrl() : null;
        } catch (Exception e) {
            System.out.println("上传视频出错：" + e.getMessage());
            return null;
        }
    }
}

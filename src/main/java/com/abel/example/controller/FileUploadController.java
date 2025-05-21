package com.abel.example.controller;

import com.abel.example.common.enums.ResultEnum;
import com.abel.example.model.response.ResponseMessage;
import com.abel.example.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * @auther wangxu
 * @date 2025/05/16
 */
@RestController
@RequestMapping("file")
@Tag(name = "视频管理", description = "视频上传、下载等操作")
@Slf4j
public class FileUploadController {

    @Autowired
    @Qualifier("fileServiceImpl")
    private FileService fileService;


    @Operation(summary = "上传视频文件")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "upload")
    public ResponseMessage uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseMessage.error("上传的文件为空");
        }

        String result = fileService.uploadFile(file);
        if (!StringUtils.isEmpty(result)) {
            return ResponseMessage.success(result);
        } else {
            return ResponseMessage.error(ResultEnum.SYSTEM_ERROR.getMsg());
        }
    }

    @Operation(summary = "异步上传视频文件")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "async-upload")
    public ResponseMessage uploadFileWithProgress(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseMessage.error("上传的文件为空");
        }
        try {
            String taskId = UUID.randomUUID().toString();

            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = taskId + fileExtension;
            fileService.uploadFileWithProgress(file, uniqueFileName)
                    .whenComplete((url, ex) -> {
                        if (ex != null) {
                            log.error("uploadFileWithProgress#uploadFileWithProgress fail:{}", ex.getMessage());
                        } else {
                            log.info("uploadFileWithProgress#uploadFileWithProgress success,url:{}", url);
                        }
                    });

            return ResponseMessage.success(uniqueFileName);
        } catch (Exception e) {
            log.error("上传初始化失败, msg:{}", e.getMessage());
            return ResponseMessage.error(ResultEnum.SYSTEM_ERROR.getCode(), "上传初始化失败: " + e.getMessage());
        }
    }


    @Operation(summary = "视频上传进度")
    @GetMapping(value = "upload-progress")
    public ResponseMessage getProgress(@RequestParam String uniqueFileName) {
        double progress = fileService.getProgress(uniqueFileName);
        if (progress < 0) {
            return ResponseMessage.success(-1.0);
        }
        return ResponseMessage.success(progress);
    }

    @Operation(summary = "获取文件的下载URL（默认7天有效期）")
    @GetMapping(value = "file-download-url")
    public ResponseMessage getDownloadUrl(@RequestParam String uniqueFileName) {
        try {
            String url = fileService.getDownloadUrl(uniqueFileName);
            return ResponseMessage.success(url);
        } catch (Exception e) {
            log.error("FileUploadController#getDownloadUrl:{}", e.getMessage());
            return ResponseMessage.error(e.getMessage());
        }
    }
}

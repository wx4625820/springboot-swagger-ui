package com.abel.example.controller;

import com.abel.example.common.enums.ResultEnum;
import com.abel.example.model.response.ResponseMessage;
import com.abel.example.service.file.FileService;
import com.abel.example.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * @auther wangxu
 * @date 2025/05/16
 */
@RestController
@RequestMapping("/file")
@Tag(name = "视频管理", description = "视频上传、下载等操作")
@Slf4j
public class FileController {

    @Autowired
    @Qualifier("fileServiceImpl")
    private FileService fileService;

    @Autowired
    private UserService userService;


    @Operation(summary = "同步上传视频文件")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/upload")
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/async-upload")
    public ResponseMessage asyncUploadFileWithProgress(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseMessage.error("上传的文件为空");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            fileService.asyncUploadFileWithProgressWrapper(file, originalFilename)
                    .whenComplete((url, ex) -> {
                        if (ex != null) {
                            log.error("FileUploadController#asyncUploadFileWithProgress fail:{}", ex.getMessage());
                        } else {
                            log.info("FileUploadController#asyncUploadFileWithProgress success,url:{}", url);
                        }
                    });

            return ResponseMessage.success(originalFilename);
        } catch (Exception e) {
            log.error("上传初始化失败, msg:{}", e.getMessage());
            return ResponseMessage.error(ResultEnum.SYSTEM_ERROR.getCode(), "上传初始化失败: " + e.getMessage());
        }
    }


    @Operation(summary = "视频上传进度(异步接口专用)")
    @GetMapping(value = "/upload-progress")
    public ResponseMessage getProgress(@RequestParam String originalFilename) {
        String objectName = userService.getUserName() + "/" + originalFilename;
        double progress = fileService.getProgress(objectName);
        if (progress < 0) {
            return ResponseMessage.success(-1.0);
        }
        return ResponseMessage.success(progress);
    }

    @Operation(summary = "根据视频名获取下载URL")
    @GetMapping(value = "/file-download-url")
    public ResponseMessage getDownloadUrl(@RequestParam String originalFilename) {
        try {
            if (!originalFilename.equals(fileService.getFile())) {
                return ResponseMessage.error(ResultEnum.NOT_FOUND.getCode(), ResultEnum.NOT_FOUND.getMsg());
            }

            String objectName = userService.getUserName() + "/" + originalFilename;

            String url = fileService.getDownloadUrl(objectName);
            return ResponseMessage.success(url);
        } catch (Exception e) {
            log.error("FileUploadController#getDownloadUrl:{}", e.getMessage());
            return ResponseMessage.error(e.getMessage());
        }
    }


    @Operation(summary = "获取已经上传的视频")
    @GetMapping(value = "/get-file")
    public ResponseMessage getFile() {
        try {
            String url = fileService.getFile();
            return ResponseMessage.success(url);
        } catch (Exception e) {
            log.error("FileUploadController#getFile:{}", e.getMessage());
            return ResponseMessage.error(e.getMessage());
        }
    }

    @Operation(summary = "删除已经上传的视频")
    @DeleteMapping("/delete")
    public ResponseMessage deleteVideo(@RequestParam String fileName) {
        boolean success = fileService.deleteFile(fileName);
        if (success) {
            return ResponseMessage.success("文件删除成功");
        } else {
            return ResponseMessage.error("文件删除失败");
        }
    }
}

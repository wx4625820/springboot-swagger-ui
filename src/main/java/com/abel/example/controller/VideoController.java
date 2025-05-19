package com.abel.example.controller;

import com.abel.example.common.enums.ResultEnum;
import com.abel.example.model.response.ResponseMessage;
import com.abel.example.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/video")
@Tag(name = "视频管理", description = "视频上传、下载等操作") // 控制器级别的描述
public class VideoController {

    @Autowired
    private FileService fileService;


    @Operation(
            summary = "上传视频文件",
            description = "接受视频文件并保存到服务器指定目录"
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseMessage<Object> uploadFile(@RequestParam("file") MultipartFile file) {
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
}

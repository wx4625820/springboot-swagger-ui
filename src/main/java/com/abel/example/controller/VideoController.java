package com.abel.example.controller;

import com.abel.example.common.enums.ResultEnum;
import com.abel.example.model.entity.User;
import com.abel.example.model.response.ResponseMessage;
import com.abel.example.service.video.VideoService;
import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/video")
@Tag(name = "视频管理", description = "视频上传、下载等操作") // 控制器级别的描述
public class VideoController {

    @Autowired
    private VideoService videoService;


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

        String result = videoService.uploadVideo(file);
        if (!StringUtils.isEmpty(result)) {
            return ResponseMessage.success(result);
        } else {
            return ResponseMessage.error(ResultEnum.SYSTEM_ERROR.getMsg());
        }
    }

    /**
     * 测试接口
     *
     * @return
     */
    @Operation(
            summary = "测试接口",
            description = "用来测试"
    )
    @ResponseBody
    @PostMapping("/test")
    public ResponseEntity query() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
//        request.getSession().setAttribute("user", new User()); //测试，手动添加用户登录的session
        User user = (User) request.getSession().getAttribute("user");
        return new ResponseEntity<>(JSON.toJSONString(user), HttpStatus.OK);
    }
}

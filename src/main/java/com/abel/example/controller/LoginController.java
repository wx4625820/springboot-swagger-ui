package com.abel.example.controller;


import com.abel.example.common.enums.ResultEnum;
import com.abel.example.common.util.Utils;
import com.abel.example.model.dto.RequestDTO.ForgotPasswordRequest;
import com.abel.example.model.dto.RequestDTO.ResetPasswordRequest;
import com.abel.example.model.response.ResponseMessage;
import com.abel.example.model.entity.User;
import com.abel.example.service.mail.MailService;
import com.abel.example.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

/**
 * @auther wangxu
 * @date 2025/05/16
 */
@RestController
@RequestMapping(value = "/user")
@Tag(name = "用户管理", description = "用户增删改查等")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    /**
     * 登录
     *
     * @return
     */
    @Operation(summary = "用户登录")
    @PostMapping(value = "/login")
    public ResponseMessage login(@RequestBody User user) {
        String email = user.getEmail();
        String password = user.getPassword();
        if (!Utils.isValidEmail(email)) {
            return ResponseMessage.error(
                    ResultEnum.BAD_REQUEST.getCode(),
                    "邮箱格式不正确！"
            );
        }

        User userDb = userService.getUserByEmail(email);
        if (userDb != null) {
            if (userDb.getPassword().equals(password)) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                attributes.getRequest().getSession().setAttribute("user", userDb); //将登陆用户信息存入到session域对象中
                return ResponseMessage.success("email:" + email + "登录成功");
            }
        }
        return ResponseMessage.error("email:" + email + "登录失败");
    }

    /**
     * 注册
     *
     * @return
     */
    @Operation(summary = "用户注册")
    @PostMapping(value = "/register")
    public ResponseMessage register(@RequestBody User user) {
        try {
            String username = user.getUsername();
            String email = user.getEmail();
            String password = user.getPassword();

            if (!Utils.isValidEmail(email)) {
                return ResponseMessage.error(
                        ResultEnum.BAD_REQUEST.getCode(),
                        "邮箱格式不正确！"
                );
            }
            User userDb = userService.getUserByUserName(username);
            if (Objects.nonNull(userDb)) {
                return ResponseMessage.error(ResultEnum.BAD_REQUEST.getCode(), username + "已注册，请更换用户名！");
            }

            userService.create(new User(username, email, password));
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            attributes.getRequest().getSession().setAttribute("user", new User(username, email, password)); //将登陆用户信息存入到session域对象中
            return ResponseMessage.success(username + "注册成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseMessage.error(ResultEnum.SYSTEM_ERROR.getMsg());
    }


    @Operation(summary = "忘记密码")
    @PostMapping("/forgot-password")
    public ResponseMessage forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail();
        if (!Utils.isValidEmail(email)) {
            return ResponseMessage.error(
                    ResultEnum.BAD_REQUEST.getCode(),
                    "邮箱格式不正确！"
            );
        }

        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseMessage.error("邮箱未注册");
        }

        // 生成随机验证码（6位）
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 存储验证码、邮箱和时间戳（毫秒）
        attr.getRequest().getSession().setAttribute("reset_code", code);
        attr.getRequest().getSession().setAttribute("reset_email", email);
        attr.getRequest().getSession().setAttribute("reset_code_time", System.currentTimeMillis());

        // 发送验证码邮件
        String subject = "找回密码验证码";
        String text = "您正在找回密码，验证码为：" + code + "（10分钟内有效）";
        mailService.sendSimpleMail(email, subject, text);

        return ResponseMessage.success("验证码已发送，请检查邮箱");
    }

    @Operation(summary = "重设密码")
    @PostMapping("/reset-password")
    public ResponseMessage resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        String newPassword = request.getNewPassword();

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String sessionCode = (String) attr.getRequest().getSession().getAttribute("reset_code");
        String sessionEmail = (String) attr.getRequest().getSession().getAttribute("reset_email");
        Long codeTime = (Long) attr.getRequest().getSession().getAttribute("reset_code_time");

        if (sessionCode == null || sessionEmail == null || codeTime == null) {
            return ResponseMessage.error("请先获取验证码");
        }

        // 校验验证码是否过期（10分钟有效）
        long now = System.currentTimeMillis();
        if (now - codeTime > 10 * 60 * 1000) {
            // 清除过期验证码信息
            attr.getRequest().getSession().removeAttribute("reset_code");
            attr.getRequest().getSession().removeAttribute("reset_email");
            attr.getRequest().getSession().removeAttribute("reset_code_time");
            return ResponseMessage.error("验证码已过期，请重新获取");
        }

        // 验证邮箱和验证码是否匹配
        if (!sessionCode.equals(code) || !sessionEmail.equals(email)) {
            return ResponseMessage.error("验证码错误");
        }

        // 修改密码
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseMessage.error("邮箱未注册");
        }
        user.setPassword(newPassword);
        userService.update(user); // 记得在 UserService 中添加 update 方法

        // 清除验证码信息
        attr.getRequest().getSession().removeAttribute("reset_code");
        attr.getRequest().getSession().removeAttribute("reset_email");
        attr.getRequest().getSession().removeAttribute("reset_code_time");

        return ResponseMessage.success("密码重置成功");
    }
}

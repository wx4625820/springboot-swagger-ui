package com.abel.example.controller;


import com.abel.example.common.enums.ResultEnum;
import com.abel.example.model.response.ResponseMessage;
import com.abel.example.model.entity.User;
import com.abel.example.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @auther wangxu
 * @date 2025/05/16
 */
@RestController
@RequestMapping(value = "users")
@Tag(name = "用户管理", description = "用户增删改查等")
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */
    @Operation(summary = "用户登录")
    @PostMapping(value = "login")
    public ResponseMessage login(@RequestParam("username") String username, @RequestParam("password") String password) {
        System.out.println("username:" + username + ", password:" + password);
        User user = userService.getUserByUserName(username);
        if (user != null) {
            if (user.getPassword().equals(password)) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                attributes.getRequest().getSession().setAttribute("user", user); //将登陆用户信息存入到session域对象中
                return ResponseMessage.success("username:" + username + "登录成功");
            }
        }
        return ResponseMessage.error("username:" + username + "登录失败");
    }

    /**
     * 注册
     *
     * @param username
     * @param password
     * @return
     */
    @Operation(summary = "用户注册")
    @PostMapping(value = "register")
    public ResponseMessage register(@RequestParam("username") String username, @RequestParam("password") String password) {
        try {
            userService.create(new User(username, password));
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            attributes.getRequest().getSession().setAttribute("user", new User(username, password)); //将登陆用户信息存入到session域对象中
            return ResponseMessage.success(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseMessage.error(ResultEnum.SYSTEM_ERROR.getMsg());
    }

    /**
     * 登录页 TODO
     *
     * @return
     */
    @Operation(summary = "获取登录页信息")
    @GetMapping(value = "login")
    public String login() {
        return "home/login";
    }

    /**
     * 注册页面 TODO
     *
     * @return
     */
    @Operation(summary = "获取注册页信息")
    @GetMapping(value = "register")
    public String register() {
        return "home/register";
    }

    /**
     * 注销
     *
     * @return
     */
    @Operation(summary = "用户注销")
    @PostMapping(value = "logout")
    public String logout() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        attributes.getRequest().getSession().removeAttribute("user");
        return "home/login";
    }
}

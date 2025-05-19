package com.abel.example.controller;


import com.abel.example.common.enums.ResultEnum;
import com.abel.example.model.response.ResponseMessage;
import com.abel.example.model.entity.User;
import com.abel.example.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/users")
@Tag(name = "用户管理")
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
    @Operation(
            summary = "用户登录",
            description = "通过用户名和密码进行登录验证，成功后将用户信息存入session"
    )
    @PostMapping("/login")
    @ResponseBody
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
     * 注销
     *
     * @return
     */
    @Operation(
            summary = "用户注销",
            description = "清除当前用户的登录会话"
    )
    @PostMapping("/logout")
    public String logout() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        attributes.getRequest().getSession().removeAttribute("user");
        return "home/login";
    }

    /**
     * 注册
     *
     * @param username
     * @param password
     * @return
     */
    @Operation(
            summary = "用户注册",
            description = "创建新用户账号"
    )
    @ResponseBody
    @PostMapping("/register")
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
     * 登录页
     *
     * @return
     */
    @Operation(summary = "获取登录页信息")
    @GetMapping("/login")
    public String login() {
        return "home/login";
    }

    /**
     * 注册页面
     *
     * @return
     */
    @Operation(summary = "获取注册页信息")
    @GetMapping("/register")
    public String register() {
        return "home/register";
    }


    /**
     * 查询用户
     *
     * @param username
     * @return
     */
    @Operation(
            summary = "查询用户",
            description = "根据用户名查询用户"
    )
    @ResponseBody
    @PostMapping("/query")
    public ResponseMessage query(@RequestParam("username") String username) {
        try {
            User user = userService.getUserByUserName(username);
            return ResponseMessage.success("username:" + user.getUsername() + ",password:" + user.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseMessage.error(ResultEnum.SYSTEM_ERROR.getMsg());
    }
}

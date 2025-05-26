package com.abel.example.model.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @auther wangxu
 * @date 2025/05/16
 */
@Data
public class User implements Serializable {

    private Long id; //编号
    private String username; //用户名
    private String password; //密码
    private String email;

    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}

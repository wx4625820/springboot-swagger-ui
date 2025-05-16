package com.abel.example.dao;

import com.abel.example.model.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;


/**
 * @auther wangxu
 * @date 2025/05/16
 */
public interface UserMapper {


    @Select("SELECT id, username, password FROM tb_user WHERE username = #{username}")
    User findByName(String name);


    @Insert("INSERT INTO tb_user(username, password) VALUES (#{username}, #{password})")
    void create(User user);
}

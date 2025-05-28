package com.abel.example.dao;

import com.abel.example.model.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


/**
 * @auther wangxu
 * @date 2025/05/16
 */
@Mapper
public interface UserMapper {

    @Select("SELECT id, username, email, password FROM tb_user WHERE email = #{email}")
    User findUserByEmail(String email);


    @Select("SELECT id, username, password, email FROM tb_user WHERE username = #{username}")
    User findByName(String name);

    @Insert("INSERT INTO tb_user(username, email, password) VALUES (#{username},#{email},#{password})")
    void create(User user);

    @Update("UPDATE tb_user SET password = #{password} WHERE email = #{email}")
    void update(User user);
}

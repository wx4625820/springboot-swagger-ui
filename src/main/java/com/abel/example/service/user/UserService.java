package com.abel.example.service.user;

import com.abel.example.model.entity.User;


/**
 * @auther wangxu
 * @date 2025/05/16
 */
public interface UserService {

    /**
     * Gets the user by name.
     *
     * @param username the user name
     * @return the user by name
     */
    User getUserByUserName(String username);


    void create(User user);

}

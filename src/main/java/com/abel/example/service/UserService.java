package com.abel.example.service;

import com.abel.example.bean.User;

import java.util.List;

/**
 * The Interface UserService.
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

package com.abel.example.service.user;

import com.abel.example.bean.User;


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

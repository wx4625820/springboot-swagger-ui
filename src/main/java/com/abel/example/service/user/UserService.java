package com.abel.example.service.user;

import com.abel.example.model.entity.User;


public interface UserService {

    User getUserByEmail(String email);

    User getUserByUserName(String userName);

    void create(User user);

    String getUserName();

    void update(User user);
}

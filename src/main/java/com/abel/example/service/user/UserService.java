package com.abel.example.service.user;

import com.abel.example.model.entity.User;



public interface UserService {

    User getUserByUserName(String username);

    void create(User user);

}

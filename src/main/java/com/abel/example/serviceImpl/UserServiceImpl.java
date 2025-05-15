package com.abel.example.serviceImpl;

import com.abel.example.bean.User;
import com.abel.example.dao.UserMapper;
import com.abel.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author abel
 * @ClassName UserServiceImpl
 * @date 2016年11月10日
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * @param username
     * @return
     */
    @Override
    public User getUserByUserName(String username) {
        return userMapper.findByName(username);
    }

    @Override
    public void create(User user) {
        userMapper.create(user);
    }
}

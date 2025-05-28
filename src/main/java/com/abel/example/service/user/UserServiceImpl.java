package com.abel.example.service.user;

import com.abel.example.model.entity.User;
import com.abel.example.dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * @auther wangxu
 * @date 2025/05/16
 */


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * @param email
     * @return
     */
    @Override
    public User getUserByEmail(String email) {
        return userMapper.findUserByEmail(email);
    }

    /**
     * @param userName
     * @return
     */
    @Override
    public User getUserByUserName(String userName) {
        return userMapper.findByName(userName);
    }

    @Override
    public void create(User user) {
        userMapper.create(user);
    }

    @Override
    public void update(User user) {
        userMapper.update(user);
    }

    @Override
    public String getUserName() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        User user = (User) attributes.getRequest().getSession().getAttribute("user");
        return user.getUsername();
    }

}

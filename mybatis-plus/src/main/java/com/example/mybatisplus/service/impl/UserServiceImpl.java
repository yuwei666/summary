package com.example.mybatisplus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mybatisplus.domain.User;
import com.example.mybatisplus.mapper.UserMapper;
import com.example.mybatisplus.service.UserService;
import lombok.Data;
import org.springframework.stereotype.Service;

/**
* @author Thinkpad
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-11-15 18:12:34
*/
@Data
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    private final UserMapper userMapper;

    @Override
    public User getUser(String id) {
        return userMapper.selectById2(id);
    }
}





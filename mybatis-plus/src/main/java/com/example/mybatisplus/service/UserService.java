package com.example.mybatisplus.service;

import com.example.mybatisplus.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Thinkpad
* @description 针对表【user】的数据库操作Service
* @createDate 2024-11-15 18:12:34
*/
public interface UserService extends IService<User> {

    User getUser(String id);

}

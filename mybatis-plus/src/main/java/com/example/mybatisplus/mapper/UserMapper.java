package com.example.mybatisplus.mapper;

import com.example.mybatisplus.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Thinkpad
* @description 针对表【user】的数据库操作Mapper
* @createDate 2024-11-15 18:12:34
* @Entity generator.domain.User
*/
public interface UserMapper extends BaseMapper<User> {

    User selectById2(String id);

}





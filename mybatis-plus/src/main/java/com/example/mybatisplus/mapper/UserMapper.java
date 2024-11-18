package com.example.mybatisplus.mapper;

import com.example.mybatisplus.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Thinkpad
* @description 针对表【user】的数据库操作Mapper
* @createDate 2024-11-15 18:12:34
* @Entity generator.domain.User
*/
public interface UserMapper extends BaseMapper<User> {

    void updateUserNameById(@Param("userName") String userName, @Param("id") Integer id);

    User selectById2(String id);

}





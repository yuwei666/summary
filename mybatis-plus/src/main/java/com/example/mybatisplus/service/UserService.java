package com.example.mybatisplus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mybatisplus.domain.PageDTO;
import com.example.mybatisplus.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mybatisplus.domain.UserQuery;
import com.example.mybatisplus.domain.UserVO;

import java.util.List;

/**
* @author Thinkpad
* @description 针对表【user】的数据库操作Service
* @createDate 2024-11-15 18:12:34
*/
public interface UserService extends IService<User> {

    User getUser(String id);

    User getUser();

    /**
     * 通用分页实体查询
     */
    Page<User> pageQuery();

    /**
     * 一个查询要弄一堆类，麻烦的要死
     * @param query
     * @return
     */
    PageDTO<UserVO> userPageQuery(UserQuery query);

   void optimisticLock();
}

package com.example.mybatisplus.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mybatisplus.domain.PageDTO;
import com.example.mybatisplus.domain.User;
import com.example.mybatisplus.domain.UserQuery;
import com.example.mybatisplus.domain.UserVO;
import com.example.mybatisplus.mapper.UserMapper;
import com.example.mybatisplus.service.UserService;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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

    /**
     * 通过wrapper查询
     * @return
     */
    @Override
    public User getUser() {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        // 注意不要写成queryWrapper.equals
        queryWrapper.eq(User::getId, "1");
        User one = this.getOne(queryWrapper);
        return one;
    }

    /**
     * 通用分页实体查询
     */
    @Override
    public Page<User> pageQuery() {
        Page<User> page = new Page<>();
        // 设置分页参数
        page.setCurrent(0).setSize(2);
        // 设置排序
        page.addOrder(OrderItem.asc("id"));
        this.page(page);

        // 页数
        long pages = page.getPages();
        // 总数
        long total = page.getTotal();

        List<User> records = page.getRecords();
        records.forEach(System.out::println);

        return page;
    }

    @Override
    public PageDTO<UserVO> userPageQuery(UserQuery query) {

        Page<User> page = Page.of(query.getPageNo(), query.getPageSize());
        page.addOrder(OrderItem.asc("id"));
        this.page(page);

        List<User> users = page.getRecords();
        if(CollectionUtils.isEmpty(users)){
            return new PageDTO<>(page.getTotal(), page.getPages(), Collections.emptyList());
        }

        List<UserVO> list = BeanUtil.copyToList(users, UserVO.class);
        return new PageDTO<>(page.getTotal(), page.getPages(), list);
    }
}





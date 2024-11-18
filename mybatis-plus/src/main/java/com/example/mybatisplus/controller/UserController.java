package com.example.mybatisplus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mybatisplus.domain.PageDTO;
import com.example.mybatisplus.domain.User;
import com.example.mybatisplus.domain.UserQuery;
import com.example.mybatisplus.domain.UserVO;
import com.example.mybatisplus.service.UserService;
import com.example.utils.common.Result;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Data
@RestController
public class UserController {

    private final UserService userService;

    @GetMapping("getUser")
    public Result getUser() {
        User user = userService.getUser();
        return Result.ok(user);
    }

    @ApiOperation("通用分页查询接口")
    @GetMapping("pageQuery")
    public Result testPageQuery() {
        Page<User> page = userService.pageQuery();
        return Result.ok(page);
    }

    /**
     * http://localhost:8086/userPageQuery?pageNo=0&pageSize=2
     */
    @ApiOperation("根据条件分页查询用户接口")
    @GetMapping("userPageQuery")
    public PageDTO userPageQuery(UserQuery query) {
        PageDTO<UserVO> page = userService.userPageQuery(query);
        return page;
    }

}

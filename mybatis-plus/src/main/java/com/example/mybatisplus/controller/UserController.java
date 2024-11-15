package com.example.mybatisplus.controller;

import com.example.mybatisplus.domain.User;
import com.example.mybatisplus.service.UserService;
import com.example.utils.common.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Data
@RestController
public class UserController {

    private final UserService userService;

    @GetMapping("getUser")
    public Result getUser() {
        User user = userService.getUser("1");
        return Result.ok(user);
    }

}

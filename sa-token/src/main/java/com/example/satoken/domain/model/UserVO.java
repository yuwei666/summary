package com.example.satoken.domain.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserVO {

    @NotBlank(message = "姓名不能为空")
    private String name;

}

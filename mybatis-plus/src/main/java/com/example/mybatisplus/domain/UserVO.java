package com.example.mybatisplus.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户VO实体")
public class UserVO {

    @ApiModelProperty("用户id")
    private Integer id;

    @ApiModelProperty("用户姓名")
    private String userName;

}

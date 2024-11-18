package com.example.mybatisplus.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询条件实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "用户查询条件实体")
public class UserQuery extends PageQuery {

    @ApiModelProperty("用户姓名")
    private Integer userName;

}

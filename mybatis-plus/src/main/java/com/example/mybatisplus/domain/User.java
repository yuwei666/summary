package com.example.mybatisplus.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName user
 */
// 当数据库中table的名称和实体类名称不一致时，一定需要使用注解 @TableName 进行显示声明表的名称。一致可以省略
@TableName(value ="user")
@Data
public class User implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    // 当数据库中table的名称和实体类名称不一致时，一定需要使用注解 @TableField 进行显示声明表的名称。一致可以省略
    @TableField(value = "user_name")
    private String userName;

}
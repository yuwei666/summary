package com.example.mybatisplus.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@ApiModel("分页结果实体")
@AllArgsConstructor
public class PageDTO<T> {

    @ApiModelProperty("总条数")
    private Long total;

    @ApiModelProperty("总页数")
    private Long pages;

    @ApiModelProperty("当前页数据集合")
    private List<T> list;

}

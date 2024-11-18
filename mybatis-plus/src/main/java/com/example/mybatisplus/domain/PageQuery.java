package com.example.mybatisplus.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "分页查询实体类")
public class PageQuery {

    @ApiModelProperty("页码")
    private Integer pageNo;

    @ApiModelProperty("页面大小")
    private Integer pageSize;

    @ApiModelProperty("排序字段")
    private String sortBy;

    @ApiModelProperty("是否升序")
    private Boolean isAsc;

}

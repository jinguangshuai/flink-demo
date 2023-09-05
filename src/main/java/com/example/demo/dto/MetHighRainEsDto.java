package com.example.demo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Auther：jinguangshuai
 * @Data：2023/8/3 - 08 - 03 - 10:36
 * @Description:com.example.demo.dto
 * @version:1.0
 */
@ApiModel(description = "暴雨解析查询对象")
@Data
public class MetHighRainEsDto {

    @ApiModelProperty(value = "预警开始时间", required = true, example = "2021-12-26 12:00:00")
    @NotBlank(message = "must not be black")
    String beginTime;

    @ApiModelProperty(value = "预警结束时间", required = true, example = "2021-12-27 12:00:00")
    @NotBlank(message = "must not be black")
    String endTime;

    @ApiModelProperty(value = "预警级别", example = "1")
    String value;

    @ApiModelProperty(value = "省份编码", example = "100000")
    String province;

    @ApiModelProperty(value = "省份名称", example = "中国")
    String provinceName;

    @ApiModelProperty(value = "区域坐标", example = "POLYGON ((121.16788823308333 23.45554198284667, 121.16729922084184 23.45631269160915, 121.16679237504343 23.45702727288238, 121.16636274309427 23.457687519614925, 121.16600537014122 23.458295227614133))")
    String geom;

    @ApiModelProperty(value = "当前页", required = false, allowableValues = "10", example = "1")
    Integer pageNo;

    @ApiModelProperty(value = "每页大小", required = false, allowableValues = "10", example = "20")
    Integer pageSize;


}


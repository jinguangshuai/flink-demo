package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Auther：jinguangshuai
 * @Data：2023/7/26 - 07 - 26 - 10:16
 * @Description:com.example.demo.entity
 * @version:1.0
 */
@Data
@ApiModel(value="LightHistoryInfo对象", description="雷电历史信息表")
public class LightningHistoryInfo {

    @ApiModelProperty(value = "主键")
    @TableId("id")
    private String id;

    @ApiModelProperty(value = "落雷时间")
    @TableField("light_time")
    @DateTimeFormat(style = "yyyy-MM-dd HH:mm:ss")
    private Date timeDate;

    @ApiModelProperty(value = "落雷经度")
    @TableField("longitude")
    private Double longitude;

    @ApiModelProperty(value = "落雷维度")
    @TableField("latitude")
    private Double latitude;

    @ApiModelProperty(value = "电流幅值")
    @TableField("peak_current")
    private Double peakCurrent;

    @ApiModelProperty(value = "回击次数")
    @TableField("multiplicity")
    private Integer multiplicity;

    @ApiModelProperty(value = "入库时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "落雷时间的7位毫秒数")
    @TableField("xsecond")
    private Integer xsecond;
}

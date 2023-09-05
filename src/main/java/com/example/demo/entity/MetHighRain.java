package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Auther：jinguangshuai
 * @Data：2023/8/1 - 08 - 01 - 9:55
 * @Description:com.example.demo.entity
 * @version:1.0
 */
@TableName(value = "met_high_rain")
@Data
@Accessors(chain = true)
public class MetHighRain {

    @TableId(value = "id")
    private String id;

    @DateTimeFormat(style = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(value = "start_time")
    private Date startTime;

    @DateTimeFormat(style = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(value = "end_time")
    private Date endTime;

    @TableField(value = "value")
    private String value;

    @TableField(value = "geom")
    private String geom;

    @TableField(value = "province")
    private String province;

    @TableField(value = "province_name")
    private String provinceName;

    @TableField(value = "version")
    private String version;

    @DateTimeFormat(style = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(value = "create_time")
    private Date createTime;


}

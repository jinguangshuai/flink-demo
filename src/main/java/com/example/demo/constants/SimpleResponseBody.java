package com.example.demo.constants;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

/**
 * @Auther：jinguangshuai
 * @Data：2023/8/3 - 08 - 03 - 10:51
 * @Description:com.example.demo.constants
 * @version:1.0
 */
@ApiModel(
        value = "SimpleResponseBody",
        description = "接口返回简易类型"
)
public class SimpleResponseBody<T>{
    @ApiModelProperty(
            value = "响应状态码",
            allowableValues = "10",
            example = "000000",
            required = true
    )
    protected String status;
    @ApiModelProperty(
            value = "错误信息",
            allowableValues = "200",
            example = "success"
    )
    protected String errors;
    @ApiModelProperty(
            value = "错误信息",
            allowableValues = "200",
            example = "success"
    )
    protected String message;
    @ApiModelProperty("本次请求处理的总记录数")
    @JsonIgnore
    @JSONField(
            serialize = false
    )
    protected int count;
    @ApiModelProperty("fields")
    @JsonIgnore
    @JSONField(
            serialize = false
    )
    private String fields;
    @ApiModelProperty("fieldMap")
    @JsonIgnore
    @JSONField(
            serialize = false
    )
    private Map<String, String> fieldMap;
    @ApiModelProperty("startID")
    @JsonIgnore
    @JSONField(
            serialize = false
    )
    private String startID;
    @ApiModelProperty("返回结果是否排序")
    @JsonIgnore
    @JSONField(
            serialize = false
    )
    private boolean hasOrder;
    @ApiModelProperty("响应数据")
    protected T result;

    public SimpleResponseBody() {
    }

    public SimpleResponseBody(T result) {
        this.status = String.valueOf(ResponseCodeEnum.SUCCESS.getValue());
        this.result = result;
    }

    public SimpleResponseBody(String status, String errors) {
        this.status = status;
        this.errors = errors;
        this.message = errors;
    }

    public SimpleResponseBody(String status, String errors, String startId, String fields, T result) {
        this.status = status;
        this.errors = errors;
        this.message = errors;
        this.startID = startId;
        this.fields = fields;
        this.result = result;
    }

    public SimpleResponseBody(String status, String errors, String startId, Map<String, String> fieldMap, boolean isOrder, T result) {
        this.status = status;
        this.errors = errors;
        this.message = errors;
        this.startID = startId;
        this.fieldMap = fieldMap;
        this.result = result;
        this.hasOrder = isOrder;
    }

    public static <T> SimpleResponseBody success(T t) {
        return new SimpleResponseBody(String.valueOf(ResponseCodeEnum.SUCCESS.getValue()), "success", "", "", t);
    }

    public static <T> SimpleResponseBody success(String startId, String fields) {
        return new SimpleResponseBody(String.valueOf(ResponseCodeEnum.SUCCESS.getValue()), "成功", startId, fields, (Object)null);
    }

    public static <T> SimpleResponseBody success(String fields, T data) {
        return new SimpleResponseBody(String.valueOf(ResponseCodeEnum.SUCCESS.getValue()), "成功", "", fields, data);
    }

    public static <T> SimpleResponseBody success(Map<String, String> fieldMap, Boolean isOrder, T data) {
        return new SimpleResponseBody(String.valueOf(ResponseCodeEnum.SUCCESS.getValue()), "成功", "", fieldMap, isOrder, data);
    }

    public static <T> SimpleResponseBody success(String startId, String fields, T data) {
        return new SimpleResponseBody(String.valueOf(ResponseCodeEnum.SUCCESS.getValue()), "成功", startId, fields, data);
    }

    public static <T> SimpleResponseBody fail(String fail) {
        return new SimpleResponseBody(String.valueOf(ResponseCodeEnum.FAILURE.getValue()), fail);
    }

    public static <T> SimpleResponseBody fail(String fail, ResponseCodeEnum enumCode) {
        return new SimpleResponseBody(String.valueOf(enumCode.getValue()), fail);
    }

    public String getStatus() {
        return this.status;
    }

    public String getErrors() {
        return this.errors;
    }

    public String getMessage() {
        return this.message;
    }

    public T getResult() {
        return this.result;
    }
}

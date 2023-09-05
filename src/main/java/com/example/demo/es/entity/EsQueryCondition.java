package com.example.demo.es.entity;

import lombok.Data;

@Data
public class EsQueryCondition {

    // 字段名称
    private String fieldName;

    // 字段值
    private Object fieldValue;

}

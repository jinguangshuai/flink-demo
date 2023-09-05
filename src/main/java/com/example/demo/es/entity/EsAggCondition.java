package com.example.demo.es.entity;

import lombok.Data;

@Data
public class EsAggCondition {

    // 分组字段
    private String groupFieldName;

    // 分组字段别名
    private String groupAliasName;

    // 聚合字段
    private String aggFieldName;

    // 聚合字段别名
    private String aggAliasName;

    // 子分组字段
    private String subGroupFieldName;

    // 子分组字段别名
    private String subGroupAliasName;
}

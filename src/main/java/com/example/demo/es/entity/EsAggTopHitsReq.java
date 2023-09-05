package com.example.demo.es.entity;

import lombok.Data;
import org.elasticsearch.search.sort.SortOrder;

@Data
public class EsAggTopHitsReq {

    // 分组字段名
    private String fieldName;

    // 结果别名
    private String aliasName;

    // 排序字段名
    private String sortFieldName;

    private SortOrder sortOrder;

    private int size;

}

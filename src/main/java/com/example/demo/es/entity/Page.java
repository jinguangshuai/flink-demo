package com.example.demo.es.entity;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class Page<T> {

    // 查询数据列表
    private List<T> records = Collections.emptyList();

    // 总数
    private long total = 0;

    // 每页显示条数，默认10
    private long size = 10;

    // 当前页
    private long current = 1;

    // 页码数
    private long pages;

}

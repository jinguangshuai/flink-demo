package com.example.demo.es.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author： htl
 * @Description:
 * @Date 2022/12/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESearchRange {
    /**
     * 最大值
     */
    private String maxValue;
    /**
     * 最小值
     */
    private String minValue;
    /**
     * 字段名
     */
    private String indexColumn;
}

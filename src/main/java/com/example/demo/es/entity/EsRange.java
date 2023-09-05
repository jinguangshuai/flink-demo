package com.example.demo.es.entity;

import lombok.Data;

@Data
public class EsRange {

    private String maxValue;

    private String minValue;

    private String maxEqualValue;

    private String minEqualValue;

    private String indexColumn;

}

package com.example.demo.es.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AggTypeEnum {

    COUNT("C", "COUNT"),
    DISTINCT_COUNT("DC", "DISTINCT_COUNT"),
    SUM("S", "SUM"),
    AVG("A", "AVG"),
    MAX("MAX", "MAX"),
    MIN("MIN", "MIN"),
    DATE_HISTOGRAM("DATE_HISTOGRAM", "DATE_HISTOGRAM");

    private String aggType;
    private String aggTypeName;

    public String getAggType(){
        return aggType;
    }

    public String getAggTypeName(){
        return aggTypeName;
    }

}

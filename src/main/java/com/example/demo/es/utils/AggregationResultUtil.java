package com.example.demo.es.utils;

import com.example.demo.es.enums.AggTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.avg.ParsedAvg;
import org.elasticsearch.search.aggregations.metrics.max.ParsedMax;
import org.elasticsearch.search.aggregations.metrics.min.ParsedMin;
import org.elasticsearch.search.aggregations.metrics.sum.ParsedSum;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;

@Slf4j
public class AggregationResultUtil {

    public static String getAggResult(Aggregations aggregations, AggTypeEnum aggTypeEnum, String aggAliasName){
        String aggType = aggTypeEnum.getAggType();
        if(AggTypeEnum.COUNT.getAggType().equals(aggType) || AggTypeEnum.DISTINCT_COUNT.getAggType().equals(aggType)){
            ValueCount valueCount = aggregations.get(aggAliasName);
            return valueCount.getValueAsString();
        }
        if(AggTypeEnum.AVG.getAggType().equals(aggType)){
            ParsedAvg parsedAvg = aggregations.get(aggAliasName);
            return parsedAvg.getValueAsString();
        }
        if(AggTypeEnum.MAX.getAggType().equals(aggType)){
            ParsedMax parsedMax = aggregations.get(aggAliasName);
            return parsedMax.getValueAsString();
        }
        if(AggTypeEnum.MIN.getAggType().equals(aggType)){
            ParsedMin parsedMin = aggregations.get(aggAliasName);
            return parsedMin.getValueAsString();
        }
        if(AggTypeEnum.SUM.getAggType().equals(aggType)){
            ParsedSum parsedSum = aggregations.get(aggAliasName);
            return parsedSum.getValueAsString();
        }
        return null;
    }

    public static String getAggAliasName(AggTypeEnum aggTypeEnum, String aggFieldName){
        return aggFieldName + "_" + aggTypeEnum.getAggType();
    }
}

package com.example.demo.es.wrapper;

import cn.hutool.core.util.ObjectUtil;
import com.example.demo.es.enums.AggTypeEnum;
import com.example.demo.es.utils.AggregationResultUtil;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;

public class AggBuilderWrapper {

    private String indexName;
    private String rootGroupFieldName;//分组字段
    private String rootGroupAliasName;//分组别名
    private String aggFieldName;//聚合字段名
    private AggTypeEnum aggTypeEnum;//聚合类型枚举
    private AggregationBuilder rootAggregationBuilder;
    private AggWrapper aggWrapper;

    public String getIndexName() {
        return indexName;
    }

    public String getRootGroupFieldName() {
        return rootGroupFieldName;
    }

    public String getRootGroupAliasName() {
        return rootGroupAliasName;
    }

    public String getAggFieldName() {
        return aggFieldName;
    }

    public AggTypeEnum getAggTypeEnum() {
        return aggTypeEnum;
    }

    public AggWrapper getAggWrapper() {
        return aggWrapper;
    }

    public AggregationBuilder build(){
        return this.rootAggregationBuilder;
    }

    public AggBuilderWrapper(String indexName, String rootGroupFieldName, String rootGroupAliasName) {
        this.indexName = indexName;
        this.rootGroupFieldName = rootGroupFieldName;
        this.rootGroupAliasName = rootGroupAliasName;
        this.aggWrapper = new AggWrapper(rootGroupFieldName,rootGroupAliasName);
        this.rootAggregationBuilder = AggregationBuilders.terms(rootGroupAliasName).field(rootGroupFieldName).size(Integer.MAX_VALUE);
    }

    public AggBuilderWrapper(String indexName) {
        this.indexName = indexName;
        this.aggWrapper = new AggWrapper();
    }

    public void parentAggregation(String parentAggFieldName, String parentAggAliasName) {
        TermsAggregationBuilder parentAggregationBuilder = AggregationBuilders.terms(parentAggAliasName).field(parentAggFieldName).size(Integer.MAX_VALUE);
        parentAggregationBuilder.subAggregation(this.rootAggregationBuilder);
        AggWrapper aggWrapper = new AggWrapper(parentAggFieldName, parentAggAliasName, this.aggWrapper);
        this.aggWrapper = aggWrapper;
        this.rootAggregationBuilder = parentAggregationBuilder;
    }

    public void count(String aggFieldName) {
        AggregationBuilder aggregationBuilder =
                AggregationBuilders.count(AggregationResultUtil.getAggAliasName(AggTypeEnum.COUNT,aggFieldName)).field(aggFieldName);
        this.aggTypeEnum = AggTypeEnum.COUNT;
        this.aggFieldName = aggFieldName;
        if(ObjectUtil.isEmpty(this.rootAggregationBuilder)){
            this.rootAggregationBuilder = aggregationBuilder;
        }else {
            this.rootAggregationBuilder.subAggregation(aggregationBuilder);
        }
    }

    public void distinctCount(String aggFieldName) {
        CardinalityAggregationBuilder cardinalityAggregationBuilder =
                AggregationBuilders.cardinality(AggregationResultUtil.getAggAliasName(AggTypeEnum.DISTINCT_COUNT,aggFieldName)).field(aggFieldName);
        this.aggTypeEnum = AggTypeEnum.DISTINCT_COUNT;
        this.aggFieldName = aggFieldName;
        if(ObjectUtil.isEmpty(this.rootAggregationBuilder)){
            this.rootAggregationBuilder = cardinalityAggregationBuilder;
        }else {
            this.rootAggregationBuilder.subAggregation(cardinalityAggregationBuilder);
        }
    }

    public void sum(String aggFieldName) {
        SumAggregationBuilder sumAggregationBuilder =
                AggregationBuilders.sum(AggregationResultUtil.getAggAliasName(AggTypeEnum.SUM,aggFieldName)).field(aggFieldName);
        this.aggTypeEnum = AggTypeEnum.SUM;
        this.aggFieldName = aggFieldName;
        if(ObjectUtil.isEmpty(this.rootAggregationBuilder)){
            this.rootAggregationBuilder = sumAggregationBuilder;
        }else {
            this.rootAggregationBuilder.subAggregation(sumAggregationBuilder);
        }
    }

    public void avg(String aggFieldName) {
        AvgAggregationBuilder avgAggregationBuilder =
                AggregationBuilders.avg(AggregationResultUtil.getAggAliasName(AggTypeEnum.AVG,aggFieldName)).field(aggFieldName);
        this.aggTypeEnum = AggTypeEnum.AVG;
        this.aggFieldName = aggFieldName;
        if(ObjectUtil.isEmpty(this.rootAggregationBuilder)){
            this.rootAggregationBuilder = avgAggregationBuilder;
        }else {
            this.rootAggregationBuilder.subAggregation(avgAggregationBuilder);
        }
    }

    public void max(String aggFieldName) {
        MaxAggregationBuilder maxAggregationBuilder =
                AggregationBuilders.max(AggregationResultUtil.getAggAliasName(AggTypeEnum.MAX,aggFieldName)).field(aggFieldName);
        this.aggTypeEnum = AggTypeEnum.MAX;
        this.aggFieldName = aggFieldName;
        if(ObjectUtil.isEmpty(this.rootAggregationBuilder)){
            this.rootAggregationBuilder = maxAggregationBuilder;
        }else {
            this.rootAggregationBuilder.subAggregation(maxAggregationBuilder);
        }
    }

    public void min(String aggFieldName) {
        MinAggregationBuilder minAggregationBuilder =
                AggregationBuilders.min(AggregationResultUtil.getAggAliasName(AggTypeEnum.MIN,aggFieldName)).field(aggFieldName);
        this.aggTypeEnum = AggTypeEnum.MIN;
        this.aggFieldName = aggFieldName;
        if(ObjectUtil.isEmpty(this.rootAggregationBuilder)){
            this.rootAggregationBuilder = minAggregationBuilder;
        }else {
            this.rootAggregationBuilder.subAggregation(minAggregationBuilder);
        }
    }
}

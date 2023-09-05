package com.example.demo.es.service;

import com.example.demo.es.entity.*;
import com.example.demo.es.wrapper.SearchSourceBuilderWrapper;

import java.util.List;
import java.util.Map;

/**
 * @Auther：jinguangshuai
 * @Data：2023/8/7 - 08 - 07 - 14:56
 * @Description:com.example.demo.es.service
 * @version:1.0
 */
public interface IntfEsQueryService {
    boolean createIndex(String indexName, String esDsl);

    boolean existIndex(String indexName);

    <T> boolean batchSave(String indexName, List<T> entityList);

    <T> boolean batchSaveAndPk(String indexName, List<T> entityList,String pkColumn);

    <T> boolean batchUpate(String indexName, List<T> entityList, String pkColumn);

    <T> List<T> conditionSearchAllData(String indexName,
                                       List<EsQueryCondition> andConditionList,
                                       List<EsQueryCondition> orConditionList,
                                       List<EsQueryCondition> dimAndConditionList,
                                       List<EsQueryCondition> dimOrConditionList,
                                       List<EsRange> multiRangeList, Class<T> clazz);

    <T> com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> mapConditionSearch(String indexName, Integer pageNum, Integer pageSize,
                                                                                         Map<String, Object> andMap, Map<String, Object> orMap,
                                                                                         Map<String, Object> dimAndMap, Map<String, Object> dimOrMap,
                                                                                         Map<String, Object> rangeUpMap, Map<String, Object> rangeDownMap, List<ESearchRange> multiRangeList, Class<T> clazz);

    <T> List<T> mapNoPageConditionSearch(String indexName,
                                         Map<String, Object> andMap, Map<String, Object> orMap,
                                         Map<String, Object> dimAndMap, Map<String, Object> dimOrMap,
                                         Map<String, Object> rangeUpMap, Map<String, Object> rangeDownMap, List<ESearchRange> multiRangeList, Class<T> clazz);

    <T> Page<T> list(SearchSourceBuilderWrapper searchSourceBuilderWrapper, Class<T> clazz);

    boolean deleteByQuery6(SearchSourceBuilderWrapper searchSourceBuilderWrapper);

    <T> List<EsAggTopHitsResp<T>> conditionAggregationTopHits(SearchSourceBuilderWrapper searchSourceBuilderWrapper,
                                                              EsAggTopHitsReq esAggTopHitsReq, Class<T> clazz);
    List<EsAggResultTree> aggregation(SearchSourceBuilderWrapper searchSourceBuilderWrapper);
}

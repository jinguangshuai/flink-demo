package com.example.demo.es.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.es.entity.*;
import com.example.demo.es.enums.AggTypeEnum;
import com.example.demo.es.service.IntfEsQueryService;
import com.example.demo.es.utils.AggregationResultUtil;
import com.example.demo.es.utils.EsUtil;
import com.example.demo.es.wrapper.AggBuilderWrapper;
import com.example.demo.es.wrapper.AggWrapper;
import com.example.demo.es.wrapper.QueryBuilderWrapper;
import com.example.demo.es.wrapper.SearchSourceBuilderWrapper;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.ParsedTopHits;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Data
@Slf4j
public class EsQueryServiceImpl implements IntfEsQueryService {

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    private EsUtil esUtil;

    @Override
    public boolean createIndex(String indexName, String esDsl) {
        return esUtil.createIndex(indexName, esDsl);
    }

    @Override
    public boolean existIndex(String indexName) {
        return esUtil.existIndex(indexName);
    }

    @Override
    public <T> boolean batchSave(String indexName, List<T> entityList) {
        return esUtil.multiAddDocId(indexName, entityList);
    }

    @Override
    public <T> boolean batchSaveAndPk(String indexName, List<T> entityList,String pkColumn){
        return esUtil.batchSaveAndPk(indexName, entityList,pkColumn);
    }

    @Override
    public <T> boolean batchUpate(String indexName, List<T> entityList, String pkColumn) {
        return esUtil.multiUpdate(indexName, entityList, pkColumn);
    }

    @Override
    public <T> List<T> conditionSearchAllData(String indexName,
                                              List<EsQueryCondition> andConditionList,
                                              List<EsQueryCondition> orConditionList,
                                              List<EsQueryCondition> dimAndConditionList,
                                              List<EsQueryCondition> dimOrConditionList,
                                              List<EsRange> multiRangeList, Class<T> clazz) {
        return esUtil.conditionSearchAllData(indexName, andConditionList, orConditionList,dimAndConditionList,dimOrConditionList,multiRangeList,clazz);
    }

    @Override
    public <T> com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> mapConditionSearch(String indexName, Integer pageNum, Integer pageSize,
                                                                                                Map<String, Object> andMap, Map<String, Object> orMap,
                                                                                                Map<String, Object> dimAndMap, Map<String, Object> dimOrMap,
                                                                                                Map<String, Object> rangeUpMap, Map<String, Object> rangeDownMap, List<ESearchRange> multiRangeList, Class<T> clazz) {
        return esUtil.mapConditionSearch(indexName, pageNum, pageSize,andMap,orMap,dimAndMap,dimOrMap,rangeUpMap,rangeDownMap,multiRangeList,clazz);
    }

    public <T> List<T> mapNoPageConditionSearch(String indexName,
                                                Map<String, Object> andMap, Map<String, Object> orMap,
                                                Map<String, Object> dimAndMap, Map<String, Object> dimOrMap,
                                                Map<String, Object> rangeUpMap, Map<String, Object> rangeDownMap, List<ESearchRange> multiRangeList, Class<T> clazz) {
        return esUtil.mapNoPageConditionSearch(indexName,andMap,orMap,dimAndMap,dimOrMap,rangeUpMap,rangeDownMap,multiRangeList,clazz);
    }

    @Override
    public <T> Page<T> list(SearchSourceBuilderWrapper searchSourceBuilderWrapper, Class<T> clazz) {
        Page<T> resultPage = new Page<>();
        List<T> resultList = Lists.newArrayList();
        try{
            String indexName = searchSourceBuilderWrapper.getIndexName();
            SearchRequest searchRequest = new SearchRequest(indexName);
            QueryBuilderWrapper queryBuilderWrapper = searchSourceBuilderWrapper.getQueryBuilderWrapper();
            BoolQueryBuilder boolQueryBuilder = queryBuilderWrapper.build();
            SearchSourceBuilder searchSourceBuilder = searchSourceBuilderWrapper.build();
            searchSourceBuilder.query(boolQueryBuilder);

            // 超时设置
            searchSourceBuilder.timeout(TimeValue.timeValueSeconds(60));
            searchRequest.source(searchSourceBuilder);
            log.info("es分页查询,索引: {}, DSL: {}", searchRequest.indices(), searchRequest.source());

            // 搜索
            SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();

            // 处理返回命中
            for(SearchHit hit: searchHits){
                String sourceAsString = hit.getSourceAsString();
                T t = JSONObject.parseObject(sourceAsString, clazz);
                resultList.add(t);
            }

            // 分页处理
            long total = searchHits.getTotalHits();
            int pageNo = searchSourceBuilderWrapper.getPageNo();
            int pageSize = searchSourceBuilderWrapper.getPageSize();

            resultPage.setCurrent(pageNo);
            resultPage.setSize(pageSize);
            resultPage.setTotal(total);
            resultPage.setRecords(resultList);
            resultPage.setPages(total == 0 ? 0 : (int) (total %pageSize ==0 ? total/pageSize : (total/pageSize)+1));
        }catch (Exception e){
            log.error("es分页查询异常", e);
        }
        return resultPage;
    }

    @Override
    public boolean deleteByQuery6(SearchSourceBuilderWrapper searchSourceBuilderWrapper) {
        String indexName = searchSourceBuilderWrapper.getIndexName();
        SearchRequest searchRequest = new SearchRequest(indexName);

        QueryBuilderWrapper queryBuilderWrapper = searchSourceBuilderWrapper.getQueryBuilderWrapper();
        BoolQueryBuilder boolQueryBuilder = queryBuilderWrapper.build();
        SearchSourceBuilder sourceBuilder = searchSourceBuilderWrapper.build();
        sourceBuilder.query(boolQueryBuilder);

        sourceBuilder.timeout(TimeValue.timeValueSeconds(60));
        searchRequest.source(sourceBuilder);
        log.info("es删除,索引: {},dsl: {}", searchRequest.indices(), searchRequest.source());
        return esUtil.deleteByQuery(indexName, searchRequest);
    }

    @Override
    public <T> List<EsAggTopHitsResp<T>> conditionAggregationTopHits(SearchSourceBuilderWrapper searchSourceBuilderWrapper, EsAggTopHitsReq esAggTopHitsReq, Class<T> clazz) {
        List<EsAggTopHitsResp<T>> resultList = Lists.newArrayList();
        String indexName = searchSourceBuilderWrapper.getIndexName();
        try {
            SearchRequest searchRequest = new SearchRequest(indexName);
            QueryBuilderWrapper queryBuilderWrapper = searchSourceBuilderWrapper.getQueryBuilderWrapper();
            BoolQueryBuilder boolQueryBuilder = queryBuilderWrapper.build();
            SearchSourceBuilder searchSourceBuilder = searchSourceBuilderWrapper.build();
            searchSourceBuilder.query(boolQueryBuilder);
            TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms(esAggTopHitsReq.getAliasName()).field(esAggTopHitsReq.getFieldName()).size(Integer.MAX_VALUE);
            TopHitsAggregationBuilder topHitsAggregationBuilder = AggregationBuilders.topHits(esAggTopHitsReq.getAliasName());
            if (StringUtils.isNotEmpty(esAggTopHitsReq.getSortFieldName()))
                topHitsAggregationBuilder.sort(esAggTopHitsReq.getSortFieldName(), esAggTopHitsReq.getSortOrder());
            if (esAggTopHitsReq.getSize() != 0)
                topHitsAggregationBuilder.size(esAggTopHitsReq.getSize());

            aggregationBuilder.subAggregation(topHitsAggregationBuilder);
            searchSourceBuilder.aggregation(aggregationBuilder);

            searchSourceBuilder.timeout(TimeValue.timeValueSeconds(60));
            searchSourceBuilder.size(0);
            searchRequest.source(searchSourceBuilder);

            log.info("elasticSearch tools method:conditionAggregationTopHits,indexName:{},DSL-->{}", searchRequest.indices(), searchRequest.source());
            long queryStart = System.currentTimeMillis();
            SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);//获取全部聚合数据
            long queryEnd = System.currentTimeMillis();
            log.info("{}索引分组查询详情es耗时{}ms", indexName, queryEnd - queryStart);
            Terms aggregationResp = searchResponse.getAggregations().get(esAggTopHitsReq.getAliasName());
            List<? extends Terms.Bucket> buckets = aggregationResp.getBuckets();
            for (Terms.Bucket bucket : buckets) {
                String aggKey = String.valueOf(bucket.getKey());
                List<T> bucketResultList = Lists.newArrayList();
                Aggregations aggregations = bucket.getAggregations();
                ParsedTopHits parsedTopHits = aggregations.get(esAggTopHitsReq.getAliasName());
                SearchHit[] hits = parsedTopHits.getHits().getHits();
                for (SearchHit hit : hits) {
                    String sourceAsString = hit.getSourceAsString();
                    T result = JSONObject.parseObject(sourceAsString, clazz);
                    bucketResultList.add(result);
                }
                EsAggTopHitsResp<T> elasticSearchAggTopHitsResp = new EsAggTopHitsResp<>(aggKey, bucketResultList);
                resultList.add(elasticSearchAggTopHitsResp);
            }
            log.info("{}索引分组查询详情结果处理耗时{}ms", indexName, System.currentTimeMillis() - queryEnd);
        } catch (Exception e) {
            log.error("{}索引分组查询列表失败", indexName, e);
        }
        return resultList;
    }

    @Override
    public List<EsAggResultTree> aggregation(SearchSourceBuilderWrapper searchSourceBuilderWrapper) {
        String indexName = searchSourceBuilderWrapper.getIndexName();
        List<EsAggResultTree> aggResultList = Lists.newArrayList();
        try {
            SearchSourceBuilder searchSourceBuilder = searchSourceBuilderWrapper.build();
            SearchRequest searchRequest = new SearchRequest(indexName);
            List<AggBuilderWrapper> aggregationBuilderWrapperList = searchSourceBuilderWrapper.getAggBuilderWrapperList();
            if (CollectionUtil.isEmpty(aggregationBuilderWrapperList)) {
                log.error("AggregationBuilderWrapperList is empty");
                return null;
            }
            // 构建查询条件
            QueryBuilderWrapper queryBuilderWrapper = searchSourceBuilderWrapper.getQueryBuilderWrapper();
            QueryBuilder queryBuilder = queryBuilderWrapper.build();
            if (ObjectUtil.isEmpty(queryBuilder)) {
                queryBuilder = QueryBuilders.matchAllQuery();
            }
            searchSourceBuilder.query(queryBuilder);

            // 构建聚合条件
            for (AggBuilderWrapper aggregationBuilderWrapper : aggregationBuilderWrapperList) {
                AggregationBuilder aggregationBuilder = aggregationBuilderWrapper.build();
                searchSourceBuilder.aggregation(aggregationBuilder);
            }

            //分页处理
            searchSourceBuilder.size(0);
            //超时设置
            searchSourceBuilder.timeout(TimeValue.timeValueSeconds(60));
            searchRequest.source(searchSourceBuilder);
            log.info("elasticSearch tools method:groupBy,indexName:{},DSL-->{}", searchRequest.indices(), searchRequest.source());
            SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);//获取全部聚合数据

            //遍历聚合参数，获取结果
            for (AggBuilderWrapper aggregationBuilderWrapper : aggregationBuilderWrapperList) {
                AggWrapper aggregationWrapper = aggregationBuilderWrapper.getAggWrapper();
                AggTypeEnum aggTypeEnum = aggregationBuilderWrapper.getAggTypeEnum();
                String aggAliasName = AggregationResultUtil.getAggAliasName(aggTypeEnum, aggregationBuilderWrapper.getAggFieldName());
                EsAggResultTree elasticSearchAggResultTree = new EsAggResultTree();
                elasticSearchAggResultTree.setFieldValue(aggAliasName);
                if (ObjectUtil.isEmpty(aggregationWrapper) || StringUtils.isEmpty(aggregationWrapper.getGroupAliasName())) {
                    // 此判断仅处理聚合操作(无分组)
                    Aggregations aggregations = searchResponse.getAggregations();
                    String aggResult = AggregationResultUtil.getAggResult(aggregations, aggTypeEnum, aggAliasName);
                    elasticSearchAggResultTree.setAggResultValue(aggResult);
                    aggResultList.add(elasticSearchAggResultTree);
                    continue;
                }
                String groupAliasName = aggregationWrapper.getGroupAliasName();
                Terms aggregationResp = searchResponse.getAggregations().get(groupAliasName);
                List<? extends Terms.Bucket> buckets = aggregationResp.getBuckets();
                List<EsAggResultTree> subAggResultList = getAggResultTree(buckets, aggTypeEnum, aggregationWrapper, aggAliasName);
                elasticSearchAggResultTree.setSubAggResultList(subAggResultList);
                aggResultList.add(elasticSearchAggResultTree);
            }
            return aggResultList;
        } catch (Exception e) {
            log.error("{}索引分组查询列表失败", indexName, e);
            return aggResultList;
        }
    }

    private List<EsAggResultTree> getAggResultTree(List<? extends Terms.Bucket> buckets,
                                                   AggTypeEnum aggTypeEnum,
                                                   AggWrapper aggregationWrapper,
                                                   String aggAliasName) {
        List<EsAggResultTree> resultTreeList = Lists.newArrayList();
        for (Terms.Bucket bucket : buckets) {
            EsAggResultTree elasticSearchAggResultTree = new EsAggResultTree();
            String rootAggValue = bucket.getKeyAsString();
            elasticSearchAggResultTree.setFieldValue(rootAggValue);
            Aggregations aggregations = bucket.getAggregations();
            AggWrapper subAggregationWrapper = aggregationWrapper.getAggWrapper();
            if (ObjectUtil.isEmpty(subAggregationWrapper) || StringUtils.isEmpty(subAggregationWrapper.getGroupAliasName())) {
                String aggResult = AggregationResultUtil.getAggResult(aggregations, aggTypeEnum, aggAliasName);
                elasticSearchAggResultTree.setAggResultValue(aggResult);
            } else {
                String subGroupAliasName = subAggregationWrapper.getGroupAliasName();
                ParsedStringTerms parsedStringTerms = aggregations.get(subGroupAliasName);
                List<? extends Terms.Bucket> subBuckets = parsedStringTerms.getBuckets();
                List<EsAggResultTree> subResultTreeList = getAggResultTree(subBuckets, aggTypeEnum, subAggregationWrapper, aggAliasName);
                elasticSearchAggResultTree.setSubAggResultList(subResultTreeList);
            }
            resultTreeList.add(elasticSearchAggResultTree);
        }
        return resultTreeList;
    }
}

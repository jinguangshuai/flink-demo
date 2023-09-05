package com.example.demo.es.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.es.entity.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class EsUtil {

    @Resource
    private RestHighLevelClient esClient;

    public final static String DEFAULT_TYPE = "_doc";

    /**
     * 判断索引是否存在
     *
     * @param indexName
     * @return
     */
    public boolean existIndex(String indexName) {
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest();
            getIndexRequest.indices(indexName);
            getIndexRequest.types(DEFAULT_TYPE);
            return esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("判断索引存在异常", e);
        }
        return false;
    }

    /**
     * 创建索引
     *
     * @param indexName
     * @param esDsl
     * @return
     */
    public boolean createIndex(String indexName, String esDsl) {
        try {
            JSONObject jsonObject = (JSONObject) JSONObject.parse(esDsl);
            Map esDslMap = JSONObject.toJavaObject(jsonObject, Map.class);
            Map settings = (Map) esDslMap.get("settings");
            Map mappings = (Map) esDslMap.get("mappings");
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            createIndexRequest.settings(settings);
            createIndexRequest.mapping(DEFAULT_TYPE, mappings);
            CreateIndexResponse response = esClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            log.info("创建索引, {}", response);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public <T> boolean multiAddDocId(String indexName, List<T> list, String columnName) {
        try {
            if (CollectionUtils.isEmpty(list)) {
                return false;
            }
            BulkRequest bulkRequest = new BulkRequest();
            list.forEach(doc -> {
                String source = JSON.toJSONString(doc);
                IndexRequest indexRequest = new IndexRequest(indexName);
                JSONObject jsonObject = JSONObject.parseObject(source);
                indexRequest.type(DEFAULT_TYPE);
                indexRequest.id(String.valueOf(jsonObject.get(columnName)));
                indexRequest.source(source, XContentType.JSON);
                bulkRequest.add(indexRequest);
            });
            BulkResponse bulkResponse = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            log.info("向索引{}中批量插入数据的结果为{}数据量{}", indexName, !bulkResponse.hasFailures(), list.size());
            log.info("向索引{}中批量插入数据的异常信息{}", indexName, bulkResponse.buildFailureMessage());
            return !bulkResponse.hasFailures();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 批量更新
     *
     * @param indexName
     * @param list
     * @param <T>
     * @return
     */
    public <T> boolean multiAddDocId(String indexName, List<T> list) {
        try {
            if (CollectionUtils.isEmpty(list)) {
                return false;
            }
            BulkRequest bulkRequest = new BulkRequest();
            list.forEach(doc -> {
                String source = JSON.toJSONString(doc);
                IndexRequest indexRequest = new IndexRequest(indexName);
                indexRequest.source(source, XContentType.JSON);
                indexRequest.type(DEFAULT_TYPE);
                bulkRequest.add(indexRequest);
            });
            BulkResponse bulkResponse = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            log.info("向索引{}批量插入数据的结果为{}数据量{}", indexName, !bulkResponse.hasFailures(), list.size());
            if (bulkResponse.hasFailures()) {
                log.info("向索引{}中批量插入数据的异常信息{}", indexName, bulkResponse.buildFailureMessage());
            }
            return !bulkResponse.hasFailures();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }

    /**
     * 批量插入，指定id
     *
     * @param indexName
     * @param list
     * @param <T>
     * @return
     */
    public <T> boolean batchSaveAndPk(String indexName, List<T> list, String pkColumn) {
        try {
            if (CollectionUtils.isEmpty(list)) {
                return false;
            }
            BulkRequest bulkRequest = new BulkRequest();
            list.forEach(doc -> {
                String source = JSON.toJSONString(doc);
                JSONObject jsonObject = JSONObject.parseObject(source);

                IndexRequest indexRequest = new IndexRequest(indexName);
                indexRequest.source(source, XContentType.JSON);
                indexRequest.type(DEFAULT_TYPE);
                if (StringUtils.isNotBlank(String.valueOf(jsonObject.get(pkColumn)))) {
                    indexRequest.id(String.valueOf(jsonObject.get(pkColumn)));
                }
                bulkRequest.add(indexRequest);
            });
            BulkResponse bulkResponse = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            log.info("向索引{}批量插入数据的结果为{}数据量{}", indexName, !bulkResponse.hasFailures(), list.size());
            if (bulkResponse.hasFailures()) {
                log.info("向索引{}中批量插入数据的异常信息{}", indexName, bulkResponse.buildFailureMessage());
            }
            return !bulkResponse.hasFailures();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }

    /**
     * 批量更新
     *
     * @param indexName
     * @param list
     * @param columnName
     * @param <T>
     * @return
     */
    public <T> boolean multiUpdate(String indexName, List<T> list, String columnName) {
        try {
            BulkRequest bulkRequest = new BulkRequest();
            list.forEach(doc -> {
                String source = JSON.toJSONString(doc);
                JSONObject jsonObject = JSONObject.parseObject(source);
                String pk = String.valueOf(jsonObject.get(columnName));
                UpdateRequest updateRequest = new UpdateRequest(indexName, DEFAULT_TYPE, pk);
                updateRequest.doc(source, XContentType.JSON).docAsUpsert(true);
                bulkRequest.add(updateRequest);
            });
            BulkResponse bulkResponse = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            log.info("向索引{}中批量更新数据的结果为{}数据量{}", indexName, !bulkResponse.hasFailures(), list.size());
            if (bulkResponse.hasFailures()) {
                log.info("向索引{}中批量更新数据的异常信息{}", indexName, bulkResponse.buildFailureMessage());
            }
        } catch (Exception e) {
            log.error("{}索引批量更新失败", indexName, e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 根据条件删除，通过滚动查询分批发送异步删除请求
     *
     * @param indexName
     * @param searchRequest
     * @return
     */
    public boolean deleteByQuery(String indexName, SearchRequest searchRequest) {
        try {
            long startTime = System.currentTimeMillis();
            Scroll scroll = new Scroll(TimeValue.timeValueMinutes(2L));
            searchRequest.scroll(scroll);
            SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
            log.info("ES查询{}索引DSL语句:{},耗时:{}", searchRequest.indices(), searchRequest.source(), System.currentTimeMillis() - startTime);
            log.info("命中数量:{}", searchResponse.getHits().getTotalHits());
            String scrollId = searchResponse.getScrollId();
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            while (ObjectUtil.isNotEmpty(searchHits)) {
                List<SearchHit> hitList = Arrays.asList(searchHits);
                BulkRequest bulkRequest = new BulkRequest();
                hitList.forEach(hit -> {
                    DeleteRequest deleteRequest = new DeleteRequest(indexName, DEFAULT_TYPE, hit.getId());
                    bulkRequest.add(deleteRequest);
                });
                ActionListener<BulkResponse> bulkResponseActionListener = new ActionListener<BulkResponse>() {
                    @Override
                    public void onResponse(BulkResponse bulkItemResponses) {
                        log.info("批量删除{}索引数据结果{}", indexName, bulkItemResponses.getItems());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        log.info("批量删除{}索引数据异常{}", indexName, e);
                    }
                };
                esClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, bulkResponseActionListener);
                //继续查询下一批数据
                SearchScrollRequest searchScrollRequest = new SearchScrollRequest();
                searchScrollRequest.scroll(scroll);
                SearchResponse searchResp = esClient.scroll(searchScrollRequest, RequestOptions.DEFAULT);
                scrollId = searchResp.getScrollId();
                searchHits = searchResp.getHits().getHits();
            }

            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = esClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            log.info("关闭scroll--->{}，响应结果{}", scrollId, clearScrollResponse.isSucceeded());
            log.info("删除{}数据并关闭scroll共耗时：{}", indexName, System.currentTimeMillis() - startTime);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("{}索引批量删除失败{}", indexName, e);
            return Boolean.FALSE;
        }
    }

    /**
     * 根据docId批量删除
     *
     * @param ids
     * @param indexName
     * @return
     */
    public boolean deleteByDocIds(List<String> ids, String indexName) {
        if (CollectionUtils.isEmpty(ids)) {
            log.info("向索引{}中批量删除ids为空", indexName);
            return Boolean.TRUE;
        }
        BulkRequest bulkRequest = new BulkRequest();
        ids.forEach(docId -> {
            DeleteRequest deleteRequest = new DeleteRequest(indexName, DEFAULT_TYPE, docId);
            bulkRequest.add(deleteRequest);
        });
        ActionListener<BulkResponse> bulkResponseActionListener = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                log.info("批量删除{}索引数据结果{}", indexName, bulkItemResponses.getItems());
            }

            @Override
            public void onFailure(Exception e) {
                log.info("批量删除{}索引数据异常{}", indexName, e);
            }
        };
        try {
            esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.info("批量删除{}索引数据异常{}", indexName, e);
            return Boolean.FALSE;
        }
    }

    /**
     * 根据条件，使用滚动查询获取所有匹配数据（避免深分页时使用）
     *
     * @param indexName
     * @param andConditionList
     * @param orConditionList
     * @param dimAndConditionList
     * @param dimOrConditionList
     * @param multiRangeList
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> conditionSearchAllData(String indexName,
                                              List<EsQueryCondition> andConditionList,
                                              List<EsQueryCondition> orConditionList,
                                              List<EsQueryCondition> dimAndConditionList,
                                              List<EsQueryCondition> dimOrConditionList,
                                              List<EsRange> multiRangeList, Class<T> clazz) {
        List<T> resultList = Lists.newArrayList();
        try {
            SearchRequest searchRequest = new SearchRequest(indexName);
            //构造搜索条件
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQueryBuilder = buildMultiQuery(andConditionList, orConditionList, dimAndConditionList, dimOrConditionList, multiRangeList);
            sourceBuilder.query(boolQueryBuilder);
            sourceBuilder.size(50000);

            //超时设置
            sourceBuilder.timeout(TimeValue.timeValueSeconds(60));
            searchRequest.source(sourceBuilder);
            List<SearchHit> hits = scrollQuery(searchRequest);
            hits.stream().forEach(hit -> {
                String sourceAsString = hit.getSourceAsString();
                T t = JSONObject.parseObject(sourceAsString, clazz);
                resultList.add(t);
            });
        } catch (Exception e) {
            log.error("索引{}批量删除失败", indexName, e);
        }
        return resultList;
    }


    /**
     * 分页查询
     *
     * @param indexName
     * @param pageNo
     * @param pageSize
     * @param andConditionList
     * @param orConditionList
     * @param dimAndConditionList
     * @param dimOrConditionList
     * @param multiRangeList
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Page<T> conditionSearch(String indexName, Integer pageNo, Integer pageSize,
                                       List<EsQueryCondition> andConditionList,
                                       List<EsQueryCondition> orConditionList,
                                       List<EsQueryCondition> dimAndConditionList,
                                       List<EsQueryCondition> dimOrConditionList,
                                       List<EsRange> multiRangeList, Class<T> clazz) {
        Page<T> pageResult = new Page<>();
        try {
            SearchRequest searchRequest = new SearchRequest(indexName);
            // 构造搜索条件
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQueryBuilder = buildMultiQuery(andConditionList, orConditionList, dimAndConditionList, dimOrConditionList, multiRangeList);
            sourceBuilder.query(boolQueryBuilder);

            // 超时设置
            sourceBuilder.timeout(TimeValue.timeValueSeconds(60));
            // 7.x版本设置此参数才会显示真实总数
            sourceBuilder.trackTotalHits(true);
            searchRequest.source(sourceBuilder);
            // 分页处理
            buildPageLimit(sourceBuilder, pageNo, pageSize);
            log.info("ES查询{}索引DSL语句:{}", searchRequest.indices(), searchRequest.source());
            // 执行搜索
            SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();

            List<T> resultList = Lists.newArrayList();
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                T t = JSONObject.parseObject(sourceAsString, clazz);
                resultList.add(t);
            }

            long total = searchResponse.getHits().getTotalHits();
            pageResult.setCurrent(pageNo);
            pageResult.setTotal(total);
            if (pageResult.getTotal() < pageSize) {
                pageResult.setSize(pageResult.getTotal());
            } else {
                pageResult.setSize(pageSize);
            }
            pageResult.setRecords(resultList);
            pageResult.setPages(total == 0 ? 0 : (int) (total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1));
        } catch (Exception e) {
            log.error("索引{}分页查询失败", indexName, e);
        }
        return pageResult;
    }


    private List<SearchHit> scrollQuery(SearchRequest searchRequest) {
        List<SearchHit> hits = Lists.newArrayList();
        Scroll scroll = new Scroll(TimeValue.timeValueSeconds(60));
        searchRequest.scroll(scroll);

        try {
            long startTime = System.currentTimeMillis();
            SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);
            log.info("ES查询{}索引DSL语句：{}", searchRequest.indices(), searchRequest.source());
            log.info("命中数量：{}", response.getHits().getTotalHits());
            String scrollId = response.getScrollId();
            SearchHit[] searchHits = response.getHits().getHits();

            while (ObjectUtil.isNotEmpty(searchHits)) {
                List<SearchHit> hitList = Lists.newArrayList();
                hits.addAll(hitList);
                SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
                searchScrollRequest.scroll(scroll);
                SearchResponse scrollResponse = esClient.scroll(searchScrollRequest, RequestOptions.DEFAULT);
                scrollId = scrollResponse.getScrollId();
                searchHits = scrollResponse.getHits().getHits();
            }
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = esClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            log.info("关闭scroll--->{}，响应结果", scrollId, clearScrollResponse.isSucceeded());
            log.info("查询并关闭scroll获取全量数据共耗时：{}", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("索引{} 查询全量数据失败", searchRequest.indices(), e);
        }
        return hits;
    }

    /**
     * 聚合统计查询
     *
     * @param indexName
     * @param andConditionList
     * @param orConditionList
     * @param dimAndConditionList
     * @param dimOrConditionList
     * @param multiRangeList
     * @param aggCondition
     * @return
     */
    public List<EsAggResult> aggregationSearch(String indexName,
                                               List<EsQueryCondition> andConditionList,
                                               List<EsQueryCondition> orConditionList,
                                               List<EsQueryCondition> dimAndConditionList,
                                               List<EsQueryCondition> dimOrConditionList,
                                               List<EsRange> multiRangeList,
                                               EsAggCondition aggCondition) {
        SearchRequest searchRequest = new SearchRequest();
        //构造搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = buildMultiQuery(andConditionList, orConditionList, dimAndConditionList, dimOrConditionList, multiRangeList);
        sourceBuilder.query(boolQueryBuilder);

        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms(aggCondition.getGroupAliasName()).field(aggCondition.getGroupFieldName()).size(Integer.MAX_VALUE);
        AggregationBuilder aggField = AggregationBuilders.count(aggCondition.getAggAliasName()).field(aggCondition.getAggFieldName());
        boolean isSubAgg = Boolean.FALSE;

        // 处理子聚合条件
        if (StringUtils.isNotEmpty(aggCondition.getSubGroupFieldName()) && StringUtils.isNotEmpty(aggCondition.getSubGroupAliasName())) {
            isSubAgg = true;
            TermsAggregationBuilder subAggregationBuilder = AggregationBuilders.terms(aggCondition.getSubGroupAliasName()).field(aggCondition.getSubGroupFieldName()).size(Integer.MAX_VALUE);
            subAggregationBuilder.subAggregation(aggField);
            aggregationBuilder.subAggregation(subAggregationBuilder);
        } else {
            aggregationBuilder.subAggregation(aggField);
        }
        // 分组条件
        sourceBuilder.aggregation(aggregationBuilder);
        // 分页处理
        sourceBuilder.size(0);
        // 超时设置
        sourceBuilder.timeout(TimeValue.timeValueSeconds(60));
        searchRequest.source(sourceBuilder);
        log.info("ES查询{}索引DSL语句：{}", indexName, searchRequest.source());

        List<EsAggResult> aggResultList = Lists.newArrayList();
        try {
            // 获取全部聚合数据
            SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
            Terms aggregationResp = searchResponse.getAggregations().get(aggCondition.getGroupAliasName());
            List<? extends Terms.Bucket> buckets = aggregationResp.getBuckets();

            for (Terms.Bucket bucket : buckets) {
                EsAggResult esAggResult = new EsAggResult();
                Aggregations aggregations = bucket.getAggregations();
                esAggResult.setFieldValue(bucket.getKeyAsString());
                if (!isSubAgg) {
                    ValueCount valueCount = aggregations.get(aggCondition.getSubGroupAliasName());
                    esAggResult.setAggResult(valueCount.getValueAsString());
                    aggResultList.add(esAggResult);
                    continue;
                }

                ParsedStringTerms parsedStringTerms = aggregations.get(aggCondition.getSubGroupAliasName());
                List<? extends Terms.Bucket> subBucketList = parsedStringTerms.getBuckets();
                for (Terms.Bucket subBucket : subBucketList) {
                    EsAggResult subEsAggResult = new EsAggResult();
                    BeanUtil.copyProperties(esAggResult, subEsAggResult);
                    subEsAggResult.setSubFieldValue(subBucket.getKeyAsString());
                    Aggregations subAggregations = subBucket.getAggregations();
                    ValueCount subValueCount = subAggregations.get(aggCondition.getSubGroupAliasName());
                    subEsAggResult.setAggResult(subValueCount.getValueAsString());
                    aggResultList.add(subEsAggResult);
                }
            }
        } catch (Exception e) {
            log.info("索引{}聚合统计查询失败", indexName, e);
        }
        return aggResultList;
    }

    public BoolQueryBuilder buildMultiQuery(List<EsQueryCondition> andConditionList,
                                            List<EsQueryCondition> orConditionList,
                                            List<EsQueryCondition> dimAndConditionList,
                                            List<EsQueryCondition> dimOrConditionList,
                                            List<EsRange> multiRangeList) {
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        // 值为true时，查询全部
        boolean searchAllFlag = true;
        // and 精确查询
        if (!CollectionUtils.isEmpty(andConditionList)) {
            andConditionList.stream().forEach(t -> {
                Object fieldValue = t.getFieldValue();
                if (ObjectUtil.isEmpty(fieldValue)) {
                    return;
                }
                HashSet hashSet = Sets.newHashSet();
                if (fieldValue instanceof HashSet) {
                    hashSet = (HashSet) fieldValue;
                    if (hashSet.size() == 0) {
                        return;
                    }
                }
                if (fieldValue instanceof List) {
                    List<String> objToList = (List<String>) fieldValue;
                    for (String obj : objToList) {
                        hashSet.add(obj);
                    }
                }
                if (fieldValue instanceof String) {
                    if (!ObjectUtil.isEmpty(fieldValue)) {
                        hashSet.add((String) fieldValue);
                    }
                }

                TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(t.getFieldName(), hashSet);
                queryBuilder.must(termsQueryBuilder);
            });
            searchAllFlag = false;
        }

        // or 精确查询
        if (!CollectionUtils.isEmpty(orConditionList)) {
            orConditionList.forEach(t -> {
                // 条件值
                List<String> orList = (List<String>) t.getFieldValue();
                for (String orItem : orList) {
                    MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(t.getFieldName(), orItem);
                    queryBuilder.should(matchQueryBuilder);
                }
            });
            searchAllFlag = false;
        }

        // and 模糊查询
        if (!CollectionUtils.isEmpty(dimAndConditionList)) {
            dimAndConditionList.forEach(t -> {
                WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(t.getFieldName(), "*" + t.getFieldValue() + "*");
                queryBuilder.must(wildcardQueryBuilder);
            });
            searchAllFlag = false;
        }

        // or 模糊查询
        if (!CollectionUtils.isEmpty(dimOrConditionList)) {
            dimOrConditionList.forEach(t -> {
                WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(t.getFieldName(), "*" + t.getFieldValue() + "*");
                queryBuilder.should(wildcardQueryBuilder);
            });
            searchAllFlag = false;
        }

        // 范围查询
        if (!CollectionUtils.isEmpty(multiRangeList)) {
//            BoolQueryBuilder minBoolQueryBuilder = QueryBuilders.boolQuery();

            multiRangeList.stream().forEach(t -> {
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(t.getIndexColumn());

                if (StringUtils.isNotEmpty(t.getMaxValue())) {
                    rangeQueryBuilder.lt(t.getMaxValue());
                }
                if (StringUtils.isNotEmpty(t.getMinValue())) {
                    rangeQueryBuilder.gt(t.getMinValue());
                }
                if (StringUtils.isNotEmpty(t.getMaxEqualValue())) {
                    rangeQueryBuilder.lte(t.getMaxEqualValue());
                }
                if (StringUtils.isNotEmpty(t.getMinEqualValue())) {
                    rangeQueryBuilder.gte(t.getMinEqualValue());
                }
                queryBuilder.filter(rangeQueryBuilder);
            });
//            queryBuilder.must(minBoolQueryBuilder);
            searchAllFlag = false;
        }

        if (searchAllFlag) {
            MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
            queryBuilder.must(matchAllQueryBuilder);
        }
        return queryBuilder;
    }

    /**
     * 通用条件查询，map类型的参数都为空时，默认查询全部
     *
     * @param indexName    索引名称
     * @param pageNum      当前页码
     * @param pageSize     每页数量
     * @param andMap       并且条件
     * @param orMap        或者条件
     * @param dimAndMap    模糊并且条件
     * @param dimOrMap     模糊或者条件
     * @param rangeUpMap   范围查询条件 >=
     * @param rangeDownMap 范围查询条件 <=
     * @return com.example.elasticsearchdemo.common.lang.PageResult<java.util.List < com.alibaba.fastjson.JSONObject>>
     * @author chenjf
     * @date 2021/7/6 11:38
     */
    public <T> com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> mapConditionSearch(String indexName, Integer pageNum, Integer pageSize,
                                                                                                Map<String, Object> andMap, Map<String, Object> orMap,
                                                                                                Map<String, Object> dimAndMap, Map<String, Object> dimOrMap,
                                                                                                Map<String, Object> rangeUpMap, Map<String, Object> rangeDownMap, List<ESearchRange> multiRangeList, Class<T> clazz) {
        SearchRequest searchRequest = new SearchRequest(indexName);
        //构造搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = buildMapMultiQuery(andMap, orMap, dimAndMap, dimOrMap, rangeUpMap, rangeDownMap, multiRangeList);
        sourceBuilder.query(boolQueryBuilder);
        //超时设置
        sourceBuilder.timeout(TimeValue.timeValueSeconds(60));
        searchRequest.source(sourceBuilder);
        //分页处理
        buildPageLimit(sourceBuilder, pageNum, pageSize);
        //执行搜索
        SearchResponse searchResponse = null;
        try {
            searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SearchHits searchHits = searchResponse.getHits();
        List<T> resultList = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            //原始查询结果数据
            String sourceAsString = hit.getSourceAsString();
            T t = JSONObject.parseObject(sourceAsString, clazz);
            resultList.add(t);
        }
        long total = searchHits.getTotalHits();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> pageResult = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        pageResult.setCurrent(pageNum);
        pageResult.setTotal(total);
        if (pageResult.getTotal() < pageSize) {
            pageResult.setSize(pageResult.getTotal());
        } else {
            pageResult.setSize(pageSize);
        }
        pageResult.setRecords(resultList);
        pageResult.setPages(total == 0 ? 0 : (int) (total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1));
        return pageResult;
    }

    /**
     * 构造多条件查询
     *
     * @param andMap
     * @param orMap
     * @param dimAndMap
     * @param dimOrMap
     * @return org.elasticsearch.index.query.BoolQueryBuilder
     * @author chenjf
     * @date 2021/7/6 11:04
     */
    public BoolQueryBuilder buildMapMultiQuery(Map<String, Object> andMap, Map<String, Object> orMap, Map<String, Object> dimAndMap, Map<String, Object> dimOrMap,
                                               Map<String, Object> rangeUpMap, Map<String, Object> rangeDownMap, List<ESearchRange> multiRangeList) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //该值为true时搜索全部
        boolean searchAllFlag = true;
        //精确查询，and
        if (!CollectionUtils.isEmpty(andMap)) {
            for (Map.Entry<String, Object> entry : andMap.entrySet()) {
                Object entryValue = entry.getValue();
                if (null == entryValue) {
                    continue;
                }
                HashSet hashSet = new HashSet();
                if (entry.getValue() instanceof HashSet) {
                    hashSet = (HashSet) entryValue;
                    if (hashSet.size() == 0) {
                        continue;
                    }
                }
                if (entry.getValue() instanceof List) {
                    List<String> valueList = (List<String>) entryValue;
                    for (String item : valueList) {
                        hashSet.add(item);
                    }
                }
                if (entry.getValue() instanceof String) {
                    String value = null;
                    if (null != entryValue) {
                        value = (String) entryValue;
                    }
                    if (org.springframework.util.StringUtils.isEmpty(value)) {
                        continue;
                    }
                    //处理未知数据码值
                    if (value.contains("_x")) {
                        value = value.replace("_x", "");
                        hashSet.add(value);
                    } else {
                        String[] valuesList = value.split(",");
                        for (String item : valuesList) {
                            hashSet.add(item);
                        }
                    }
                }
                TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(entry.getKey(), hashSet);
                boolQueryBuilder.must(termsQueryBuilder);
            }
            searchAllFlag = false;
        }
        //精确查询，or
        if (!CollectionUtils.isEmpty(orMap)) {
            for (Map.Entry<String, Object> entry : orMap.entrySet()) {
                List<String> orList = (List<String>) entry.getValue();//条件值集合
                for (String orItem : orList) {
                    MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(entry.getKey(), orItem);
                    boolQueryBuilder.should(matchQueryBuilder);
                }
            }
            searchAllFlag = false;
        }
        //模糊查询，and
        if (!CollectionUtils.isEmpty(dimAndMap)) {
            for (Map.Entry<String, Object> entry : dimAndMap.entrySet()) {
                WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(entry.getKey(), "*" + entry.getValue() + "*");
                boolQueryBuilder.must(wildcardQueryBuilder);
            }
            searchAllFlag = false;
        }
        //模糊查询，or
        if (!CollectionUtils.isEmpty(dimOrMap)) {
            for (Map.Entry<String, Object> entry : dimOrMap.entrySet()) {
                WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(entry.getKey(), "*" + entry.getValue() + "*");
                boolQueryBuilder.should(wildcardQueryBuilder);
            }
            searchAllFlag = false;
        }
        //范围查询 >=
        if (CollectionUtil.isNotEmpty(rangeUpMap)) {
            for (Map.Entry<String, Object> entry : rangeUpMap.entrySet()) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(entry.getKey()).gte(entry.getValue()));
            }
            searchAllFlag = false;
        }
        //范围查询 <=
        if (CollectionUtil.isNotEmpty(rangeDownMap)) {
            for (Map.Entry<String, Object> entry : rangeDownMap.entrySet()) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(entry.getKey()).lte(entry.getValue()));
            }
            searchAllFlag = false;
        }

        //范围查询，range 每个range之间以or方式拼接 与其余条件以and方式拼接 示例： and（0<=x<1 or 2<=X<3）
        if (CollectionUtil.isNotEmpty(multiRangeList)) {
            BoolQueryBuilder minBoolQueryBuilder = QueryBuilders.boolQuery();
            for (ESearchRange eSearchRange : multiRangeList) {
                minBoolQueryBuilder.should(QueryBuilders.rangeQuery(eSearchRange.getIndexColumn())
                        .lt(eSearchRange.getMaxValue())//<
                        .gte(eSearchRange.getMinValue()));//>=
            }
            boolQueryBuilder.must(minBoolQueryBuilder);
            searchAllFlag = false;
        }

        if (searchAllFlag) {
            MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
            boolQueryBuilder.must(matchAllQueryBuilder);
        }

        return boolQueryBuilder;
    }

    /**
     * 通用条件查询，map类型的参数都为空时，默认查询全部
     *
     * @param indexName    索引名称
     * @param andMap       并且条件
     * @param orMap        或者条件
     * @param dimAndMap    模糊并且条件
     * @param dimOrMap     模糊或者条件
     * @param rangeUpMap   范围查询条件 >=
     * @param rangeDownMap 范围查询条件 <=
     * @return com.example.elasticsearchdemo.common.lang.PageResult<java.util.List < com.alibaba.fastjson.JSONObject>>
     * @author JGS
     * @date 2023/08/07 11:38
     */
    public <T> List<T> mapNoPageConditionSearch(String indexName,
                                                Map<String, Object> andMap, Map<String, Object> orMap,
                                                Map<String, Object> dimAndMap, Map<String, Object> dimOrMap,
                                                Map<String, Object> rangeUpMap, Map<String, Object> rangeDownMap, List<ESearchRange> multiRangeList, Class<T> clazz) {
        SearchRequest searchRequest = new SearchRequest(indexName);
        //构造搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = buildMapMultiQuery(andMap, orMap, dimAndMap, dimOrMap, rangeUpMap, rangeDownMap, multiRangeList);
        sourceBuilder.query(boolQueryBuilder);
        //超时设置
        sourceBuilder.timeout(TimeValue.timeValueSeconds(60));
        searchRequest.source(sourceBuilder);
        //执行搜索
        SearchResponse searchResponse = null;
        try {
            searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SearchHits searchHits = searchResponse.getHits();
        List<T> resultList = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            //原始查询结果数据
            String sourceAsString = hit.getSourceAsString();
            T t = JSONObject.parseObject(sourceAsString, clazz);
            resultList.add(t);
        }
        if (CollUtil.isNotEmpty(resultList)) {
            return resultList;
        } else {
            return Collections.emptyList();
        }

    }

    public void buildPageLimit(SearchSourceBuilder sourceBuilder, Integer pageNo, Integer pageSize) {
        if (sourceBuilder != null && !ObjectUtil.isEmpty(pageNo) && !ObjectUtil.isEmpty(pageSize)) {
            sourceBuilder.from(pageSize * (pageNo - 1));
            sourceBuilder.size(pageSize);
        }
    }


}

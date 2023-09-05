package com.example.demo.es.wrapper;

import com.google.common.collect.Lists;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

public class SearchSourceBuilderWrapper {

    private String indexName;

    private Integer pageNo   = 1;

    private Integer pageSize = 10;

    private SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    private QueryBuilderWrapper queryBuilderWrapper = new QueryBuilderWrapper();

    private List<AggBuilderWrapper> aggBuilderWrapperList = Lists.newArrayList();

    private AggBuilderWrapper aggBuilderWrapper;

    public SearchSourceBuilderWrapper(String indexName){
        this.indexName = indexName;
        this.aggBuilderWrapper = new AggBuilderWrapper(indexName);
        this.aggBuilderWrapperList.add(this.aggBuilderWrapper);
    }

    public SearchSourceBuilderWrapper(String indexName, String rootGroupFieldName, String rootGroupAliasName){
        this.indexName = indexName;
        this.aggBuilderWrapper = new AggBuilderWrapper(indexName, rootGroupFieldName, rootGroupAliasName);
        this.aggBuilderWrapperList.add(aggBuilderWrapper);
    }

    public String getIndexName(){
        return indexName;
    }

    public Integer getPageNo(){
        return pageNo;
    }

    public Integer getPageSize(){
        return pageSize;
    }

    public QueryBuilderWrapper getQueryBuilderWrapper(){
        return queryBuilderWrapper;
    }

    public AggBuilderWrapper getAggBuilderWrapper(){
        return aggBuilderWrapper;
    }

    public List<AggBuilderWrapper> getAggBuilderWrapperList(){
        return aggBuilderWrapperList;
    }

    public SearchSourceBuilder build(){
        return this.searchSourceBuilder;
    }

    public SearchSourceBuilderWrapper page(@NotNull Integer pageNo, @NotNull Integer pageSize){
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.searchSourceBuilder.from(pageSize * (pageNo-1));
        this.searchSourceBuilder.size(pageSize);
        return this;
    }

    public SearchSourceBuilderWrapper sortByDesc(@NotNull String fieldName){
        this.searchSourceBuilder.sort(fieldName);;
        return this;
    }

    public SearchSourceBuilderWrapper includes(@NotNull String[] includes){
        this.searchSourceBuilder.fetchSource(includes, null);
        return this;
    }

    public SearchSourceBuilderWrapper multipleAgg(@NotNull AggBuilderWrapper aggBuilderWrapper){
        this.aggBuilderWrapper = aggBuilderWrapper;
        return this;
    }
}

package com.example.demo.es.wrapper;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.elasticsearch.index.query.*;

import java.util.HashSet;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class QueryBuilderWrapper {

    private BoolQueryBuilder root = QueryBuilders.boolQuery();

    public BoolQueryBuilder build(){
        return root;
    }

    public QueryBuilderWrapper must(QueryBuilderWrapper queryBuilderWrapper){
        this.root.must(queryBuilderWrapper.root);
        return this;
    }

    public QueryBuilderWrapper or(QueryBuilderWrapper queryBuilderWrapper){
        this.root.should(queryBuilderWrapper.root);
        return this;
    }

    public QueryBuilderWrapper filter(QueryBuilderWrapper queryBuilderWrapper){
        this.root.filter(queryBuilderWrapper.root);
        return this;
    }

    public QueryBuilderWrapper mustNot(QueryBuilderWrapper queryBuilderWrapper){
        this.root.mustNot(queryBuilderWrapper.root);
        return this;
    }

    /**
     * 所有的要匹配上
     * @param queryBuilderWrapper
     * @return
     */
    public QueryBuilderWrapper all(QueryBuilderWrapper queryBuilderWrapper){
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        this.root.must(matchAllQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper ne(String fieldName, Object fieldValue){
        this.root.mustNot(QueryBuilders.termQuery(fieldName, fieldValue));
        return this;
    }

    public QueryBuilderWrapper notLike(String fieldName, String fieldValue){
        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(fieldName, fieldValue);
        this.root.mustNot(wildcardQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper notNull(String fieldName){
        this.root.mustNot(QueryBuilders.existsQuery(fieldName));
        return this;
    }

    public QueryBuilderWrapper eq(String fieldName, Object fieldValue){
        if(null == fieldValue){
            return this;
        }
        HashSet hashSet = Sets.newHashSet();

        if(fieldValue instanceof HashSet){
            hashSet = (HashSet)fieldValue;
            if(hashSet.size()==0){
                return this;
            }
        }

        if(fieldValue instanceof List){
            List<String> valueList = (List<String>) fieldValue;
            for(String item : valueList){
                hashSet.add(item);
            }
        }

        if(fieldValue instanceof String){
            if(ObjectUtil.isEmpty(fieldValue)){
                return this;
            }
            hashSet.add(fieldValue);
        }
        this.root.must(QueryBuilders.termsQuery(fieldName, hashSet));
        return this;
    }

    public QueryBuilderWrapper gt(String fieldName, Object fieldValue){
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName);
        rangeQueryBuilder.gt(fieldValue);
        this.root.must(rangeQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper gte(String fieldName, Object fieldValue){
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName);
        rangeQueryBuilder.gte(fieldValue);
        this.root.must(rangeQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper lt(String fieldName, Object fieldValue){
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName);
        rangeQueryBuilder.lt(fieldValue);
        this.root.must(rangeQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper lte(String fieldName, String fieldValue){
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName);
        rangeQueryBuilder.lte(fieldValue);
        this.root.must(rangeQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper like(String fieldName, String fieldValue){
        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(fieldName, "*"+ fieldValue +"*");
        this.root.must(wildcardQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper likeLeft(String fieldName, String fieldValue){
        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(fieldName, "*" + fieldValue);
        this.root.must(wildcardQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper likeRight(String fieldName, String fieldValue){
        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(fieldName, fieldValue +"*");
        this.root.must(wildcardQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper orEq(String fieldName, Object fieldValue){
        this.root.should(QueryBuilders.termQuery(fieldName, fieldValue));
        return this;
    }

    public QueryBuilderWrapper orGte(String fieldName, Object fieldValue){
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName);
        rangeQueryBuilder.gte(fieldValue);
        this.root.should(rangeQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper orGt(String fieldName, Object fieldValue){
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName);
        rangeQueryBuilder.gt(fieldValue);
        this.root.should(rangeQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper orLt(String fieldName, Object fieldValue){
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName);
        rangeQueryBuilder.lt(fieldValue);
        this.root.should(rangeQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper orLte(String fieldName, Object fieldValue){
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName);
        rangeQueryBuilder.lte(fieldValue);
        this.root.should(rangeQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper orLike(String fieldName, String fieldValue){
        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(fieldName, "*" + fieldValue + "*" );
        this.root.should(wildcardQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper orLikeLeft(String fieldName, String fieldValue){
        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(fieldName, "*" +fieldValue);
        this.root.should(wildcardQueryBuilder);
        return this;
    }

    public QueryBuilderWrapper orLikeRight(String fieldName, String fieldValue) {
        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(fieldName, fieldValue + "*");
        this.root.should(wildcardQueryBuilder);
        return this;
    }

}

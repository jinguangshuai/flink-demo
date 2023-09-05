package com.example.demo.es.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggWrapper {

    // 分组字段
    private String groupFieldName;

    // 分组别名
    private String groupAliasName;

    private AggWrapper aggWrapper;

    public AggWrapper(String groupFieldName, String groupAliasName){
        this.groupFieldName = groupFieldName;
        this.groupAliasName = groupAliasName;
    }

}

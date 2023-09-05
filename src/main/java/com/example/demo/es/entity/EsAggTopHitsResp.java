package com.example.demo.es.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EsAggTopHitsResp<T> {

    private String aggKey;

    private List<T> aggDetailList;
    
}

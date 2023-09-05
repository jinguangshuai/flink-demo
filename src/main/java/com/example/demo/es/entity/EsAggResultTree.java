package com.example.demo.es.entity;

import lombok.Data;

import java.util.List;

@Data
public class EsAggResultTree {

    private String fieldValue;

    private String aggResultValue;

    private List<EsAggResultTree> subAggResultList;
}

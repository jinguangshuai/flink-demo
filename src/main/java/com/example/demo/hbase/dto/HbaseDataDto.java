package com.example.demo.hbase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HbaseDataDto {

    private String name;
    private String value;
}

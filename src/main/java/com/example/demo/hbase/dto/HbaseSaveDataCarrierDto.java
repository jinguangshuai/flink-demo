package com.example.demo.hbase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import java.io.Serializable;

/**
 * hbase异步插入dto
 *
 * @author : wenziang
 * @since : 2022/6/7
 */
@Data
@AllArgsConstructor
public class HbaseSaveDataCarrierDto implements Serializable {
    private Table table;
    private Put put;
}

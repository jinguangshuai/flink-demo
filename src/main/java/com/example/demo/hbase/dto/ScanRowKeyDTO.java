package com.example.demo.hbase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @ClassName : ScanRowKey  //类名
 * @Description : scan查询rowkey对象  //描述
 * @Author : MingMaster  //作者
 * @Date: 2020-08-28 //时间
 */
@Data
@AllArgsConstructor
public class ScanRowKeyDTO {
    private String startRowKey;
    private String endRowKey;
}

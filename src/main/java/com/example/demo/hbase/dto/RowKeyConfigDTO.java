package com.example.demo.hbase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : RowkeyConfig  //类名
 * @Description : rowkey配置  //描述
 * @Author : MingMaster  //作者
 * @Date: 2020-09-05 //时间
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RowKeyConfigDTO {

    private String tableName;
    private String saltMethod;
    private String rowKeyRule;
    private String scanRule;
    private String opRelateKey;
    private String opRelateTable;
    private String modelRelateKey;
    private String aclineDotRelateKey;
    private String aclineDotRelateTable;
    private String windingRelateKey;
    private String windingRelateTable;
    private String regionNumber;
    private String libraryName;
}

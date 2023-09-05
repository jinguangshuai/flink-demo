package com.example.demo.hbase.service;


import com.alicloud.openservices.tablestore.model.BatchWriteRowRequest;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.FilterList;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hbase服务接口
 *
 * @author 4600061779
 */
public interface IHBaseService {

    /**
     * 根据表名获取到HTable实例
     *
     * @param tableName HBase表名
     * @return HTable实例
     */
    Table getTable(String nameSpace, String tableName);


    /**
     * 根据表名称判断数据表是否存在
     *
     * @param tableNameStr
     * @return
     */
    boolean existTable(String nameSpace, String tableNameStr);

    /**
     * 根据表名称判断数据表是否存在
     *
     * @param nameSpace
     * @return
     */
    boolean creatNameSpace(String nameSpace);

    /**
     * 根据表名和默认配置列簇名创建HBASE数据表
     *
     * @param tableNameStr
     */
    void createTable(String nameSpace, String tableNameStr, String familyName, String rk);

    /**
     * 根据表名和默认配置列簇名创建HBASE数据表
     *
     * @param tableNameStr
     */
    void deleteTable(String nameSpace, String tableNameStr, String familyName);

    /*  *//**
     * 通过列队异步批量保存数据到hbase
     *
     * @param tableName     HBase表名
     * @param rowKey        唯一ID
     * @param keyToValueMap key:字段名，value: 字段值
     * @param columnFamily  指定列簇
     * @return
     *//*
    boolean saveDataToQueue(String tableName, String rowKey, Map<String, String> keyToValueMap, String columnFamily);

   */

    /**
     * 通过列队异步批量保存数据到hbase，指定namespace
     *
     * @param tableName     HBase表名
     * @param rowKey        唯一ID
     * @param keyToValueMap key:字段名，value: 字段值
     * @param columnFamily  指定列簇
     * @return
     */

    boolean saveDataToQueue(String tableName, String rowKey, Map<String, String> keyToValueMap, String columnFamily, String namespace);

    /**
     * 为table添加列簇
     *
     * @param nameSpace
     * @param tableNameStr
     * @param columnFamily
     * @return
     * @throws IOException
     */
    boolean addFamilyForTable(String nameSpace, String tableNameStr, String columnFamily) throws IOException;


    /**
     * 根据入参构建Put实例
     *
     * @param tableName HBase表名
     * @param rowKey    HBase表的rowkey
     * @param cf        HBase表的columnfamily
     * @param column    HBase表的列key
     * @param value     写入HBase表的值value
     */
    Put bulkPut(String tableName, String rowKey, String cf, String column, String value);


    /**
     * 根据rowkeys批量查询数据
     *
     * @param rowKeys    rowKye集合
     * @param tableName  HBase表名
     * @param familyName HBase列簇
     * @param columns    HBase字段名
     * @param namespace  命名空间
     * @return Map<唯一ID, Map < 字段名, 字段值>>
     */
    Map<String, Map<String, String>> getListByKeys(List<String> rowKeys, String tableName, String familyName, List<String> columns, String namespace);

    /**
     * 根据rowkey查询单条记录
     *
     * @param rowKey     rowKye唯一索引
     * @param tableName  HBase表名
     * @param familyName HBase列簇
     * @param columns    HBase字段名
     * @return Map<唯一ID, Map < 字段名, 字段值>>
     */
    Map<String, String> getOneByRowKey(String rowKey, String tableName, String familyName, List<String> columns, String nameSpace);

    /**
     * 根据rowkey查询单条记录并过滤
     *
     * @param rowKey     rowKye唯一索引
     * @param tableName  HBase表名
     * @param familyName HBase列簇
     * @param columns    HBase字段名
     * @param fileteMap  过滤器。key为键，value为值
     * @return Map<唯一ID, Map < 字段名, 字段值>>
     */
    Map<String, String> getOneByRowKeyAndFilter(String rowKey, String tableName, String familyName, List<String> columns, Map<String, String> fileteMap);


    /**
     * 添加一条数据
     *
     * @param rowKey      rowKye唯一索引
     * @param tableName   HBase表名
     * @param familyName  HBase列簇
     * @param keyAndValue key为字段名。value为字段值
     * @return true 成功，false 失败
     */
    boolean addRow(String rowKey, String tableName, String familyName, Map<String, String> keyAndValue);

    boolean batchInsert(BatchWriteRowRequest batchWriteRowRequest);

    boolean batchInsertBigData(BatchWriteRowRequest batchWriteRowRequest);

    boolean describeTable(String tableName);

    /**
     * 根据startRow和stopRow 固定匹配查询数据
     *
     * @param startRow   开始rowKey
     * @param endRow     借宿rowKey
     * @param tableName  HBase表名
     * @param familyName HBase列簇
     * @param columns    HBase字段名
     * @return Map<唯一ID, Map < 字段名, 字段值>>
     */
    Map<String, Map<String, String>> getFixedListByScan(String startRow, String endRow, String tableName, String familyName, String nameSpace, List<String> columns, Set<String> rowKeySet, int limit);

    /**
     * 根据startRow和stopRow 匹配查询数据
     *
     * @param startRow   开始rowKey
     * @param endRow     借宿rowKey
     * @param tableName  HBase表名
     * @param familyName HBase列簇
     * @param columns    HBase字段名
     * @param namespace  命名空间
     * @return Map<唯一ID, Map < 字段名, 字段值>>
     */
    Map<String, Map<String, String>> getListByScan(String startRow, String endRow, String tableName, String familyName, String namespace, List<String> columns);

    List<Map<String, String>> getAllByFilter(String tableName, String familyName, FilterList filterList);

    ResultScanner getAllFromTable(String tableName, String familyName, String nameSpace);

    /**
     *
     * @param startRow   开始rowKey
     * @param endRow     结束rowKey
     * @param tableName  HBase表名
     * @param familyName HBase列簇
     * @return
     */
    Map<String, Map<String, String>> getAllListByScan(String startRow, String endRow,String accurateRowKey, String tableName, String familyName, String namespace);



}

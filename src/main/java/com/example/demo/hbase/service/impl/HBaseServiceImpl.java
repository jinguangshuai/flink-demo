package com.example.demo.hbase.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.a.eye.datacarrier.DataCarrier;
import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.Row;
import com.alicloud.openservices.tablestore.model.*;
import com.google.common.collect.Maps;
import com.example.demo.hbase.dto.HbaseSaveDataCarrierDto;
import com.example.demo.hbase.properties.HBaseProperties;
import com.example.demo.hbase.service.IHBaseService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueExcludeFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author admin
 */
@Slf4j
@Data
@Service
public class HBaseServiceImpl implements IHBaseService {
    private HBaseProperties hBaseProperties;

    private MrsLoginService mrsLoginService;

    @Autowired
    @Qualifier("saveHbaseDataCarrier")
    private DataCarrier<HbaseSaveDataCarrierDto> dataCarrier;
    private Admin admin;
    private Connection conn;
    private String nameSpace;
    private String columnFamily;

    public SyncClient syncClient;
    private Map<String, SyncClient> syncClientMap = Maps.newHashMap();

    public HBaseServiceImpl(HBaseProperties hBaseProperties, MrsLoginService mrsLoginService, @Value("${jsEnv:false}")Boolean js) {

        this.hBaseProperties = hBaseProperties;
        this.mrsLoginService = mrsLoginService;

        log.info("hBaseProperties:{}", hBaseProperties);
        Configuration configuration = null;
        String type = hBaseProperties.getType();
        if (StrUtil.isEmpty(type)) {
            throw new RuntimeException("配置文件中hbase.type不能为空");
        }
        this.nameSpace = hBaseProperties.getNameSpace();
        this.columnFamily = hBaseProperties.getColumnFamily();
        if (type.equals("mrs2") || type.equals("mrs3")) {
            configuration = genMrsConf();
        } else if (type.equals("ots")) {
            syncClient = syncClientMap.get("syncClient");
            if(Objects.isNull(syncClient)){
                syncClient = genOtsConf();
                syncClientMap.put("syncClient", syncClient);
            }
            configuration = genOtsConfOld();
        } else if (type.equals("cdh")) {
            configuration = genCdhConf(js);
        } else {
            String errorInfo = StrUtil.format("--->>hbase组件初始化失败，配置hbase.type：{}匹配失败，目前只支持mrs2、mrs3、ots、chd四种类型！", type);
            throw new RuntimeException(errorInfo);
        }
        try {
            conn = ConnectionFactory.createConnection(configuration);
            admin = conn.getAdmin();
        } catch (IOException e) {
            log.error("--->>hbase建立连接失败！", e);
            //throw new RuntimeException("hbase建立连接失败");
        }
}

    /**
     * OTS - HBASE 初始化
     *
     * @return
     */
    private SyncClient genOtsConf() {
        HBaseProperties.OtsProperties ots = hBaseProperties.getOts();
        log.info("=============》》初始化OTS-hbase工具！！！");
        log.info("endpoint:{},accessKeyId:{},accessKeySecret:{},instanceName:{}",ots.getEndpoint(),ots.getAccessKeyId(),ots.getAccessKeySecret(),ots.getInstanceName());
        SyncClient syncClient = new SyncClient(ots.getEndpoint(),ots.getAccessKeyId(),ots.getAccessKeySecret(),ots.getInstanceName());
        log.info("初始化OTS-Client成功");
        return syncClient;
    }

    /**
     * MRS - HBASE 初始化
     *
     * @return
     */
    private Configuration genMrsConf() {
        String type = hBaseProperties.getType();
        log.info("=============》》初始化{}-hbase工具！！！", type);
        Configuration configuration = HBaseConfiguration.create();
        HBaseProperties.MrsProperties mrsProperties = hBaseProperties.getMrs();
        initResource(configuration, mrsProperties);
        configuration.set("hbase.zookeeper.quorum", mrsProperties.getHbaseZookeeperQuorum());
        configuration.set("hbase.zookeeper.property.clientPort", mrsProperties.getHbaseZookeeperPropertyClientPort());
        if (!StrUtil.equals("simple", mrsProperties.getHbaseSecurityAuthentication())) {
            configuration.set("hbase.security.authentication", mrsProperties.getHbaseSecurityAuthentication());
            configuration.set("hadoop.security.authentication", mrsProperties.getHbaseSecurityAuthentication());
            configuration.set("hbase.regionserver.kerberos.principal", mrsProperties.getRegionserverKerberosPrincipal());
            configuration.set("hbase.master.kerberos.princial", mrsProperties.getMasterKerberosPrincipal());
            mrsLoginService.mrsHbaseLogin(configuration);
        }
        log.info("hbase配置项信息中hadoop.rpc.protection的值为{}", configuration.get("hadoop.rpc.protection"));
        return configuration;
    }

    /**
     * 初始化Hbase相关配置信息
     *
     * @param configuration
     * @param mrsProperties
     */
    private void initResource(Configuration configuration, HBaseProperties.MrsProperties mrsProperties) {
        if (StrUtil.isNotBlank(mrsProperties.getCoreSiteFilePath())) {
            File coreSiteFile = new File(mrsProperties.getCoreSiteFilePath());
            if (coreSiteFile.exists()) {
                configuration.addResource(new Path(coreSiteFile.getAbsolutePath()));
                log.info("--->使用现场core-site.xml文件");
            } else {
                log.info("--->使用默认core-default.xml文件");
            }
        }
        if (StrUtil.isNotBlank(mrsProperties.getHbaseSiteFilePath())) {
            File hbaseSiteFile = new File(mrsProperties.getHbaseSiteFilePath());
            if (hbaseSiteFile.exists()) {
                configuration.addResource(new Path(hbaseSiteFile.getAbsolutePath()));
                log.info("--->使用现场hbase-site.xml文件");
            } else {
                log.info("--->使用默认hbase-default.xml文件");
            }
        }
        if (StrUtil.isNotBlank(mrsProperties.getHdfsSiteFilePath())) {
            File hdfsSiteFile = new File(mrsProperties.getHdfsSiteFilePath());
            if (hdfsSiteFile.exists()) {
                configuration.addResource(new Path(hdfsSiteFile.getAbsolutePath()));
                log.info("--->使用现场hdfs-site.xml文件");
            } else {
                log.info("--->使用默认hdfs-default.xml文件");
            }
        }
    }

    /**
     * CDH - HBASE 初始化
     *
     * @return
     */
    private Configuration genCdhConf(Boolean js) {
        log.info("=============》》初始化CDH-hbase工具！！！");
        System.setProperty("java.security.krb5.conf","/tmp/krb5.conf");
        Configuration configuration = HBaseConfiguration.create();
        HBaseProperties.CdhProperties cdhProperties = hBaseProperties.getCdh();
        if (StrUtil.isNotBlank(cdhProperties.getPort())) {
            configuration.set("hbase.zookeeper.property.clientPort", cdhProperties.getPort());
        }
        //configuration.set("hbase.client.keyvalue.maxsize", cdhProperties.getMaxsize());

        if (js){
            try {
                configuration.addResource(new Path("/tmp/hbase-site.xml"));
                configuration.set("hadoop.security.authentication", "kerberos");
                /*configuration.set("hbase.security.authentication", "kerberos");
                configuration.set("hbase.cluster.distributed", "true");
                configuration.set("hbase.rpc.protection", "authentication");
                configuration.set("hbase.regionserver.kerberos.principal", "hbase/_HOST@HADOOP.COM");
                configuration.set("hbase.master.kerberos.principal", "hbase/_HOST@HADOOP.COM");*/
                UserGroupInformation.setConfiguration(configuration);
                UserGroupInformation.loginUserFromKeytab("lczx@HADOOP.COM", "/tmp/lczx.keytab");
            }catch(Exception e) {
                log.error("认证失败信息如下，" + e);
            }
        }else {
            configuration.set("hbase.zookeeper.quorum", cdhProperties.getZookeeperQuorum());
            configuration.set("hbase.client.username", cdhProperties.getUsername());
            configuration.set("hbase.client.password", cdhProperties.getPassword());
        }
        return configuration;
    }

    /**
     * OTS - HBASE 初始化
     *
     * @return
     */
    private Configuration genOtsConfOld() {
        log.info("=============》》初始化OTS-hbase工具！！！");
        Configuration configuration = HBaseConfiguration.create();
        HBaseProperties.OtsProperties otsProperties = hBaseProperties.getOts();
        configuration.set("hbase.client.connection.impl", "com.alicloud.tablestore.hbase.TablestoreConnection");
        configuration.set("tablestore.client.endpoint", otsProperties.getEndpoint());
        configuration.set("tablestore.client.instancename", otsProperties.getInstanceName());
        configuration.set("tablestore.client.accesskeyid", otsProperties.getAccessKeyId());
        configuration.set("tablestore.client.accesskeysecret", otsProperties.getAccessKeySecret());
        configuration.set("hbase.client.tablestore.family", columnFamily);
        return configuration;
    }


    /**
     * 根据表名获取到HTable实例
     *
     * @param tableNameStr HBase表名
     * @return HTable实例
     */
    @Override
    public Table getTable(String nameSpace, String tableNameStr) {
        Table table = null;
        if (StringUtils.isEmpty(nameSpace)) {
            nameSpace = this.nameSpace;
        }
        try {
            TableName tableName = getTableName(nameSpace, tableNameStr);
            table = conn.getTable(tableName);
        } catch (IOException e) {
            log.error("--->>根据表名:{},获取Htable实例失败！", tableNameStr, e);
        }
        return table;
    }


    /**
     * 根据表名称判断数据表是否存在
     *
     * @param tableNameStr
     * @return
     */
    @Override
    public boolean existTable(String nameSpace, String tableNameStr) {
        if ("ots".equalsIgnoreCase(hBaseProperties.getType())) {
            // 获取所有的表
            ListTableResponse response = syncClient.listTable();
            for(String tableName : response.getTableNames()){
                if(tableNameStr.equalsIgnoreCase(tableName)){
                    return true;
                }
            }
            return false;
        }else{
            if (StringUtils.isEmpty(nameSpace)) {
                nameSpace = this.nameSpace;
            }
            TableName tableName = getTableName(nameSpace, tableNameStr);
            try {
                return admin.tableExists(tableName);
            } catch (IOException e) {
                log.error("--->>根据表名称判断数据表是否存在异常!", e);
            }
            return false;
        }
    }

    @Override
    public boolean creatNameSpace(String nameSpace) {
        if ("ots".equalsIgnoreCase(hBaseProperties.getType())) {
            log.info("ots不需要创建命名空间");
            return false;
        }
        try {
            //获取所有的命名空间
            NamespaceDescriptor[] namespaceDescriptors = admin.listNamespaceDescriptors();
            for (NamespaceDescriptor namespaceDescriptor : namespaceDescriptors) {
                if (nameSpace.equals(namespaceDescriptor.getName())) {
                    log.info("命名空间已存在！");
                    return false;
                }
            }
        } catch (IOException e) {
            log.error("获取命名空间列表异常！", e);
            return false;
        }
        NamespaceDescriptor.Builder builder = NamespaceDescriptor.create(nameSpace);
        NamespaceDescriptor descriptor = builder.build();
        try {
            admin.createNamespace(descriptor);
        } catch (IOException e) {
            log.error("创建namespace-{}失败！-》", nameSpace, e);
            return false;
        }
        return true;
    }

    /**
     * 根据表名称和列簇名称创建HBASE数据表
     *
     * @param tableNameStr
     * @param columnFamily
     */
    @Override
    public void createTable(String nameSpace, String tableNameStr, String columnFamily, String rk) {
        if("ots".equalsIgnoreCase(hBaseProperties.getType())){
            TableMeta tableMeta = new TableMeta(tableNameStr);
            // 为主表添加主键列
            tableMeta.addPrimaryKeyColumn(new PrimaryKeySchema(rk, PrimaryKeyType.STRING));
            // 数据的过期时间, -1表示永不过期。带索引表的数据表数据生命周期必须设置为-1
            int timeToLive = -1;
            // 保存的最大版本数，1表示每列上最多保存一个版本即保存最新的版本。带索引表的数据表最大版本数必须设置为1。
            int maxVersions = 1;
            TableOptions tableOptions = new TableOptions(timeToLive, maxVersions);

            CreateTableRequest request = new CreateTableRequest(tableMeta, tableOptions);
            request.setReservedThroughput(new ReservedThroughput(new CapacityUnit(0,0)));
            getSyncClient().createTable(request);
        }else{
            TableName tableName = getTableName(nameSpace, tableNameStr);
            HTableDescriptor desc = new HTableDescriptor(tableName);
            HColumnDescriptor colDesc = new HColumnDescriptor(columnFamily);
            desc.addFamily(colDesc);
            try {
                admin.createTable(desc);
                log.info("--->>已创建表:{}", tableNameStr);
            } catch (IOException e) {
                log.error("--->>根据表名称和列簇名称创建hbase数据表异常!", e);
            }
        }
    }

    /**
     * 根据参数删除对应表
     *
     * @param tableNameStr
     * @param columnFamily
     */
    @Override
    public void deleteTable(String nameSpace, String tableNameStr, String columnFamily) {
        TableName tableName = getTableName(nameSpace, tableNameStr);
        HTableDescriptor desc = new HTableDescriptor(tableName);
        HColumnDescriptor colDesc = new HColumnDescriptor(columnFamily);
        desc.addFamily(colDesc);
        try {
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
            log.info("--->>已创建表:{}", tableNameStr);
        } catch (IOException e) {
            log.error("--->>根据表名称和列簇名称刪除hbase数据表异常!", e);
        }
    }


    /**
     * 通过列队异步批量保存数据到hbase
     *
     * @param tableName     HBase表名
     * @param rowKey        唯一ID
     * @param keyToValueMap key:字段名，value: 字段值
     * @return
     */
    @Override
    public boolean saveDataToQueue(String tableName, String rowKey, Map<String, String> keyToValueMap, String columnFamily, String nameSpace) {
        Table table = getTable(nameSpace, tableName);
        Set<String> keySet = keyToValueMap.keySet();
        //阿里云最大插入列为1000，此处对大于1000的列进行切割，分批生成put对象
        List<List<String>> columnPages = CollUtil.split(keySet, 1000);

        for (List<String> columns : columnPages) {
            Put put = new Put(Bytes.toBytes(rowKey));
            for (String column : columns) {
                String value = keyToValueMap.get(column);
                if (column == null || value == null) {
                    log.warn("--->>tableName:{},rowKey:{},键值{}为空!", tableName, rowKey, keyToValueMap);
                    continue;
                }
                put.addColumn(columnFamily.getBytes(), column.getBytes(), value.getBytes());
            }

            if (put.size() > 0) {
                dataCarrier.produce(new HbaseSaveDataCarrierDto(table, put));
            }
/*            try {
                admin.flush(table.getName());
            } catch (IOException e) {
                log.error("hbase刷新失败", e);
            }*/
        }

        return true;
    }

    /***
     * 为table添加列簇
     * @param tableNameStr     HBase表名
     * @param columnFamily 指定列簇
     * @return
     * @throws IOException
     */
    @Override
    public boolean addFamilyForTable(String nameSpace, String tableNameStr, String columnFamily) throws IOException {
        TableName tableName = getTableName(nameSpace, tableNameStr);
        admin.disableTable(tableName);
        HTableDescriptor tableDescriptor = admin.getTableDescriptor(tableName);
        HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(columnFamily);
        tableDescriptor.addFamily(hColumnDescriptor);
        admin.modifyTable(tableName, tableDescriptor);
        admin.enableTable(tableName);
        return true;
    }


    /**
     * 根据入参构建Put实例
     *
     * @param tableName HBase表名
     * @param rowKey    HBase表的rowkey
     * @param cf        HBase表的columnfamily
     * @param column    HBase表的列key
     * @param value     写入HBase表的值value
     */
    @Override
    public Put bulkPut(String tableName, String rowKey, String cf, String column, String value) {
        Put put = new Put(Bytes.toBytes(rowKey));
        if (value == null) {
            log.warn("--->>tableName:{},rowKey:{},字段:{}的值为空!", tableName, rowKey, column);
            return null;
        }
        put.addColumn(Bytes.toBytes(cf), Bytes.toBytes(column), Bytes.toBytes(value));
        //将数据添加到队列
        return put;
    }

    /**
     * 根据rowkeys批量查询数据
     *
     * @param rowKeys    rowKye集合
     * @param tableName  HBase表名
     * @param familyName HBase列簇
     * @param columns    HBase字段名
     * @return Map<唯一ID, Map < 字段名, 字段值>>
     */
    @Override
    public Map<String, Map<String, String>> getListByKeys(List<String> rowKeys, String tableName, String familyName, List<String> columns, String nameSpace) {
        log.info("---->>查询tableName:{},rowkey,rowKeys：{}", tableName, rowKeys);
        log.debug("--->>查询条件：columnFamily:{},columnList:{},nameSpace:{}", familyName, columns, nameSpace);

        Map<String, Map<String, String>> listByKeys = new ConcurrentHashMap<>();
        int pageSize = 10000;
        if (StrUtil.equals("ots", hBaseProperties.getType())) {
            pageSize = 1000;
        }
        List<List<String>> columnPages = ListUtil.split(columns, pageSize);
        if (CollUtil.isNotEmpty(columnPages)) {
            columnPages.forEach(columnPage -> {
                Map<String, Map<String, String>> queryRes = getListByKeysAndSubColumn(rowKeys, tableName, columnFamily, columnPage, nameSpace);
                if (ObjectUtil.isNotEmpty(queryRes)) {
                    //循环查询结果，如果listByKeys有相同的key，就将值累加，如果没有，就put一个新key和value进去
                    for (String key : queryRes.keySet()) {
                        if (listByKeys.containsKey(key)) {
                            listByKeys.get(key).putAll(queryRes.get(key));
                        } else {
                            Map<String, String> value = queryRes.get(key);
                            listByKeys.put(key, value);
                        }
                    }
                }
            });
        } else {
            Map<String, Map<String, String>> queryRes = getListByKeysAndSubColumn(rowKeys, tableName, columnFamily, nameSpace);
            if (ObjectUtil.isNotEmpty(queryRes)) {
                //循环查询结果，如果listByKeys有相同的key，就将值累加，如果没有，就put一个新key和value进去
                for (String key : queryRes.keySet()) {
                    if (listByKeys.containsKey(key)) {
                        listByKeys.get(key).putAll(queryRes.get(key));
                    } else {
                        Map<String, String> value = queryRes.get(key);
                        listByKeys.put(key, value);
                    }
                }
            }
        }

        return listByKeys;
    }

    private Map<String, Map<String, String>> getListByKeysAndSubColumn(List<String> rowKeys, String tableName, String familyName, List<String> columns, String nameSpace) {
        Map<String, Map<String, String>> resultMap = new HashMap<>();
        //构建查询条件
        Table table = getTable(nameSpace, tableName);
        List<Get> gets = new ArrayList<>();
        rowKeys.forEach(rowKey -> {
            Get get = new Get(rowKey.getBytes());
            columns.forEach(key -> {
                get.addColumn(familyName.getBytes(), key.getBytes());
            });
            gets.add(get);
        });
        //解析查询结果
        try {
            Result[] result = table.get(gets);
            if (result.length == 0) {
                log.info("--->>tableName:{},根据rowKey集合查询结果为空!", tableName);
            }
            getResultMap(resultMap, result);
        } catch (IOException e) {
            log.error("--->>tableName:{},根据rowKey集合获取数据失败!", tableName, e);
        }
        return resultMap;
    }

    private Map<String, Map<String, String>> getListByKeysAndSubColumn(List<String> rowKeys, String tableName, String familyName, String nameSpace) {
        Map<String, Map<String, String>> resultMap = new HashMap<>();
        //构建查询条件
        Table table = getTable(nameSpace, tableName);
        List<Get> gets = new CopyOnWriteArrayList<>();
        rowKeys.parallelStream().forEach(rowKey -> {
            Get get = new Get(rowKey.getBytes());
            get.addFamily(familyName.getBytes());
            gets.add(get);
        });
        //解析查询结果
        try {
            Result[] result = table.get(gets);
            if (result.length == 0) {
                log.info("--->>tableName:{},根据rowKey集合查询结果为空!", tableName);
            }
            getResultMap(resultMap, result);
        } catch (IOException e) {
            log.error("--->>tableName:{},根据rowKey集合获取数据失败!", tableName, e);
        }
        return resultMap;
    }

    private void getResultMap(Map<String, Map<String, String>> resultMap, Result[] result) {
        for (Result result1 : result) {
            for (Cell cell : result1.rawCells()) {
                String rowKey = Bytes.toString(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                String key = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                if (!resultMap.containsKey(rowKey)) {
                    resultMap.put(rowKey, new HashMap<>());
                }
                resultMap.get(rowKey).put(key, value);
            }
        }
    }

    /**
     * 根据rowkey查询单条记录
     *
     * @param rowKey     rowKye唯一索引
     * @param tableName  HBase表名
     * @param familyName HBase列簇
     * @param columns    HBase字段名
     * @return Map<唯一ID, Map < 字段名, 字段值>>
     */
    @Override
    public Map<String, String> getOneByRowKey(String rowKey, String tableName, String familyName, List<String> columns, String nameSpace) {
        Map<String, String> kvMap = Maps.newHashMap();

        if("ots".equalsIgnoreCase(hBaseProperties.getType())){
            //构造主键
            PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
            primaryKeyBuilder.addPrimaryKeyColumn("RK", PrimaryKeyValue.fromString(rowKey));
            PrimaryKey primaryKey = primaryKeyBuilder.build();

            //OTS 读取一行数据
            SingleRowQueryCriteria criteria = new SingleRowQueryCriteria(tableName, primaryKey);
            //设置读取最新版本
            criteria.setMaxVersions(1);
            GetRowResponse getRowResponse = syncClient.getRow(new GetRowRequest(criteria));
            Row row = getRowResponse.getRow();
            if(!Objects.isNull(row)){
                Column[] column = row.getColumns();
                Arrays.asList(column).stream().forEach(t-> kvMap.put(t.getName(), t.getValue().asString()));
            }
        }else{
            log.info("---->>查询tableName:{},rowKeys：{}", tableName, rowKey);
            log.debug("--->>查询条件：columnFamily:{},columnList:{},nameSpace:{}", familyName, columns, nameSpace);
            //构建查询条件
            List<List<String>> split = ListUtil.split(columns, 999);
            if (CollUtil.isEmpty(columns)) {
                log.warn("查询项不能为空");
                return null;
            }
            if (split.size() == 1) {
                try {
                    getHbaseValues(rowKey, tableName, familyName, nameSpace, kvMap, columns);
                } catch (IOException e) {
                    log.error("--->>tableName:{},根据rowKey获取单条记录失败!", tableName, e);
                }
            } else {
                for (List<String> columnList : split) {
                    try {
                        getHbaseValues(rowKey, tableName, familyName, nameSpace, kvMap, columnList);
                    } catch (IOException e) {
                        log.error("--->>tableName:{},根据rowKey获取单条记录失败!", tableName, e);
                    }
                }
            }
            log.debug("--->>查询tableName:{},结果：{}", tableName, kvMap);
            return kvMap;
        }
        return kvMap;
    }

    private void getHbaseValues(String rowKey, String tableName, String familyName, String nameSpace, Map<String, String> kvMap, List<String> columns) throws IOException {
        Table table = getTable(nameSpace, tableName);
        Get get = new Get(rowKey.getBytes());
        columns.forEach(key -> {
            get.addColumn(familyName.getBytes(), key.getBytes());
        });
        Result result = table.get(get);
        Cell[] cells = result.rawCells();
        if (cells.length == 0) {
            log.info("--->>查询hbase结果为空!");
            return;
        }
        for (Cell cell : cells) {
            String key = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
            String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            kvMap.put(key, value);
        }
    }

    /**
     * 根据rowkey查询单条记录
     *
     * @param rowKey     rowKye唯一索引
     * @param tableName  HBase表名
     * @param familyName HBase列簇
     * @param columns    HBase字段名
     * @return Map<唯一ID, Map < 字段名, 字段值>>
     */
    @Override
    public Map<String, String> getOneByRowKeyAndFilter(String rowKey, String tableName, String familyName, List<String> columns, Map<String, String> fileteMap) {
        log.info("---->>查询tableName:{},rowKeys：{}", tableName, rowKey);
        log.debug("--->>查询条件：columnFamily:{},columnList:{},nameSpace:{}", familyName, columns, nameSpace);
        Map<String, String> kvMap = new HashMap<>();
        //构建查询条件
        try {
            Table table = getTable(nameSpace, tableName);
            Get get = new Get(rowKey.getBytes());
            columns.forEach(key -> {
                get.addColumn(familyName.getBytes(), key.getBytes());
            });
            //根据过滤条件生成过滤器
            for (String key : fileteMap.keySet()) {
                SingleColumnValueExcludeFilter scve = new SingleColumnValueExcludeFilter(familyName.getBytes(), key.getBytes(), CompareFilter.CompareOp.EQUAL, fileteMap.get(key).getBytes());
                get.setFilter(scve);
            }
            Result result = table.get(get);
            Cell[] cells = result.rawCells();
            if (cells.length == 0) {
                log.info("--->>查询hbase结果为空!");
                return null;
            }
            for (Cell cell : cells) {
                String key = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                kvMap.put(key, value);
            }
        } catch (IOException e) {
            log.error("--->>tableName:{},根据rowKey获取单条记录失败!", tableName, e);
        }
        log.debug("--->>查询tableName:{},结果：{}", tableName, kvMap);
        return kvMap;
    }

    /**
     * 添加一条数据
     *
     * @param rowKey      rowKye唯一索引
     * @param tableName   HBase表名
     * @param familyName  HBase列簇
     * @param keyAndValue key为字段名。value为字段值
     * @return true 成功，false 失败
     */
    @Override
    public boolean addRow(String rowKey, String tableName, String familyName, Map<String, String> keyAndValue) {
        try {
            Put put = new Put(rowKey.getBytes());
            keyAndValue.forEach((key, valye) -> {
                put.addColumn(familyName.getBytes(), key.getBytes(), valye.getBytes());
            });
            Table table = getTable(nameSpace, tableName);
            table.put(put);
        } catch (IOException e) {
            log.error("--->>HBase添加数据失败!", e);
        }
        return true;
    }

    @Override
    public boolean batchInsert(BatchWriteRowRequest batchWriteRowRequest) {
        BatchWriteRowResponse response = syncClient.batchWriteRow(batchWriteRowRequest);
        if (!response.isAllSucceed()) {
            for (BatchWriteRowResponse.RowResult rowResult : response.getFailedRows()) {
                log.info("失败的行：{}, 失败原因{}",
                        batchWriteRowRequest.getRowChange(rowResult.getTableName(), rowResult.getIndex())
                                .getPrimaryKey(), rowResult.getError());
            }
            /**
             * 可以通过createRequestForRetry方法再构造一个请求对失败的行进行重试。此处只给出构造重试请求的部分。
             * 推荐的重试方法是使用SDK的自定义重试策略功能，支持对batch操作的部分行错误进行重试。设置重试策略后，调用接口处无需增加重试代码。
             */
            BatchWriteRowRequest retryRequest = batchWriteRowRequest.createRequestForRetry(response.getFailedRows());
            syncClient.batchWriteRow(retryRequest);
        }
        return true;
    }

    @Override
    public boolean batchInsertBigData(BatchWriteRowRequest batchWriteRowRequest) {
        int rowsCount = batchWriteRowRequest.getRowsCount();
        // 若批量插入的总行数大于200，则需要分多次请求
        if (rowsCount > 200) {
            Map<String, List<RowChange>> changes = batchWriteRowRequest.getRowChange();
            for (Map.Entry<String, List<RowChange>> entry : changes.entrySet()) {
                //获取一个key的list与长度
                List<RowChange> list = entry.getValue();
                int size = list.size();
                //长度大于200，需要分割该key的list
                if (list != null && size > 200) {
                    for (int i = 0; i < (size%200 == 0 ? size/200 : size/200+1); i++) {
                        int start = i * 200;
                        int end = Math.min((i+1)*200,size);
                        List<RowChange> reqList = list.subList(start, end);
                        BatchWriteRowRequest subReq = new BatchWriteRowRequest();
                        reqList.stream().forEach(req->subReq.addRowChange(req));
                        BatchWriteRowResponse response = syncClient.batchWriteRow(subReq);
                        if (!response.isAllSucceed()) {
                            for (BatchWriteRowResponse.RowResult rowResult : response.getFailedRows()) {
                                log.info("失败的行：{}, 失败原因{}",
                                        subReq.getRowChange(rowResult.getTableName(), rowResult.getIndex())
                                                .getPrimaryKey(), rowResult.getError());
                            }
                            /**
                             * 可以通过createRequestForRetry方法再构造一个请求对失败的行进行重试。此处只给出构造重试请求的部分。
                             * 推荐的重试方法是使用SDK的自定义重试策略功能，支持对batch操作的部分行错误进行重试。设置重试策略后，调用接口处无需增加重试代码。
                             */
                            BatchWriteRowRequest retryRequest = subReq.createRequestForRetry(response.getFailedRows());
                            syncClient.batchWriteRow(retryRequest);
                        }
                    }
                } else {
                    //长度不大于200，可能存在多个key的情况，按key分多次请求
                    BatchWriteRowRequest subReq = new BatchWriteRowRequest();
                    list.stream().forEach(req->subReq.addRowChange(req));
                    BatchWriteRowResponse response = syncClient.batchWriteRow(subReq);
                    if (!response.isAllSucceed()) {
                        for (BatchWriteRowResponse.RowResult rowResult : response.getFailedRows()) {
                            log.info("失败的行：{}, 失败原因{}",
                                    subReq.getRowChange(rowResult.getTableName(), rowResult.getIndex())
                                            .getPrimaryKey(), rowResult.getError());
                        }
                        /**
                         * 可以通过createRequestForRetry方法再构造一个请求对失败的行进行重试。此处只给出构造重试请求的部分。
                         * 推荐的重试方法是使用SDK的自定义重试策略功能，支持对batch操作的部分行错误进行重试。设置重试策略后，调用接口处无需增加重试代码。
                         */
                        BatchWriteRowRequest retryRequest = subReq.createRequestForRetry(response.getFailedRows());
                        syncClient.batchWriteRow(retryRequest);
                    }
                }
            }
        } else {
            //总列数不大于200，直接请求
            BatchWriteRowResponse response = syncClient.batchWriteRow(batchWriteRowRequest);
            if (!response.isAllSucceed()) {
                for (BatchWriteRowResponse.RowResult rowResult : response.getFailedRows()) {
                    log.info("失败的行：{}, 失败原因{}",
                            batchWriteRowRequest.getRowChange(rowResult.getTableName(), rowResult.getIndex())
                                    .getPrimaryKey(), rowResult.getError());
                }
                /**
                 * 可以通过createRequestForRetry方法再构造一个请求对失败的行进行重试。此处只给出构造重试请求的部分。
                 * 推荐的重试方法是使用SDK的自定义重试策略功能，支持对batch操作的部分行错误进行重试。设置重试策略后，调用接口处无需增加重试代码。
                 */
                BatchWriteRowRequest retryRequest = batchWriteRowRequest.createRequestForRetry(response.getFailedRows());
                syncClient.batchWriteRow(retryRequest);
            }
        }

        return true;
    }

    @Override
    public boolean describeTable(String tableName) {
        try{
            DescribeTableRequest request = new DescribeTableRequest(tableName);
            DescribeTableResponse response = syncClient.describeTable(request);
            TableMeta tableMeta = response.getTableMeta();
        }catch (Exception e){
            log.error("表描述信息错误{}", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 根据startRow和stopRow 匹配查询数据
     *
     * @param startRow   开始rowKey
     * @param endRow     借宿rowKey
     * @param tableName  HBase表名
     * @param familyName HBase列簇
     * @param columns    HBase字段名
     * @return Map<唯一ID, Map < 字段名, 字段值>>
     */
    @Override
    public Map<String, Map<String, String>> getListByScan(String startRow, String endRow, String tableName, String familyName, String nameSpace, List<String> columns) {
        log.debug("--->>查询条件：columnFamily:{},columnList:{},nameSpace:{}", familyName, columns, nameSpace);
        Map<String, Map<String, String>> resultMap = new HashMap<>(4);
        Table table = getTable(nameSpace, tableName);
        if (!existTable(nameSpace, tableName)) {
            log.error("表{}：{}不存在", nameSpace, tableName);
            return null;
        }
        //量测项阿里云ots每次查询1000项， 其他全部一次查询
        List<List<String>> split = ListUtil.split(columns, 1000);
        if (split.size() == 1 || CollUtil.isEmpty(columns) || !StrUtil.equals(hBaseProperties.getType(), "ots")) {
            getHbaseDataByScan(startRow, endRow, familyName, columns, resultMap, table);
        } else {
            split.forEach(columnList -> getHbaseDataByScan(startRow, endRow, familyName, columnList, resultMap, table));
        }
        log.info("---->>范围rk查询，tableName:{},rowKeyStart:{},endRowKeys:{}", tableName, startRow, endRow);
        if (CollUtil.isNotEmpty(resultMap)) {
            log.debug("--->>查询结果,tableName:{},值：{}", tableName, resultMap);
        }
        return resultMap;
    }

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
    @Override
    public Map<String, Map<String, String>> getFixedListByScan(String startRow, String endRow, String tableName, String familyName, String nameSpace, List<String> columns, Set<String> rowKeySet, int limit) {
        log.debug("--->>查询条件：columnFamily:{},columnList:{},nameSpace:{}", familyName, columns, nameSpace);
        Map<String, Map<String, String>> resultMap = new HashMap<>(4);
        Table table = getTable(nameSpace, tableName);
        //量测项阿里云ots每次查询1000项， 其他全部一次查询
        List<List<String>> split = ListUtil.split(columns, 1000);
        if (split.size() == 1 || CollUtil.isEmpty(columns) || !StrUtil.equals(hBaseProperties.getType(), "ots")) {
            getFixedHbaseDataByScan(startRow, endRow, familyName, columns, resultMap, table, rowKeySet, limit);
        } else {
            split.forEach(columnList -> getFixedHbaseDataByScan(startRow, endRow, familyName, columnList, resultMap, table, rowKeySet, limit));
        }
        log.info("---->>范围rk查询，tableName:{},rowKeyStart:{},endRowKeys:{}", tableName, startRow, endRow);
        log.debug("--->>查询结果,tableName:{},值：{}", tableName, resultMap);
        return resultMap;
    }

    /**
     * 通过scan获取hbase数据
     *
     * @param startRow   开始rowKey
     * @param endRow     结束rowKey
     * @param familyName HBase列簇
     * @param columns    HBase字段名
     * @param resultMap  查询结果
     * @param table      表实例
     */

    private void getHbaseDataByScan(String startRow, String endRow, String familyName, List<String> columns, Map<String, Map<String, String>> resultMap, Table table) {
        ResultScanner results = null;
        try {
            Scan scan = new Scan();
            scan.setStartRow(startRow.getBytes());
            scan.setStopRow(endRow.getBytes());

            //配置cache
            int cacheSize = hBaseProperties.getScanCacheSize();
            if ("ots".equals(hBaseProperties.getType())) {
                cacheSize = 5000;
            }
            scan.setCaching(cacheSize);

            columns.forEach(column -> {
                scan.addColumn(familyName.getBytes(), column.getBytes());
            });
            results = table.getScanner(scan);
            results.forEach(result -> {
                for (Cell cell : result.listCells()) {
                    String rowKey = Bytes.toString(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                    String key = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                    String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                    if (!resultMap.containsKey(rowKey)) {
                        resultMap.put(rowKey, new HashMap<>(4));
                    }
                    resultMap.get(rowKey).put(key, value);
                }
            });
        } catch (IOException e) {
            log.error("--->>范围查询数据异常：", e);
        } finally {
            try {
                //关闭ResultScanner，否则RegionServer对应资源无法释放
                if(null != results){
                    results.close();
                }
            } catch (Exception e) {
                log.error("--->>关闭ResultScanner失败：", e);
            }

        }
    }

    /**
     * 通过scan获取固定的hbase数据
     *
     * @param startRow   开始rowKey
     * @param endRow     结束rowKey
     * @param familyName HBase列簇
     * @param columns    HBase字段名
     * @param resultMap  查询结果
     * @param table      表实例
     */
    private void getFixedHbaseDataByScan(String startRow, String endRow, String familyName, List<String> columns, Map<String, Map<String, String>> resultMap, Table table, Set<String> rowKeySet, int limit) {
        ResultScanner results = null;
        try {
            Scan scan = new Scan();
            scan.setStartRow(startRow.getBytes());
            scan.setStopRow(endRow.getBytes());

            //配置cache
            int cacheSize = hBaseProperties.getScanCacheSize();
            if ("ots".equals(hBaseProperties.getType())) {
                cacheSize = 5000;
            }
            scan.setCaching(cacheSize);

            columns.forEach(column -> {
                scan.addColumn(familyName.getBytes(), column.getBytes());
            });
            results = table.getScanner(scan);
            for (Result result : results) {
                boolean isFullKey = false;
                for (Cell cell : result.listCells()) {
                    String rowKey = Bytes.toString(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                    String key = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                    String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                    if (StrUtil.equals("IS_DELETED", key) && StrUtil.equals("0", value)) {
                        rowKeySet.add(rowKey);
                        if (rowKeySet.size() > limit) {
                            isFullKey = true;
                            break;
                        }
                    }
                    if (!resultMap.containsKey(rowKey)) {
                        resultMap.put(rowKey, new HashMap<>(4));
                    }
                    resultMap.get(rowKey).put(key, value);
                }
                if (isFullKey) {
                    log.info("不分页查询hbase数据已达到查询总数！,rowKey总数:{}", rowKeySet.size());
                    break;
                }
            }
        } catch (IOException e) {
            log.error("--->>范围查询数据异常：", e);
        } finally {
            try {
                //关闭ResultScanner，否则RegionServer对应资源无法释放
                if(null != results){
                    results.close();
                }
            } catch (Exception e) {
                log.error("--->>关闭ResultScanner失败：", e);
            }

        }
    }

    private TableName getTableName(String nameSpace, String tableNameStr) {
        TableName tableName;
        if ("ots".equalsIgnoreCase(hBaseProperties.getType())) {
            tableName = TableName.valueOf(tableNameStr);
        } else {
            tableName = TableName.valueOf(nameSpace, tableNameStr);
        }
        return tableName;
    }

    @Override
    public List<Map<String, String>> getAllByFilter(String tableName, String familyName, FilterList filterList) {
        log.info("---->>查询tableName:{}", tableName);
        log.debug("--->>查询条件：columnFamily:{},nameSpace:{}", familyName, nameSpace);
        List<Map<String, String>> res = new ArrayList<>();
        Scan scan = new Scan();
        //构建查询条件
        try {
            Table table = getTable(nameSpace, tableName);
            scan.setFilter(filterList);
            ResultScanner results = table.getScanner(scan);
            results.forEach(result -> res.add(new HashMap<String, String>() {{
                result.listCells().forEach(cell -> {
                    String key = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                    String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                    put(key, value);
                });
            }}));
        } catch (IOException e) {
            log.error("--->>tableName:{},根据过滤条件获取多条记录失败!", tableName, e);
        }
        log.debug("--->>查询tableName:{},结果：{}", tableName, res);
        return res;
    }

    @Override
    public ResultScanner getAllFromTable(String tableName, String familyName, String nameSpace) {
        log.info("---->>查询tableName:{}", tableName);
        log.debug("--->>查询条件：columnFamily:{},nameSpace:{}", familyName, nameSpace);
        ResultScanner results = null;
        //构建查询条件
        try {
            Table table = getTable(nameSpace, tableName);
            results = table.getScanner(new Scan());
        } catch (IOException e) {
            log.error("--->>tableName:{},根据过滤条件获取多条记录失败!", tableName, e);
        }
        log.debug("--->>查询tableName:{},结果：{}", tableName, results);
        return results;
    }

    @Override
    public Map<String, Map<String, String>> getAllListByScan(String startRow, String endRow, String accurateRowKey, String tableName, String familyName, String nameSpace) {
        log.debug("--->>查询条件：columnFamily:{},nameSpace:{}", familyName, nameSpace);
        Map<String, Map<String, String>> resultMap = new HashMap<>(4);
        ResultScanner results = null;
        //构建查询条件
        try {
            Table table = getTable(nameSpace, tableName);
            Scan scan = new Scan();
            if (StrUtil.isNotBlank(accurateRowKey)){
                scan.setStartRow(accurateRowKey.getBytes());
                scan.setStopRow(accurateRowKey.getBytes());
            }else {
                scan.setStartRow(startRow.getBytes());
                scan.setStopRow(endRow.getBytes());
            }
            results = table.getScanner(scan);
            results.forEach(result -> {
                for (Cell cell : result.listCells()) {
                    String rowKey = Bytes.toString(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                    String key = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                    String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                    if (!resultMap.containsKey(rowKey)) {
                        resultMap.put(rowKey, new HashMap<>(4));
                    }
                    resultMap.get(rowKey).put(key, value);
                }
            });
        } catch (IOException e) {
            log.error("--->>tableName:{},根据过滤条件获取多条记录失败!", tableName, e);
        } finally {
            try {
                //关闭ResultScanner，否则RegionServer对应资源无法释放
                if(null != results){
                    results.close();
                }
            } catch (Exception e) {
                log.error("--->>关闭ResultScanner失败：", e);
            }

        }
        log.info("---->>范围rk查询，tableName:{},rowKeyStart:{},endRowKeys:{}", tableName, startRow, endRow);
        log.debug("--->>查询结果,tableName:{},值：{}", tableName, resultMap);
        return resultMap;
    }
}

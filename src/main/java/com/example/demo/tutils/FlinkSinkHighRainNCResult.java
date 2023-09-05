package com.example.demo.tutils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.alicloud.openservices.tablestore.model.*;
import com.esotericsoftware.minlog.Log;
import com.example.demo.constants.EsCreateIndexEnum;
import com.example.demo.entity.MetHighRain;
import com.example.demo.es.entity.EsQueryCondition;
import com.example.demo.es.service.IntfEsQueryService;
import com.example.demo.hbase.service.IHBaseService;
import com.example.demo.service.IMetHighRainService;
import com.example.demo.utils.ApplicationContextUtil;
import com.example.demo.utils.PgSinkMapperUtil;
import com.example.demo.vo.EsMetHighRain;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @Auther：jinguangshuai
 * @Data：2023/7/25 - 07 - 25 - 15:30
 * @Description:com.example.demo.utils
 * @version:1.0
 */
@Slf4j
public class FlinkSinkHighRainNCResult<T> extends PgSinkMapperUtil<T> {


    static ThreadPoolExecutor highRainNCResultThreadPoolExecutor = null;

    static {
        String namePrefix = "flinkSinkHighRainNCResult-";
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                // -%d不要少
                .setNameFormat(namePrefix + "%d")
                .setDaemon(true)
                .build();
        highRainNCResultThreadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                5, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                threadFactory, (r, executor) -> {
            //打印日志,添加监控等
            log.error("flink task is rejected!");
        });
    }

    /**
     * Context接口中返回关于时间的信息
     * Returns the current processing time. long currentProcessingTime();
     * Returns the current event-time watermark. long currentWatermark();
     * Returns the timestamp of the current input record or {@code null} if the element does not have an assigned timestamp. Long timestamp();
     * invoke方法是sink数据处理逻辑的方法，source端传来的数据都在invoke方法中进行处理
     * 其中invoke方法中第一个参数类型与RichSinkFunction<>中的泛型对应。
     */
    @Override
//    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.SERIALIZABLE)
    public void invoke(Object object, Context context) {
        try {
            //线程池执行，提升系统性能
            highRainNCResultThreadPoolExecutor.execute(() -> {
                List<MetHighRain> list = super.castList(object, MetHighRain.class);
                ApplicationContext ac = ApplicationContextUtil.getApplicationContext();
                IMetHighRainService iMetHighRainService = (IMetHighRainService) ac.getBean("IMetHighRainServiceImpl");
                if (CollUtil.isNotEmpty(list)) {
                    //插入数据库
                    iMetHighRainService.saveOrUpdateBatch(list);
                    //插入OTS
                    highRainNcResultSaveToOts(list);
                    //插入ES
                    saveHighRainNcResultIndexToEs(list);
                }
            });
        } catch (Exception e) {
//            e.printStackTrace();
            Log.info("多线程执行异常！");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    /**
     * *批量存入OTS
     * @param list
     */
    public void highRainNcResultSaveToOts(List<MetHighRain> list) {
        BatchWriteRowRequest batchWriteRowRequest = new BatchWriteRowRequest();
        list.forEach(t -> {
            try {
                //构造主键
                String rowKey = generateRowKey(t.getId(), t.getStartTime());
                PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
                primaryKeyBuilder.addPrimaryKeyColumn("RK", PrimaryKeyValue.fromString(rowKey));
                RowPutChange rowPutChange = new RowPutChange("met_high_rain", primaryKeyBuilder.build());
                //设置条件更新
                //RowExistenceExpectation.IGNORE表示无论此行是否存在均会插入新数据，如果之前行已存在，则写入数据时会覆盖原有数据。
                //RowExistenceExpectation.EXPECT_EXIST表示只有此行存在时才会插入新数据，写入数据时会覆盖原有数据。
                //RowExistenceExpectation.EXPECT_NOT_EXIST表示只有此行不存在时才会插入数据。
                Condition condition = new Condition(RowExistenceExpectation.IGNORE);
                rowPutChange.setCondition(condition);

                rowPutChange.addColumn("id", ColumnValue.fromString(Optional.of(t.getId()).orElse("")));
                rowPutChange.addColumn("startTime", ColumnValue.fromString(
                        null != t.getStartTime() ? DateUtil.format(t.getStartTime(), "yyyy-MM-dd HH:mm:ss") : ""));
                rowPutChange.addColumn("endTime", ColumnValue.fromString(
                        null != t.getEndTime() ? DateUtil.format(t.getEndTime(), "yyyy-MM-dd HH:mm:ss") : ""));
                rowPutChange.addColumn("value", ColumnValue.fromString(Optional.of(t.getValue()).orElse("")));
                rowPutChange.addColumn("geom", ColumnValue.fromString(Optional.of(t.getGeom()).orElse("")));
                rowPutChange.addColumn("province", ColumnValue.fromString(Optional.of(t.getProvince()).orElse("")));
                rowPutChange.addColumn("provinceName", ColumnValue.fromString(Optional.of(t.getProvinceName()).orElse("")));
                rowPutChange.addColumn("version", ColumnValue.fromString(Optional.of(t.getVersion()).orElse("")));
                rowPutChange.addColumn("createTime", ColumnValue.fromString(
                        null != t.getCreateTime() ? DateUtil.format(t.getCreateTime(), "yyyy-MM-dd HH:mm:ss") : ""));
                batchWriteRowRequest.addRowChange(rowPutChange);
            } catch (Exception e) {
                log.error("暴雨原始解析NC文件录入OTS异常", e);
            }
        });
        //批量插入OTS
        ApplicationContext ac = ApplicationContextUtil.getApplicationContext();
        IHBaseService iMetHighRainService = (IHBaseService) ac.getBean("HBaseServiceImpl");
        iMetHighRainService.batchInsert(batchWriteRowRequest);
    }
    public String generateRowKey(String id, Date time) {
        return DigestUtils.md5DigestAsHex(id.getBytes()) + DateUtil.format(time, "yyyyMMddHHmmss");
    }


    /**
     * *rowKey批量存入ES
     */
    public void saveHighRainNcResultIndexToEs(List<MetHighRain> list){
        List<JSONObject> result = Lists.newArrayList();
        try {
            list.forEach(t->{
                JSONObject jsonObject = new JSONObject();
                //hbase行键
                jsonObject.put("rowKey",generateRowKey(t.getId(), t.getStartTime()));
                jsonObject.put("id",t.getId());
                jsonObject.put("startTime",DateUtil.format(t.getStartTime(),"yyyy-MM-dd HH:mm:ss"));
                jsonObject.put("endTime",DateUtil.format(t.getEndTime(),"yyyy-MM-dd HH:mm:ss"));
                jsonObject.put("value",t.getValue());
                jsonObject.put("geom",t.getGeom());
                jsonObject.put("province",t.getProvince());
                jsonObject.put("provinceName",t.getProvinceName());
                jsonObject.put("version",t.getVersion());
                jsonObject.put("createTime",DateUtil.format(t.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
                List<JSONObject> jsonObjects = Collections.singletonList(jsonObject);
                result.addAll(jsonObjects);
            });

            //插入ES

            //save or update
            ApplicationContext ac = ApplicationContextUtil.getApplicationContext();
            IntfEsQueryService intfEsQueryService = (IntfEsQueryService) ac.getBean("esQueryServiceImpl");
            if(CollUtil.isNotEmpty(result)){
                List<String> ids = result.stream().map(m -> m.getString("id")).collect(Collectors.toList());
                boolean existIndex = intfEsQueryService.existIndex("met_high_rain");
                if(existIndex){
                    //查询现有数据
                    List<EsQueryCondition> andConditionList = new ArrayList<>();
                    EsQueryCondition esQueryCondition = new EsQueryCondition();
                    esQueryCondition.setFieldName("id");
                    esQueryCondition.setFieldValue(ids);
                    andConditionList.add(esQueryCondition);

                    Map<String, Object> andMap = new HashMap<>();
                    if (CollUtil.isNotEmpty(ids)) {
                        andMap.put("id", ids);
                    }
//                    List<EsMetHighRain> existList = intfEsQueryService.conditionSearchAllData("met_high_rain",andConditionList,null,null,null,null,EsMetHighRain.class);
//                    Page<EsMetHighRain> existList = intfEsQueryService.mapConditionSearch("met_high_rain",null,null,andMap,null,null,null,null,null,null,EsMetHighRain.class);
                    List<EsMetHighRain> existList = intfEsQueryService.mapNoPageConditionSearch("met_high_rain",andMap,null,null,null,null,null,null,EsMetHighRain.class);
                    if(CollUtil.isNotEmpty(existList)){
                        List<String> existListIds = existList.stream().map(m -> m.getId()).collect(Collectors.toList());
                        List<JSONObject> existJsonObject = new ArrayList<>();
                        List<JSONObject> addJsonObject = new ArrayList<>();
                        result.forEach(t->{
                            if(existListIds.contains(t.getString("id"))){
                                existJsonObject.add(t);
                            }else{
                                addJsonObject.add(t);
                            }
                        });
                        if(CollUtil.isNotEmpty(existJsonObject)){
                            intfEsQueryService.batchUpate("met_high_rain",existJsonObject,"id");
                        }
                        if(CollUtil.isNotEmpty(addJsonObject)){
                            intfEsQueryService.batchSaveAndPk("met_high_rain",addJsonObject,"id");
                        }
                    }else{
                        intfEsQueryService.batchSaveAndPk("met_high_rain",result,"id");
                    }
                }else{
                    //创建索引
                    synchronized (new Object()){
                        intfEsQueryService.createIndex("met_high_rain", EsCreateIndexEnum.MET_HIGH_RAIN_INDEX.getDsl());
                    }
                    //插入索引
                    intfEsQueryService.batchSaveAndPk("met_high_rain",result,"id");
                }
            }

        }catch (Exception e){
            log.error("暴雨原始解析NC文件录入ES异常", e);
        }
    }


}


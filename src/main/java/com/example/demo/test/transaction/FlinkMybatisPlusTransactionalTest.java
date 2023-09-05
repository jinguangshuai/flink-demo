package com.example.demo.test.transaction;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.LightningHistoryInfo;
import com.example.demo.utils.transactionUtil.PgMybatisPlusTransactionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.*;

/**
 * @Auther：jinguangshuai
 * @Data：2023/7/26 - 07 - 26 - 15:06
 * @Description:com.example.demo.test
 * @version:1.0
 */
@Configuration
@EnableScheduling
@EnableAsync
@Slf4j
public class FlinkMybatisPlusTransactionalTest {

    static String  kafkaInputBootStrapServers = "192.168.2.128:9092";
    static String kafkaInputGroupId="consumer-test";
    static String kafkaInputTopic="test";
    static String type="com.alibaba.druid.pool.DruidDataSource";
    static String PGUrl="jdbc:postgresql://192.168.0.100:5432/meteorology-zb?currentSchema=public&stringtype=unspecified&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8";
    static String PGUserName="postgres";
    static  String PGPassWord="123456";

//    @PostConstruct
//    @Scheduled(cron = "${env.dancing.cron:* 0/1 * * * ?}")
    public static void test(){
        // 1. 创建流式执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setParallelism(1);
        env.setRestartStrategy(RestartStrategies.noRestart());  // 根据需要设置
        // 2. 从kafka中读取数据
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaInputBootStrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,kafkaInputGroupId);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,true);   // 设置自动提交offset
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,100);   //提交时间间隔
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");   //key的反序列化
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");   //value反序列化
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        DataStreamSource<String> KafkaStream = env.addSource(new FlinkKafkaConsumer<String>(kafkaInputTopic,new SimpleStringSchema(),properties));

        KafkaStream.print();

        // 4.转换为Event对象（具体转换逻辑）
        SingleOutputStreamOperator<Object> mapStream = KafkaStream.map(new MapFunction<String, Object>() {
            @Override
            public Object map(String s){
                List<LightningHistoryInfo> lightningHistoryInfos = new ArrayList<>();
                try {
                    //具体校验解析流程
                    JSONObject jsonObject = JSON.parseObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    //校验JSON
                    if (null != jsonArray && jsonArray.size() > 0) {
                        jsonArray.stream().forEach(j->{
                            JSONObject item = (JSONObject)j;
                            log.info("历史雷电信息数据：{}", item);
                            LightningHistoryInfo dto = new LightningHistoryInfo();
                            dto.setId(UUID.randomUUID().toString());
                            dto.setLongitude(NumberUtil.parseDouble(Optional.ofNullable(item.getString("longitude")).orElse(null)));
                            dto.setLatitude(NumberUtil.parseDouble(Optional.ofNullable(item.getString("latitude")).orElse(null)));
                            dto.setTimeDate(DateUtil.parse(Optional.ofNullable(item.getString("timeDate")).orElse(""), "yyyy-MM-dd HH:mm:ss"));
                            dto.setPeakCurrent(NumberUtil.parseDouble(Optional.ofNullable(item.getString("peakCurrent")).orElse(null)));
                            dto.setMultiplicity(NumberUtil.parseInt(Optional.ofNullable(item.getString("multiplicity")).orElse(null)));
                            dto.setXsecond(NumberUtil.parseInt(Optional.ofNullable(item.getString("XSecond")).orElse(null)));
                            dto.setCreateTime(new Date());
                            lightningHistoryInfos.add(dto);
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    log.error("模型转换异常！");
                }
                if(CollUtil.isNotEmpty(lightningHistoryInfos)){
                    return lightningHistoryInfos;
                }else {
                    return Collections.emptyList();
                }
            }
        });
        mapStream.print();

        // INSERT INTO user_info(age,name,sex) VALUES (?,?,?);
        // update user_info set age = ? where age = ?;
        // delete from user_info where age = ?;
        // select * from user_info where age = ?

        DataStreamSink dataStreamSink = mapStream.addSink(new PgMybatisPlusTransactionUtil());
        dataStreamSink.getClass();

        // 6.执行程序
        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("执行失败！");
        }
    }
}

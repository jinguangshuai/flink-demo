package com.example.demo.test;

import com.example.demo.entity.UserInfo;
import com.example.demo.utils.PgDataSourceUtil;
import com.example.demo.utils.PgInsertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

//import lombok.extern.slf4j.Slf4j;

/**
 * @Auther：jinguangshuai
 * @Data：2023/6/30 - 06 - 30 - 11:02
 * @Description:com.example.demo.controller
 * @version:1.0
 */
@Configuration
@EnableScheduling
@EnableAsync
@Slf4j
public class FlinkDataSourceUtilTest {


    public String  kafkaInputBootStrapServers = "192.168.2.128:9092";
    public String kafkaInputGroupId="consumer-test";
    public String kafkaInputTopic="test";
    public String type="com.alibaba.druid.pool.DruidDataSource";
    public String PGUrl="jdbc:postgresql://192.168.0.100:5432/meteorology-zb?currentSchema=public&stringtype=unspecified&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8";
    public String PGUserName="postgres";
    public  String PGPassWord="123456";

//    @PostConstruct
//    @Scheduled(cron = "${env.dancing.cron:* 0/1 * * * ?}")
    public void test(){

        // 1. 创建流式执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setParallelism(1);
        env.setRestartStrategy(RestartStrategies.noRestart());  // 根据需要设置
        // 2. 自定义数据源
        DataStreamSource<UserInfo> dataSource = env.addSource(new PgDataSourceUtil()).setParallelism(1);
        dataSource.print();

        // 3.转换为Event对象（具体转换逻辑）
        //transfrom
        SingleOutputStreamOperator<Object> mapStream = dataSource.map(new MapFunction<UserInfo, Object>() {
            @Override
            public Object map(UserInfo s) throws Exception {
                List<String> list =new ArrayList<>();
                list.add(s.getAge().isEmpty()? "":s.getAge());
                list.add(s.getName().isEmpty()? "":s.getName());
                list.add(s.getSex().isEmpty()? "":s.getSex());
                String[] s2 = new String[list.size()];
                list.toArray(s2);
                return s2;
            }
        });
        mapStream.print();

        // INSERT INTO user_info(age,name,sex) VALUES (?,?,?);
        // update user_info set age = ? where age = ?;
        // delete from user_info where age = ?;
        // select * from user_info where age = ?

        //4.输出sink
        DataStreamSink dataStreamSink = mapStream.addSink(new PgInsertUtil("INSERT INTO user_info(age,name,sex) VALUES (?,?,?)"));

        // 5.执行程序
        new Thread(()->{
            try {
                env.execute();
            } catch (Exception e) {
                e.printStackTrace();
                log.info("执行失败！");
            }
        }).start();

    }

}

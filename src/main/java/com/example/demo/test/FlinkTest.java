package com.example.demo.test;

import com.example.demo.entity.UserInfo;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

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
//@Slf4j
public class FlinkTest {


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
        // 2. 从配置文件中加载配置参数
//        ParameterTool parameters = ParameterTool.fromArgs(args);
//        String propertiesPath = parameters.get("path",System.getProperty("user.dir")+"/environment-wildfire/src/main/resources/application.properties");
//        ParameterTool parameterTool = ParameterTool.fromPropertiesFile(propertiesPath);
//        env.getConfig().setGlobalJobParameters(parameterTool);
//        String kafkaInputBootStrapServers = parameterTool.set("kafkaInputBootStrapServers");
//        String kafkaInputGroupId = parameterTool.get("kafkaInputGroupId");
//        String kafkaInputTopic = parameterTool.get("kafkaInputTopic");
//        String PGUrl = parameterTool.get("PGUrl");
//        String PGUserName = parameterTool.get("PGUserName");
//        String PGPassWord = parameterTool.get("PGPassWord");
        // 3. 从kafka中读取数据
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

        // 4.转换为Event对象
        SingleOutputStreamOperator<UserInfo> mapStream = KafkaStream.map(new MapFunction<String, UserInfo>() {
            @Override
            public UserInfo map(String s) throws Exception {
                String[] fields = s.split(",");
                return new UserInfo(fields[0].trim(), fields[1].trim(), fields[2].trim(),fields[3].trim());
            }
        });
        mapStream.print();
        // 5. 将数据写入postgres
        mapStream.addSink(JdbcSink.sink(
                "INSERT INTO user_info(age,name,sex) VALUES (?,?,?)",
                // 标准写法：创建类实现JdbcStatementBuilder接口
                // new MyJdbcStatementBuilder(),
                // 简便写法：使用lambada表达式
                ((preparedStatement,event) -> {
                    preparedStatement.setString(1,event.age);
                    preparedStatement.setString(2,event.name);
                    preparedStatement.setString(3,event.sex);
                }),
                JdbcExecutionOptions.builder()
                        .withBatchSize(5)
                        .withBatchIntervalMs(2)
                        .withMaxRetries(3)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl(PGUrl)
                        .withDriverName("org.postgresql.Driver")
                        .withUsername(PGUserName)
                        .withPassword(PGPassWord)
                        .build()
        ));
        // 6.执行程序
        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
//            log.info("执行失败！");
        }

    }

}

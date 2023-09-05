package com.example.demo.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.config.CharsetCustomNewSerializer;
import com.example.demo.entity.MetHighRain;
import com.example.demo.tutils.FlinkSinkHighRainNCResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Auther：jinguangshuai
 * @Data：2023/7/26 - 07 - 26 - 10:21
 * @Description:com.example.demo.task
 * @version:1.0
 */
@Configuration
@EnableScheduling
@EnableAsync
@Slf4j
public class FlinkHighRainNCResult {


    static String kafkaInputBootStrapServers = "192.168.2.128:9092";
    static String kafkaInputGroupId = "GID_meteorologyHighRainNcResult";
    static String kafkaInputTopic = "meteorologyHighRainNcResultTopic";

    @PostConstruct
    public static void test() {
        // 1. 创建流式执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setParallelism(1);
//        env.setRestartStrategy(RestartStrategies.noRestart());  // 根据需要设置
        //解决org.apache.flink.runtime.JobException: Recovery is suppressed by NoRestartBackoffTimeStrategy
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                1, // 尝试重启的次数
                Time.of(10, TimeUnit.SECONDS) // 间隔
        ));
        //解决kryo序列化首次无法被java类加载器加载问题
        env.getConfig().registerTypeWithKryoSerializer(Charset.forName("UTF-8").getClass(), CharsetCustomNewSerializer.class);

        // 2. 手动连接从kafka中读取数据
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaInputBootStrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaInputGroupId);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);   // 设置自动提交offset
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 100);   //提交时间间隔
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");   //key的反序列化
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");   //value反序列化
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        DataStreamSource<String> KafkaStream = env.addSource(new FlinkKafkaConsumer<String>(kafkaInputTopic, new SimpleStringSchema(), properties));
        KafkaStream.print();
        // 3.转换为Event对象（具体转换逻辑）
        SingleOutputStreamOperator<Object> mapStream = KafkaStream.map(new MapFunction<String, Object>() {
            @Override
            public Object map(String s) {
                List<MetHighRain> metHighRainList = new ArrayList<>();
                try {
                    //具体校验解析流程
                    JSONObject jsonObject = JSON.parseObject(s);
                    log.info("暴雨原始解析数据：{}", jsonObject);
                    MetHighRain metHighRain = new MetHighRain();
                    metHighRain.setId(Optional.ofNullable(jsonObject.getString("id")).orElse(""));
                    metHighRain.setStartTime(null != jsonObject.getString("startTime") ?
                            DateUtil.parse(timestampToDate(Long.parseLong(jsonObject.getString("startTime")), "yyyy-MM-dd HH:mm:ss")) : null);
                    metHighRain.setEndTime(null != jsonObject.getString("endTime") ?
                            DateUtil.parse(timestampToDate(Long.parseLong(jsonObject.getString("endTime")), "yyyy-MM-dd HH:mm:ss")) : null);
                    metHighRain.setValue(Optional.ofNullable(jsonObject.getString("value")).orElse(null));
                    metHighRain.setGeom(Optional.ofNullable(jsonObject.getString("geom")).orElse(null));
                    metHighRain.setProvince(Optional.ofNullable(jsonObject.getString("province")).orElse(null));
                    metHighRain.setProvinceName(Optional.ofNullable(jsonObject.getString("provinceName")).orElse(null));
                    metHighRain.setVersion(Optional.ofNullable(jsonObject.getString("version")).orElse(null));
                    metHighRain.setCreateTime(null != jsonObject.getString("createTime") ?
                            DateUtil.parse(timestampToDate(Long.parseLong(jsonObject.getString("createTime")), "yyyy-MM-dd HH:mm:ss")) : null);
                    metHighRainList.add(metHighRain);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("暴雨原始解析数据-模型转换异常！");
                }
                if (CollUtil.isNotEmpty(metHighRainList)) {
                    return metHighRainList;
                } else {
                    return Collections.emptyList();
                }
            }
        });
        mapStream.print();

        DataStreamSink dataStreamSink = mapStream.addSink(new FlinkSinkHighRainNCResult());
        dataStreamSink.getClass();

        // 6.执行程序
        new Thread(() -> {
            try {
                env.execute();
            } catch (Exception e) {
                e.printStackTrace();
                log.info("执行失败！");
            }
        }).start();
    }


    //时间戳转换
    public static String timestampToDate(long timestamp, String format) {
        Date date = new Date(timestamp);
        SimpleDateFormat sd = new SimpleDateFormat(format);
        return sd.format(date);
    }

//    public static void main(String[] args) {
//        String s = timestampToDate(Long.parseLong("1678363200000"), "yyyy-MM-dd HH:mm:ss");
//        DateTime parse = DateUtil.parse(s, "yyyy-MM-dd HH:mm:ss");
//        System.out.println(parse);
//        System.out.println(s);
//    }
}

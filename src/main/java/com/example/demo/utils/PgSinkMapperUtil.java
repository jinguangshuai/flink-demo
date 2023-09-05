package com.example.demo.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Auther：jgs
 * @Data：2023/7/13 - 07 - 13 - 9:34
 * @Description:com.example.demo.utils
 * @version:1.0
 */
@Slf4j
public class PgSinkMapperUtil<T> extends RichSinkFunction<T> {

    public Connection connect = null;

    public static int THREADS = Runtime.getRuntime().availableProcessors() + 1;
    @Value("${thread-pool.Flink.core-pool-size:4}")
    public static int corePoolSize = THREADS;
    @Value("${threadPool.Flink.max-pool-size:12}")
    public static int maxPoolSize = 2 * THREADS;
    @Value("${threadPool.Flink.queue-capacity:1024}")
    public static int queueCapacity = 1024;
    static ThreadPoolExecutor threadPoolExecutor = null;

    static {
        String namePrefix = "flinkLoadExecutor-";
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                // -%d不要少
                .setNameFormat(namePrefix + "%d")
                .setDaemon(true)
                .build();
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
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
    public void invoke(Object object, Context context) {

    }

    public <T> List<T> castList(Object obj, Class<T> clazz){
        List<T> result = new ArrayList<>();
        if(obj instanceof List<?>){
            for (Object o : (List<?>) obj){
                result.add(clazz.cast(o));
            }
            return result;
        }
        return new ArrayList<>();
    }


    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://192.168.0.100:5432/meteorology-zb?currentSchema=public&stringtype=unspecified&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8");
        dataSource.setUsername("postgres");
        dataSource.setPassword("123456");
        //数据库连接池初始值
        dataSource.setInitialSize(5);
        //数据库连接池最小值
        dataSource.setMinIdle(10);
        //数据库连接池最大值
        dataSource.setMaxActive(20);
        //构建druid数据库连接池，避免数据库重复创建与销毁的性能问题
        connect = dataSource.getConnection(dataSource.getUsername(), dataSource.getPassword());
    }


    @Override
    public void close() throws Exception {
        super.close();
        //关闭数据库连接
        if (connect != null) {
            try {
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //关闭当前已经执行完毕的线程
//        threadPoolExecutor.shutdown();
    }

}

package com.example.demo.config;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author
 * @date 2020/8/11
 */
@Slf4j
@Configuration
public class EnvThreadPool {
    /**
     * 获得Java虚拟机可用的处理器个数 + 1
     */
    private static final int THREADS = Runtime.getRuntime().availableProcessors() + 1;

    @Value("${thread-pool.Flink.core-pool-size:4}")
    private int corePoolSize = THREADS;
    @Value("${threadPool.Flink.max-pool-size:12}")
    private int maxPoolSize = 2 * THREADS;
    @Value("${threadPool.Flink.queue-capacity:1024}")
    private int queueCapacity = 1024;
    private String namePrefix = "flinkLoadExecutor-";

    final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            // -%d不要少
            .setNameFormat(namePrefix + "%d")
            .setDaemon(true)
            .build();

    /**
     * @return
     */
    @Bean("EnvAsyncExecutor")
    public ThreadPoolExecutor asyncExecutor() {
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                5, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                threadFactory, (r, executor) -> {
            //打印日志,添加监控等
            log.error("flink task is rejected!");
        });
    }
}

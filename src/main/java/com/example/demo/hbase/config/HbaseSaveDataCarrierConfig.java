package com.example.demo.hbase.config;

import com.a.eye.datacarrier.DataCarrier;
import com.a.eye.datacarrier.buffer.BufferStrategy;
import com.example.demo.hbase.consumer.HbaseSaveConsumer;
import com.example.demo.hbase.dto.HbaseSaveDataCarrierDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * dataCarrier配置类
 *
 * @author : wenziang
 * @since : 2022/6/7
 */
@Configuration
public class HbaseSaveDataCarrierConfig {
    @Value("${dataCarrier.consumerNum:2}")
    private Integer consumerNum;
    @Value("${dataCarrier.producerNum:1}")
    private Integer producerNum;

    @Bean("saveHbaseDataCarrier")
    public DataCarrier<HbaseSaveDataCarrierDto> getSaveHbaseDataCarrier() {
        DataCarrier<HbaseSaveDataCarrierDto> dataCarrier = new DataCarrier<>(producerNum, 5000);
        dataCarrier.consume(HbaseSaveConsumer.class, consumerNum);
        dataCarrier.setBufferStrategy(BufferStrategy.BLOCKING);
        return dataCarrier;
    }
}

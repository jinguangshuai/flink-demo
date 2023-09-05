package com.example.demo.hbase.consumer;

import com.a.eye.datacarrier.consumer.IConsumer;
import com.example.demo.hbase.dto.HbaseSaveDataCarrierDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.util.List;

/**
 * @author : wenziang
 * @since : 2022/6/7
 */
@Slf4j
public class HbaseSaveConsumer implements IConsumer<HbaseSaveDataCarrierDto> {

    @Override
    public void init() {

    }

    @Override
    public void consume(List<HbaseSaveDataCarrierDto> data) {
        for (HbaseSaveDataCarrierDto dto : data) {
            try {
                //异步插入数据
                Table table = dto.getTable();
                table.put(dto.getPut());
                log.debug("rowKey:{},tableName:{}", dto.getPut().getFamilyCellMap(), table.getName().getNameAsString());
            } catch (IOException e) {
                log.error("插入数据失败！错误是", e);
            }
        }
    }

    @Override
    public void onError(List<HbaseSaveDataCarrierDto> data, Throwable t) {

    }

    @Override
    public void onExit() {

    }
}

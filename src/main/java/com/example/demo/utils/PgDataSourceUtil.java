package com.example.demo.utils;

import com.example.demo.entity.UserInfo;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Auther：jinguangshuai
 * @Data：2023/7/18 - 07 - 18 - 17:07
 * @Description:com.example.demo.utils
 * @version:1.0
 */
public class PgDataSourceUtil extends RichParallelSourceFunction<UserInfo> {

    Boolean running = true;
    int j = 10;
    Lock lock = new ReentrantLock();

    @Override
    public void run(SourceContext<UserInfo> sourceContext) throws Exception {
        lock.lock();
        try {
            Random rand = new Random();
            while (running && j > 0) {
                Long curTime = System.currentTimeMillis();
                for (int i = 0; i < 10; i++) {
                    Double curTemp = rand.nextGaussian();
                    UserInfo userInfo = new UserInfo(String.valueOf(i),"sensor_" + i, curTime.toString(), curTemp.toString());
                    sourceContext.collect(userInfo);
                    Thread.sleep(100);
                    j--;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void cancel() {
        running = false;
    }

}

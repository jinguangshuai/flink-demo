package com.example.demo.utils;

import cn.hutool.core.collection.CollUtil;
import com.esotericsoftware.minlog.Log;
import com.example.demo.entity.UserInfo;
import com.example.demo.service.IUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.springframework.context.ApplicationContext;

import java.util.List;


/**
 * @Auther：jinguangshuai
 * @Data：2023/7/25 - 07 - 25 - 15:30
 * @Description:com.example.demo.utils
 * @version:1.0
 */
@Slf4j
public class PgSinkMapperUtilExtend<T> extends PgSinkMapperUtil<T>{


    /**
     * Context接口中返回关于时间的信息
     * Returns the current processing time. long currentProcessingTime();
     * Returns the current event-time watermark. long currentWatermark();
     * Returns the timestamp of the current input record or {@code null} if the element does not have an assigned timestamp. Long timestamp();
     * invoke方法是sink数据处理逻辑的方法，source端传来的数据都在invoke方法中进行处理
     * 其中invoke方法中第一个参数类型与RichSinkFunction<>中的泛型对应。
     */
    @Override
    public void invoke(Object object, SinkFunction.Context context) {
        try {
            //线程池执行，提升系统性能
            super.threadPoolExecutor.execute(() -> {
                //object转为list
                List<UserInfo> list = super.castList(object,UserInfo.class);
                if (CollUtil.isNotEmpty(list)) {
                    ApplicationContext ac = ApplicationContextUtil.getApplicationContext();
                    IUserInfo userService = (IUserInfo) ac.getBean("IUserInfoServiceImpl");
                    userService.saveOrUpdateBatch(list);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.info("多线程执行异常！");
        }
    }



}

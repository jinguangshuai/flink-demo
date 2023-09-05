package com.example.demo.utils.transactionUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.esotericsoftware.minlog.Log;
import com.example.demo.entity.LightningHistoryInfo;
import com.example.demo.service.ILightningHistoryInfo;
import com.example.demo.utils.ApplicationContextUtil;
import com.example.demo.utils.PgSinkMapperUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
public class PgMybatisPlusTransactionUtil<T> extends PgSinkMapperUtil<T> {

    static ThreadPoolExecutor mybatisPlusTransactionThreadPoolExecutor = null;
    static {
        String namePrefix = "mybatisPlusTransactionThreadPoolExecutor-";
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                // -%d不要少
                .setNameFormat(namePrefix + "%d")
                .setDaemon(true)
                .build();
        mybatisPlusTransactionThreadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
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
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.SERIALIZABLE)
    public void invoke(Object object, Context context) {
        try {
            //线程池执行，提升系统性能
//            mybatisPlusTransactionThreadPoolExecutor.execute(() -> {
            //object转为list
            List<LightningHistoryInfo> list = super.castList(object,LightningHistoryInfo.class);
            ApplicationContext ac = ApplicationContextUtil.getApplicationContext();
            ILightningHistoryInfo iLightningHistoryInfo = (ILightningHistoryInfo) ac.getBean("ILightningHistoryInfoServiceImpl");
            if (CollUtil.isNotEmpty(list)) {
                iLightningHistoryInfo.saveOrUpdateBatch(list);
            }

            System.out.println(1/0);

            LightningHistoryInfo dto = new LightningHistoryInfo();
            dto.setId("f7f4e28c-9206-46e7-a98b-a323c9e5f973");
            dto.setLongitude(1.55);
            dto.setLatitude(34.727153);
            dto.setTimeDate(DateUtil.parse("2023-08-15 21:07:18", "yyyy-MM-dd HH:mm:ss"));
            dto.setPeakCurrent(-15.300000);
            dto.setMultiplicity(2);
            dto.setXsecond(1345777);
            dto.setCreateTime(new Date());
            iLightningHistoryInfo.saveOrUpdate(dto);

//            });
        } catch (Exception e) {
            Log.info("多线程执行异常！");
            //不抛运行时异常事务无法执行
            throw new RuntimeException();
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

}

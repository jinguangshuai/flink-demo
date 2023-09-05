package com.example.demo.hbase.service.impl;


import cn.hutool.core.convert.Convert;
import com.example.demo.hbase.properties.HBaseProperties;
import com.example.demo.hbase.util.HbaseLoginUtil;
import com.example.demo.hbase.util.KafkaSecurityPrepare;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.security.User;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author hehaoqi
 * @date 2020/9/8
 */
@Slf4j
@Component
public class MrsLoginService implements BeanPostProcessor {

    private static final String ZOOKEEPER_DEFAULT_LOGIN_CONTEXT_NAME = "Client";
    private static final String ZOOKEEPER_DEFAULT_SERVER_PRINCIPAL = "zookeeper/hadoop";

    @Autowired
    private HBaseProperties hBaseProperties;

    @Value("${mrs-kafka.enabled:false}")
    private String mrskafkaEnable;

    /**
     * 类加载时，监测到消息组件的初始化，要先完成登录mrs操作
     */
    @Override
    @Nullable
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.contains("eventPublisher")) {
            this.mrsKafkaLogin();
        }
        return bean;
    }


    /**
     * MRS - hbase 用户登录
     *
     * @throws IOException
     */
    public void mrsHbaseLogin(org.apache.hadoop.conf.Configuration configuration) {
        if (User.isHBaseSecurityEnabled(configuration)) {
            HBaseProperties.MrsProperties mrsProperties = hBaseProperties.getMrs();
            log.info("============》》开始{}-HBASE安全认证!", hBaseProperties.getType());
            try {
                HbaseLoginUtil.setJaasConf(ZOOKEEPER_DEFAULT_LOGIN_CONTEXT_NAME, mrsProperties.getUserName(), mrsProperties.getUserKeytabFilePath());
                HbaseLoginUtil.setZookeeperServerPrincipal(ZOOKEEPER_DEFAULT_SERVER_PRINCIPAL);
                HbaseLoginUtil.login(mrsProperties.getUserName(), mrsProperties.getUserKeytabFilePath(), mrsProperties.getKrb5FilePath(), configuration);
            } catch (Exception e) {
                log.error("--->>MRS-HBASE安全认证失败!", e);
            }
        } else {
            log.info("HBase非安全模式登陆，hbase.security.authentication：{}", configuration.get("hbase.security.authentication"));
        }
    }

    /**
     * mrs - kafka 用户登录
     *
     * @throws
     */
    private void mrsKafkaLogin() {
        if (Convert.toBool(mrskafkaEnable)) {
            try {
                HBaseProperties.MrsProperties mrsProperties = hBaseProperties.getMrs();
                log.info("=============》》开始MRS3-KAFKA安全认证！");
                KafkaSecurityPrepare.kerbrosLogin(mrsProperties.getUserName(), mrsProperties.getUserKeytabFilePath(), mrsProperties.getKrb5FilePath());
            } catch (Exception e) {
                log.error("--->>MRS-KAFKA安全认证失败！", e);
            }
        }
    }

}
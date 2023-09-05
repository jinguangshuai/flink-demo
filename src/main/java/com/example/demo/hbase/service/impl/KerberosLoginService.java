package com.example.demo.hbase.service.impl;

import com.example.demo.hbase.properties.HBaseProperties;
import com.example.demo.hbase.util.HbaseLoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class KerberosLoginService {
    private static final String ZOOKEEPER_DEFAULT_LOGIN_CONTEXT_NAME = "Client";
    private static final String ZOOKEEPER_DEFAULT_SERVER_PRINCIPAL = "zookeeper/hadoop";

    @Autowired
    private HBaseProperties hBaseProperties;

    /**
     * kerberos - hbase 用户登录
     *
     * @throws IOException
     */
    public void hbaseLogin(org.apache.hadoop.conf.Configuration configuration) {
        if (User.isHBaseSecurityEnabled(configuration)) {
            HBaseProperties.CdhProperties cdhProperties = hBaseProperties.getCdh();
            log.info("============》》开始kerberos-HBASE安全认证!");
            try {
                HbaseLoginUtil.setJaasConf(ZOOKEEPER_DEFAULT_LOGIN_CONTEXT_NAME, cdhProperties.getKeytabUser(), cdhProperties.getUserKeytabFilePath());
                HbaseLoginUtil.setZookeeperServerPrincipal(ZOOKEEPER_DEFAULT_SERVER_PRINCIPAL);
                HbaseLoginUtil.login(cdhProperties.getKeytabUser(), cdhProperties.getUserKeytabFilePath(),
                        cdhProperties.getKrb5ConfPath(), configuration);
            } catch (Exception e) {
                log.error("--->>kerberos-HBASE安全认证失败!", e);
            }
        } else {
            log.info("HBase非安全模式登陆，hbase.security.authentication：{}", configuration.get("hbase.security.authentication"));
        }
    }

}

package com.example.demo.hbase.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @author: hehaoqi
 * @date: 2022/1/14 15:11
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "hbase")
public class HBaseProperties {
    /**
     * HBASE 是否可用
     */
    private Boolean enabled;
    /**
     * HBASE 平台环境
     * 根据现场使用的hbase类型配置。共三种类型cdh、mrs2、ots、mrs3
     */
    private String type;
    /**
     *
     */
    private String nameSpace;
    /**
     * 列簇
     */
    private String columnFamily;
    /**
     * 批量保存数量
     */
    private int batchSaveSize = 2000;
    /**
     * 缓存队列大小
     */
    private int bufferQueueSize = 1000000;
    /**
     * Scan缓存大小
     */
    private int scanCacheSize = 10000;
    /**
     * CDH服务相关配置
     */
    private CdhProperties cdh;
    /**
     * OTS服务相关配置
     */
    private OtsProperties ots;
    /**
     * mrs服务相关配置
     */
    private MrsProperties mrs;

    @Data
    @NoArgsConstructor
    public static class MrsProperties {

        private String hbaseZookeeperQuorum;

        private String hbaseZookeeperPropertyClientPort;

        private String userName;

        private String hbaseSecurityAuthentication;

        private String masterKerberosPrincipal;

        private String regionserverKerberosPrincipal;

        private String coreSiteFilePath;

        private String hbaseSiteFilePath;

        private String hdfsSiteFilePath;

        private String hadoopRpcProtection;

        private String krb5FilePath;

        private String userKeytabFilePath;
    }

    @Data
    @NoArgsConstructor
    public static class OtsProperties {
        /**
         *
         */
        private String endpoint;
        /**
         *
         */
        private String instanceName;
        /**
         *
         */
        private String accessKeyId;
        /**
         *
         */
        private String accessKeySecret;
    }

    @Data
    @NoArgsConstructor
    public static class CdhProperties {
        /**
         *
         */
        private String zookeeperQuorum;
        /**
         *
         */
        private String rootDir;
        /**
         *
         */
        private String maxsize = "5000";
        /**
         *
         */
        private String username = "lczx";
        /**
         *
         */
        private String password = "lczx10HBase";
        /**
         *
         */
        private String port = "2181";
        private String krb5ConfPath;
        private String hbaseSecurityAuthentication;
        private String userKeytabFilePath;
        private String keytabUser;
        private String regionserverKerberosPrincipal;
        private String masterKerberosPrincipal;
        private boolean needKerberos = true;

        public boolean getNeedKerberos() {
            return needKerberos;
        }

        public void setNeedKerberos(boolean needKerberos) {
            this.needKerberos = needKerberos;
        }
    }
}

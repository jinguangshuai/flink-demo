package com.example.demo.hbase.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * @author 4600061779
 */
@Slf4j
public class KafkaSecurityPrepare {


    public static void securityPrepare(String userName, String keytab, String krbFile) throws IOException {
        //windows路径下分隔符替换
        keytab = keytab.replace("\\", "\\\\");
        krbFile = krbFile.replace("\\", "\\\\");

        HbaseLoginUtil.setJaasConf("KafkaClient", userName, keytab);
        KafkaLoginUtil.setKrb5Config(krbFile);
        KafkaLoginUtil.setZookeeperServerPrincipal("zookeeper/hadoop.hadoop.com");
        KafkaLoginUtil.setJaasFile(userName, keytab);
    }

    public static Boolean isSecurityModel() {
        Boolean isSecurity = false;
        String krbFilePath = System.getProperty("user.dir") + File.separator + "conf" + File.separator + "kafkaSecurityMode";

        Properties securityProps = new Properties();

        // file does not exist.
        if (!isFileExists(krbFilePath)) {
            return isSecurity;
        }

        try {
            securityProps.load(new FileInputStream(krbFilePath));
            if ("yes".equalsIgnoreCase(securityProps.getProperty("kafka.client.security.mode"))) {
                isSecurity = true;
            }
        } catch (Exception e) {
            log.info("The Exception occured : {}.", e);
        }

        return isSecurity;
    }

    /*
     * 判断文件是否存在
     */
    private static boolean isFileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public static void kerbrosLogin(String userName, String keytab, String krbFile) {
        try {
            log.info("Securitymode start.");
            //!!注意，安全认证时，需要用户手动修改为自己申请的机机账号
            securityPrepare(userName, keytab, krbFile);
        } catch (IOException e) {
            log.error("Security prepare failure.");
            log.error("The IOException occured : {}.", e);
            return;
        }
        log.info("Security prepare success.");
    }

}

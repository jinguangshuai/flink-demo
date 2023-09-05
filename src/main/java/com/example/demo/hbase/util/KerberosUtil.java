package com.example.demo.hbase.util;

import lombok.extern.slf4j.Slf4j;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author 4600061779
 */
@Slf4j
public class KerberosUtil {

    /**
     * Zookeeper quorum principal.
     */
    public static final String ZOOKEEPER_AUTH_PRINCIPAL = "zookeeper.server.principal";
    /**
     * java security krb5 file path
     */
    public static final String JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";
    /**
     * java security login file path
     */
    public static final String JAVA_SECURITY_LOGIN_CONF = "java.security.auth.login.config";
    /**
     * line operator string
     */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    /**
     * jaas file postfix
     */
    private static final String JAAS_POSTFIX = ".jaas.conf";
    /**
     * is IBM jdk or not
     */
    private static final boolean IS_IBM_JDK = System.getProperty("java.vendor").contains("IBM");
    /**
     * IBM jdk login module
     */
    private static final String IBM_LOGIN_MODULE = "com.ibm.security.auth.module.Krb5LoginModule required";
    /**
     * oracle jdk login module
     */
    private static final String SUN_LOGIN_MODULE = "com.sun.security.auth.module.Krb5LoginModule required";

    public static void securityPrepare(String userName, String keytab, String krbFile, String ZKPrincipal, String loginContextName) throws IOException {
        //windows路径下分隔符替换
        keytab = keytab.replace("\\", "\\\\");
        krbFile = krbFile.replace("\\", "\\\\");

        setJaasConf(loginContextName, userName, keytab);
        setKrb5Config(krbFile);
        setZookeeperServerPrincipal(ZKPrincipal);
        setJaasFile(userName, keytab);
    }


    /**
     * 设置krb5文件
     *
     * @param krb5ConfFile
     * @throws IOException
     */
    private static void setKrb5Config(String krb5ConfFile)
            throws IOException {
        System.setProperty(JAVA_SECURITY_KRB5_CONF, krb5ConfFile);
        String ret = System.getProperty(JAVA_SECURITY_KRB5_CONF);
        if (ret == null) {
            throw new IOException(JAVA_SECURITY_KRB5_CONF + " is null.");
        }
        if (!ret.equals(krb5ConfFile)) {
            throw new IOException(JAVA_SECURITY_KRB5_CONF + " is " + ret + " is not " + krb5ConfFile + ".");
        }
    }

    /**
     * 设置jaas.conf文件
     *
     * @param principal
     * @param keytabPath
     * @throws IOException
     */
    private static void setJaasFile(String principal, String keytabPath)
            throws IOException {
        String jaasPath =
                new File(System.getProperty("java.io.tmpdir")) + File.separator + System.getProperty("user.name")
                        + JAAS_POSTFIX;
        // windows路径下分隔符替换
        jaasPath = jaasPath.replace("\\", "\\\\");
        // 删除jaas文件
        deleteJaasFile(jaasPath);
        writeJaasFile(jaasPath, principal, keytabPath);
        System.setProperty(JAVA_SECURITY_LOGIN_CONF, jaasPath);
    }

    private static void setJaasConf(String loginContextName, String principal, String keytabFile)
            throws IOException {
        if ((loginContextName == null) || (loginContextName.length() <= 0)) {
            log.error("input loginContextName is invalid.");
            throw new IOException("input loginContextName is invalid.");
        }

        if ((principal == null) || (principal.length() <= 0)) {
            log.error("input principal is invalid.");
            throw new IOException("input principal is invalid.");
        }

        if ((keytabFile == null) || (keytabFile.length() <= 0)) {
            log.error("input keytabFile is invalid.");
            throw new IOException("input keytabFile is invalid.");
        }

        File userKeytabFile = new File(keytabFile);
        if (!userKeytabFile.exists()) {
            log.error("userKeytabFile(" + userKeytabFile.getAbsolutePath() + ") does not exsit.");
            throw new IOException("userKeytabFile(" + userKeytabFile.getAbsolutePath() + ") does not exsit.");
        }
        String keyTabFilePath = userKeytabFile.getAbsolutePath();
        javax.security.auth.login.Configuration.setConfiguration(new JaasConfiguration(loginContextName, principal,
                keyTabFilePath));

        javax.security.auth.login.Configuration conf = javax.security.auth.login.Configuration.getConfiguration();
        if (!(conf instanceof JaasConfiguration)) {
            log.error("javax.security.auth.login.Configuration is not JaasConfiguration.");
            throw new IOException("javax.security.auth.login.Configuration is not JaasConfiguration.");
        }

        AppConfigurationEntry[] entrys = conf.getAppConfigurationEntry(loginContextName);
        if (entrys == null) {
            log.error("javax.security.auth.login.Configuration has no AppConfigurationEntry named " + loginContextName
                    + ".");
            throw new IOException("javax.security.auth.login.Configuration has no AppConfigurationEntry named "
                    + loginContextName + ".");
        }

        boolean checkPrincipal = false;
        boolean checkKeytab = false;
        for (int i = 0; i < entrys.length; i++) {
            if (entrys[i].getOptions().get("principal").equals(principal)) {
                checkPrincipal = true;
            }

            if (IS_IBM_JDK) {
                if (entrys[i].getOptions().get("useKeytab").equals(keytabFile)) {
                    checkKeytab = true;
                }
            } else {
                if (entrys[i].getOptions().get("keyTab").equals(keyTabFilePath)) {
                    checkKeytab = true;
                }
            }
        }

        if (!checkPrincipal) {
            log.error("AppConfigurationEntry named " + loginContextName + " does not have principal value of "
                    + principal + ".");
            throw new IOException("AppConfigurationEntry named " + loginContextName
                    + " does not have principal value of " + principal + ".");
        }

        if (!checkKeytab) {
            log.error("AppConfigurationEntry named " + loginContextName + " does not have keyTab value of "
                    + keytabFile + ".");
            throw new IOException("AppConfigurationEntry named " + loginContextName + " does not have keyTab value of "
                    + keytabFile + ".");
        }
    }

    /**
     * 设置zookeeper服务端principal
     *
     * @param zkServerPrincipal
     * @throws IOException
     */
    private static void setZookeeperServerPrincipal(String zkServerPrincipal)
            throws IOException {
        System.setProperty(ZOOKEEPER_AUTH_PRINCIPAL, zkServerPrincipal);
        String ret = System.getProperty(ZOOKEEPER_AUTH_PRINCIPAL);
        if (ret == null) {
            throw new IOException(ZOOKEEPER_AUTH_PRINCIPAL + " is null.");
        }
        if (!ret.equals(zkServerPrincipal)) {
            throw new IOException(ZOOKEEPER_AUTH_PRINCIPAL + " is " + ret + " is not " + zkServerPrincipal + ".");
        }
    }

    /**
     * 写入jaas文件
     *
     * @throws IOException 写文件异常
     */
    private static void writeJaasFile(String jaasPath, String principal, String keytabPath)
            throws IOException {
        FileWriter writer = new FileWriter(new File(jaasPath));
        try {
            writer.write(getJaasConfContext(principal, keytabPath));
            writer.flush();
        } catch (IOException e) {
            throw new IOException("Failed to create jaas.conf File");
        } finally {
            writer.close();
        }
    }

    private static void deleteJaasFile(String jaasPath)
            throws IOException {
        File jaasFile = new File(jaasPath);
        if (jaasFile.exists()) {
            if (!jaasFile.delete()) {
                throw new IOException("Failed to delete exists jaas file.");
            }
        }
    }

    private static String getJaasConfContext(String principal, String keytabPath) {
        Module[] allModule = Module.values();
        StringBuilder builder = new StringBuilder();
        for (Module modlue : allModule) {
            builder.append(getModuleContext(principal, keytabPath, modlue));
        }
        return builder.toString();
    }

    private static String getModuleContext(String userPrincipal, String keyTabPath, Module module) {
        StringBuilder builder = new StringBuilder();
        if (IS_IBM_JDK) {
            builder.append(module.getName()).append(" {").append(LINE_SEPARATOR);
            builder.append(IBM_LOGIN_MODULE).append(LINE_SEPARATOR);
            builder.append("credsType=both").append(LINE_SEPARATOR);
            builder.append("principal=\"" + userPrincipal + "\"").append(LINE_SEPARATOR);
            builder.append("useKeytab=\"" + keyTabPath + "\"").append(LINE_SEPARATOR);
            builder.append("debug=true;").append(LINE_SEPARATOR);
            builder.append("};").append(LINE_SEPARATOR);
        } else {
            builder.append(module.getName()).append(" {").append(LINE_SEPARATOR);
            builder.append(SUN_LOGIN_MODULE).append(LINE_SEPARATOR);
            builder.append("useKeyTab=true").append(LINE_SEPARATOR);
            builder.append("keyTab=\"" + keyTabPath + "\"").append(LINE_SEPARATOR);
            builder.append("principal=\"" + userPrincipal + "\"").append(LINE_SEPARATOR);
            builder.append("useTicketCache=false").append(LINE_SEPARATOR);
            builder.append("storeKey=true").append(LINE_SEPARATOR);
            builder.append("debug=true;").append(LINE_SEPARATOR);
            builder.append("};").append(LINE_SEPARATOR);
        }

        return builder.toString();
    }


}

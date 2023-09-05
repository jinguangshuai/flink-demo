package com.example.demo.hbase.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.security.authentication.util.KerberosUtil;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * copy from hbase zkutil 0.94&0.98 A JAAS configuration that defines the login modules that we want to use for
 * login.
 */
@Slf4j
public class JaasConfiguration extends javax.security.auth.login.Configuration {
    private static final Map<String, String> BASIC_JAAS_OPTIONS = new HashMap<String, String>();

    /**
     * is IBM jdk or not
     */
    private static final boolean IS_IBM_JDK = System.getProperty("java.vendor").contains("IBM");
    private static final Map<String, String> KEYTAB_KERBEROS_OPTIONS = new HashMap<String, String>();
    private static final AppConfigurationEntry KEYTAB_KERBEROS_logIN = new AppConfigurationEntry(
            KerberosUtil.getKrb5LoginModuleName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, KEYTAB_KERBEROS_OPTIONS);
    private static final AppConfigurationEntry[] KEYTAB_KERBEROS_CONF =
            new AppConfigurationEntry[]{KEYTAB_KERBEROS_logIN};

    static {
        String jaasEnvVar = System.getenv("HBASE_JAAS_DEBUG");
        if (jaasEnvVar != null && "true".equalsIgnoreCase(jaasEnvVar)) {
            BASIC_JAAS_OPTIONS.put("debug", "true");
        }
    }

    static {
        if (IS_IBM_JDK) {
            KEYTAB_KERBEROS_OPTIONS.put("credsType", "both");
        } else {
            KEYTAB_KERBEROS_OPTIONS.put("useKeyTab", "true");
            KEYTAB_KERBEROS_OPTIONS.put("useTicketCache", "false");
            KEYTAB_KERBEROS_OPTIONS.put("doNotPrompt", "true");
            KEYTAB_KERBEROS_OPTIONS.put("storeKey", "true");
        }
        KEYTAB_KERBEROS_OPTIONS.putAll(BASIC_JAAS_OPTIONS);
    }

    private final String loginContextName;
    private final boolean useTicketCache;
    private final String keytabFile;
    private final String principal;
    private javax.security.auth.login.Configuration baseConfig;


    public JaasConfiguration(String loginContextName, String principal, String keytabFile) throws IOException {
        this(loginContextName, principal, keytabFile, keytabFile == null || keytabFile.length() == 0);
    }

    private JaasConfiguration(String loginContextName, String principal, String keytabFile, boolean useTicketCache) throws IOException {
        try {
            this.baseConfig = javax.security.auth.login.Configuration.getConfiguration();
        } catch (SecurityException e) {
            this.baseConfig = null;
        }
        this.loginContextName = loginContextName;
        this.useTicketCache = useTicketCache;
        this.keytabFile = keytabFile;
        this.principal = principal;

        initKerberosOption();
        log.info("JaasConfiguration loginContextName=" + loginContextName + " principal=" + principal
                + " useTicketCache=" + useTicketCache + " keytabFile=" + keytabFile);
    }

    private void initKerberosOption() throws IOException {
        if (!useTicketCache) {
            if (IS_IBM_JDK) {
                KEYTAB_KERBEROS_OPTIONS.put("useKeytab", keytabFile);
            } else {
                KEYTAB_KERBEROS_OPTIONS.put("keyTab", keytabFile);
                KEYTAB_KERBEROS_OPTIONS.put("useKeyTab", "true");
                KEYTAB_KERBEROS_OPTIONS.put("useTicketCache", useTicketCache ? "true" : "false");
            }
        }
        KEYTAB_KERBEROS_OPTIONS.put("principal", principal);
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String appName) {
        if (loginContextName.equals(appName)) {
            return KEYTAB_KERBEROS_CONF;
        }
        if (baseConfig != null) {
            return baseConfig.getAppConfigurationEntry(appName);
        }
        return (null);
    }
}

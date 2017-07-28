package com.lvmama.crm.up.esutil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by huoqiang on 26/7/2017.
 */
public class ESClientConfig {
    private static final Log logger = LogFactory.getLog(ESClientConfig.class);

    /** ES集群名称，默认为：lmm_ultimate_search */
    public static String CLUSTER_NAME = "elasticsearch";
    /** ES服务地址，默认为：lmm_ultimate_search */
    public static String ADDR[] = new String[]{"127.0.0.1"};
    /** ES传输端口，默认为：9300 */
    public static int PORT = 9300;

    public static int THREAD_NUM = 1;
    public static int STEP_LEN = 1000;
    public static boolean DEBUG_MODE = false;

    private static Properties propData = new Properties();;

    static {
        init();
    }

    public static synchronized void init() {
        InputStream inputStream = null;
        try {
            inputStream = ESClientConfig.class.getClassLoader().getResourceAsStream("local/elasticsearch.properties");
            propData.load(inputStream);

            CLUSTER_NAME = propData.getProperty("elasticsearch.cluster_name");
            String addr = propData.getProperty("elasticsearch.addr");
            ADDR = addr.split(",");
            PORT = Integer.parseInt(propData.getProperty("elasticsearch.port"));

            THREAD_NUM = Integer.parseInt(propData.getProperty("THREAD_NUM"));
            STEP_LEN = Integer.parseInt(propData.getProperty("STEP_LEN"));
            String debugMode = propData.getProperty("DEBUG_MODE");
            if (debugMode != null && "true".equals(debugMode)) {
                DEBUG_MODE = true;
            }
        } catch (Exception e) {
            CLUSTER_NAME = "elasticsearch";
            ADDR = new String[]{"127.0.0.1"};
            PORT = 9300;

            THREAD_NUM = 1;
            STEP_LEN = 1000;
            DEBUG_MODE = false;

            logger.error(e.getMessage(), e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取elasticsearch.properties中的某项配置
     */
    public static String getString(String keyword) {
        return propData.getProperty(keyword);
    }
}

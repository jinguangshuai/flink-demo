package com.example.demo.hbase.util;

import com.obs.services.ObsClient;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObsObject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * obs操作 -工具类
 */
@Slf4j
public class ObsUtil {

    private ObsClient obsClient;
    //资源地址
    private final String endpoint;
    //访问云服务资源时的秘钥
    private final String accessKeyId;
    //访问云服务资源时的秘钥
    private final String accessKeySecret;
    //桶名称
    private final String bucket;

    public ObsUtil(String endpoint, String accessKeyId, String accessKeySecret, String bucket){
        //资源地址
        this.endpoint = endpoint;
        //访问云服务资源时的秘钥
        this.accessKeyId = accessKeyId;
        //访问云服务资源时的秘钥
        this.accessKeySecret = accessKeySecret;
        //桶名称
        this.bucket = bucket;
    }


    /**
     * 初始化加载、连接桶bucket
     */
    public ObsUtil build() throws Exception {
        try {
            configCheck();
            initClient();
            list("configs");
            return this;
        } catch (Exception e) {
            throw new Exception(new Throwable().getStackTrace()[0] + "--- [ new ObsUtil propObs： %s ]", e);
        }
    }

    /**
     * 非空校验
     */
    private void configCheck() throws Exception {
        if (endpoint == null || endpoint.isEmpty()
            || accessKeyId == null || accessKeyId.isEmpty()
            || accessKeySecret == null || accessKeySecret.isEmpty()
            || bucket == null || bucket.isEmpty()) {
            log.info("未取到华为环境参数");
          throw new Exception("配置使用Obs文件存储，但是obs对应的必要属性 endpoint、accessKeyId、accessKeySecret、bucket没有正确设定。");
        }
    }

    /**
     * 初始化obs连接
     */
    private void initClient() {
      obsClient = new ObsClient(accessKeyId, accessKeySecret, endpoint);
    }


    /**
     * 读取obs上的文件
     */
    public InputStream readFile(String pathKey) throws Exception {
         try {
             ObsObject obsObject = obsClient.getObject(bucket, pathKey);
             return obsObject.getObjectContent();
         } catch (Exception e) {
             log.error(String.format("下载文件 readFile:[%s]", pathKey), e);
             throw new Exception(new Throwable().getStackTrace()[0] + String.format("下载文件 readFile:[%s]", pathKey), e);
         }
    }

    public List<String> list(String prefix) throws Exception {
          try {
                System.out.println("----jin dao list li mian------bucket------prefix：" + prefix);
                ListObjectsRequest request = new ListObjectsRequest(bucket);
                request.setPrefix(prefix);
                ObjectListing result = obsClient.listObjects(request);
                List<String> fileNameList = new ArrayList<String>();
                for (ObsObject obsObject : result.getObjects()) {
                  fileNameList.add(obsObject.getObjectKey());
                }
                System.out.println("----------bucket------list：" + fileNameList);
                return fileNameList;
          } catch (Exception e) {
                throw new Exception(new Throwable().getStackTrace()[0] + String.format("文件查询 prefix:[%s]", prefix), e);
          }
    }

    /**
     * 关闭连接
     */
    public void close() {
          try {
                if (obsClient != null) {
                    obsClient.close();
                }
          } catch (IOException e) {
                log.error("-----obs connect close error!!", e);
          }
    }
}

package com.example.demo.es.config;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class EsSearchConfig {

    @Value("${spring.elasticsearch.nodes}")
    private List<String> nodes;

    @Value("${spring.elasticsearch.schema:http}")
    private String schema;

    @Value("${spring.elasticsearch.max-connect-total:50}")
    private Integer maxConnectTotal;

    @Value("${spring.elasticsearch.max-connect-per-route:10}")
    private Integer maxConnectPerRoute;

    @Value("${spring.elasticsearch.connection-request-timeout-millis:500}")
    private Integer connectionRequestTimeoutMillis;

    @Value("${spring.elasticsearch.socket-timeout-millis:30000}")
    private Integer socketTimeoutMillis;

    @Value("${spring.elasticsearch.connect-timeout-millis:1000}")
    private Integer connectTimeoutMillis;

    @Bean
    public RestHighLevelClient getRestHighLevelClient(){
        List<HttpHost> httpHosts = Lists.newArrayList();
        for(String node : nodes){
            try{
                String[] address = StringUtils.split(node, ":");
                httpHosts.add(new HttpHost(address[0], Integer.parseInt(address[1]), schema));
            }catch (Exception e){
                throw new IllegalStateException("invalid es address "+ node +" ", e);
            }

        }

        return EsClientBuilder.build(httpHosts)
                .setConnectionRequestTimeoutMillis(connectionRequestTimeoutMillis)
                .setConnectTimeoutMillis(connectTimeoutMillis)
                .setSocketTimeoutMillis(socketTimeoutMillis)
                .setMaxConnectTotal(maxConnectTotal)
                .setMaxConnectPerRoute(maxConnectPerRoute).create();
    }

}

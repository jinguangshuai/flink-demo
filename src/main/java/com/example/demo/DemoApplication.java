package com.example.demo;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.MethodParameter;

@SpringBootApplication(scanBasePackages = {"com.example.demo"},exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, SecurityAutoConfiguration.class})
@EnableDiscoveryClient
@MapperScan("com.example.demo.mapper")
@ComponentScan("com.example.demo")
//@EnableFeignClients
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }



    public boolean supports(MethodParameter returnType, Class converterType) {
        return !returnType.getDeclaringClass().getName().contains("springfox");
    }


}

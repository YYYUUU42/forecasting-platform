package org.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient //注册到注册中心
@ComponentScan("org.example") //为了扫描到service-utils
@MapperScan("org.example.mapper")
public class ServiceWarningAndForecastingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceWarningAndForecastingApplication.class, args);
    }

}

package com.jianqing;

import com.jianqing.framework.cache.CacheProperties;
import com.jianqing.framework.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.jianqing.**.mapper")
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, CacheProperties.class})
public class JianQingApplication {

    public static void main(String[] args) {
        SpringApplication.run(JianQingApplication.class, args);
    }
}

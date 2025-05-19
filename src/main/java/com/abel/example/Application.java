package com.abel.example;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import static org.springframework.boot.SpringApplication.run;


/**
 * @auther wangxu
 * @date 2025/05/16
 */
@EnableFileStorage
@SpringBootApplication
@EnableCaching
@MapperScan("com.abel.example.dao")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
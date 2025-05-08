package com.github.karma;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@Slf4j
@SpringBootApplication
@MapperScan(basePackages = "com.github.karma.mapper")
public class KarmaApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(KarmaApplication.class);
        application.run(args);
        log.info("Application start up successfully ...");
    }

}

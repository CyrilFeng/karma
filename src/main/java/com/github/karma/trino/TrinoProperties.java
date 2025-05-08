package com.github.karma.trino;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "trino")
public class TrinoProperties {

    // Trino Coordinator 地址
    private String server;

    // Trino 用户
    private String username;

    // Trino 用户密码
    private String password;

    // Trino catalog
    private String catalog;

    // Trino schema
    private String schema;

}

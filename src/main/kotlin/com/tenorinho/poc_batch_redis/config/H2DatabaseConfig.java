package com.tenorinho.poc_batch_redis.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.database.h2")
public class H2DatabaseConfig {
    private String url;
    private String driverClassName;
    private String username;
    private String password;
    private String poolName;
    private Integer maxPoolSize;
    private Integer minPoolSize;
    private Integer maxLifetime;
    private Integer validationTimeout;
    private Integer connectionTimeout;
    private Integer idleTimeout;
    private Integer leakDetectionThreshold;

    @Primary
    @Bean(name = "dataSource")
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setPoolName(poolName);
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setMinimumIdle(minPoolSize);
        hikariConfig.setMaxLifetime(maxLifetime);
        hikariConfig.setValidationTimeout(validationTimeout);
        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setIdleTimeout(idleTimeout);
        hikariConfig.setLeakDetectionThreshold(leakDetectionThreshold);
        return new HikariDataSource(hikariConfig);
    }
}
package com.tenorinho.poc_batch_redis.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.database.mysql")
public class MySqlDatabaseConfig {
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

    @Bean(name = "mySqlDataSource")
    public DataSource mySqlDataSource(){
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
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory (@Qualifier("mySqlDataSource") DataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.tenorinho.poc_batch_redis.model");
        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        return em;
    }
    @Bean
    public PlatformTransactionManager transactionManager (@Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactoryBean.getObject());
        return transactionManager;
    }
}
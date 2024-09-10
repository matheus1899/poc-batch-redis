package com.tenorinho.poc_batch_redis.step;

import com.tenorinho.poc_batch_redis.model.Paises;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import javax.sql.DataSource;

@Slf4j
@Configuration
public class JdbcCursorReaderConfig {
 @Autowired @Qualifier("mySqlDataSource")
 private DataSource dataSource;
 private static final String sql = "select * from paises where valido = 'N'";


 @Bean("jdbcCursorItemReader")
 public JdbcCursorItemReader<Paises> jdbcCursorItemReader() {
   return new JdbcCursorItemReaderBuilder<Paises>()
           .name("jdbcCursorItemReader")
           .dataSource(dataSource)
           .sql(sql)
           .rowMapper(getRowMapper())
           .verifyCursorPosition(false)
           .build();
 }
 private RowMapper<Paises> getRowMapper(){
   return (rs, rowNum) ->  Paises.builder()
           .id(rs.getInt(1))
           .nome(rs.getString(2)).build();
 }
}

package com.tenorinho.poc_batch_redis.config;

import com.tenorinho.poc_batch_redis.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Slf4j
@Configuration
public class RedisConfig {
  @Bean LettuceConnectionFactory connectionFactory() {
    return new LettuceConnectionFactory();
  }
  @Bean public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory());
    return template;
  }
  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory, TaskTimeOutListener taskTimeoutListener) {
    String hashInstance = System.getProperty(Constants.REDIS_INSTANCE_HASH_KEY_PROPERTY);
    RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
    listenerContainer.setConnectionFactory(connectionFactory);
    listenerContainer.addMessageListener(taskTimeoutListener, new PatternTopic("__keyspace*__:iy_batch.poc_batch_redis.jobs:JobUpperCase.*"));
    listenerContainer.addMessageListener(taskTimeoutListener, new PatternTopic("__keyspace*__:iy_batch.poc_batch_redis.instances:"+hashInstance+".*"));
    listenerContainer.setErrorHandler(e -> log.error("Error in redisMessageListenerContainer", e));
    return listenerContainer;
  }
}
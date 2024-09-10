package com.tenorinho.poc_batch_redis.config;

import com.tenorinho.poc_batch_redis.repository.RedisInstanceRepository;
import com.tenorinho.poc_batch_redis.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class ShutdownConfig implements ApplicationListener<AvailabilityChangeEvent<?>> {
  @Autowired private RedisInstanceRepository redisInstanceRepository;
  private ShutdownConfig(){}
  @Bean public static ShutdownConfig shutdownConfig() {
    return new ShutdownConfig();
  }
  public void terminateApplication(){
    String hashInstance = System.getProperty(Constants.REDIS_INSTANCE_HASH_KEY_PROPERTY);
    log.info("|| Deletando instância no Redis - ["+hashInstance+"]");
    redisInstanceRepository.deleteById(hashInstance);
  }
  @Override
  public void onApplicationEvent(AvailabilityChangeEvent<?> event) {
    if(event.getState().toString().equalsIgnoreCase("REFUSING_TRAFFIC")){
      terminateApplication();
    }
  }
}
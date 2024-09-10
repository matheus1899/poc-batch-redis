package com.tenorinho.poc_batch_redis.config;

import com.tenorinho.poc_batch_redis.model.AppInstanceRedis;
import com.tenorinho.poc_batch_redis.repository.RedisInstanceRepository;
import com.tenorinho.poc_batch_redis.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

@Slf4j @Component
public class InitConfig implements InitializingBean {
  @Autowired private RedisInstanceRepository redisInstanceRepository;

  @Override
  public void afterPropertiesSet() {
    Iterable<AppInstanceRedis> redisInstances = redisInstanceRepository.findAll();
    //TODO
    long instanceQuantity = StreamSupport.stream(redisInstances.spliterator(), false).filter(Objects::nonNull).count();
    AtomicReference<Boolean> hasManager = new AtomicReference<>(Boolean.TRUE);
    if(instanceQuantity != 0){
      redisInstanceRepository.findAll().forEach((e)->{
        if(e.getIsManager()){
          hasManager.set(Boolean.FALSE);
        }
      });
    }
    if(hasManager.get()){
      AppInstanceRedis instanceRedis = redisInstanceRepository.save(new AppInstanceRedis(null, hasManager.get(), 0L));
      System.setProperty(Constants.REDIS_INSTANCE_HASH_KEY_PROPERTY, instanceRedis.getId());
      log.info("|| InstanceHash: [" + instanceRedis.getId() +"] - Instância de Gerenciamento");

    }
    else{
      SecureRandom secureRandom = new SecureRandom();
      Long delay = secureRandom.nextLong(200-50)+50;
      AppInstanceRedis instanceRedis = redisInstanceRepository.save(new AppInstanceRedis(null, hasManager.get(), delay));
      System.setProperty(Constants.REDIS_INSTANCE_HASH_KEY_PROPERTY, instanceRedis.getId());
      log.info("|| InstanceHash: [" + instanceRedis.getId() +"] - Instância de Execução");
    }
  }
}
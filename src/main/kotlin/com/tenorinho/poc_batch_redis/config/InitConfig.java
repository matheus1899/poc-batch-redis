package com.tenorinho.poc_batch_redis.config;

import com.tenorinho.poc_batch_redis.model.AppInstanceRedis;
import com.tenorinho.poc_batch_redis.model.JobRedis;
import com.tenorinho.poc_batch_redis.repository.RedisInstanceRepository;
import com.tenorinho.poc_batch_redis.repository.RedisJobRepository;
import com.tenorinho.poc_batch_redis.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

@Slf4j @Component
public class InitConfig implements InitializingBean {
  @Autowired private RedisInstanceRepository redisInstanceRepository;
  @Autowired private RedisJobRepository redisJobRepository;
  @Value("${app.scheduled.JobUpperCase.cron}")
  private String cronDefault;

  @Override
  public void afterPropertiesSet(){
    saveJobUpperCaseOnRedis();
    Iterable<AppInstanceRedis> redisInstances = redisInstanceRepository.findAll();
    //TODO
    long instanceQuantity = StreamSupport.stream(redisInstances.spliterator(), false).filter(Objects::nonNull).count();
    AtomicReference<Boolean> hasManager = new AtomicReference<>(Boolean.TRUE);
    AtomicReference<Long> delay = new AtomicReference<>(0L);
    if(instanceQuantity != 0){
      redisInstanceRepository.findAll().forEach((e)->{
        if(e.getDelay() > delay.get()){
          delay.set(e.getDelay());
        }
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
      AppInstanceRedis instanceRedis = redisInstanceRepository.save(new AppInstanceRedis(null, hasManager.get(), delay.get()+100L));
      System.setProperty(Constants.REDIS_INSTANCE_HASH_KEY_PROPERTY, instanceRedis.getId());
      log.info("|| InstanceHash: [" + instanceRedis.getId() +"] - Instância de Execução");
    }
  }
  private void saveJobUpperCaseOnRedis(){
    Optional<JobRedis> optJob = redisJobRepository.findById(Constants.JOB_UPPER_CASE_NAME);
    if(optJob.isEmpty()){
      log.info("|| Job: ["+Constants.JOB_UPPER_CASE_NAME+"] não existe. Criando...");
      JobRedis jobRedis = JobRedis.builder()
                                  .id(Constants.JOB_UPPER_CASE_NAME)
                                  .startDateTimeLastExecution(null)
                                  .isExecuting(Boolean.FALSE)
                                  .cron(cronDefault)
                                  .durationLastExecution(null)
                                  .build();
      log.info("|| Persistindo no Redis");
      redisJobRepository.save(jobRedis);
    }
  }
}
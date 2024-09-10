package com.tenorinho.poc_batch_redis.config;

import com.tenorinho.poc_batch_redis.model.AppInstanceRedis;
import com.tenorinho.poc_batch_redis.model.JobRedis;
import com.tenorinho.poc_batch_redis.model.Paises;
import com.tenorinho.poc_batch_redis.repository.RedisInstanceRepository;
import com.tenorinho.poc_batch_redis.repository.RedisJobRepository;
import com.tenorinho.poc_batch_redis.step.PaisesItemProcessor;
import com.tenorinho.poc_batch_redis.step.PaisesItemReader;
import com.tenorinho.poc_batch_redis.step.PaisesItemWriter;
import com.tenorinho.poc_batch_redis.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@EnableScheduling
@Configuration
@Slf4j
public class BatchConfig {
  @Autowired private RedisJobRepository redisJobRepository;
  @Autowired private RedisInstanceRepository redisInstanceRepository;
  @Autowired private PlatformTransactionManager platformTransactionManager;
  @Autowired private PaisesItemReader paisesItemReader;
  @Autowired private PaisesItemWriter paisesItemWriter;
  @Autowired private RedisJobRepository repository;
  @Autowired private JobRepository jobRepository;
  @Autowired private JobLauncher jobLauncher;

  @Scheduled(cron = "${app.scheduled.JobUpperCase.cron}")
  public void scheduledJobUpperCase() throws Exception {
    String hashInstance = System.getProperty(Constants.REDIS_INSTANCE_HASH_KEY_PROPERTY);
    Optional<AppInstanceRedis> optAppInstanceRedis = redisInstanceRepository.findById(hashInstance);
    Optional<JobRedis> optJob = redisJobRepository.findById(Constants.JOB_UPPER_CASE_NAME);
    Long delay = 0L;

    if(!optAppInstanceRedis.isPresent()){
      throw new RuntimeException("AppInstance is null. Verificar integridade dos dados no Redis - ID: ["+hashInstance+"]");
    }
    if(optAppInstanceRedis.isPresent() && optAppInstanceRedis.get().getIsManager()){
      log.info("|| Instância de gerenciamento - a execução será feita em outra instância");
      return;
    }
    delay = optAppInstanceRedis.get().getDelay();
    if(!optJob.isPresent()){
      log.info("|| Job: ["+Constants.JOB_UPPER_CASE_NAME+"] não existe. Criando...");
      JobRedis jobRedis = JobRedis.builder()
              .id(Constants.JOB_UPPER_CASE_NAME)
              .startDateTimeLastExecution(System.currentTimeMillis()+"")
              .isExecuting(Boolean.FALSE)
              .cron("")
              .durationLastExecution("")
              .build();
      log.info("|| Persistindo no Redis");
      repository.save(jobRedis);
    }
    if(optJob.isPresent()){
      if(optJob.get().getIsExecuting() == Boolean.TRUE){
        log.info("|| Job em execução em outra instância");
        return;
      }
      else{
        JobRedis jobRedis = optJob.get();
        jobRedis.setIsExecuting(Boolean.TRUE);
        repository.save(jobRedis);
      }
    }
    log.info("|| INICIO "+Constants.JOB_UPPER_CASE_NAME+" com delay de "+delay+"ms - "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())+" ====");
    JobParameters parameters = new JobParametersBuilder().addString("JobUpperCase", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()).toString()).toJobParameters();
    jobLauncher.run(getJobUpperCase(), parameters);
    log.info("|| FIM "+Constants.JOB_UPPER_CASE_NAME+" "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())+" ====");
  }
  public org.springframework.batch.core.Job getJobUpperCase() {
    return new JobBuilder(Constants.JOB_UPPER_CASE_NAME, jobRepository)
            .preventRestart()
            .start(getStep())
            .build();
  }
  public Step getStep() {
    return new StepBuilder("step"+Constants.JOB_UPPER_CASE_NAME, jobRepository)
            .<List<Paises>, List<Paises>>chunk(1000, platformTransactionManager)
            .reader(paisesItemReader)
            .processor(new PaisesItemProcessor())
            .writer(paisesItemWriter)
            .build();
  }
}
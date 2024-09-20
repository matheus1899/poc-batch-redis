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
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.core.RedisKeyspaceEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@EnableScheduling
public class BatchConfig implements SchedulingConfigurer {
  @Autowired private RedisJobRepository redisJobRepository;
  @Autowired private RedisInstanceRepository redisInstanceRepository;
  @Autowired private PlatformTransactionManager platformTransactionManager;
  @Autowired private PaisesItemReader paisesItemReader;
  @Autowired private PaisesItemWriter paisesItemWriter;
  @Autowired private JobRepository jobRepository;
  @Autowired private JobLauncher jobLauncher;

  public BatchConfig(){}

  @Value("${app.scheduled.JobUpperCase.cron}")
  private String cronDefault;
  private String cronRedis;

  @Bean public Executor taskExecutor() {
    return Executors.newSingleThreadScheduledExecutor();
  }
  //@SneakyThrows //TODO - Tratamento de exceção deve ser aprimorado
  @Override public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    Optional<JobRedis> optJobRedis = redisJobRepository.findById(Constants.JOB_UPPER_CASE_NAME);
    if(optJobRedis.isPresent()){
      JobRedis jobRedis = optJobRedis.get();
      cronRedis = jobRedis.getCron();
    }
    log.info("|| cronDefault: '" + cronDefault + "' - cronRedis: '" + cronRedis+"' ||");
    taskRegistrar.setScheduler(taskExecutor());
    /*
    * taskRegistrar.addCronTask(null);
    * taskRegistrar.addCronTask(null, null);
    * taskRegistrar.getCronTaskList();
    * taskRegistrar.setCronTasks();
    */
    taskRegistrar.addTriggerTask(new TriggerTask(this::scheduledJobUpperCase, new CronTrigger(ObjectUtils.firstNonNull(cronRedis, cronDefault))));
  }

  public void scheduledJobUpperCase() {
    //region |     Redis     |
    String hashInstance = System.getProperty(Constants.REDIS_INSTANCE_HASH_KEY_PROPERTY);
    Optional<AppInstanceRedis> optAppInstanceRedis = redisInstanceRepository.findById(hashInstance);
    Optional<JobRedis> optJob = redisJobRepository.findById(Constants.JOB_UPPER_CASE_NAME);
    Long delay = 0L;

    if(!optAppInstanceRedis.isPresent()){
      throw new RuntimeException("AppInstance is null. Verificar integridade dos dados no Redis - ID: ["+hashInstance+"]");
    }
    //if(optAppInstanceRedis.isPresent() && optAppInstanceRedis.get().getIsManager()){
    //  log.info("|| Instância de gerenciamento - a execução será feita em outra instância");
    //  return;
    //}
    //delay = optAppInstanceRedis.get().getDelay();
    if(optJob.isPresent()){
      if(optJob.get().getIsExecuting() == Boolean.TRUE){
        log.info("|| Job em execução em outra instância");
        return;
      }
      else{
        JobRedis jobRedis = optJob.get();
        jobRedis.setIsExecuting(Boolean.TRUE);
        redisJobRepository.save(jobRedis);
      }
    }
    //endregion
    log.info("|| INICIO "+Constants.JOB_UPPER_CASE_NAME+" com delay de "+delay+"ms - "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
    JobParameters parameters = new JobParametersBuilder().addString("JobUpperCase", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()).toString()).toJobParameters();
    try {
      jobLauncher.run(getJobUpperCase(), parameters);
    }
    catch (Exception e) {
      log.error("|| "+e.getMessage());
    }
    log.info("|| FIM "+Constants.JOB_UPPER_CASE_NAME+" "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())+" ====");
  }
  //=============================================================================
  public org.springframework.batch.core.Job getJobUpperCase() {
    return new JobBuilder(Constants.JOB_UPPER_CASE_NAME, jobRepository)
            .preventRestart()
            .start(getStepJobUpperCase())
            .build();
  }
  //=============================================================================
  public Step getStepJobUpperCase() {
    return new StepBuilder("step"+Constants.JOB_UPPER_CASE_NAME, jobRepository)
            .<List<Paises>, List<Paises>>chunk(1000, platformTransactionManager)
            .reader(paisesItemReader)
            .processor(new PaisesItemProcessor())
            .writer(paisesItemWriter)
            .build();
  }
  //=============================================================================
}
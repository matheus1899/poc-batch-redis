package com.tenorinho.poc_batch_redis.config;

import com.tenorinho.poc_batch_redis.model.Paises;
import com.tenorinho.poc_batch_redis.repository.PaisesRepository;
import com.tenorinho.poc_batch_redis.step.PaisesItemProcessor;
import com.tenorinho.poc_batch_redis.step.PaisesItemReader;
import com.tenorinho.poc_batch_redis.step.PaisesItemWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@EnableScheduling
@Configuration
@Slf4j
public class BatchConfig {
    @Autowired private PaisesItemReader paisesItemReader;
    @Autowired private PaisesItemWriter paisesItemWriter;
    @Autowired private JobLauncher jobLauncher;
    @Autowired private JobRepository jobRepository;
    @Autowired private PlatformTransactionManager platformTransactionManager;

    @Scheduled(cron = "${app.scheduled.JobUpperCase.cron}")
    public void scheduledJobUpperCase() throws Exception {
        log.info("=== INICIO JobUpperCase "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())+" ====");
        //TODO Aqui ja entra a parte de verificação de execução desse job no Redis
        JobParameters parameters = new JobParametersBuilder().addString("JobUpperCase", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()).toString()).toJobParameters();
        jobLauncher.run(getJobUpperCase(), parameters);
        log.info("=== FIM JobUpperCase "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())+" ====");
    }
    public Job getJobUpperCase() {
        return new JobBuilder("JobUpperCase", jobRepository)
                .preventRestart()
                .start(getStep())
                .build();
    }
    public Step getStep() {
        return new StepBuilder("stepJobUpperCase", jobRepository)
                .<List<Paises>, List<Paises>>chunk(1000, platformTransactionManager)
                .reader(paisesItemReader)
                .processor(new PaisesItemProcessor())
                .writer(paisesItemWriter)
                .build();
    }
}
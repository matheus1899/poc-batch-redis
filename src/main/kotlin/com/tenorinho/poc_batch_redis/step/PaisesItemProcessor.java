package com.tenorinho.poc_batch_redis.step;


import com.tenorinho.poc_batch_redis.model.JobRedis;
import com.tenorinho.poc_batch_redis.model.Paises;
import com.tenorinho.poc_batch_redis.repository.RedisJobRepository;
import com.tenorinho.poc_batch_redis.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Component
public class PaisesItemProcessor implements ItemProcessor<List<Paises>, List<Paises>>{
    @Autowired private RedisJobRepository jobUpperCaseRepository;

    @Override
    public List<Paises> process(List<Paises> list) throws InterruptedException {
        log.info("|| ======= INICIO Processor =======");
        Random random = new Random();
        Long l = random.nextLong();

        for(int i = 0; i < 50; i++){
            Thread.sleep(1000);
        }

        if(list == null || list.isEmpty()){
            Optional<JobRedis> opt = jobUpperCaseRepository.findById(Constants.JOB_UPPER_CASE_NAME);
            JobRedis jobRedis = opt.get();
            if(jobRedis != null){
                jobRedis.setIsExecuting(Boolean.FALSE);
                log.info("|| Atualizando status no Redis");
                jobUpperCaseRepository.save(jobRedis);
            }
            else{
                //TODO Usar instancia recuperada do Redis
                jobRedis = JobRedis.builder()
                        .id(Constants.JOB_UPPER_CASE_NAME)
                        .cron("")
                        .isExecuting(Boolean.FALSE)
                        .startDateTimeLastExecution("")
                        .durationLastExecution("")
                        .build();
                jobUpperCaseRepository.save(jobRedis);
            }
            log.info("|| Sem items para processar");
            log.info("|| ========= FIM Processor ========");
            return null;
        }
        log.info("|| Tamanho: " + list.size());
        for(int i = 0; i < list.size(); i++){
            Paises pais = list.get(i);
            pais.setNome(pais.getNome().toUpperCase());
            pais.setValido("S");
            pais.setPodRandomValue(l+"");
        }
        log.info("|| ========= FIM Processor ========");
        return list;
    }
}

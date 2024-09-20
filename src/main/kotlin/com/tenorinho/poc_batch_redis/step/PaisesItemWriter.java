package com.tenorinho.poc_batch_redis.step;

import com.tenorinho.poc_batch_redis.model.JobRedis;
import com.tenorinho.poc_batch_redis.model.Paises;
import com.tenorinho.poc_batch_redis.repository.PaisesRepository;
import com.tenorinho.poc_batch_redis.repository.RedisJobRepository;
import com.tenorinho.poc_batch_redis.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class PaisesItemWriter implements ItemWriter<List<Paises>> {
  @Autowired PaisesRepository paisesRepository;
  @Autowired
  RedisJobRepository jobUpperCaseRepository;

  @Override
  public void write(Chunk<? extends List<Paises>> chunk) {
    log.info("|| ======== INICIO Gravação =======");
    List<Paises> paises = chunk.getItems().get(0);
    if(paises == null || paises.isEmpty()){
      log.info("|| Sem items para gravar");
      log.info("|| FIM Gravação");
      return;
    }
    log.info("|| Tamanho: " + paises.size());
    paises.forEach(e -> paisesRepository.save(e));
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
              .cron(null)
              .isExecuting(Boolean.FALSE)
              .startDateTimeLastExecution(null)
              .durationLastExecution(null)
              .build();
      jobUpperCaseRepository.save(jobRedis);
    }
    log.info("|| ========= FIM Gravação =========");
  }
}

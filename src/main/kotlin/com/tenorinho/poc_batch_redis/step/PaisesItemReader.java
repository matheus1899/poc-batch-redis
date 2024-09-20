package com.tenorinho.poc_batch_redis.step;

import com.tenorinho.poc_batch_redis.model.JobRedis;
import com.tenorinho.poc_batch_redis.model.Paises;
import com.tenorinho.poc_batch_redis.repository.PaisesRepository;
import com.tenorinho.poc_batch_redis.repository.RedisJobRepository;
import com.tenorinho.poc_batch_redis.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.scanner.Constant;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class PaisesItemReader implements ItemStreamReader<List<Paises>> {
  @Autowired @Qualifier("jdbcCursorItemReader")
  public ItemStreamReader<Paises> jdbcCursorItemReader;
  @Autowired private RedisJobRepository jobUpperCaseRepository;

  @Override
  public List<Paises> read() throws Exception {
    log.info("|| ======== INICIO Leitura ========");
    Paises atual = jdbcCursorItemReader.read();

    if(atual == null){
      Optional<JobRedis> opt = jobUpperCaseRepository.findById(Constants.JOB_UPPER_CASE_NAME);
      JobRedis jobRedis = opt.get();
      if(jobRedis != null){
        jobRedis.setIsExecuting(Boolean.FALSE);
        log.info("|| Atualizando status no Redis");
        jobUpperCaseRepository.save(jobRedis);
      }
      else{
        //TODO Usar instância recuperada do Redis
        jobRedis = JobRedis.builder()
                .id(Constants.JOB_UPPER_CASE_NAME)
                .cron("")
                .isExecuting(Boolean.FALSE)
                .startDateTimeLastExecution("")
                .durationLastExecution("")
                .build();
        jobUpperCaseRepository.save(jobRedis);
      }
      log.info("|| Nenhum item encontrado");
      return null;
    }
    List<Paises> lista = new LinkedList<>();
    while(atual != null){
      lista.add(atual);
      atual = jdbcCursorItemReader.read();
    }
    log.info("|| Tamanho: " + lista.size());
    log.info("|| ========= FIM Leitura ==========");
    return lista;
  }

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    this.jdbcCursorItemReader.open(executionContext);
  }
  @Override
  public void update(ExecutionContext executionContext) throws ItemStreamException {
    this.jdbcCursorItemReader.update(executionContext);
  }
  @Override
  public void close() throws ItemStreamException {
    this.jdbcCursorItemReader.close();
  }
}

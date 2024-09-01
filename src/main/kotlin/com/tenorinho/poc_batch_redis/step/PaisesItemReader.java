package com.tenorinho.poc_batch_redis.step;

import com.tenorinho.poc_batch_redis.model.Paises;
import com.tenorinho.poc_batch_redis.repository.PaisesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class PaisesItemReader implements ItemStreamReader<List<Paises>> {
  @Autowired @Qualifier("jdbcCursorItemReader")
  public ItemStreamReader<Paises> jdbcCursorItemReader;

  @Override
  public List<Paises> read() throws Exception {
    log.info("==== INICIO Leitura ====");
    Paises atual = jdbcCursorItemReader.read();

    if(atual == null){
      log.info("|| Nenhum item encontrado");
      log.info("==== FIM Leitura ====");
      return null;
    }
    List<Paises> lista = new LinkedList<>();
    while(atual != null){
      lista.add(atual);
      atual = jdbcCursorItemReader.read();
    }
    log.info("|| Tamanho: " + lista.size());
    log.info("==== FIM Leitura ====");
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

package com.tenorinho.poc_batch_redis.step;

import com.tenorinho.poc_batch_redis.model.Paises;
import com.tenorinho.poc_batch_redis.repository.PaisesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class PaisesItemWriter implements ItemWriter<List<Paises>> {
  @Autowired PaisesRepository paisesRepository;

  @Override
  public void write(Chunk<? extends List<Paises>> chunk) {
    log.info("==== INICIO Gravação ====");
    List<Paises> paises = chunk.getItems().get(0);
    if(paises == null || paises.isEmpty()){
      log.info("|| Sem items para gravar");
      log.info("==== FIM Gravação ====");
      return;
    }
    log.info("|| Tamanho: " + paises.size());
    paises.forEach(e->paisesRepository.save(e));
    log.info("==== FIM Gravação ====");
  }
}

package com.tenorinho.poc_batch_redis.step;


import com.tenorinho.poc_batch_redis.model.Paises;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import java.util.List;
import java.util.Random;

@Slf4j
public class PaisesItemProcessor implements ItemProcessor<List<Paises>, List<Paises>>{
    @Override
    public List<Paises> process(List<Paises> list){
        log.info("==== INICIO Processor ====");
        if(list == null || list.isEmpty()){
            log.info("|| Sem items para processar");
            log.info("==== FIM Processor ====");
            return null;
        }
        log.info("|| Tamanho:" + list.size());
        list.forEach(e -> e.setNome(e.getNome().toUpperCase()));
        Random random = new Random();
        Long l = random.nextLong();
        list.add(new Paises(null, l+""));
        list.add(new Paises(null, l+""));
        list.add(new Paises(null, l+""));
        list.add(new Paises(null, l+""));
        list.add(new Paises(null, l+""));
        log.info("==== FIM Processor ====");
        return list;
    }
}

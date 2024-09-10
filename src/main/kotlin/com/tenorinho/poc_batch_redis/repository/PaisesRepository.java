package com.tenorinho.poc_batch_redis.repository;

import com.tenorinho.poc_batch_redis.model.Paises;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaisesRepository extends JpaRepository<Paises, Integer> {}
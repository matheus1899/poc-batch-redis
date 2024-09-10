package com.tenorinho.poc_batch_redis.repository;

import com.tenorinho.poc_batch_redis.model.JobRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisJobRepository extends CrudRepository<JobRedis, String> {
}

package com.tenorinho.poc_batch_redis.model;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("iy_batch.poc_batch_redis.jobs")
public class JobRedis {
  @Id String id;
  String cron;
  String durationLastExecution;
  String startDateTimeLastExecution;
  Boolean isExecuting;
  Boolean isEnabled;
}
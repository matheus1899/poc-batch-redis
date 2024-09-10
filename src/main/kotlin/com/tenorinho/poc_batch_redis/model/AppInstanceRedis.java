package com.tenorinho.poc_batch_redis.model;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "iy_batch.poc_batch_redis.instances")
public class AppInstanceRedis {
  @Id String id;
  Boolean isManager;
  Long delay;
}
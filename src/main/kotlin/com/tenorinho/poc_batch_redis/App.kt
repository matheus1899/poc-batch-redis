package com.tenorinho.poc_batch_redis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource
import org.springframework.data.redis.core.RedisKeyValueAdapter
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@EnableRedisRepositories(enableKeyspaceEvents= RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
@SpringBootApplication
@PropertySource("classpath:META-INF/build-info.properties")
//@PropertySource("classpath:build-info.properties")
class App
fun main(args: Array<String>) {
	runApplication<App>(*args)
}

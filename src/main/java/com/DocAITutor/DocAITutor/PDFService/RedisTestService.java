package com.DocAITutor.DocAITutor.PDFService;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTestService {
	private final RedisTemplate<String, Object> redisTemplate; 
	
	
	
	
	public RedisTestService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
//		redisTemplate.opsForValue().set("test:key", "hello world");
//		Object value = redisTemplate.opsForValue().get("test:key");
//		System.out.println(value);

	}
	
	
	
	public void test() {
		redisTemplate.opsForValue().set("test:key", "hello world");
		Object value = redisTemplate.opsForValue().get("test:key");
		System.out.println(value);
	}

}

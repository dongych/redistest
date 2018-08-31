/*package com.redis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.redis.config.RedisUtil;

@RunWith(SpringRunner.class)
@SpringBootTest({ "service.port: 8080" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(classes = { com.redis.Application.class })
public class RedisTest2 {
	private @Autowired RedisUtil redisUtil;
	
	@Test
	public void testT(){
		ExecutorService executorService = Executors.newFixedThreadPool(20);
		for(int i=0;i<10000;i++){
			final int j = i ;
			executorService.execute(new Runnable(){
				@Override
				public void run() {
					try {
						redisUtil.add(Thread.currentThread().getName() +":"+ j, "test", 1000 * 60 * 5);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			});
		}
	}

	@Test
	public void testRedisSave() {
		for (int i = 0; i < 10; i++) {
			new TestThread(i).start();
		}
	}

	class TestThread extends Thread {
		int i = 0;

		public TestThread(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			try {
				//for (int j = 0; j < 10; j++) {
					redisUtil.add(Thread.currentThread().getName() + ":" + i, "test", 60);
				//}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
*/
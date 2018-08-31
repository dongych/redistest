/*package com.redis;

import java.util.ArrayList;
import java.util.List;
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
@SpringBootTest({ "service.port: 12345" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(classes = { com.redis.Application.class })
public class RedisTest {
	private @Autowired RedisUtil redisUtil;

	@Test
	public void testRedis() throws Exception {
		System.out.println("redisUtil:::::"+redisUtil);
		for(int i=0;i<10000;i++){
			redisUtil.add("sss"+i, "1111", 1000 * 60 * 5);
		}
	}
	
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
	@SuppressWarnings("unchecked")
	@Test
	public void testRedisTask() {

		for (int i = 0; i < 1; i++) {
			List<String> list = new ArrayList<String>();
			for (int k = 0; k < 10000; k++) {
				list.add(k + ":bread");
			}
			new TestThread1(list, redisUtil).start();
		}

	}
}



class TestThread1 extends Thread {
	List<String> list = null;
	RedisUtil redisUtil;

	public TestThread1(List<String> list, RedisUtil redisUtil) {
		this.list = list;
		this.redisUtil = redisUtil;
	}
	
	public void run() {
		System.out.println("lst:"+list);
		for (int i=0;i<list.size();i++) {
			String b = list.get(i) ;
			
			try {
				// 每次睡眠一个随机时间
				redisUtil.add(Thread.currentThread().getName() +":"+ b, "test", 1000 * 60 * 5);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	

}

*/
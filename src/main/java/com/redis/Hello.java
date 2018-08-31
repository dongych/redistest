package com.redis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redis.config.RedisUtils;

@RestController
public class Hello {
	private @Autowired RedisUtils redisUtils;

	@RequestMapping("/hello")
	public String sayHello() {

		 test1();
		/*for (int i = 0; i < 10; i++) {
			List<String> list = new ArrayList<String>();
			for (int k = 0; k < 5000; k++) {
				list.add(k + ":bread");
			}
			new TestThread(list).start();
		}*/

		return "hello world";
	}
	
	public void test1() {
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		for (int i = 0; i < 10000; i++) {
			final int j = i;
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						redisUtils.store(Thread.currentThread().getName() + ":" + j, "test", 1000 * 60 * 5);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			});
		}
	}
	
	private class TestThread extends Thread {
		List<String> list = null;

		public TestThread(List<String> list) {
			this.list = list;
		}

		public void run() {
			for (int i=0;i<list.size();i++) {
				String b = list.get(i) ;
				System.out.println(b);
				Date date = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
				String time = sdf.format(date);
				try {
					// 每次睡眠一个随机时间
					redisUtils.store(Thread.currentThread().getName() +":"+ b, time, 1000 * 60 * 5);
					//Thread.sleep((int) (Math.random() * 5000));
					//String foo = redisUtils.get(Thread.currentThread().getName() +":"+ b);
					//System.out.println("【输出>>>>】key:" + Thread.currentThread().getName() + "<>" + foo);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

	}
}

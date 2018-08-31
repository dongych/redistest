package com.redis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.redis.config.RedisUtils;
/**
 *  下面的测试数据不准确，放到Hello.java中测试
 *  测试包有问题。
 * */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(classes = { com.redis.Application.class })
public class TestRedisUtils {
	private @Autowired RedisUtils redisUtils;

	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;

	@Test
	public void testDeleteByRegExp() {
		redisUtils.deleteByRegExp("*key*");
	}

	@Test
	public void testScanAndDeleteByPrefix() {
		generateRandomKeys(10000);
		redisUtils.scanAndDeleteByPrefix(new ArrayList<String>(Arrays.asList("key-1")));
	}

	private void generateRandomKeys(int nrKeys) {
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		for (int i = 0; i < nrKeys; i++) {
			conn.set(("key" + "-" + i).getBytes(), ("key" + "-" + i).getBytes());
		}
	}

	@Test
	public void testRedisSave() {
		for (int i = 0; i < 1000; i++) {
			TestThread t = new TestThread(i, redisUtils);
			t.start();
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
			new TestThread1(list, redisUtils).start();
		}

	}

	@Test
	public void testT() {
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		for (int i = 0; i < 100000; i++) {
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
}

class TestThread extends Thread {
	int i = 0;
	RedisUtils redisUtils;

	public TestThread(int i, RedisUtils redisUtils) {
		this.i = i;
		this.redisUtils = redisUtils;
	}

	public void run() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String time = sdf.format(date);
		try {
			// 每次睡眠一个随机时间
			redisUtils.store("key" + i, time, 1000 * 60 * 5);
			Thread.sleep((int) (Math.random() * 5000));
			String foo = redisUtils.get("key" + i);
			System.out.println("【输出>>>>】key:" + foo + " 第:" + i + "个线程");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

class TestThread1 extends Thread {
	List<String> list = null;
	RedisUtils redisUtils;

	public TestThread1(List<String> list, RedisUtils redisUtils) {
		this.list = list;
		this.redisUtils = redisUtils;
	}

	public void run() {
		System.out.println("lst:"+list);
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
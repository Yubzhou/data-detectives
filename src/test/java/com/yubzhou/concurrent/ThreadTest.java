package com.yubzhou.concurrent;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class ThreadTest {
	@Test
	public void test01() throws Exception {
		List<Thread> threads = new ArrayList<>();
		List<FutureTask<Integer>> tasks = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			int finalI = i;
			FutureTask<Integer> task = new FutureTask<>(() -> {
				TimeUnit.SECONDS.sleep(1);
				return finalI;
			});
			tasks.add(task);
			Thread t = new Thread(task, "Thread-" + finalI);
			t.start();
			threads.add(t);
		}
		int total = tasks.stream().mapToInt(task -> {
			try {
				return task.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}).sum();
		System.out.println("Total: " + total);
	}

	@Test
	public void test02() throws Exception {
		CountDownLatch latch = new CountDownLatch(10);
		List<FutureTask<Integer>> tasks = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			int finalI = i;
			FutureTask<Integer> task = new FutureTask<>(() -> {
				TimeUnit.SECONDS.sleep(1);
				latch.countDown();
				return finalI;
			});
			tasks.add(task);
			Thread t = new Thread(task, "Thread-" + finalI);
			t.start();
		}
		latch.await();
		int total = tasks.stream().mapToInt(task -> {
			try {
				return task.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}).sum();
		System.out.println("Total: " + total);
	}
}

package com.yubzhou.concurrent;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchTest {
	public static void main(String[] args) {
		int count = 10;
		CountDownLatch latch = new CountDownLatch(count);
		for (int i = 0; i < count; i++) {
			new Thread(() -> {
				try {
					Thread.sleep(1000);
					System.out.println(Thread.currentThread().getName() + " is done");
					latch.countDown();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}, "Thread-" + i).start();
		}
		try {
			long start = System.currentTimeMillis();
			System.out.println("Waiting for all threads to finish...");
			latch.await();
			long end = System.currentTimeMillis();
			System.out.println("All threads are done");
			System.out.println("Time elapsed: " + (end - start) + " ms");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

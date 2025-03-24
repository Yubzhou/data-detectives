package com.yubzhou.concurrent;

import org.junit.jupiter.api.Test;

import java.util.concurrent.FutureTask;

public class FutureTaskTest {
	@Test
	public void test01() throws Exception {
		FutureTask<String> futureTask = new FutureTask<>(() -> {
			System.out.println("futureTask开始执行...");
			Thread.sleep(3000);
			System.out.println("futureTask执行完成...");
			return "Hello, World!";
		});
		new Thread(futureTask).start();

		System.out.println("主线程执行...");

		// System.out.println(futureTask.get());

		System.out.println("主线程结束...");

	}

	public static void main(String[] args) throws Exception {
		new FutureTaskTest().test01();
	}
}

package com.yubzhou.concurrent;


import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompletableFutureTest {
	// 模拟耗时操作
	private static void sleep(int seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static void allOf() {
		// 创建多个异步任务
		CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
			// 模拟耗时操作
			sleep(2);
			return "Task 1 Result";
		});

		CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
			sleep(2);
			return "Task 2 Result";
		});

		CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
			sleep(1);
			return "Task 3 Result";
		});

		// 使用 allOf 方法等待所有任务完成
		CompletableFuture<Void> allFutures = CompletableFuture.allOf(task1, task2, task3);

		// // 当所有任务完成时，获取每个任务的结果并进行处理
		// CompletableFuture<String> combinedFuture = allFutures.thenApply(v -> {
		// 	// 获取每个任务的结果
		// 	String result1 = task1.join();
		// 	String result2 = task2.join();
		// 	String result3 = task3.join();
		//
		// 	// 组合结果
		// 	return result1 + ", " + result2 + ", " + result3;
		// });

		// 等待
		System.out.println("Waiting for results...");
		long start = System.currentTimeMillis();
		allFutures.join();
		long end = System.currentTimeMillis();

		// 获取最终结果
		String finalResult = task1.getNow(null) + ", " + task2.getNow(null) + ", " + task3.getNow(null);

		System.out.println("Combined Result: " + finalResult + ", type: " + finalResult.getClass());
		System.out.println("Time taken: " + (end - start) + " ms");
	}

	@Test
	public void test() throws Exception {
		allOf();
	}

	@Test
	public void test01() throws Exception {
		CompletableFuture<String> future = CompletableFuture.completedFuture("hello!")
				.thenApply(s -> s + "world!");
		assertEquals("hello!world!", future.get());
		// 这次调用将被忽略。因为 thenApply 方法会返回一个新的CompletableFuture，而不是在原有 CompletableFuture 上添加回调函数，而是返回了一个新的 CompletableFuture。
		future.thenApply(s -> s + "nice!");
		assertEquals("hello!world!", future.get());
	}

	@Test
	public void test02() throws Exception {
		CompletableFuture<String> future = CompletableFuture.completedFuture("hello!")
				.thenApply(s -> s + "world!").thenApply(s -> s + "nice!");
		assertEquals("hello!world!nice!", future.get());
	}

	@Test
	public void test03() throws Exception {
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello from supplyAsync");
		System.out.println(future.join()); // 输出：Hello from supplyAsync
	}

	@Test
	public void test04() throws Exception {
		CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
					System.out.println(System.currentTimeMillis());
					sleep(1);
					System.out.println(System.currentTimeMillis());
					return "Hello";
				})
				// thenRun方法需等待任务完成，才会执行
				.thenRun(() -> {
					System.out.println("Task completed");
					System.out.println(System.currentTimeMillis());
				});
		future.join();// 输出：Task completed
	}

	@Test
	public void test05() throws Exception {
		CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
					System.out.println(System.currentTimeMillis());
					sleep(1);
					System.out.println(System.currentTimeMillis());
					return "Hello";
				})
				// thenRun方法需等待任务完成，才会执行
				.thenRunAsync(() -> {
					sleep(1);
					System.out.println("Task completed");
					System.out.println(System.currentTimeMillis());
				});
		future.join();// 输出：Task completed
	}

	@Test
	public void test06() throws Exception {
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
					System.out.println(System.currentTimeMillis());
					sleep(1);
					System.out.println(System.currentTimeMillis());
					return "Hello";
				})
				// thenRun方法需等待任务完成，才会执行
				.thenApply(s -> {
					System.out.println("Task completed");
					System.out.println(System.currentTimeMillis());
					return s + " world";
				});
		future.join();// 输出：Task completed
	}

	@Test
	public void test07() throws Exception {
		CompletableFuture<Void> voidCompletableFuture = CompletableFuture.supplyAsync(() -> {
			System.out.println("阶段1 线程: " + Thread.currentThread().getName());
			sleep(1);
			return "Order123";
		}).thenApplyAsync(order -> {
			System.out.println("阶段2 线程: " + Thread.currentThread().getName());
			sleep(1);
			return order + " validated";
		}).thenApplyAsync(order -> {
			System.out.println("阶段3 线程: " + Thread.currentThread().getName());
			sleep(1);
			return order + " paid";
		}).thenAcceptAsync(order -> {
			System.out.println("阶段4 线程: " + Thread.currentThread().getName());
			System.out.println("最终结果: " + order);
		});
		System.out.println("主线程执行其他任务");
		sleep(2);
		System.out.println("主线程等待异步任务");
		voidCompletableFuture.join();
		System.out.println("主线程执行完成");
	}

	@Test
	public void test08() throws Exception {
		CompletableFuture.runAsync(() -> {
			System.out.println(Thread.currentThread().getName() + " time: " + System.currentTimeMillis());
			try {
				sleep(2);
				int i = 1 / 0;
			} catch (Exception e) {
				throw new CompletionException(e);
			} finally {
				System.out.println(Thread.currentThread().getName() + " time: " + System.currentTimeMillis());
			}
		}).exceptionally(e -> {
			System.out.println("Exception caught: " + e.getMessage());
			return null;
		});
		System.out.println("Main thread time: " + System.currentTimeMillis());
		sleep(3);
		System.out.println("Main thread finished");
		System.out.println("Main thread time: " + System.currentTimeMillis());
	}
}
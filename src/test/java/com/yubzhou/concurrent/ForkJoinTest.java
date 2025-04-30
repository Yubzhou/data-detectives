package com.yubzhou.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinTest {

	public static void main(String[] args) {
		int start = 1, end = 10000, taskSize = (int) Math.ceil((end - start + 1) / 4.0);
		try {
			forkJoin(start, end, taskSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("-----".repeat(10));
		sum(start, end);
	}

	public static void forkJoin(int start, int end, int taskSize) throws ExecutionException, InterruptedException {
		ForkJoinPool pool = new ForkJoinPool();
		SubTask task = new SubTask(start, end, taskSize);
		long startTime = System.currentTimeMillis();
		long result = pool.submit(task).get();
		long endTime = System.currentTimeMillis();
		System.out.print("Fork/Join result: " + result);
		System.out.println(", time: " + (endTime - startTime) + "ms");
		pool.shutdown();
	}

	public static void sum(int start, int end) {
		long result = 0;
		long startTime = System.currentTimeMillis();
		for (int i = start; i <= end; i++) {
			result += i;
		}
		long endTime = System.currentTimeMillis();
		System.out.print("Sum result: " + result);
		System.out.println(", time: " + (endTime - startTime) + "ms");
	}

	public static class SubTask extends RecursiveTask<Long> {

		private final int start;
		private final int end;
		// private final int subTaskSize;
		// 每份子任务的大小
		private final int taskSize;

		public SubTask(int start, int end, int taskSize) {
			this.start = start;
			this.end = end;
			this.taskSize = taskSize;
		}

		@Override
		protected Long compute() {
			if ((end - start) <= taskSize) {
				long result = 0;
				for (int i = start; i <= end; i++) {
					result += i;
				}
				return result;
			}
			int mid = start + (end - start) / 2;
			SubTask leftTask = new SubTask(start, mid, taskSize);
			SubTask rightTask = new SubTask(mid + 1, end, taskSize);
			leftTask.fork();
			rightTask.fork();
			return leftTask.join() + rightTask.join();
		}
	}
}

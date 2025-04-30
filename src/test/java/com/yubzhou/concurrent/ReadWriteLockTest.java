package com.yubzhou.concurrent;

import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockTest {
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
	private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
	private int sharedData = 0;

	// 读操作
	public int readData() {
		readLock.lock(); // 获取读锁
		try {
			System.out.println(Thread.currentThread().getName() + " 读取数据: " + sharedData);
			return sharedData;
		} finally {
			readLock.unlock(); // 释放读锁
		}
	}

	// 写操作
	public void writeData(int newValue) {
		writeLock.lock(); // 获取写锁
		try {
			sharedData = newValue;
			System.out.println(Thread.currentThread().getName() + " 写入数据: " + newValue);
		} finally {
			writeLock.unlock(); // 释放写锁
		}
	}

	public boolean tryRead() {
		if (readLock.tryLock()) { // 尝试获取读锁
			try {
				// 执行读操作
				System.out.println(Thread.currentThread().getName() + " 读取数据: " + sharedData);
				return true;
			} finally {
				readLock.unlock();
			}
		} else {
			System.out.println(Thread.currentThread().getName() + " 读锁被占用，无法执行读操作");
			return false;
		}
	}

	public boolean tryWrite(int newValue) {
		if (writeLock.tryLock()) { // 尝试获取写锁
			// 查看是否持有写锁
			System.out.println(Thread.currentThread().getName() + " 是否持有写锁: " + writeLock.isHeldByCurrentThread());
			try {
				// 执行写操作
				sharedData = newValue;
				System.out.println(Thread.currentThread().getName() + " 写入数据: " + newValue);
				return true;
			} finally {
				writeLock.unlock();
			}
		} else {
			System.out.println(Thread.currentThread().getName() + " 写锁被占用，无法执行写操作");
			return false;
		}
	}

	public void lockDowngrade() {
		writeLock.lock(); // 获取写锁
		try {
			// 修改数据
			sharedData = 42;
			// 在释放写锁前获取读锁（锁降级关键）
			readLock.lock();
		} finally {
			writeLock.unlock(); // 释放写锁，此时仍持有读锁
		}

		try {
			System.out.println("锁降级后读取数据: " + sharedData);
		} finally {
			readLock.unlock(); // 释放读锁
		}
	}

	@Test
	public void test01() {
		final ReadWriteLockTest test = new ReadWriteLockTest();

		// 模拟多个读线程
		for (int i = 0; i < 5; i++) {
			new Thread(test::readData, "ReadThread-" + i).start();
		}

		// 模拟写线程
		new Thread(() -> test.writeData(100)).start();
	}

	@Test
	public void test02() throws Exception {
		final ReadWriteLockTest test = new ReadWriteLockTest();

		// 启动多个读线程和写线程
		for (int i = 0; i < 10; i++) {
			new Thread(test::readData, "ReadThread-" + i).start();
			new Thread(() -> test.writeData((int) (Math.random() * 100)), "WriteThread-" + i).start();
		}
	}

	@Test
	public void test03() throws Exception {
		final ReadWriteLockTest test = new ReadWriteLockTest();

		// 启动多个读线程和写线程
		for (int i = 0; i < 30; i++) {
			new Thread(test::tryRead, "ReadThread-" + i).start();
			new Thread(() -> test.tryWrite((int) (Math.random() * 100)), "WriteThread-" + i).start();
		}
	}

	@Test
	public void test04() throws Exception {
		final ReadWriteLockTest test = new ReadWriteLockTest();

		// 测试锁降级
		new Thread(test::lockDowngrade).start();
	}
}
package com.yubzhou.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisLockUtil {

	private final RedisTemplate<String, Object> redisTemplate;

	@Autowired
	public RedisLockUtil(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	private static final String LOCK_PREFIX = "lock:";  // 锁前缀
	private static final int DEFAULT_LEASE_TIME = 30_000;  // 默认锁持有时间30秒（毫秒）
	private static final long DEFAULT_WAIT_TIME = 5_000;    // 默认等待时间5秒（毫秒）

	// 解锁Lua脚本（原子操作）
	private static final String UNLOCK_SCRIPT =
			"if redis.call('get', KEYS[1]) == ARGV[1] then " +
					"return redis.call('del', KEYS[1]) " +
					"else " +
					"return 0 " +
					"end";

	// 续期Lua脚本
	private static final String RENEW_SCRIPT =
			"if redis.call('get', KEYS[1]) == ARGV[1] then " +
					"return redis.call('expire', KEYS[1], ARGV[2]) " +
					"else " +
					"return 0 " +
					"end";

	//------------------ 核心方法 ------------------//

	/**
	 * 尝试立即获取锁（非阻塞）
	 *
	 * @param lockKey   锁名称（自动添加前缀）
	 * @param leaseTime 锁持有时间（毫秒）
	 * @return 锁唯一标识（失败返回null）
	 */
	public String tryLock(String lockKey, int leaseTime) {
		String lockValue = UUID.randomUUID().toString();
		Boolean success = redisTemplate.opsForValue()
				.setIfAbsent(LOCK_PREFIX + lockKey, lockValue, leaseTime, TimeUnit.SECONDS);
		return Boolean.TRUE.equals(success) ? lockValue : null;
	}

	/**
	 * 尝试获取锁（带等待时间）
	 *
	 * @param lockKey   锁名称
	 * @param waitTime  最大等待时间（毫秒）
	 * @param leaseTime 锁持有时间（毫秒）
	 * @return 锁唯一标识（失败返回null）
	 */
	public String tryLock(String lockKey, long waitTime, int leaseTime) {
		String lockValue = UUID.randomUUID().toString();
		long endTime = System.currentTimeMillis() + waitTime * 1000;

		while (System.currentTimeMillis() < endTime) {
			Boolean success = redisTemplate.opsForValue()
					.setIfAbsent(LOCK_PREFIX + lockKey, lockValue, leaseTime, TimeUnit.SECONDS);
			if (Boolean.TRUE.equals(success)) {
				return lockValue;
			}

			try {
				// 短暂休眠后重试（避免高频请求）
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		return null;
	}

	/**
	 * 释放锁
	 *
	 * @param lockKey   锁名称
	 * @param lockValue 锁唯一标识
	 * @return 是否释放成功
	 */
	public boolean unlock(String lockKey, String lockValue) {
		if (lockValue == null || lockValue.isEmpty()) return false;

		RedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
		Long result = redisTemplate.execute(
				script,
				Collections.singletonList(LOCK_PREFIX + lockKey),
				lockValue
		);
		return result != null && result == 1L;
	}

	/**
	 * 自动续期（需在获取锁后调用）
	 *
	 * @param lockKey   锁名称
	 * @param lockValue 锁唯一标识
	 * @param leaseTime 续期时间（秒）
	 * @return 是否续期成功
	 */
	public boolean renewLease(String lockKey, String lockValue, int leaseTime) {
		RedisScript<Long> script = new DefaultRedisScript<>(RENEW_SCRIPT, Long.class);
		Long result = redisTemplate.execute(
				script,
				Collections.singletonList(LOCK_PREFIX + lockKey),
				lockValue,
				String.valueOf(leaseTime)
		);
		return result != null && result == 1L;
	}

	//------------------ 快捷方法 ------------------//

	/**
	 * 获取锁（使用默认等待时间和持有时间）
	 *
	 * @param lockKey 锁名称
	 * @return 锁唯一标识
	 */
	public String tryLock(String lockKey) {
		return tryLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME);
	}

	/**
	 * 自动管理锁的生命周期（包含续期）
	 *
	 * @param lockKey   锁名称
	 * @param leaseTime 锁持有时间（毫秒）
	 * @param task      加锁后执行的任务
	 */
	public void executeWithLock(String lockKey, int leaseTime, Runnable task) {
		String lockValue = tryLock(lockKey, leaseTime);
		if (lockValue == null) {
			throw new RuntimeException("Failed to acquire lock: " + lockKey);
		}

		Thread renewalThread = null;
		try {
			// 启动续期线程（每leaseTime/3秒续期一次）
			renewalThread = new Thread(() -> {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						TimeUnit.SECONDS.sleep(leaseTime / 3);
						renewLease(lockKey, lockValue, leaseTime);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			});
			renewalThread.setDaemon(true);
			renewalThread.start();

			// 执行业务逻辑
			task.run();
		} finally {
			// 停止续期线程
			if (renewalThread != null && renewalThread.isAlive()) {
				renewalThread.interrupt();
			}
			unlock(lockKey, lockValue);
		}
	}
}
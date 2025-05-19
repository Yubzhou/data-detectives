// package com.yubzhou.util;
//
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.core.script.DefaultRedisScript;
// import org.springframework.data.redis.core.script.RedisScript;
// import org.springframework.stereotype.Component;
//
// import java.util.Collections;
// import java.util.UUID;
// import java.util.concurrent.TimeUnit;
//
// @Component
// @Slf4j
// public class RedisLockUtil {
//
// 	private final RedisTemplate<String, Object> redisTemplate;
//
// 	@Autowired
// 	public RedisLockUtil(RedisTemplate<String, Object> redisTemplate) {
// 		this.redisTemplate = redisTemplate;
// 	}
//
// 	private static final String LOCK_PREFIX = "lock:";  // 锁前缀
// 	private static final int DEFAULT_LEASE_TIME = 30_000;  // 默认锁持有时间30秒（毫秒）
// 	private static final long DEFAULT_WAIT_TIME = 5_000;    // 默认等待时间5秒（毫秒）
//
// 	// 解锁Lua脚本（原子操作）
// 	private static final String UNLOCK_SCRIPT =
// 			"if redis.call('get', KEYS[1]) == ARGV[1] then " +
// 					"return redis.call('del', KEYS[1]) " +
// 					"else " +
// 					"return 0 " +
// 					"end";
//
// 	// 续期Lua脚本
// 	private static final String RENEW_SCRIPT =
// 			"if redis.call('get', KEYS[1]) == ARGV[1] then " +
// 					"return redis.call('pexpire', KEYS[1], ARGV[2]) " +
// 					"else " +
// 					"return 0 " +
// 					"end";
//
// 	//------------------ 核心方法 ------------------//
//
// 	/**
// 	 * 尝试立即获取锁（非阻塞）
// 	 *
// 	 * @param lockKey   锁名称（自动添加前缀）
// 	 * @param leaseTime 锁持有时间（毫秒）
// 	 * @return 锁唯一标识（失败返回null）
// 	 */
// 	public String tryLock(String lockKey, long leaseTime) {
// 		String lockValue = UUID.randomUUID().toString();
// 		Boolean success = redisTemplate.opsForValue()
// 				.setIfAbsent(LOCK_PREFIX + lockKey, lockValue, leaseTime, TimeUnit.MILLISECONDS);
// 		return Boolean.TRUE.equals(success) ? lockValue : null;
// 	}
//
// 	/**
// 	 * 尝试获取锁（带等待时间）
// 	 *
// 	 * @param lockKey   锁名称
// 	 * @param waitTime  最大等待时间（毫秒）
// 	 * @param leaseTime 锁持有时间（毫秒）
// 	 * @return 锁唯一标识（失败返回null）
// 	 */
// 	public String tryLock(String lockKey, long waitTime, long leaseTime) {
// 		String lockValue = UUID.randomUUID().toString();
// 		long endTime = System.currentTimeMillis() + waitTime;
//
// 		while (System.currentTimeMillis() < endTime) {
// 			Boolean success = redisTemplate.opsForValue()
// 					.setIfAbsent(LOCK_PREFIX + lockKey, lockValue, leaseTime, TimeUnit.MILLISECONDS);
// 			if (Boolean.TRUE.equals(success)) {
// 				return lockValue;
// 			}
//
// 			try {
// 				// 短暂休眠后重试（避免高频请求）
// 				TimeUnit.MILLISECONDS.sleep(100);
// 			} catch (InterruptedException e) {
// 				Thread.currentThread().interrupt();
// 				break;
// 			}
// 		}
// 		return null;
// 	}
//
// 	/**
// 	 * 释放锁
// 	 *
// 	 * @param lockKey   锁名称
// 	 * @param lockValue 锁唯一标识
// 	 * @return 是否释放成功
// 	 */
// 	public boolean unlock(String lockKey, String lockValue) {
// 		if (lockValue == null || lockValue.isEmpty()) return false;
//
// 		RedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
// 		Long result = redisTemplate.execute(
// 				script,
// 				Collections.singletonList(LOCK_PREFIX + lockKey),
// 				lockValue
// 		);
// 		return result != null && result == 1L;
// 	}
//
// 	/**
// 	 * 自动续期（需在获取锁后调用）
// 	 *
// 	 * @param lockKey   锁名称
// 	 * @param lockValue 锁唯一标识
// 	 * @param leaseTime 续期时间（毫秒）
// 	 * @return 是否续期成功
// 	 */
// 	public boolean renewLease(String lockKey, String lockValue, long leaseTime) {
// 		RedisScript<Long> script = new DefaultRedisScript<>(RENEW_SCRIPT, Long.class);
// 		Long result = redisTemplate.execute(
// 				script,
// 				Collections.singletonList(LOCK_PREFIX + lockKey),
// 				lockValue,
// 				String.valueOf(leaseTime)
// 		);
// 		return result != null && result == 1L;
// 	}
//
// 	//------------------ 快捷方法 ------------------//
//
// 	/**
// 	 * 获取锁（使用默认等待时间和持有时间）
// 	 *
// 	 * @param lockKey 锁名称
// 	 * @return 锁唯一标识
// 	 */
// 	public String tryLock(String lockKey) {
// 		return tryLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME);
// 	}
//
// 	/**
// 	 * 自动管理锁的生命周期（包含续期）
// 	 *
// 	 * @param lockKey   锁名称
// 	 * @param leaseTime 锁持有时间（毫秒）
// 	 * @param task      加锁后执行的任务
// 	 */
// 	public void executeWithLock(String lockKey, long leaseTime, Runnable task) {
// 		String lockValue = tryLock(lockKey, leaseTime);
// 		if (lockValue == null) {
// 			throw new RuntimeException("Failed to acquire lock: " + lockKey);
// 		}
//
// 		Thread renewalThread = null;
// 		try {
// 			// 启动续期线程（每leaseTime/3续期一次）
// 			renewalThread = new Thread(() -> {
// 				while (!Thread.currentThread().isInterrupted()) {
// 					try {
// 						TimeUnit.MILLISECONDS.sleep(leaseTime / 3);
// 						renewLease(lockKey, lockValue, leaseTime);
// 					} catch (InterruptedException e) {
// 						Thread.currentThread().interrupt();
// 					}
// 				}
// 			});
// 			renewalThread.setDaemon(true);
// 			renewalThread.start();
//
// 			// 执行业务逻辑
// 			task.run();
// 		} finally {
// 			// 停止续期线程
// 			if (renewalThread != null && renewalThread.isAlive()) {
// 				renewalThread.interrupt();
// 			}
// 			unlock(lockKey, lockValue);
// 		}
// 	}
// }

package com.yubzhou.util;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class RedisLockUtil {

	private final RedisTemplate<String, Object> redisTemplate;

	@Autowired
	public RedisLockUtil(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// 共享线程池用于续期任务
	private static final ScheduledExecutorService RENEWAL_SCHEDULER = Executors.newScheduledThreadPool(4);

	private static final String LOCK_PREFIX = "lock:";  // 锁前缀
	private static final int DEFAULT_LEASE_TIME = 30_000;  // 默认锁持有时间（毫秒）
	private static final long DEFAULT_WAIT_TIME = 5_000;    // 默认等待时间（毫秒）
	private static final long MIN_SLEEP_TIME = 50; // 最小休眠时间（毫秒）
	private static final long MAX_SLEEP_TIME = 1000; // 最大休眠时间（毫秒）

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
					"return redis.call('pexpire', KEYS[1], ARGV[2]) " +
					"else " +
					"return 0 " +
					"end";

	// 销毁线程池
	@PreDestroy
	public void destroy() {
		RENEWAL_SCHEDULER.shutdown();
		try {
			if (!RENEWAL_SCHEDULER.awaitTermination(5, TimeUnit.SECONDS)) {
				RENEWAL_SCHEDULER.shutdownNow();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	//------------------ 核心方法 ------------------//

	/**
	 * 尝试获取锁，立即返回结果
	 *
	 * @param lockKey   锁名称
	 * @param leaseTime 锁持有时间（毫秒）
	 * @return 锁标识，失败返回null
	 */
	public String tryLock(String lockKey, long leaseTime) {
		validateLeaseTime(leaseTime);
		String lockValue = UUID.randomUUID().toString();
		Boolean success = redisTemplate.opsForValue()
				.setIfAbsent(LOCK_PREFIX + lockKey, lockValue, leaseTime, TimeUnit.MILLISECONDS);
		return Boolean.TRUE.equals(success) ? lockValue : null;
	}

	/**
	 * 尝试获取锁，支持等待时间和退避策略
	 *
	 * @param lockKey   锁名称
	 * @param waitTime  最大等待时间（毫秒）
	 * @param leaseTime 锁持有时间（毫秒）
	 * @return 锁标识，失败返回null
	 */
	public String tryLock(String lockKey, long waitTime, long leaseTime) {
		validateWaitAndLeaseTime(waitTime, leaseTime);
		String lockValue = UUID.randomUUID().toString();
		long endTime = System.currentTimeMillis() + waitTime;
		long currentSleepTime = MIN_SLEEP_TIME;
		ThreadLocalRandom random = ThreadLocalRandom.current();

		while (System.currentTimeMillis() < endTime) {
			Boolean success = redisTemplate.opsForValue()
					.setIfAbsent(LOCK_PREFIX + lockKey, lockValue, leaseTime, TimeUnit.MILLISECONDS);
			if (Boolean.TRUE.equals(success)) {
				return lockValue;
			}

			// 使用随机退避避免活锁
			long sleepTime = Math.min(
					currentSleepTime + random.nextLong(currentSleepTime),
					MAX_SLEEP_TIME
			);
			sleepTime = Math.min(sleepTime, endTime - System.currentTimeMillis());

			if (sleepTime <= 0) break;

			try {
				TimeUnit.MILLISECONDS.sleep(sleepTime);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
			currentSleepTime = Math.min(currentSleepTime * 2, MAX_SLEEP_TIME);
		}
		return null;
	}

	/**
	 * 释放锁
	 *
	 * @param lockKey   锁名称
	 * @param lockValue 锁标识
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
	 * @param leaseTime 续期时间（毫秒）
	 * @return 是否续期成功
	 */
	public boolean renewLease(String lockKey, String lockValue, long leaseTime) {
		validateLeaseTime(leaseTime);
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
	public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, Callable<T> task) throws Exception {
		validateLeaseTime(leaseTime);
		String lockValue = tryLock(lockKey, waitTime, leaseTime);
		if (lockValue == null) {
			throw new RuntimeException("Failed to acquire lock: " + lockKey);
		}

		// 创建一个原子布尔值，用于标记锁是否保持
		AtomicBoolean isLockHeld = new AtomicBoolean(true);
		ScheduledFuture<?> renewalFuture = null;

		try {
			// 启动续期任务，首次延迟为leaseTime的1/3，之后固定间隔
			renewalFuture = RENEWAL_SCHEDULER.scheduleAtFixedRate(() -> {
				if (!isLockHeld.get()) {
					return;
				}
				if (!renewLease(lockKey, lockValue, leaseTime)) {
					log.error("锁续期失败，锁可能已失效: {}", lockKey);
					isLockHeld.set(false); // 标记锁失效
					throw new RuntimeException("Lock renewal failed for " + lockKey);
				}
			}, leaseTime / 3, leaseTime / 3, TimeUnit.MILLISECONDS);

			return task.call();
		} catch (Exception e) {
			log.error("执行任务时发生异常", e);
			throw e;
		} finally {
			if (renewalFuture != null) {
				renewalFuture.cancel(true);
			}
			isLockHeld.set(false);
			unlock(lockKey, lockValue);
		}
	}

	/**
	 * 自动管理锁的生命周期（包含续期）
	 *
	 * @param lockKey   锁名称
	 * @param leaseTime 锁持有时间（毫秒）
	 * @param task      加锁后执行的任务
	 */
	public void executeWithLock(String lockKey, long waitTime, long leaseTime, Runnable task) {
		try {
			executeWithLock(lockKey, waitTime, leaseTime, () -> {
				task.run();
				return null;
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void validateLeaseTime(long leaseTime) {
		if (leaseTime <= 0) {
			throw new IllegalArgumentException("Lease time must be positive");
		}
	}

	private void validateWaitAndLeaseTime(long waitTime, long leaseTime) {
		if (waitTime <= 0 || leaseTime <= 0) {
			throw new IllegalArgumentException("Wait time and lease time must be positive");
		}
	}
}
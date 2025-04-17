package com.yubzhou.common;

public class RedisConstant {
	// 以下全部时间单位均为秒


	// ===================== sms redis key =================================

	// 存储refreshToken的前缀
	public static final String USER_REFRESH_TOKEN_PREFIX = "user:refreshToken:";
	// refreshToken有效期
	public static final long USER_REFRESH_TOKEN_EXPIRE_TIME = 5 * 24 * 60 * 60; // 5天


	// 短信验证码限流的前缀（一分钟之内只能发送一次短信验证码）
	public static final String SMS_LIMIT_PREFIX = "login:sms_limit:";
	public static final long SMS_LIMIT_EXPIRE_TIME = 60; // 60秒


	// 存储短信验证码的前缀
	public static final String SMS_CAPTCHA_PREFIX = "login:sms_captcha:";
	// 短信验证码有效期
	public static final long SMS_CAPTCHA_EXPIRE_TIME = 5 * 60; // 5分钟


	// SMS每日限流的前缀
	public static final String SMS_DAILY_LIMIT_PREFIX = "login:sms_daily_limit:";
	// SMS每日限流的有效期
	public static final long SMS_DAILY_LIMIT_EXPIRE_TIME = 24 * 60 * 60; // 1天


	// 登录锁的前缀（检查账号是否被锁定（验证码错误次数限制，如5次错误后锁定1小时））
	public static final String SMS_LOCK_PREFIX = "login:sms_lock:";
	// 登录锁的有效期
	public static final long SMS_LOCK_EXPIRE_TIME = 60 * 60; // 1小时
	// 短信验证码错误次数的前缀
	public static final String SMS_ERROR_COUNT_PREFIX = "login:sms_error_count:";


	// ===================== hot news redis key =================================
	// 新闻锁前缀
	public static final String NEWS_LOCK_PREFIX = "news:lock:";

	// 新闻分类锁前缀
	public static final String NEWS_CATEGORY_LOCK_PREFIX = "news:category:lock:";

	// 新闻元数据（记录当前新闻的最小ID和最大ID）
	public static final String NEWS_META = "news:meta";

	// 新闻详情的前缀
	public static final String NEWS_DETAIL_PREFIX = "news:detail:";

	// 新闻分类信息前缀
	public static final String NEWS_CATEGORY_META = "news:category:meta";

	// 新闻-分类关联
	public static final String NEWS_CATEGORY_SET_PREFIX = "news:category:set:";

	// 保存每1小时热点新闻
	public static final String HOT_NEWS_HOUR_PREFIX = "news:hot:1h:";
	public static final String HOT_NEWS_HOUR_MERGED_PREFIX = HOT_NEWS_HOUR_PREFIX + "merged:";

	// 保存每天热点新闻
	public static final String HOT_NEWS_DAY = "news:hot:1d";
	public static final String HOT_NEWS_DAY_PREFIX = HOT_NEWS_DAY + ":";

	// 最近24小时热点新闻
	public static final String HOT_NEWS_24HOUR = "news:hot:24h";
	public static final String HOT_NEWS_24HOUR_PREFIX = HOT_NEWS_24HOUR + ":";
	public static final String HOT_NEWS_24HOUR_CACHE_TOP10 = HOT_NEWS_24HOUR_PREFIX + "cache_top10";
	public static final String HOT_NEWS_24HOUR_MERGED_PREFIX = HOT_NEWS_24HOUR_PREFIX + "merged:";

	// 最近7天热点新闻
	public static final String HOT_NEWS_WEEK = "news:hot:7d";
	public static final String HOT_NEWS_WEEK_PREFIX = HOT_NEWS_WEEK + ":";
	public static final String HOT_NEWS_WEEK_CACHE_TOP10 = HOT_NEWS_WEEK_PREFIX + "cache_top10";

	// 更新用户新闻行为记录（即用户对某一新闻是否进行了支持、反对、收藏等操作，以及取消支持、取消反对、取消收藏等操作）
	public static final String USER_NEWS_ACTION_PREFIX = "news:user_action:";

	// 记录用户对某一评论是否点赞过
	public static final String USER_COMMENT_LIKE_PREFIX = "news:comment:like:";
	public static final long USER_COMMENT_LIKE_EXPIRE_TIME = 3 * 60 * 60; // 3小时

	// 推荐新闻的前缀
	public static final String NEWS_RECOMMEND_PREFIX = "news:recommend:";
	public static final long NEWS_RECOMMEND_EXPIRE_TIME = 2 * 60 * 60; // 2小时
}

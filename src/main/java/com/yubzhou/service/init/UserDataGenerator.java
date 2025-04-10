package com.yubzhou.service.init;

import com.yubzhou.model.po.User;
import com.yubzhou.model.po.UserProfile;
import com.yubzhou.service.UserProfileService;
import com.yubzhou.service.UserService;
import com.yubzhou.util.BCryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDataGenerator {

	private final UserService userService;
	private final UserProfileService userProfileService;

	private static final Random RANDOM = new Random();

	private static final String NICKNAMES = "虚空织梦者,星核游魂,焰尾龙鳞,月蚀回声,暗渊歌姬,碎镜旅人,沸腾的星芒,神谕解谜人,空间褶皱猎人,焦糖黑洞,虚妄之瞳,时砂盗贼,碎神之子," +
			"沉默的星鲸,焰火炼金师,雾隐回廊主," +
			"暗河星骸," +
			"碎光织网者,沸腾的虚空,碳基幻象师,以太捕手,虚空鲸歌者,暗物质裁缝,时之锈蚀者,焰尾星图师,碎镜迷宫主,沸腾的星核,神谕解谜人,空间褶皱猎人,焦糖黑洞,虚妄之瞳,时砂盗贼,碎神之子,沉默的星鲸,焰火炼金师," +
			"雾隐回廊主,暗河星骸,碎光织网者,沸腾的虚空,碳基幻象师,以太捕手,虚空鲸歌者,暗物质裁缝,时之锈蚀者,焰尾星图师,碎镜迷宫主,沸腾的星核,神谕解谜人,空间褶皱猎人,焦糖黑洞,虚妄之瞳,时砂盗贼,碎神之子," +
			"沉默的星鲸,焰火炼金师,雾隐回廊主,暗河星骸,碎光织网者,沸腾的虚空,碳基幻象师,以太捕手,虚空鲸歌者,暗物质裁缝,时之锈蚀者,焰尾星图师,碎镜迷宫主,沸腾的星核,神谕解谜人,空间褶皱猎人,焦糖黑洞,虚妄之瞳," +
			"时砂盗贼,碎神之子,沉默的星鲸,焰火炼金师,雾隐回廊主,暗河星骸,碎光织网者,沸腾的虚空,碳基幻象师,以太捕手,虚空鲸歌者,暗物质裁缝,时之锈蚀者,焰尾星图师,碎镜迷宫主,沸腾的星核,神谕解谜人,空间褶皱猎人,焦糖黑洞," +
			"虚妄之瞳,时砂盗贼,碎神之子,沉默的星鲸,焰火炼金师,雾隐回廊主,暗河星骸,碎光织网者,沸腾的虚空,碳基幻象师,以太捕手,虚空鲸歌者,暗物质裁缝,时之锈蚀者,焰尾星图师,碎镜迷宫主,沸腾的星核,星海拾荒者,雾隐者,月光编织者," +
			"虚妄之眼,时空褶皱,焦糖量子,薄荷星云,暗流摆渡人,碎光调酒师,云端驯风者,碳基诗人,沸腾的代码,棉花糖黑洞,噢麦拉捕手,银河碎瓷匠,碎纸机骑士,青铜齿轮守望者,焦糖玛奇朵星舰长,暗河星尘收集者,云端鲸鱼画家,糖霜画师骑士," +
			"猫尾巷拾荒者,暗夜萤火虫诗人,青铜玫瑰捕手,碎光编织者骑士,沸腾的星空船长,棉花糖风暴守夜人," +
			"焦糖布丁摆渡人,暗河星屑收集者,银河碎语者,碎星守望者骑士,沸腾的月亮邮差,云端鲸鱼画家,糖霜猫,暗夜萤火虫骑士,青铜齿轮守夜人,碎光收藏家,沸腾的彩虹船,棉花糖火箭手,焦糖玛奇朵碎纸机,暗河星尘旅人," +
			"银河捕梦者,碎纸机诗人,沸腾的墨水瓶,云端杂货铺,糖果邮差,猫头鹰守夜人,暗河摆渡人,碎星收集者,焦糖布丁骑士,棉花糖火箭,银河拾荒者,碎纸机骑士,沸腾的月亮,云端鲸鱼,糖霜画师,暗夜萤火虫,青铜齿轮,碎光收藏家,沸腾的彩虹,棉花糖云朵,焦糖松饼骑士," +
			"暗河摆渡人,银河碎屑,碎星守望者,沸腾的书页,云端邮差,糖果猫头鹰,猫尾巷诗人,暗夜拾荒者,青铜玫瑰,碎光编织者,沸腾的星空,棉花糖风暴骑士,焦糖玛奇朵守夜人,暗河星尘,银河捕梦者,碎纸机骑士,沸腾的墨水,云端鲸鱼诗人,糖霜猫," +
			"暗夜萤火虫骑士,青铜齿轮守夜人,碎光收藏家,沸腾的彩虹船,棉花糖火箭手,焦糖布丁摆渡人,暗河星屑收集者,银河碎语者,碎星守望者骑士,沸腾的月亮邮差,云端鲸鱼画家,糖霜画师骑士,猫尾巷拾荒者,暗夜萤火虫诗人,青铜玫瑰捕手,碎光编织者骑士," +
			"沸腾的星空船长,棉花糖风暴守夜人,焦糖玛奇朵碎纸机，星尘旅人,墨影狐,雪鸮,逐风者,萤火之森,暗夜蔷薇,沸腾的茶包,咕噜博士,糖果海盗,碎月骑士,棉花云,焦糖布丁,银河邮差,嗷呜猫,炽焰蝶,青铜鹿,云端摆渡人,糖霜独角兽,碎纸机,暗河灯," +
			"碎光诗人,沸腾的咖啡杯,云朵裁缝,焦糖松饼,猫尾巷,暗夜萤火虫,银河捕手,沸腾的书本,碎纸机,棉花糖风暴,焦糖玛奇朵,暗夜拾荒者,青铜铃铛,碎光织梦者,沸腾的墨水瓶,云端杂货铺,糖果邮差,猫头鹰守夜人,暗河摆渡人,碎星收集者,焦糖布丁骑士," +
			"棉花糖火箭,银河拾荒者,碎纸机诗人,沸腾的月亮,云端鲸鱼,糖霜画师,暗夜萤火虫,青铜齿轮,碎光收藏家,沸腾的彩虹,棉花糖云朵,焦糖松饼骑士,暗河摆渡人,银河碎屑,碎星守望者,沸腾的书页,云端邮差,糖果猫头鹰,猫尾巷诗人,暗夜拾荒者,青铜玫瑰," +
			"碎光编织者,沸腾的星空,棉花糖风暴骑士,焦糖玛奇朵守夜人,暗河星尘,银河捕梦者,碎纸机骑士,沸腾的墨水,云端鲸鱼诗人,糖霜猫,暗夜萤火虫骑士,青铜齿轮守夜人,碎光收藏家,沸腾的彩虹船,棉花糖火箭手,焦糖布丁摆渡人,暗河星屑收集者,银河碎语者," +
			"碎星守望者骑士,沸腾的月亮邮差,云端鲸鱼画家,糖霜画师骑士,猫尾巷拾荒者,暗夜萤火虫诗人,青铜玫瑰捕手,碎光编织者骑士,沸腾的星空船长,棉花糖风暴守夜人,焦糖玛奇朵碎纸机";

	public static String generatePhoneNumber() {
		StringBuilder sb = new StringBuilder();

		// 第一位固定为 1
		sb.append('1');

		// 第二位随机生成 3-9 之间的数字
		int secondDigit = RANDOM.nextInt(7) + 3; // 3到9（包含）
		sb.append(secondDigit);

		// 剩下的 9 位随机生成 0-9 的数字
		for (int i = 0; i < 9; i++) {
			sb.append(RANDOM.nextInt(10));
		}

		return sb.toString();
	}

	public void batchInsertUserAndProfile() {
		String[] nicknames = NICKNAMES.split(",");
		User firstUser = batchInsertUser(nicknames.length);
		batchInsertUserProfile(nicknames, firstUser.getId(), firstUser.getId() + nicknames.length);
	}

	private User batchInsertUser(int size) {
		Map<String, User> userMap = new HashMap<>();
		String password = "zyb123456";
		String phone;
		for (int i = 0; i < size; i++) {
			phone = generatePhoneNumber();
			User user = new User();
			user.setPhone(phone);
			user.setPassword(BCryptUtil.encode(password));
			userMap.putIfAbsent(phone, user);
		}
		List<User> users = userMap.values().stream().toList();
		userService.saveBatch(users, 500);
		log.info("批量插入用户成功, 用户数量:{}", userMap.size());
		log.info("firstUser: {}", users.get(0));
		return users.get(0);
	}

	private void batchInsertUserProfile(String[] nicknames, long minId, long maxId) {
		List<UserProfile> userProfiles = new ArrayList<>();
		for (long i = minId; i < maxId; i++) {
			UserProfile userProfile = new UserProfile();
			userProfile.setUserId(i);
			userProfile.setNickname(nicknames[(int) (i - minId)]);
			userProfile.setGender((short) RANDOM.nextInt(3));
			userProfiles.add(userProfile);
		}
		userProfileService.saveBatch(userProfiles, 500);
		log.info("批量插入用户信息成功, 用户信息数量:{}", userProfiles.size());
	}
}
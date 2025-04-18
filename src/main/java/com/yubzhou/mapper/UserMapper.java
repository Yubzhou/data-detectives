package com.yubzhou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yubzhou.common.MinAndMaxId;
import com.yubzhou.model.po.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {
	// 获取最小和最大ID
	@Select("SELECT MIN(`id`) AS `minId`, MAX(`id`) AS `maxId` FROM `users`")
	MinAndMaxId getMinAndMaxId();
}

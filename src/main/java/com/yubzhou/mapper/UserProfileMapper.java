package com.yubzhou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yubzhou.model.po.UserProfile;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

public interface UserProfileMapper extends BaseMapper<UserProfile> {
	@Select("SELECT DISTINCT avatar_url FROM user_profiles")
	Set<String> selectDistinctAvatarUrls();
}

package com.yubzhou.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// 在插入或更新的时候自动填充时间字段
@Component // 确保被Spring管理
public class AutoFillHandler implements MetaObjectHandler {

	@Override
	public void insertFill(MetaObject metaObject) {
		// 插入时填充创建时间和更新时间
		// fieldName为实体类的字段名
		this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
		this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		// 更新时填充更新时间
		this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
	}
}
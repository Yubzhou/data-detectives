CREATE DATABASE IF NOT EXISTS `data_detectives`;
USE `data_detectives`;

CREATE TABLE IF NOT EXISTS `user`
(
    `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone`         VARCHAR(20)      NOT NULL COMMENT '手机号（格式：13812345678）',
    `password`      VARCHAR(60)      NOT NULL COMMENT 'BCrypt加密后的密码，固定长度为60',
    `role`          TINYINT UNSIGNED NOT NULL DEFAULT 2 COMMENT '用户角色（0:超级管理员，1:管理员，2:用户）',
    `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '账号状态（0:禁用，1:正常，2:锁定，3:注销）',
    `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_at` DATETIME         NULL     DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `phone_unique` (`phone`)
) COMMENT '用户表';

CREATE TABLE IF NOT EXISTS `user_profile`
(
    `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '用户信息ID',
    `user_id`           BIGINT UNSIGNED  NOT NULL COMMENT '用户ID',
    `nickname`          VARCHAR(50)      NOT NULL DEFAULT '匿名用户' COMMENT '昵称',
    `gender`            TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '性别（0:未知，1:男，2:女）',
    `avatar_url`        VARCHAR(255)     NULL     DEFAULT NULL COMMENT '头像url（只存储相对地址，即不带http://localhost:8080）',
    `interested_fields` VARCHAR(50)      NULL     DEFAULT NULL COMMENT '感兴趣领域（多个领域用逗号分隔）',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) COMMENT '用户信息表';

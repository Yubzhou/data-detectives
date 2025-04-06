CREATE DATABASE IF NOT EXISTS `data_detectives`;
USE `data_detectives`;

CREATE TABLE IF NOT EXISTS `users`
(
    `id`            BIGINT UNSIGNED AUTO_INCREMENT COMMENT '用户ID',
    `phone`         VARCHAR(20)      NOT NULL COMMENT '手机号（格式：13812345678）',
    `password`      VARCHAR(60)      NOT NULL COMMENT 'BCrypt加密后的密码，固定长度为60',
    `role`          TINYINT UNSIGNED NOT NULL DEFAULT 2 COMMENT '用户角色（0:超级管理员，1:管理员，2:用户）',
    `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '账号状态（0:禁用，1:正常，2:锁定，3:注销）',
    `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_at` DATETIME         NULL     DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_phone` (`phone`)
) COMMENT '用户表';

CREATE TABLE IF NOT EXISTS `user_profiles`
(
    `id`                BIGINT UNSIGNED AUTO_INCREMENT COMMENT '用户信息ID',
    `user_id`           BIGINT UNSIGNED  NOT NULL COMMENT '用户ID',
    `nickname`          VARCHAR(50)      NOT NULL DEFAULT '匿名用户' COMMENT '昵称',
    `gender`            TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '性别（0:未知，1:男，2:女）',
    `avatar_url`        VARCHAR(255)     NULL     DEFAULT NULL COMMENT '头像url（只存储相对地址，即不带http://localhost:8080）',
    `interested_fields` VARCHAR(50)      NULL     DEFAULT NULL COMMENT '感兴趣领域（多个领域用逗号分隔）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_user` (`user_id`)
) COMMENT '用户信息表';

-- 检测记录表（带复合索引）
CREATE TABLE IF NOT EXISTS detection_records
(
    id               BIGINT UNSIGNED AUTO_INCREMENT COMMENT '检测ID',
    user_id          BIGINT UNSIGNED                 NOT NULL COMMENT '用户ID',
    title            VARCHAR(255)                    NOT NULL COMMENT '检测标题',
    content          TEXT                            NOT NULL COMMENT '检测内容',
    source_url       VARCHAR(512)                    NULL COMMENT '原文链接',
    detection_result ENUM ('真实','部分真实','虚假') NOT NULL COMMENT '检测结论',
    result_details   TEXT                            NOT NULL COMMENT '详细分析',
    accuracy         DECIMAL(5, 2)                   NOT NULL COMMENT '准确率',
    created_at       DATETIME                        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    is_favorite      TINYINT UNSIGNED                NOT NULL DEFAULT 0 COMMENT '收藏状态（0:未收藏，1:已收藏）',
    PRIMARY KEY (id),
    INDEX idx_user_result_time (user_id, created_at, detection_result) USING BTREE COMMENT '用户id+时间+检测结论联合索引'
    # FULLTEXT INDEX idx_content_search (title, content) COMMENT '全文检索索引'
) COMMENT ='检测记录表';

-- 新闻表（存储核心新闻数据）
CREATE TABLE IF NOT EXISTS news
(
    id         BIGINT UNSIGNED AUTO_INCREMENT COMMENT '新闻ID',
    title      VARCHAR(200) NOT NULL COMMENT '新闻标题',
    content    TEXT         NOT NULL COMMENT '新闻内容',
    views      INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览量',
    supports   INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '支持数',
    opposes    INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '反对数',
    comments   INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
    favorites  INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
    version    INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '版本号（用于乐观锁更新）',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_time (created_at DESC)
) COMMENT '新闻表';

-- 新闻分类表
CREATE TABLE IF NOT EXISTS categories
(
    id   INT UNSIGNED AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(20) NOT NULL COMMENT '分类名称',
    PRIMARY KEY (id),
    UNIQUE INDEX uniq_name (name)
) COMMENT '新闻分类表';

-- 新闻-分类关联表（多对多关系）
CREATE TABLE IF NOT EXISTS news_categories
(
    news_id     INT UNSIGNED NOT NULL COMMENT '新闻ID',
    category_id INT UNSIGNED NOT NULL COMMENT '分类ID',
    PRIMARY KEY (news_id, category_id)
) COMMENT '新闻-分类关联表';

-- 评论表（支持互动数据）
CREATE TABLE IF NOT EXISTS comments
(
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    user_id    BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    news_id    BIGINT UNSIGNED NOT NULL COMMENT '新闻ID',
    content    VARCHAR(500)    NOT NULL COMMENT '评论内容（最大长度500）',
    likes      INT UNSIGNED DEFAULT 0 COMMENT '点赞数',
    dislikes   INT UNSIGNED DEFAULT 0 COMMENT '点踩数',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_news (news_id),
    INDEX idx_time (created_at DESC)
) COMMENT '评论表';

-- 用户-新闻关联表（多对多关系）
CREATE TABLE IF NOT EXISTS user_news
(
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    news_id BIGINT UNSIGNED NOT NULL COMMENT '新闻ID',
    PRIMARY KEY (user_id, news_id)
) COMMENT '用户-新闻关联表';
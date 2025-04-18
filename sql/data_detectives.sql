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
    `avatar_url`        VARCHAR(255)     NOT NULL DEFAULT '/default/default_avatar.png' COMMENT '头像url（只存储相对地址，即不带http://localhost:8080）',
    `interested_fields` VARCHAR(50)      NULL     DEFAULT NULL COMMENT '感兴趣领域（多个领域用逗号分隔）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_user` (`user_id`)
) COMMENT '用户信息表';

-- 设置用户头像为默认头像（/default/default_avatar.png）
# UPDATE `user_profiles` SET `avatar_url` = '/default/default_avatar.png' WHERE `avatar_url` IS NULL;

-- 检测记录表（带复合索引）
CREATE TABLE IF NOT EXISTS `detection_records`
(
    `id`                    BIGINT UNSIGNED AUTO_INCREMENT COMMENT '检测ID',
    `user_id`               BIGINT UNSIGNED  NOT NULL COMMENT '用户ID',
    `content`               TEXT             NOT NULL COMMENT '检测内容',
    `detection_result`      TINYINT UNSIGNED NOT NULL COMMENT '检测结论（0：虚假，1：真实）',
    `reliability`           DECIMAL(4, 1)    NOT NULL COMMENT '可信度（直接存去除百分号%的数，即89%直接存89）',
    `text_analysis`         TEXT             NOT NULL COMMENT '文本描述分析结果',
    `common_sense_analysis` TEXT             NOT NULL COMMENT '常识推理分析结果',
    `detection_type`        TINYINT UNSIGNED NOT NULL COMMENT '检测类型（0: 高效率模式，1: 高精度模式）',
    `news_category`         VARCHAR(25)      NULL COMMENT '新闻分类名称（如政治、经济、科技等）',
    `favorite`              TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏状态（0:未收藏，1:已收藏）',
    `created_at`            DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    `detection_date`        DATE AS (DATE(`created_at`)) VIRTUAL COMMENT '虚拟生成列',
    PRIMARY KEY (`id`),
    INDEX `idx_user_time_type_result` (`user_id`, `created_at`, `detection_type`, `detection_result`) COMMENT
        '用户id+时间+检测类型+检测结论联合索引',
    INDEX `idx_user_date` (`user_id`, `detection_date` DESC)
) COMMENT ='检测记录表';

SHOW CREATE TABLE `detection_records`;


# ALTER TABLE `detection_records`
#     ADD COLUMN `detection_date` DATE AS (DATE(`created_at`)) VIRTUAL,
#     ADD INDEX `idx_user_date_desc` (`user_id`, `detection_date` DESC);
#
# ALTER TABLE `detection_records`
#     DROP INDEX `idx_user_date`,
#     ADD INDEX `idx_user_date_desc` (`user_id`, `detection_date` DESC);

# -- 统计用户的总检测次数和最长连续检测天数（基于用户变量）
# SELECT
#     -- 直接统计总记录数（不去重）
#     (SELECT COUNT(*) FROM `detection_records` WHERE `user_id` = 4) AS `total_detections`,
#     -- 计算最长连续天数（需去重日期）
#     IFNULL(MAX(`cont_days`), 0)                                    AS `max_continuous_days`
# FROM (SELECT `grp`,
#              COUNT(*) AS `cont_days`
#       FROM (SELECT `detection_date`,
#                    @`grp` := IF(
#                            DATEDIFF(`detection_date`, @`prev_date`) = 1 AND @`prev_user` = `user_id`,
#                            @`grp`,
#                            @`grp` + 1
#                              ) AS `grp`,
#                    @`prev_date` := `detection_date`,
#                    @`prev_user` := `user_id`
#             FROM (SELECT DISTINCT `user_id`, `detection_date`
#                   FROM `detection_records`
#                   WHERE `user_id` = 4) AS `dedup_dates`, -- 先对日期去重
#                  (SELECT @`prev_date` := NULL, @`prev_user` := NULL, @`grp` := 0) `vars`
#             ORDER BY `user_id`, `detection_date`) AS `t`
#       GROUP BY `grp`) AS `t2`;


-- 统计用户的总检测次数和最长连续检测天数（基于窗口函数）
SELECT (SELECT COUNT(*) FROM `detection_records` WHERE `user_id` = 4) AS `total_detections`,
       IFNULL(MAX(`cont_days`), 0)                                    AS `max_continuous_days`
FROM (SELECT COUNT(*) AS `cont_days`
      FROM (SELECT `user_id`,
                   `detection_date`,
                   DATE_SUB(`detection_date`, INTERVAL ROW_NUMBER() OVER (ORDER BY `detection_date`) DAY) AS `grp`
            FROM (SELECT DISTINCT `user_id`, `detection_date`
                  FROM `detection_records`
                  WHERE `user_id` = 4) AS `dedup`) AS `t`
      GROUP BY `grp`) AS `t2`;


-- 新闻表（存储核心新闻数据）
CREATE TABLE IF NOT EXISTS `news`
(
    `id`         BIGINT UNSIGNED AUTO_INCREMENT COMMENT '新闻ID',
    `title`      VARCHAR(200) NOT NULL COMMENT '新闻标题',
    `content`    TEXT         NOT NULL COMMENT '新闻内容',
    `cover_url`  VARCHAR(255) NULL     DEFAULT NULL COMMENT '封面url（只存储相对地址，即不带http://localhost:8080）',
    `views`      INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览量',
    `supports`   INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '支持数',
    `opposes`    INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '反对数',
    `comments`   INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
    `favorites`  INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
    `version`    INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '版本号（用于乐观锁更新）',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_time_desc` (`created_at` DESC),
    FULLTEXT INDEX `ft_title` (`title`) WITH PARSER ngram -- 启用ngram分词，支持中文
) COMMENT '新闻表';

-- 根据标题搜索新闻，按相关性排序，并限制结果数量
# SELECT *,
#        MATCH(`title`) AGAINST('中国' IN NATURAL LANGUAGE MODE) AS `relevance_score` -- 计算相关性得分
# FROM `news`
# WHERE MATCH(`title`) AGAINST('中国' IN NATURAL LANGUAGE MODE) -- 过滤匹配项
# ORDER BY `relevance_score` DESC -- 按相关性得分降序
# LIMIT 10;

-- 为title创建全文索引并且使用ngram全文解析器进行分词
-- CREATE FULLTEXT INDEX ft_title ON data_detectives.news (title) WITH PARSER `ngram`;

-- 查看全文检索配置
-- show variables like '%token%';

-- 新闻分类表
CREATE TABLE IF NOT EXISTS `categories`
(
    `id`   INT UNSIGNED AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(20) NOT NULL COMMENT '分类名称',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uniq_name` (`name`)
) COMMENT '新闻分类表';

-- 新闻-分类关联表（多对多关系）
CREATE TABLE IF NOT EXISTS `news_categories`
(
    `news_id`     BIGINT UNSIGNED NOT NULL COMMENT '新闻ID',
    `category_id` INT UNSIGNED    NOT NULL COMMENT '分类ID',
    PRIMARY KEY (`news_id`, `category_id`)
) COMMENT '新闻-分类关联表';

-- 评论表（支持互动数据）
CREATE TABLE IF NOT EXISTS `comments`
(
    `id`         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `news_id`    BIGINT UNSIGNED NOT NULL COMMENT '新闻ID',
    `comment`    VARCHAR(500)    NOT NULL COMMENT '评论内容（最大长度500）',
    `likes`      INT UNSIGNED DEFAULT 0 COMMENT '点赞数',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    -- 用户中心我的评论里面按时间排序
    INDEX `idx_user_created_desc` (`user_id`, `created_at` DESC),
    -- 用户中心我的评论里面按点赞排序
    INDEX `idx_user_likes_desc` (`user_id`, `likes` DESC),
    -- 新闻详情页评论列表按时间排序
    INDEX `idx_news_created_desc` (`news_id`, `created_at` DESC),
    -- 新闻详情页评论列表按点赞排序
    INDEX `idx_news_likes_desc` (`news_id`, `likes` DESC)
) COMMENT '评论表';

-- 当新闻的反对数为0时，删除全部该新闻的评论
DELETE
FROM `comments`
WHERE `news_id` IN (SELECT `id` FROM `news` WHERE `opposes` = 0);
-- 并且也更新对应的评论数为0
UPDATE `news`
SET `comments` = 0
WHERE `opposes` = 0;


SELECT COUNT(*)
FROM `comments`
WHERE `news_id` = 70;

-- 将新闻的评论数更新到新闻表中
UPDATE `news` `n`
SET `n`.`comments` = (SELECT COUNT(*)
                      FROM `comments` `c`
                      WHERE `c`.`news_id` = `n`.`id`);

-- 删除用户id大于394的评论
DELETE FROM `comments` WHERE `user_id` > 394;


-- 用户-新闻关联表（多对多关系）
CREATE TABLE IF NOT EXISTS `user_news`
(
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `news_id` BIGINT UNSIGNED NOT NULL COMMENT '新闻ID',
    PRIMARY KEY (`user_id`, `news_id`)
) COMMENT '用户-新闻关联表';
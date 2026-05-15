CREATE DATABASE IF NOT EXISTS `my_blog`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `my_blog`;

CREATE TABLE IF NOT EXISTS `blog_article` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Article ID',
  `title` VARCHAR(200) NOT NULL COMMENT 'Article title',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT 'Article summary',
  `content` LONGTEXT NOT NULL COMMENT 'Article content',
  `cover` VARCHAR(500) DEFAULT NULL COMMENT 'Cover image URL',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT 'View count',
  `is_top` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Top flag: 0 no, 1 yes',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Status: 0 hidden, 1 published',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  KEY `idx_blog_article_status` (`status`),
  KEY `idx_blog_article_is_top` (`is_top`),
  KEY `idx_blog_article_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article table';

CREATE TABLE IF NOT EXISTS `blog_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Tag ID',
  `name` VARCHAR(50) NOT NULL COMMENT 'Tag name',
  `color` VARCHAR(30) DEFAULT NULL COMMENT 'Tag color',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blog_tag_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tag table';

CREATE TABLE IF NOT EXISTS `blog_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Category ID',
  `name` VARCHAR(50) NOT NULL COMMENT 'Category name',
  `sort` INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blog_category_name` (`name`),
  KEY `idx_blog_category_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Category table';

DROP PROCEDURE IF EXISTS `add_blog_article_category_column`;
DELIMITER //
CREATE PROCEDURE `add_blog_article_category_column`()
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'blog_article'
      AND COLUMN_NAME = 'category_id'
  ) THEN
    ALTER TABLE `blog_article` ADD COLUMN `category_id` BIGINT DEFAULT NULL COMMENT 'Category ID' AFTER `cover`;
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'blog_article'
      AND INDEX_NAME = 'idx_blog_article_category_id'
  ) THEN
    ALTER TABLE `blog_article` ADD INDEX `idx_blog_article_category_id` (`category_id`);
  END IF;
END//
DELIMITER ;
CALL `add_blog_article_category_column`();
DROP PROCEDURE IF EXISTS `add_blog_article_category_column`;

CREATE TABLE IF NOT EXISTS `blog_article_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Relation ID',
  `article_id` BIGINT NOT NULL COMMENT 'Article ID',
  `tag_id` BIGINT NOT NULL COMMENT 'Tag ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blog_article_tag` (`article_id`, `tag_id`),
  KEY `idx_blog_article_tag_article_id` (`article_id`),
  KEY `idx_blog_article_tag_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article tag relation table';

CREATE TABLE IF NOT EXISTS `blog_config` (
  `config_key` VARCHAR(100) NOT NULL COMMENT 'Config key',
  `config_value` VARCHAR(1000) DEFAULT NULL COMMENT 'Config value',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT 'Remark',
  PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Blog config table';

CREATE TABLE IF NOT EXISTS `blog_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'User ID',
  `username` VARCHAR(50) NOT NULL COMMENT 'Username',
  `password_hash` VARCHAR(64) NOT NULL COMMENT 'SHA-256 password hash',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT 'Nickname',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Enabled flag: 0 disabled, 1 enabled',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blog_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Blog user table';

INSERT INTO `blog_user` (`username`, `password_hash`, `nickname`, `enabled`)
SELECT 'admin', 'd291d80d8b0aa6783b6a060ed49e0759c54fb84f7bf72e525b462dd1d8bd46d3', 'Admin', 1
WHERE NOT EXISTS (
  SELECT 1 FROM `blog_user` WHERE `username` = 'admin'
);

CREATE TABLE IF NOT EXISTS `blog_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Comment ID',
  `article_id` BIGINT NOT NULL COMMENT 'Article ID',
  `nickname` VARCHAR(50) NOT NULL COMMENT 'Commenter nickname',
  `email` VARCHAR(100) DEFAULT NULL COMMENT 'Commenter email',
  `content` VARCHAR(1000) NOT NULL COMMENT 'Comment content',
  `approved` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Approved flag: 0 pending, 1 approved',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  KEY `idx_blog_comment_article_id` (`article_id`),
  KEY `idx_blog_comment_approved` (`approved`),
  KEY `idx_blog_comment_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Blog comment table';

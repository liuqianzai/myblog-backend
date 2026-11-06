-- Flyway 基线迁移脚本
--
-- 本文件由真实运行库（my_blog，MySQL 8.0）导出，用于替换原先与库结构严重漂移的 schema.sql。
-- 之所以需要基线：项目已有生产数据，如果从空库开始逐条执行历史迁移，会与现有表结构冲突。
-- 配置里使用 baseline-on-migrate + baseline-version=1，
-- 已存在的库会直接标记为已执行过 V1，只有 V2 及之后的迁移才会真正执行。
--
-- 导出命令：mysqldump --no-data --skip-comments --compact my_blog

SET NAMES utf8mb4;

CREATE TABLE `blog_article` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '文章标题',
  `summary` varchar(500) DEFAULT NULL COMMENT '文章摘要',
  `content` longtext NOT NULL COMMENT 'Markdown正文内容',
  `cover` varchar(255) DEFAULT NULL COMMENT '封面图URL',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `view_count` int DEFAULT '0' COMMENT '阅读量',
  `is_top` tinyint(1) DEFAULT '0' COMMENT '是否置顶 (0否 1是)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 (0草稿 1发布)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `author_id` bigint DEFAULT NULL COMMENT '作者ID',
  `slug` varchar(150) DEFAULT NULL COMMENT '文章访问别名',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_article_status_time` (`status`,`create_time`),
  KEY `idx_article_top_time` (`is_top`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章表';
CREATE TABLE `blog_article_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_tag` (`article_id`,`tag_id`),
  KEY `idx_article` (`article_id`),
  KEY `idx_tag` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章标签关联表';
CREATE TABLE `blog_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `slug` varchar(80) DEFAULT NULL COMMENT '分类别名',
  `description` varchar(255) DEFAULT NULL COMMENT '分类描述',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章分类表';
CREATE TABLE `blog_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '关联文章ID',
  `nickname` varchar(50) NOT NULL COMMENT '评论者昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '评论者邮箱',
  `content` text NOT NULL COMMENT '评论内容',
  `approved` tinyint(1) DEFAULT '0' COMMENT '是否审核通过 0否 1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_article` (`article_id`),
  KEY `idx_approved` (`approved`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';
CREATE TABLE `blog_config` (
  `config_key` varchar(50) NOT NULL COMMENT '配置项Key (如: author_name)',
  `config_value` text COMMENT '配置项值',
  `remark` varchar(255) DEFAULT NULL COMMENT '配置说明',
  PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全局配置表';
CREATE TABLE `blog_file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `original_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_name` varchar(255) NOT NULL COMMENT '存储文件名',
  `url` varchar(500) NOT NULL COMMENT '访问地址',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=426 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件资源表';
CREATE TABLE `blog_friend_link` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '网站名称',
  `url` varchar(255) NOT NULL COMMENT '网站地址',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像或Logo',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 0隐藏 1显示',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='友情链接表';
CREATE TABLE `blog_page` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL COMMENT '页面标题',
  `slug` varchar(100) NOT NULL COMMENT '页面路径标识',
  `content` longtext NOT NULL COMMENT '页面内容',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 0隐藏 1显示',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_page_slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='独立页面表';
CREATE TABLE `blog_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '标签名称 (如: 3DGS, Java)',
  `color` varchar(20) DEFAULT '#2563EB' COMMENT '标签显示的颜色(对应前端科技蓝)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';
CREATE TABLE `blog_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '加密后的密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `role` varchar(30) DEFAULT 'admin' COMMENT '角色',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 0禁用 1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户/管理员表';

-- 补齐高频查询缺失的索引
--
-- 1) blog_article.category_id：按分类筛选文章时没有可用索引，
--    现有索引是 (status, create_time) 与 (is_top, create_time)，前缀都不含 category_id。
-- 2) blog_comment.create_time：评论列表固定按 create_time 倒序分页，
--    没有索引时 MySQL 需要对结果集做 filesort。
--
-- 说明：MySQL 不支持 CREATE INDEX IF NOT EXISTS，若某环境已手工建过同名索引，
-- 需要先确认 flyway_schema_history 的版本状态再执行。

CREATE INDEX idx_article_category ON blog_article (category_id);

CREATE INDEX idx_comment_time ON blog_comment (create_time);

package com.liuyang.myblog.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MarkdownParserTest {

    @Test
    public void testFrontMatterParsing() {
        String md = """
                ---
                title: 深入理解 Spring Boot 自动装配
                summary: 这是一篇深度解析自动装配原理的文章
                category: 后端架构
                tags: [Spring, Java, 后端]
                isTop: true
                ---
                
                # 正文开头
                
                这里是真正的正文内容。
                """;

        MarkdownParser.ParsedArticle article = MarkdownParser.parse(md, "test.md");

        Assertions.assertEquals("深入理解 Spring Boot 自动装配", article.getTitle());
        Assertions.assertEquals("这是一篇深度解析自动装配原理的文章", article.getSummary());
        Assertions.assertEquals("后端架构", article.getCategory());
        Assertions.assertEquals(List.of("Spring", "Java", "后端"), article.getTags());
        Assertions.assertTrue(article.getIsTop());
        Assertions.assertTrue(article.getContent().contains("这里是真正的正文内容。"));
    }

    @Test
    public void testHeadingH1Fallback() {
        String md = """
                # Redis 核心数据结构与实战
                
                Redis 是一个高性能的 key-value 数据库。
                """;

        MarkdownParser.ParsedArticle article = MarkdownParser.parse(md, "redis-note.md");

        Assertions.assertEquals("Redis 核心数据结构与实战", article.getTitle());
        Assertions.assertTrue(article.getSummary().contains("Redis 是一个高性能"));
        // 验证正文中移除了第一行 H1 避免重复显示
        Assertions.assertFalse(article.getContent().startsWith("# Redis"));
        Assertions.assertTrue(article.getContent().contains("Redis 是一个高性能的 key-value 数据库。"));
    }

    @Test
    public void testFilenameFallback() {
        String md = """
                没有任何标题的一篇文章，
                只有正文描述。
                """;

        MarkdownParser.ParsedArticle article = MarkdownParser.parse(md, "我的第一篇技术随笔.md");

        Assertions.assertEquals("我的第一篇技术随笔", article.getTitle());
        Assertions.assertTrue(article.getSummary().contains("没有任何标题的一篇文章"));
    }
}

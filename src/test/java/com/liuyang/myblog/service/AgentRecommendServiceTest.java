package com.liuyang.myblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuyang.myblog.domain.po.BlogArticle;
import com.liuyang.myblog.domain.po.BlogArticleTag;
import com.liuyang.myblog.domain.po.BlogCategory;
import com.liuyang.myblog.domain.po.BlogTag;
import com.liuyang.myblog.domain.vo.ArticleSimpleVO;
import com.liuyang.myblog.mapper.BlogArticleMapper;
import com.liuyang.myblog.mapper.BlogArticleTagMapper;
import com.liuyang.myblog.mapper.BlogCategoryMapper;
import com.liuyang.myblog.mapper.BlogTagMapper;
import com.liuyang.myblog.service.impl.AgentRecommendServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentRecommendServiceTest {

    @Mock
    private BlogArticleMapper blogArticleMapper;

    @Mock
    private BlogArticleTagMapper blogArticleTagMapper;

    @Mock
    private BlogTagMapper blogTagMapper;

    @Mock
    private BlogCategoryMapper blogCategoryMapper;

    private AgentRecommendService agentRecommendService;

    @BeforeEach
    void setUp() {
        agentRecommendService = new AgentRecommendServiceImpl(
                blogArticleMapper, blogArticleTagMapper, blogTagMapper, blogCategoryMapper
        );
    }

    @Test
    @DisplayName("测试探索推荐：基于时间衰减与热度打分输出文章列表")
    void testGetExploreArticles() {
        BlogArticle a1 = new BlogArticle();
        a1.setId(1L);
        a1.setTitle("微服务架构设计");
        a1.setStatus(true);
        a1.setIsTop(false);
        a1.setViewCount(100);
        a1.setCreateTime(LocalDateTime.now().minusDays(2));

        BlogArticle a2 = new BlogArticle();
        a2.setId(2L);
        a2.setTitle("置顶好文：Java并发精粹");
        a2.setStatus(true);
        a2.setIsTop(true);
        a2.setViewCount(50);
        a2.setCreateTime(LocalDateTime.now().minusDays(10));

        when(blogArticleMapper.selectList(any())).thenReturn(List.of(a1, a2));
        when(blogArticleTagMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<ArticleSimpleVO> list = agentRecommendService.getExploreArticles(5);

        assertNotNull(list);
        assertEquals(2, list.size());
        // 置顶文章排在第一位
        assertEquals(2L, list.get(0).getId());
        assertEquals("置顶好文：Java并发精粹", list.get(0).getTitle());
    }

    @Test
    @DisplayName("测试语义标签推荐：根据标签命中候选文章")
    void testGetArticlesByMetaTags() {
        BlogTag tag1 = new BlogTag();
        tag1.setId(10L);
        tag1.setName("高并发");

        BlogTag tag2 = new BlogTag();
        tag2.setId(11L);
        tag2.setName("Redis");

        when(blogTagMapper.selectList(isNull())).thenReturn(List.of(tag1, tag2));

        BlogArticleTag rel = new BlogArticleTag();
        rel.setArticleId(5L);
        rel.setTagId(10L);

        when(blogArticleTagMapper.selectList(any())).thenReturn(List.of(rel));

        BlogArticle target = new BlogArticle();
        target.setId(5L);
        target.setTitle("Redis在高并发场景下的实践");
        target.setStatus(true);
        target.setViewCount(300);

        when(blogArticleMapper.selectList(any())).thenReturn(List.of(target));

        List<ArticleSimpleVO> results = agentRecommendService.getArticlesByMetaTags(List.of("高并发"), null, 5);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(5L, results.get(0).getId());
        assertEquals("Redis在高并发场景下的实践", results.get(0).getTitle());
    }
}

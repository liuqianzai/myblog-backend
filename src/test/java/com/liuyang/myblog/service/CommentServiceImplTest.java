package com.liuyang.myblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.common.PageResult;
import com.liuyang.myblog.domain.po.BlogComment;
import com.liuyang.myblog.domain.vo.CommentVO;
import com.liuyang.myblog.mapper.BlogArticleMapper;
import com.liuyang.myblog.mapper.BlogCommentMapper;
import com.liuyang.myblog.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private BlogCommentMapper blogCommentMapper;

    @Mock
    private BlogArticleMapper blogArticleMapper;

    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(blogCommentMapper, blogArticleMapper);
    }

    @Test
    @DisplayName("公开评论列表不返回评论者邮箱")
    void testPublicCommentsHideEmail() {
        BlogComment comment = new BlogComment();
        comment.setId(1L);
        comment.setArticleId(10L);
        comment.setNickname("访客");
        comment.setEmail("visitor@example.com");
        comment.setContent("写得很清楚");
        comment.setApproved(Boolean.TRUE);
        comment.setCreateTime(LocalDateTime.now());

        Page<BlogComment> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(comment));
        when(blogCommentMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<CommentVO> result = commentService.pagePublicComments(10L, 1L, 10L);

        assertEquals(1, result.getRecords().size());
        CommentVO vo = result.getRecords().get(0);
        assertEquals("访客", vo.getNickname());
        // CommentVO 根本没有 email 字段，这里再从序列化层面兜一层：确保不会被意外带出
        assertFalse(vo.toString().contains("visitor@example.com"));
    }

    @Test
    @DisplayName("缺少文章 ID 时拒绝查询")
    void testPublicCommentsRequireArticleId() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> commentService.pagePublicComments(null, 1L, 10L));
        assertEquals(400, ex.getCode());
    }
}

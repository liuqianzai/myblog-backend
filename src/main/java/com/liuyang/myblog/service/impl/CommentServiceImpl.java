package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.common.PageResult;
import com.liuyang.myblog.domain.dto.CommentDTO;
import com.liuyang.myblog.domain.dto.CommentReviewDTO;
import com.liuyang.myblog.domain.po.BlogArticle;
import com.liuyang.myblog.domain.po.BlogComment;
import com.liuyang.myblog.mapper.BlogArticleMapper;
import com.liuyang.myblog.mapper.BlogCommentMapper;
import com.liuyang.myblog.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CommentServiceImpl implements CommentService {
    private final BlogCommentMapper blogCommentMapper;
    private final BlogArticleMapper blogArticleMapper;

    public CommentServiceImpl(BlogCommentMapper blogCommentMapper, BlogArticleMapper blogArticleMapper) {
        this.blogCommentMapper = blogCommentMapper;
        this.blogArticleMapper = blogArticleMapper;
    }

    @Override
    public PageResult<BlogComment> pagePublicComments(Long articleId, Long page, Long size) {
        if (articleId == null) {
            throw BusinessException.badRequest("article id is required");
        }
        return pageComments(articleId, Boolean.TRUE, page, size);
    }

    @Override
    public PageResult<BlogComment> pageAdminComments(Long articleId, Boolean approved, Long page, Long size) {
        return pageComments(articleId, approved, page, size);
    }

    @Override
    public Long createComment(CommentDTO commentDTO) {
        if (commentDTO == null || commentDTO.getArticleId() == null
                || !StringUtils.hasText(commentDTO.getNickname())
                || !StringUtils.hasText(commentDTO.getContent())) {
            throw BusinessException.badRequest("article id, nickname and content are required");
        }
        BlogArticle article = blogArticleMapper.selectById(commentDTO.getArticleId());
        if (article == null || !Boolean.TRUE.equals(article.getStatus())) {
            throw BusinessException.notFound("article not found");
        }

        BlogComment comment = new BlogComment();
        comment.setArticleId(commentDTO.getArticleId());
        comment.setNickname(commentDTO.getNickname());
        comment.setEmail(commentDTO.getEmail());
        comment.setContent(commentDTO.getContent());
        comment.setApproved(Boolean.FALSE);
        blogCommentMapper.insert(comment);
        return comment.getId();
    }

    @Override
    public void reviewComment(Long id, CommentReviewDTO reviewDTO) {
        if (reviewDTO == null || reviewDTO.getApproved() == null) {
            throw BusinessException.badRequest("approved is required");
        }
        BlogComment comment = blogCommentMapper.selectById(id);
        if (comment == null) {
            throw BusinessException.notFound("comment not found");
        }
        comment.setApproved(reviewDTO.getApproved());
        blogCommentMapper.updateById(comment);
    }

    @Override
    public void deleteComment(Long id) {
        if (blogCommentMapper.deleteById(id) == 0) {
            throw BusinessException.notFound("comment not found");
        }
    }

    private PageResult<BlogComment> pageComments(Long articleId, Boolean approved, Long page, Long size) {
        long pageNo = page == null || page < 1 ? 1 : page;
        long pageSize = size == null || size < 1 ? 10 : Math.min(size, 100);
        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<BlogComment>()
                .orderByDesc(BlogComment::getCreateTime);
        if (articleId != null) {
            wrapper.eq(BlogComment::getArticleId, articleId);
        }
        if (approved != null) {
            wrapper.eq(BlogComment::getApproved, approved);
        }
        Page<BlogComment> result = blogCommentMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }
}

package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.domain.dto.ArticleTagDTO;
import com.liuyang.myblog.domain.po.BlogArticleTag;
import com.liuyang.myblog.mapper.BlogArticleTagMapper;
import com.liuyang.myblog.service.ArticleTagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleTagServiceImpl implements ArticleTagService {
    private final BlogArticleTagMapper blogArticleTagMapper;

    public ArticleTagServiceImpl(BlogArticleTagMapper blogArticleTagMapper) {
        this.blogArticleTagMapper = blogArticleTagMapper;
    }

    @Override
    public List<BlogArticleTag> listByArticleId(Long articleId) {
        if (articleId == null) {
            throw BusinessException.badRequest("article id is required");
        }
        return blogArticleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                .eq(BlogArticleTag::getArticleId, articleId));
    }

    @Override
    public Long createRelation(ArticleTagDTO articleTagDTO) {
        if (articleTagDTO == null || articleTagDTO.getArticleId() == null || articleTagDTO.getTagId() == null) {
            throw BusinessException.badRequest("article id and tag id are required");
        }
        BlogArticleTag relation = new BlogArticleTag();
        relation.setArticleId(articleTagDTO.getArticleId());
        relation.setTagId(articleTagDTO.getTagId());
        blogArticleTagMapper.insert(relation);
        return relation.getId();
    }

    @Override
    public void deleteRelation(Long id) {
        if (blogArticleTagMapper.deleteById(id) == 0) {
            throw BusinessException.notFound("article tag relation not found");
        }
    }
}

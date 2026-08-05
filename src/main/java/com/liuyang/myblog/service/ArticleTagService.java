package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.ArticleTagDTO;
import com.liuyang.myblog.domain.po.BlogArticleTag;

import java.util.List;

/**
 * 文章标签关联业务逻辑接口
 */
public interface ArticleTagService {/**
 * 列表查询By文章Id
 */

    List<BlogArticleTag> listByArticleId(Long articleId);    /**
     * 创建Relation
     */


    Long createRelation(ArticleTagDTO articleTagDTO);    /**
     * 删除Relation
     */


    void deleteRelation(Long id);
}

package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.ArticleTagDTO;
import com.liuyang.myblog.domain.po.BlogArticleTag;

import java.util.List;

public interface ArticleTagService {
    List<BlogArticleTag> listByArticleId(Long articleId);

    Long createRelation(ArticleTagDTO articleTagDTO);

    void deleteRelation(Long id);
}

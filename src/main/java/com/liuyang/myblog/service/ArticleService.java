package com.liuyang.myblog.service;

import com.liuyang.myblog.common.PageResult;
import com.liuyang.myblog.domain.dto.ArticleDTO;
import com.liuyang.myblog.domain.vo.ArchiveVO;
import com.liuyang.myblog.domain.vo.ArticleVO;

import java.util.List;

public interface ArticleService {
    PageResult<ArticleVO> pageArticles(Long page, Long size, String keyword, Boolean status, Long categoryId, Long tagId);

    ArticleVO getArticle(Long id, boolean increaseViewCount, Boolean status);

    Long createArticle(ArticleDTO articleDTO);

    void updateArticle(Long id, ArticleDTO articleDTO);

    void deleteArticle(Long id);

    List<ArchiveVO> listArchives();
}

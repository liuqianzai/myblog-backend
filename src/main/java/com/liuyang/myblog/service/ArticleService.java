package com.liuyang.myblog.service;

import com.liuyang.myblog.common.PageResult;
import com.liuyang.myblog.domain.dto.ArticleDTO;
import com.liuyang.myblog.domain.vo.ArchiveVO;
import com.liuyang.myblog.domain.vo.ArticleVO;

import java.util.List;

/**
 * 文章业务逻辑接口
 */
public interface ArticleService {/**
 * 分页查询文章
 */

    PageResult<ArticleVO> pageArticles(Long page, Long size, String keyword, Boolean status, Long categoryId, Long tagId);    /**
     * 获取文章
     */


    ArticleVO getArticle(Long id, boolean increaseViewCount, Boolean status);    /**
     * 创建文章
     */


    Long createArticle(ArticleDTO articleDTO);    /**
     * 更新文章
     */


    void updateArticle(Long id, ArticleDTO articleDTO);    /**
     * 删除文章
     */


    void deleteArticle(Long id);    /**
     * 列表查询归档
     */


    List<ArchiveVO> listArchives();
}

package com.liuyang.myblog.service;

import com.liuyang.myblog.common.PageResult;
import com.liuyang.myblog.domain.dto.ArticleDTO;
import com.liuyang.myblog.domain.vo.ArchiveVO;
import com.liuyang.myblog.domain.vo.ArticleImportVO;
import com.liuyang.myblog.domain.vo.ArticleVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文章业务逻辑接口
 */
public interface ArticleService {
    /**
     * 批量导入 Markdown 文章
     */
    ArticleImportVO importArticles(List<MultipartFile> files, Long defaultCategoryId);

    /**
     * 分页查询文章
     *
     * @param includeContent 是否在列表里带正文。前台列表不需要正文（longtext 会让响应体积膨胀几十倍），
     *                       后台列表按原样返回，保持既有行为不变
     */
    PageResult<ArticleVO> pageArticles(Long page, Long size, String keyword, Boolean status,
                                       Long categoryId, Long tagId, boolean includeContent);

    /**
     * 分页查询文章（默认带正文，兼容历史调用）
     */
    default PageResult<ArticleVO> pageArticles(Long page, Long size, String keyword, Boolean status,
                                              Long categoryId, Long tagId) {
        return pageArticles(page, size, keyword, status, categoryId, tagId, true);
    }

    /**
     * 获取文章
     */
    ArticleVO getArticle(Long id, boolean increaseViewCount, Boolean status);

    /**
     * 创建文章
     */
    Long createArticle(ArticleDTO articleDTO);

    /**
     * 更新文章
     */
    void updateArticle(Long id, ArticleDTO articleDTO);

    /**
     * 删除文章
     */
    void deleteArticle(Long id);

    /**
     * 列表查询归档
     */
    List<ArchiveVO> listArchives();
}

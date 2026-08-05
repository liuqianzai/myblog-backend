package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.common.PageResult;
import com.liuyang.myblog.domain.dto.ArticleDTO;
import com.liuyang.myblog.domain.vo.ArticleVO;
import com.liuyang.myblog.service.ArticleService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/articles")
/**
 * 后台文章管理控制器
 */
public class AdminArticleController {
    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ApiResponse<PageResult<ArticleVO>> pageArticles(@RequestParam(required = false) Long page,
                                                           @RequestParam(required = false) Long size,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) Boolean status,
                                                           @RequestParam(required = false) Long categoryId,
                                                           @RequestParam(required = false) Long tagId) {
        return ApiResponse.ok(articleService.pageArticles(page, size, keyword, status, categoryId, tagId));
    }
    /**
     * 获取文章
     */

    @GetMapping("/{id}")
    public ApiResponse<ArticleVO> getArticle(@PathVariable Long id) {
        return ApiResponse.ok(articleService.getArticle(id, false, null));
    }
    /**
     * 创建文章
     */

    @PostMapping
    public ApiResponse<Long> createArticle(@RequestBody ArticleDTO articleDTO) {
        return ApiResponse.ok(articleService.createArticle(articleDTO));
    }
    /**
     * 更新文章
     */

    @PutMapping("/{id}")
    public ApiResponse<Void> updateArticle(@PathVariable Long id, @RequestBody ArticleDTO articleDTO) {
        articleService.updateArticle(id, articleDTO);
        return ApiResponse.ok();
    }
    /**
     * 删除文章
     */

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ApiResponse.ok();
    }
}

package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.common.PageResult;
import com.liuyang.myblog.domain.vo.ArticleVO;
import com.liuyang.myblog.service.ArticleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
public class ArticleController {
    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ApiResponse<PageResult<ArticleVO>> pageArticles(@RequestParam(required = false) Long page,
                                                           @RequestParam(required = false) Long size,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) Long categoryId,
                                                           @RequestParam(required = false) Long tagId) {
        return ApiResponse.ok(articleService.pageArticles(page, size, keyword, Boolean.TRUE, categoryId, tagId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleVO> getArticle(@PathVariable Long id) {
        return ApiResponse.ok(articleService.getArticle(id, true, Boolean.TRUE));
    }

}

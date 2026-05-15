package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.domain.dto.ArticleTagDTO;
import com.liuyang.myblog.domain.po.BlogArticleTag;
import com.liuyang.myblog.service.ArticleTagService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/article-tags")
public class ArticleTagController {
    private final ArticleTagService articleTagService;

    public ArticleTagController(ArticleTagService articleTagService) {
        this.articleTagService = articleTagService;
    }

    @GetMapping
    public ApiResponse<List<BlogArticleTag>> listByArticleId(@RequestParam Long articleId) {
        return ApiResponse.ok(articleTagService.listByArticleId(articleId));
    }

    @PostMapping
    public ApiResponse<Long> createRelation(@RequestBody ArticleTagDTO articleTagDTO) {
        return ApiResponse.ok(articleTagService.createRelation(articleTagDTO));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRelation(@PathVariable Long id) {
        articleTagService.deleteRelation(id);
        return ApiResponse.ok();
    }
}

package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.domain.vo.ArchiveVO;
import com.liuyang.myblog.service.ArticleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/archives")
/**
 * 文章归档管理控制器
 */
public class ArchiveController {
    private final ArticleService articleService;

    public ArchiveController(ArticleService articleService) {
        this.articleService = articleService;
    }
    /**
     * 列表查询归档
     */

    @GetMapping
    public ApiResponse<List<ArchiveVO>> listArchives() {
        return ApiResponse.ok(articleService.listArchives());
    }
}

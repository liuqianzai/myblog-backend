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
public class ArchiveController {
    private final ArticleService articleService;

    public ArchiveController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ApiResponse<List<ArchiveVO>> listArchives() {
        return ApiResponse.ok(articleService.listArchives());
    }
}

package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.domain.po.BlogPage;
import com.liuyang.myblog.service.PageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pages")
/**
 * 独立页面管理控制器
 */
public class PageController {
    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    /**
     * 获取所有已发布的页面列表
     */
    @GetMapping
    public ApiResponse<List<BlogPage>> listActivePages() {
        return ApiResponse.ok(pageService.listActive());
    }

    /**
     * 获取BySlug
     */
    @GetMapping("/{slug}")
    public ApiResponse<BlogPage> getBySlug(@PathVariable String slug) {
        return ApiResponse.ok(pageService.getBySlug(slug));
    }
}

package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.domain.dto.PageDTO;
import com.liuyang.myblog.domain.po.BlogPage;
import com.liuyang.myblog.service.PageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/pages")
/**
 * 后台独立页面管理控制器
 */
public class AdminPageController {
    private final PageService pageService;

    public AdminPageController(PageService pageService) {
        this.pageService = pageService;
    }
    /**
     * 列表查询All
     */

    @GetMapping
    public ApiResponse<List<BlogPage>> listAll() {
        return ApiResponse.ok(pageService.listAll());
    }
    /**
     * 获取ById
     */

    @GetMapping("/{id}")
    public ApiResponse<BlogPage> getById(@PathVariable Long id) {
        return ApiResponse.ok(pageService.getById(id));
    }
    /**
     * 创建
     */

    @PostMapping
    public ApiResponse<Long> create(@RequestBody PageDTO dto) {
        return ApiResponse.ok(pageService.create(dto));
    }
    /**
     * 更新
     */

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody PageDTO dto) {
        pageService.update(id, dto);
        return ApiResponse.ok();
    }
    /**
     * 删除
     */

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        pageService.delete(id);
        return ApiResponse.ok();
    }
}

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
public class AdminPageController {
    private final PageService pageService;

    public AdminPageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping
    public ApiResponse<List<BlogPage>> listAll() {
        return ApiResponse.ok(pageService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<BlogPage> getById(@PathVariable Long id) {
        return ApiResponse.ok(pageService.getById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody PageDTO dto) {
        return ApiResponse.ok(pageService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody PageDTO dto) {
        pageService.update(id, dto);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        pageService.delete(id);
        return ApiResponse.ok();
    }
}

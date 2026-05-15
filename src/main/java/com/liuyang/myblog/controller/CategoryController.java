package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.domain.dto.CategoryDTO;
import com.liuyang.myblog.domain.po.BlogCategory;
import com.liuyang.myblog.service.CategoryService;
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
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<BlogCategory>> listCategories() {
        return ApiResponse.ok(categoryService.listCategories());
    }

    @GetMapping("/{id}")
    public ApiResponse<BlogCategory> getCategory(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.getCategory(id));
    }

    @PostMapping
    public ApiResponse<Long> createCategory(@RequestBody CategoryDTO categoryDTO) {
        return ApiResponse.ok(categoryService.createCategory(categoryDTO));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        categoryService.updateCategory(id, categoryDTO);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.ok();
    }
}

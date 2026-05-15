package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.CategoryDTO;
import com.liuyang.myblog.domain.po.BlogCategory;

import java.util.List;

public interface CategoryService {
    List<BlogCategory> listCategories();

    BlogCategory getCategory(Long id);

    Long createCategory(CategoryDTO categoryDTO);

    void updateCategory(Long id, CategoryDTO categoryDTO);

    void deleteCategory(Long id);
}

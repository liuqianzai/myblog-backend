package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.CategoryDTO;
import com.liuyang.myblog.domain.po.BlogCategory;

import java.util.List;

/**
 * 分类业务逻辑接口
 */
public interface CategoryService {/**
 * 列表查询Categories
 */

    List<BlogCategory> listCategories();    /**
     * 获取分类
     */


    BlogCategory getCategory(Long id);    /**
     * 创建分类
     */


    Long createCategory(CategoryDTO categoryDTO);    /**
     * 更新分类
     */


    void updateCategory(Long id, CategoryDTO categoryDTO);    /**
     * 删除分类
     */


    void deleteCategory(Long id);
}

package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.PageDTO;
import com.liuyang.myblog.domain.po.BlogPage;

import java.util.List;

/**
 * 独立页面业务逻辑接口
 */
public interface PageService {/**
 * 获取BySlug
 */

    BlogPage getBySlug(String slug);    /**
     * 列表查询All
     */


    List<BlogPage> listAll();

    /**
     * 获取所有已发布的页面
     */
    List<BlogPage> listActive();

    /**
     * 获取ById
     */
    BlogPage getById(Long id);    /**
     * 创建
     */


    Long create(PageDTO dto);    /**
     * 更新
     */


    void update(Long id, PageDTO dto);    /**
     * 删除
     */


    void delete(Long id);
}

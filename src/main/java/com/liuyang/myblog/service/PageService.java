package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.PageDTO;
import com.liuyang.myblog.domain.po.BlogPage;

import java.util.List;

public interface PageService {
    BlogPage getBySlug(String slug);

    List<BlogPage> listAll();

    BlogPage getById(Long id);

    Long create(PageDTO dto);

    void update(Long id, PageDTO dto);

    void delete(Long id);
}

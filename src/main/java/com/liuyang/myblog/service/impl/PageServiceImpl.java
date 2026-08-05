package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.domain.dto.PageDTO;
import com.liuyang.myblog.domain.po.BlogPage;
import com.liuyang.myblog.mapper.BlogPageMapper;
import com.liuyang.myblog.service.PageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
/**
 * 独立页面业务逻辑接口实现类
 */
public class PageServiceImpl implements PageService {
    private final BlogPageMapper blogPageMapper;

    public PageServiceImpl(BlogPageMapper blogPageMapper) {
        this.blogPageMapper = blogPageMapper;
    }
    /**
     * 获取BySlug
     */

    @Override
    public BlogPage getBySlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw BusinessException.badRequest("slug is required");
        }
        BlogPage page = blogPageMapper.selectOne(new LambdaQueryWrapper<BlogPage>()
                .eq(BlogPage::getSlug, slug)
                .eq(BlogPage::getStatus, Boolean.TRUE)
                .last("limit 1"));
        if (page == null) {
            throw BusinessException.notFound("page not found");
        }
        return page;
    }
    @Override
    public List<BlogPage> listAll() {
        return blogPageMapper.selectList(new LambdaQueryWrapper<BlogPage>()
                .orderByAsc(BlogPage::getId));
    }

    /**
     * 获取所有已发布的页面
     */
    @Override
    public List<BlogPage> listActive() {
        return blogPageMapper.selectList(new LambdaQueryWrapper<BlogPage>()
                .eq(BlogPage::getStatus, Boolean.TRUE)
                .orderByAsc(BlogPage::getId));
    }
    /**
     * 获取ById
     */

    @Override
    public BlogPage getById(Long id) {
        BlogPage page = blogPageMapper.selectById(id);
        if (page == null) {
            throw BusinessException.notFound("page not found");
        }
        return page;
    }
    /**
     * 创建
     */

    @Override
    public Long create(PageDTO dto) {
        validate(dto);
        BlogPage page = new BlogPage();
        copy(dto, page);
        if (page.getStatus() == null) {
            page.setStatus(Boolean.TRUE);
        }
        blogPageMapper.insert(page);
        return page.getId();
    }
    /**
     * 更新
     */

    @Override
    public void update(Long id, PageDTO dto) {
        validate(dto);
        BlogPage page = getById(id);
        copy(dto, page);
        blogPageMapper.updateById(page);
    }
    /**
     * 删除
     */

    @Override
    public void delete(Long id) {
        if (blogPageMapper.deleteById(id) == 0) {
            throw BusinessException.notFound("page not found");
        }
    }    /**
     * validate
     */


    private void validate(PageDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTitle())
                || !StringUtils.hasText(dto.getSlug())
                || !StringUtils.hasText(dto.getContent())) {
            throw BusinessException.badRequest("title, slug and content are required");
        }
    }    /**
     * copy
     */


    private void copy(PageDTO source, BlogPage target) {
        target.setTitle(source.getTitle());
        target.setSlug(source.getSlug());
        target.setContent(source.getContent());
        target.setStatus(source.getStatus());
    }
}

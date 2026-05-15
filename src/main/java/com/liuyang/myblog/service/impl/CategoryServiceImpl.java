package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.domain.dto.CategoryDTO;
import com.liuyang.myblog.domain.po.BlogArticle;
import com.liuyang.myblog.domain.po.BlogCategory;
import com.liuyang.myblog.mapper.BlogArticleMapper;
import com.liuyang.myblog.mapper.BlogCategoryMapper;
import com.liuyang.myblog.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final BlogCategoryMapper blogCategoryMapper;
    private final BlogArticleMapper blogArticleMapper;

    public CategoryServiceImpl(BlogCategoryMapper blogCategoryMapper, BlogArticleMapper blogArticleMapper) {
        this.blogCategoryMapper = blogCategoryMapper;
        this.blogArticleMapper = blogArticleMapper;
    }

    @Override
    public List<BlogCategory> listCategories() {
        return blogCategoryMapper.selectList(new LambdaQueryWrapper<BlogCategory>()
                .orderByAsc(BlogCategory::getSort)
                .orderByAsc(BlogCategory::getId));
    }

    @Override
    public BlogCategory getCategory(Long id) {
        BlogCategory category = blogCategoryMapper.selectById(id);
        if (category == null) {
            throw BusinessException.notFound("category not found");
        }
        return category;
    }

    @Override
    public Long createCategory(CategoryDTO categoryDTO) {
        validate(categoryDTO);
        BlogCategory category = new BlogCategory();
        copy(categoryDTO, category);
        blogCategoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void updateCategory(Long id, CategoryDTO categoryDTO) {
        validate(categoryDTO);
        BlogCategory category = getCategory(id);
        copy(categoryDTO, category);
        blogCategoryMapper.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        if (blogCategoryMapper.deleteById(id) == 0) {
            throw BusinessException.notFound("category not found");
        }
        blogArticleMapper.update(null, new LambdaUpdateWrapper<BlogArticle>()
                .eq(BlogArticle::getCategoryId, id)
                .set(BlogArticle::getCategoryId, null));
    }

    private void validate(CategoryDTO categoryDTO) {
        if (categoryDTO == null || !StringUtils.hasText(categoryDTO.getName())) {
            throw BusinessException.badRequest("category name is required");
        }
    }

    private void copy(CategoryDTO source, BlogCategory target) {
        target.setName(source.getName());
        target.setSlug(source.getSlug());
        target.setDescription(source.getDescription());
        target.setSort(source.getSort() == null ? 0 : source.getSort());
    }
}

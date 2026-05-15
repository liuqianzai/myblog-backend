package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.domain.dto.TagDTO;
import com.liuyang.myblog.domain.po.BlogArticleTag;
import com.liuyang.myblog.domain.po.BlogTag;
import com.liuyang.myblog.mapper.BlogArticleTagMapper;
import com.liuyang.myblog.mapper.BlogTagMapper;
import com.liuyang.myblog.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {
    private final BlogTagMapper blogTagMapper;
    private final BlogArticleTagMapper blogArticleTagMapper;

    public TagServiceImpl(BlogTagMapper blogTagMapper, BlogArticleTagMapper blogArticleTagMapper) {
        this.blogTagMapper = blogTagMapper;
        this.blogArticleTagMapper = blogArticleTagMapper;
    }

    @Override
    public List<BlogTag> listTags() {
        return blogTagMapper.selectList(new LambdaQueryWrapper<BlogTag>().orderByAsc(BlogTag::getName));
    }

    @Override
    public BlogTag getTag(Long id) {
        BlogTag tag = blogTagMapper.selectById(id);
        if (tag == null) {
            throw BusinessException.notFound("tag not found");
        }
        return tag;
    }

    @Override
    public Long createTag(TagDTO tagDTO) {
        validateTag(tagDTO);
        BlogTag tag = new BlogTag();
        tag.setName(tagDTO.getName());
        tag.setColor(tagDTO.getColor());
        blogTagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    public void updateTag(Long id, TagDTO tagDTO) {
        validateTag(tagDTO);
        BlogTag tag = getTag(id);
        tag.setName(tagDTO.getName());
        tag.setColor(tagDTO.getColor());
        blogTagMapper.updateById(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        if (blogTagMapper.deleteById(id) == 0) {
            throw BusinessException.notFound("tag not found");
        }
        blogArticleTagMapper.delete(new LambdaQueryWrapper<BlogArticleTag>().eq(BlogArticleTag::getTagId, id));
    }

    private void validateTag(TagDTO tagDTO) {
        if (tagDTO == null || !StringUtils.hasText(tagDTO.getName())) {
            throw BusinessException.badRequest("tag name is required");
        }
    }
}

package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.domain.dto.FriendLinkDTO;
import com.liuyang.myblog.domain.po.BlogFriendLink;
import com.liuyang.myblog.mapper.BlogFriendLinkMapper;
import com.liuyang.myblog.service.FriendLinkService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class FriendLinkServiceImpl implements FriendLinkService {
    private final BlogFriendLinkMapper blogFriendLinkMapper;

    public FriendLinkServiceImpl(BlogFriendLinkMapper blogFriendLinkMapper) {
        this.blogFriendLinkMapper = blogFriendLinkMapper;
    }

    @Override
    public List<BlogFriendLink> listVisible() {
        return blogFriendLinkMapper.selectList(new LambdaQueryWrapper<BlogFriendLink>()
                .eq(BlogFriendLink::getStatus, Boolean.TRUE)
                .orderByAsc(BlogFriendLink::getSort)
                .orderByAsc(BlogFriendLink::getId));
    }

    @Override
    public List<BlogFriendLink> listAll() {
        return blogFriendLinkMapper.selectList(new LambdaQueryWrapper<BlogFriendLink>()
                .orderByAsc(BlogFriendLink::getSort)
                .orderByAsc(BlogFriendLink::getId));
    }

    @Override
    public BlogFriendLink getById(Long id) {
        BlogFriendLink link = blogFriendLinkMapper.selectById(id);
        if (link == null) {
            throw BusinessException.notFound("friend link not found");
        }
        return link;
    }

    @Override
    public Long create(FriendLinkDTO dto) {
        validate(dto);
        BlogFriendLink link = new BlogFriendLink();
        copy(dto, link);
        if (link.getStatus() == null) {
            link.setStatus(Boolean.TRUE);
        }
        blogFriendLinkMapper.insert(link);
        return link.getId();
    }

    @Override
    public void update(Long id, FriendLinkDTO dto) {
        validate(dto);
        BlogFriendLink link = getById(id);
        copy(dto, link);
        blogFriendLinkMapper.updateById(link);
    }

    @Override
    public void delete(Long id) {
        if (blogFriendLinkMapper.deleteById(id) == 0) {
            throw BusinessException.notFound("friend link not found");
        }
    }

    private void validate(FriendLinkDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getName()) || !StringUtils.hasText(dto.getUrl())) {
            throw BusinessException.badRequest("name and url are required");
        }
    }

    private void copy(FriendLinkDTO source, BlogFriendLink target) {
        target.setName(source.getName());
        target.setUrl(source.getUrl());
        target.setAvatar(source.getAvatar());
        target.setDescription(source.getDescription());
        target.setSort(source.getSort() == null ? 0 : source.getSort());
        target.setStatus(source.getStatus());
    }
}

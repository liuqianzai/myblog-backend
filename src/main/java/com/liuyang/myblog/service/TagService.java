package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.TagDTO;
import com.liuyang.myblog.domain.po.BlogTag;

import java.util.List;

public interface TagService {
    List<BlogTag> listTags();

    BlogTag getTag(Long id);

    Long createTag(TagDTO tagDTO);

    void updateTag(Long id, TagDTO tagDTO);

    void deleteTag(Long id);
}

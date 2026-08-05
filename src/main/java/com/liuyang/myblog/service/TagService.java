package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.TagDTO;
import com.liuyang.myblog.domain.po.BlogTag;

import java.util.List;

/**
 * 标签业务逻辑接口
 */
public interface TagService {/**
 * 列表查询标签
 */

    List<BlogTag> listTags();    /**
     * 获取标签
     */


    BlogTag getTag(Long id);    /**
     * 创建标签
     */


    Long createTag(TagDTO tagDTO);    /**
     * 更新标签
     */


    void updateTag(Long id, TagDTO tagDTO);    /**
     * 删除标签
     */


    void deleteTag(Long id);
}

package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.FriendLinkDTO;
import com.liuyang.myblog.domain.po.BlogFriendLink;

import java.util.List;

/**
 * 友情链接业务逻辑接口
 */
public interface FriendLinkService {/**
 * 列表查询Visible
 */

    List<BlogFriendLink> listVisible();    /**
     * 列表查询All
     */


    List<BlogFriendLink> listAll();    /**
     * 获取ById
     */


    BlogFriendLink getById(Long id);    /**
     * 创建
     */


    Long create(FriendLinkDTO dto);    /**
     * 更新
     */


    void update(Long id, FriendLinkDTO dto);    /**
     * 删除
     */


    void delete(Long id);
}

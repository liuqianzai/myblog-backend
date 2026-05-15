package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.FriendLinkDTO;
import com.liuyang.myblog.domain.po.BlogFriendLink;

import java.util.List;

public interface FriendLinkService {
    List<BlogFriendLink> listVisible();

    List<BlogFriendLink> listAll();

    BlogFriendLink getById(Long id);

    Long create(FriendLinkDTO dto);

    void update(Long id, FriendLinkDTO dto);

    void delete(Long id);
}

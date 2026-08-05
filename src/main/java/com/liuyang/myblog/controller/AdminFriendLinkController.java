package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.domain.dto.FriendLinkDTO;
import com.liuyang.myblog.domain.po.BlogFriendLink;
import com.liuyang.myblog.service.FriendLinkService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/friend-links")
/**
 * 后台友情链接管理控制器
 */
public class AdminFriendLinkController {
    private final FriendLinkService friendLinkService;

    public AdminFriendLinkController(FriendLinkService friendLinkService) {
        this.friendLinkService = friendLinkService;
    }
    /**
     * 列表查询All
     */

    @GetMapping
    public ApiResponse<List<BlogFriendLink>> listAll() {
        return ApiResponse.ok(friendLinkService.listAll());
    }
    /**
     * 获取ById
     */

    @GetMapping("/{id}")
    public ApiResponse<BlogFriendLink> getById(@PathVariable Long id) {
        return ApiResponse.ok(friendLinkService.getById(id));
    }
    /**
     * 创建
     */

    @PostMapping
    public ApiResponse<Long> create(@RequestBody FriendLinkDTO dto) {
        return ApiResponse.ok(friendLinkService.create(dto));
    }
    /**
     * 更新
     */

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody FriendLinkDTO dto) {
        friendLinkService.update(id, dto);
        return ApiResponse.ok();
    }
    /**
     * 删除
     */

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        friendLinkService.delete(id);
        return ApiResponse.ok();
    }
}

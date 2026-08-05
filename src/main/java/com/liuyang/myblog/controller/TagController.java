package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.domain.dto.TagDTO;
import com.liuyang.myblog.domain.po.BlogTag;
import com.liuyang.myblog.service.TagService;
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
@RequestMapping("/tags")
/**
 * 标签管理控制器
 */
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }
    /**
     * 列表查询标签
     */

    @GetMapping
    public ApiResponse<List<BlogTag>> listTags() {
        return ApiResponse.ok(tagService.listTags());
    }
    /**
     * 获取标签
     */

    @GetMapping("/{id}")
    public ApiResponse<BlogTag> getTag(@PathVariable Long id) {
        return ApiResponse.ok(tagService.getTag(id));
    }
    /**
     * 创建标签
     */

    @PostMapping
    public ApiResponse<Long> createTag(@RequestBody TagDTO tagDTO) {
        return ApiResponse.ok(tagService.createTag(tagDTO));
    }
    /**
     * 更新标签
     */

    @PutMapping("/{id}")
    public ApiResponse<Void> updateTag(@PathVariable Long id, @RequestBody TagDTO tagDTO) {
        tagService.updateTag(id, tagDTO);
        return ApiResponse.ok();
    }
    /**
     * 删除标签
     */

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.ok();
    }
}

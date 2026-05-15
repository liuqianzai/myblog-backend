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
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ApiResponse<List<BlogTag>> listTags() {
        return ApiResponse.ok(tagService.listTags());
    }

    @GetMapping("/{id}")
    public ApiResponse<BlogTag> getTag(@PathVariable Long id) {
        return ApiResponse.ok(tagService.getTag(id));
    }

    @PostMapping
    public ApiResponse<Long> createTag(@RequestBody TagDTO tagDTO) {
        return ApiResponse.ok(tagService.createTag(tagDTO));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateTag(@PathVariable Long id, @RequestBody TagDTO tagDTO) {
        tagService.updateTag(id, tagDTO);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.ok();
    }
}

package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.common.PageResult;
import com.liuyang.myblog.domain.dto.CommentDTO;
import com.liuyang.myblog.domain.po.BlogComment;
import com.liuyang.myblog.service.CommentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ApiResponse<PageResult<BlogComment>> pageComments(@RequestParam Long articleId,
                                                             @RequestParam(required = false) Long page,
                                                             @RequestParam(required = false) Long size) {
        return ApiResponse.ok(commentService.pagePublicComments(articleId, page, size));
    }

    @PostMapping
    public ApiResponse<Long> createComment(@RequestBody CommentDTO commentDTO) {
        return ApiResponse.ok(commentService.createComment(commentDTO));
    }
}

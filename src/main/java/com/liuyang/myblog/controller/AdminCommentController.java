package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.common.PageResult;
import com.liuyang.myblog.domain.dto.CommentReviewDTO;
import com.liuyang.myblog.domain.po.BlogComment;
import com.liuyang.myblog.service.CommentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/comments")
public class AdminCommentController {
    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ApiResponse<PageResult<BlogComment>> pageComments(@RequestParam(required = false) Long articleId,
                                                             @RequestParam(required = false) Boolean approved,
                                                             @RequestParam(required = false) Long page,
                                                             @RequestParam(required = false) Long size) {
        return ApiResponse.ok(commentService.pageAdminComments(articleId, approved, page, size));
    }

    @PatchMapping("/{id}/review")
    public ApiResponse<Void> reviewComment(@PathVariable Long id, @RequestBody CommentReviewDTO reviewDTO) {
        commentService.reviewComment(id, reviewDTO);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ApiResponse.ok();
    }
}

package com.liuyang.myblog.service;

import com.liuyang.myblog.common.PageResult;
import com.liuyang.myblog.domain.dto.CommentDTO;
import com.liuyang.myblog.domain.dto.CommentReviewDTO;
import com.liuyang.myblog.domain.po.BlogComment;

public interface CommentService {
    PageResult<BlogComment> pagePublicComments(Long articleId, Long page, Long size);

    PageResult<BlogComment> pageAdminComments(Long articleId, Boolean approved, Long page, Long size);

    Long createComment(CommentDTO commentDTO);

    void reviewComment(Long id, CommentReviewDTO reviewDTO);

    void deleteComment(Long id);
}

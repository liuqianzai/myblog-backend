package com.liuyang.myblog.service;

import com.liuyang.myblog.common.PageResult;
import com.liuyang.myblog.domain.dto.CommentDTO;
import com.liuyang.myblog.domain.dto.CommentReviewDTO;
import com.liuyang.myblog.domain.po.BlogComment;

/**
 * 评论业务逻辑接口
 */
public interface CommentService {/**
 * 分页查询Public评论
 */

    PageResult<BlogComment> pagePublicComments(Long articleId, Long page, Long size);    /**
     * 分页查询Admin评论
     */


    PageResult<BlogComment> pageAdminComments(Long articleId, Boolean approved, Long page, Long size);    /**
     * 创建评论
     */


    Long createComment(CommentDTO commentDTO);    /**
     * review评论
     */


    void reviewComment(Long id, CommentReviewDTO reviewDTO);    /**
     * 删除评论
     */


    void deleteComment(Long id);
}

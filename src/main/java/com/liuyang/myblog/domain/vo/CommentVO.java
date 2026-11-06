package com.liuyang.myblog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公开评论视图对象
 *
 * 与 BlogComment 实体的区别：去掉了 email 字段。
 * 游客读取评论列表时不应该拿到其他评论者的邮箱地址，这属于个人信息泄露。
 */
@Data
public class CommentVO {
    private Long id;
    private Long articleId;
    private String nickname;
    private String content;
    private LocalDateTime createTime;
}

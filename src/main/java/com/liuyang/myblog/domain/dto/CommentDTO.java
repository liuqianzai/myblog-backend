package com.liuyang.myblog.domain.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Long articleId;
    private String nickname;
    private String email;
    private String content;
}

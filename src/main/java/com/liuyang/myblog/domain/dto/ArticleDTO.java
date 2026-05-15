package com.liuyang.myblog.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArticleDTO {
    private String title;
    private String summary;
    private String content;
    private String cover;
    private Long categoryId;
    private Boolean isTop;
    private Boolean status;
    private List<Long> tagIds;
}

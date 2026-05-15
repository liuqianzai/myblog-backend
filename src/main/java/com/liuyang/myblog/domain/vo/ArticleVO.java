package com.liuyang.myblog.domain.vo;

import com.liuyang.myblog.domain.po.BlogCategory;
import com.liuyang.myblog.domain.po.BlogTag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleVO {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String cover;
    private Long categoryId;
    private Integer viewCount;
    private Boolean isTop;
    private Boolean status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private BlogCategory category;
    private List<BlogTag> tags;
}

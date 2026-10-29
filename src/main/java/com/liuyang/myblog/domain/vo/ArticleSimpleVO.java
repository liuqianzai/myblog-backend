package com.liuyang.myblog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 精简文章视图对象(用于推荐与小宠物卡片)
 *
 * blog_article：id, title, summary, cover, viewCount, createTime
 * blog_category：categoryName
 * 计算/逻辑/其他：tags, matchReason
 */
@Data
public class ArticleSimpleVO {
    // ---- blog_article ----
    private Long id;
    private String title;
    private String summary;
    private String cover;
    private Integer viewCount;
    private LocalDateTime createTime;

    // ---- blog_category ----
    private String categoryName;

    // ---- 计算/逻辑/其他 ----
    private List<String> tags;
    private String matchReason;
}

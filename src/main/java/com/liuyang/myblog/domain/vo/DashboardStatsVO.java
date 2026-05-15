package com.liuyang.myblog.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsVO {
    private Long totalArticles;
    private Long publishedArticles;
    private Long hiddenArticles;
    private Long totalTags;
    private Long totalCategories;
    private Long totalComments;
    private Long pendingComments;
    private Long totalViews;
}

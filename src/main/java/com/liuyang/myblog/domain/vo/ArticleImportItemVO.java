package com.liuyang.myblog.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章批量导入明细项
 *
 * 数据来源：
 * 【blog_article】：articleId, title
 * 【计算/逻辑/其他】：originalFilename, success, errorMessage
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleImportItemVO {
    private String originalFilename;
    private Long articleId;
    private String title;
    private Boolean success;
    private String errorMessage;
}

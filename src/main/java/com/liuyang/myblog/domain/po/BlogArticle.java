package com.liuyang.myblog.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体类
 */
@Data
@TableName("blog_article")
public class BlogArticle {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String summary;

    private String content;

    private String cover;

    private Long categoryId;

    private Integer viewCount;

    @TableField("is_top")
    private Boolean isTop;

    private Boolean status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Long authorId;

    private String slug;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}

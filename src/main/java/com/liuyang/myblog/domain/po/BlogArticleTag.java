package com.liuyang.myblog.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文章-标签关联实体类
 */
@Data
@TableName("blog_article_tag")
public class BlogArticleTag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long tagId;

}

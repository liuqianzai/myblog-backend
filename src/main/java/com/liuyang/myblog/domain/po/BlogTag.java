package com.liuyang.myblog.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 标签实体类
 */
@Data
@TableName("blog_tag")
public class BlogTag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String color;
}

package com.liuyang.myblog.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_friend_link")
public class BlogFriendLink {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String url;

    private String avatar;

    private String description;

    private Integer sort;

    private Boolean status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

package com.liuyang.myblog.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_user")
public class BlogUser {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    // 核心修复：告诉MyBatis-Plus 对应数据库的 password 字段
    @TableField("password")
    private String passwordHash;

    private String nickname;
    private String avatar;    // 你数据库有这个字段
    private String email;     // 你数据库有这个字段
    private String role;      // 你数据库有这个字段
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
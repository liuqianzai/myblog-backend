package com.liuyang.myblog.domain.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("blog_config")
public class BlogConfig {
    @TableId
    private String configKey;

    private String configValue;

    private String remark;
}

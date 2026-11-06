package com.liuyang.myblog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryDTO {
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过 50 个字符")
    private String name;

    @Size(max = 50, message = "分类别名长度不能超过 50 个字符")
    private String slug;

    @Size(max = 255, message = "分类描述长度不能超过 255 个字符")
    private String description;

    private Integer sort;
}

package com.liuyang.myblog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ArticleDTO {
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 255, message = "文章标题长度不能超过 255 个字符")
    private String title;

    @Size(max = 500, message = "文章摘要长度不能超过 500 个字符")
    private String summary;

    @NotBlank(message = "文章正文不能为空")
    private String content;

    @Size(max = 255, message = "封面地址长度不能超过 255 个字符")
    private String cover;

    private Long categoryId;
    private Boolean isTop;
    private Boolean status;
    private List<Long> tagIds;
}
